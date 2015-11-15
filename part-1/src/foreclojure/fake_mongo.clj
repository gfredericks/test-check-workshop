(ns foreclojure.fake-mongo
  (:require [com.gfredericks.webscale :as webscale]
            ring.middleware.session.store))

(defn ^:private bad-get-uuids
  "Don't use this haha."
  [seed]
  (let [r (java.util.Random. seed)]
    (->> (repeatedly #(.nextLong r))
         (partition 2)
         (map (fn [[x1 x2]]
                (java.util.UUID. x1 x2))))))

(defn ^:private compile-where-map
  "Returns a predicate."
  [where-map]
  (apply every-pred
         (for [[k v] where-map]
           (if (re-find #"^[a-zA-Z]|_id$" (name k))
             (cond ((some-fn string? nil? #(instance? Boolean %)
                             integer?) v)
                   (fn [m] (= v (get m k)))

                   (and (map? v)
                        (= [:$in] (keys v)))
                   (let [xs (set (:$in v))]
                     (fn [m] (contains? xs (get m k))))

                   (and (map? v)
                        (= [:$nin] (keys v)))
                   (let [xs (set (:$nin v))]
                     (fn [m] (not (contains? xs (get m k)))))

                   (and (map? v)
                        (= [:$ne] (keys v)))
                   (let [x (:$ne v)]
                     (fn [m] (not= x (get m k))))

                   :elso
                   (throw (ex-info "Unknown value type!" {:where-map where-map})))
             (throw (ex-info "WTF" {:where-map where-map}))))))

(defn ^:private compile-actions
  [actions]
  (apply comp
         (for [[k v] actions]
           (case k
             :$set #(merge % v)
             :$addToSet (fn [rec]
                          (reduce (fn [rec [k item]]
                                    (update rec k (fnil conj #{}) item))
                                  rec
                                  v))

             (throw (Exception. "WTF??"))))))

(defmulti ^:private update-state
  (fn [state ev]
    (:type ev)))


(defmethod update-state :insert
  [state {:keys [table doc]}]
  (assert (:_id doc))
  (assoc-in state [table (:_id doc)] doc))

(defmethod update-state :update
  [state {:keys [table where actions opts random-id] :as arg}]
  (let [pred (compile-where-map where)
        func (compile-actions actions)
        {:keys [upsert multiple]} opts]
    (update state table
            (fn [recs]
              (let [[new-recs matched]
                    (reduce (fn [[new-recs matched] id]
                              (let [rec (get new-recs id)]
                                (if (pred rec)
                                  [(update new-recs id func) (inc matched)]
                                  [new-recs matched])))
                            [recs 0]
                            (keys recs))]
                (when (and (not multiple) (< 1 matched))
                  (throw (ex-info "Multiple matches!"
                                  {:arg arg})))
                (if (and (zero? matched)
                         upsert)
                  (let [new-id (or (:_id where) random-id)
                        new-rec (assoc where :_id new-id)]
                    (assoc new-recs new-id (func new-rec)))
                  new-recs))))))

(defmethod update-state :delete
  [state {:keys [table where]}]
  (let [pred (compile-where-map where)]
    (update state table
            (fn [recs]
              (reduce (fn [recs [id rec]]
                        (cond-> recs
                          (pred rec)
                          (dissoc id)))
                      recs
                      recs)))))

(def ^:private the-db
  (webscale/create update-state {} "fake-mongo"))

(defn ^:private records
  [table]
  (vals (get @the-db table)))

(defn insert!
  [table doc]
  (webscale/update! the-db
                    {:type :insert
                     :table table
                     :doc (update doc :_id #(or % (str (java.util.UUID/randomUUID))))})
  :ok)

(defn update!
  [table where-map actions & {:as opts}]
  {:pre [(every? #{:$set :$addToSet} (keys actions))
         (every? #{:upsert :multiple} (keys opts))]}
  (webscale/update! the-db
                    (cond->
                        {:type :update
                         :table table
                         :where where-map
                         :actions actions
                         :opts opts}
                      (:upsert opts)
                      (assoc :random-id (str (java.util.UUID/randomUUID))))))

(defn destroy!
  [table where-map]
  (webscale/update! the-db
                    {:type :delete
                     :table table
                     :where where-map}))

(defmacro ^:private stubs
  [& names]
  (cons 'do
        (for [name names]
          `(defn ~name [& args#] (assert (not ~(format "IMPLEMENTED (%s)" name)))))))

(stubs fetch-and-modify destroy!)

(defmacro ^:private noops
  [& names]
  (cons 'do
        (for [name names]
          `(defn ~name [& args#]))))

(noops mongo! authenticate add-index!)

(defn fetch
  [table & {:keys [only where sort limit] :as opts}]
  {:pre [(every? #{:only :where :sort :limit} (keys opts))]}
  (cond->> (records table)
    where
    (filter (compile-where-map where))

    only
    (map #(select-keys % only))

    sort
    (sort-by (case sort
               {:_id 1} :_id
               (throw (Exception. "WHAT SORT"))))

    limit
    (take limit)))

(defn fetch-one
  [& args]
  (first (apply fetch args)))

(doseq [v (vals (ns-publics *ns*))
        :let [{:keys [name]} (meta v)]]
  (alter-var-root v
                  (fn [orig]
                    (fn [& args]
                      (try (apply orig args)
                           (catch Throwable t
                             (let [msg (str "Exception in " name)
                                   data {:args args}]
                               (print msg "")
                               (prn data)
                               (println (.getMessage t))
                               (throw (ex-info msg data t)))))))))

(def ring-session-store
  (reify ring.middleware.session.store/SessionStore
    (read-session [store key]
      (or (:data (fetch-one ::sessions :where {:_id key}))
          {}))
    (write-session [store key data]
      (let [key (or key (str (java.util.UUID/randomUUID)))]
        (update! ::sessions {:_id key}
                 {:$set {:data data}}
                 :upsert true)
        key))
    (delete-session [store key]
      (destroy! ::sessions {:_id key})
      nil)))
