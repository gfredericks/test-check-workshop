(ns foreclojure.data-set
  (:use [foreclojure.fake-mongo])
  (:require [clojure.java.io :as io]))

(def this-file (io/resource "foreclojure/data_set.clj"))

;;
;; Should the descriptions aim to actually introduce new concepts, or
;; just describe the problem and leave the cheatsheet for that?
;;
;; I'm leaning toward the latter
;;

(def the-problems
  [;;
   ;; "Simple" generators
   ;;

   {:title "Constancy is its own reward"
    :description "Create a generator that always generates 42."
    :tags ["simple"]
    :tests '[(gen/generator? __)
             (->> (gen/sample __ 1000)
                  (every? #(= % 42)))]
    :good-answers '[(gen/return 42)]
    :bad-answers  '[gen/nat]}
   {:title "Black and white"
    :description "Create a generator of true and false."
    :tags ["simple"]
    :tests '[(->> (gen/sample __ 1000)
                  (every? #(instance? Boolean %)))
             (->> (gen/sample __ 1000)
                  (frequencies)
                  (vals)
                  (every? #(< 400 % 600)))]
    :good-answers '[gen/boolean
                    (gen/elements [true false])]
    :bad-answers '[gen/nat
                   (gen/return true)
                   (gen/return false)
                   (gen/fmap zero? gen/nat)]}
   {:title "Numbers!"
    :description "Create a generator of integers"
    :tags ["simple"]
    :tests '[(->> (gen/sample __ 1000)
                  (every? integer?))
             (->> (gen/sample __ 1000)
                  ;; Generates a mild variety of integers
                  (distinct)
                  (count)
                  (< 50))]
    :good-answers '[gen/nat
                    gen/int
                    gen/pos-int
                    gen/s-pos-int
                    gen/large-integer
                    (gen/large-integer* {:min -60000 :max -59900})
                    (gen/let [x (gen/double* {:min -1000000 :max 1000000
                                              :infinite? false :NaN? false})]
                      (long x))]
    :bad-answers '[(gen/return 42)
                   gen/boolean
                   gen/ratio]}
   {:title "Strings"
    :description "Create a generator of strings of ASCII characters"
    :tags ["simple"]
    :tests '[(->> (gen/sample __ 1000)
                  (every? string?))
             (->> (gen/sample __ 1000)
                  (apply concat)
                  (map int)
                  (every? #(<= 0 % 127)))
             (->> (gen/sample __ 1000)
                  ;; check variety of characters
                  (apply concat)
                  (distinct)
                  (count)
                  (< 50))
             ;; TODO: statsy tests
             ]
    :good-answers '[gen/string-ascii]
    :bad-answers '[gen/string
                   gen/nat
                   gen/keyword
                   (gen/vector gen/string-ascii)
                   (gen/return "blah")
                   (gen/let [n gen/nat] (str n))]}
   {:title "Enumerationalization"
    :description "Create a generator of :foo, :bar, or :baz."
    :tags ["simple"]
    :tests '[(->> (gen/sample __ 1000)
                  (every? #{:foo :bar :baz}))
             (->> (gen/sample __ 1000)
                  (distinct)
                  (count)
                  (= 3))
             (->> (gen/sample __ 1000)
                  ;; check that value appears at least 200 times
                  (frequencies)
                  (vals)
                  (every? #(< 200 %)))]
    :good-answers '[(gen/elements [:foo :bar :baz])]
    :bad-answers '[(gen/elements [:foo :bar])
                   (gen/elements [:foo :bar :baz :booze])
                   (gen/return :foo)
                   gen/keyword]}


   ;;
   ;; Data structure generators
   ;;
   {:title "A list is a variable-length homogeneous collection"
    :description "Create a generator of lists of booleans."
    :tags ["collections"]
    :tests '[(->> (gen/sample __ 1000)
                  (every? seq?))
             (->> (gen/sample __ 1000)
                  (apply concat)
                  (every? #(instance? Boolean %)))
             (let [[c1 c2]
                   (->> (gen/sample __ 1000)
                        (apply concat)
                        (frequencies)
                        (vals))]
               (< 1/2 (/ c1 c2) 3/2))
             ;; TODO: statsy tests
             ]
    :good-answers '[(gen/list gen/boolean)]
    :bad-answers '[gen/boolean
                   (gen/list (gen/return true))
                   (gen/list gen/nat)
                   (gen/return '(true false true))]}
   {:title "A list is a fixed-length homogeneous collection"
    :description "Create a generator of lists of five booleans."
    :tags ["collections"]
    :tests '[(->> (gen/sample __ 1000)
                  (every? sequential?))
             (->> (gen/sample __ 1000)
                  (map count)
                  (every? #(= 5 %)))
             (->> (gen/sample __ 1000)
                  (apply concat)
                  (every? #(instance? Boolean %)))
             ;; this happens before 333 almost all of the time
             (->> (gen/sample __ 1000)
                  ;; should generate all possible values
                  (distinct)
                  (count)
                  (= 32))
             ;; TODO: statsy tests
             ]
    :good-answers '[(apply gen/tuple
                           (repeat 5 gen/boolean))
                    (gen/vector gen/boolean 5)]
    :bad-answers '[(gen/list gen/boolean)
                   (apply gen/tuple
                          (gen/repeat 5 (gen/return false)))
                   (gen/return (repeat 5 true))]}

   {:title "A list is a fixed-length heterogeneous collection"
    :description "Create a generator of pairs of booleans and integers, e.g. <code>[true 408293]</code>. The integers should be generated from a large range."
    :tags ["collections"]
    :tests '[(->> (gen/sample __ 1000)
                  (every? #(and (vector? %)
                                (= 2 (count %)))))
             (->> (gen/sample __ 1000)
                  (map first)
                  (every? #(instance? Boolean %)))
             (->> (gen/sample __ 1000)
                  (map second)
                  (every? integer?))
             (->> (gen/sample __ 1000)
                  ;; generates a lot of different values
                  (distinct)
                  (count)
                  (< 500))
             ;; TODO: statsy tests
             ]
    :good-answers '[(gen/tuple gen/boolean gen/large-integer)
                    (gen/tuple gen/boolean (gen/large-integer*
                                            {:min -3 :max 110000}))
                    (gen/tuple gen/boolean (gen/scale #(* % 1000) gen/nat))
                    (gen/let [b gen/boolean
                              x gen/large-integer]
                      [b x])]
    :bad-answers '[(gen/tuple gen/boolean gen/nat)
                   (gen/tuple gen/boolean gen/int)
                   (gen/tuple gen/boolean)
                   (gen/vector (gen/one-of gen/boolean gen/int) 2)
                   (gen/tuple gen/boolean gen/int gen/int)
                   (gen/return [true 42])]}

   {:title "A map is like a mathematical function."
    :description "Create a generator of maps from strings to integers"
    :tags ["collections"]
    :tests '[(->> (gen/sample __ 1000)
                  (every? map?))
             (->> (gen/sample __ 1000)
                  (mapcat keys)
                  (every? string?))
             (->> (gen/sample __ 1000)
                  (mapcat vals)
                  (every? integer?))
             (->> (gen/sample __ 1000)
                  (some empty?))
             (->> (gen/sample __ 1000)
                  ;; generates lots of different keys
                  (mapcat keys)
                  (distinct)
                  (count)
                  (< 1000))
             (->> (gen/sample __ 1000)
                  ;; generates lots of different value sets
                  (map vals)
                  (distinct)
                  (count)
                  (< 500))
             (->> (gen/sample __ 1000)
                  ;; Has a variety of sizes
                  (map count)
                  (distinct)
                  (count)
                  (< 20))
             ;; TODO: statsy tests
             ]
    :good-answers '[(gen/map gen/string-ascii gen/int)
                    (gen/fmap (fn [[ks vs]] (zipmap ks vs))
                              (gen/tuple (gen/list gen/string)
                                         (gen/list gen/int)))]
    :bad-answers '[(gen/return {})
                   (gen/return {"hey" 42})
                   (gen/fmap (fn [[k v]] {k v})
                             (gen/tuple gen/string))]}

   {:title "A map is like a row in a database."
    :description "Create a generator of maps with keys :name, :age, :height, and values of strings, integers, and doubles, respectively."
    :tags ["collections"]
    :tests '[(->> (gen/sample __ 1000)
                  (every? #(and (map? %)
                                (= #{:name :age :height}
                                   (set (keys %))))))
             (->> (gen/sample __ 1000)
                  (map (juxt :name :age :height))
                  (map #(map type %))
                  (every? #(= % [String Long Double])))
             (->> (gen/sample __ 1000)
                  ;; Generates a variety of values
                  (distinct)
                  (count)
                  (< 900))
             ;; TODO: statsy tests
             ]
    :good-answers '[(gen/hash-map :name gen/string
                                  :age gen/nat
                                  :height gen/double)]
    :bad-answers '[(gen/return {:name "Gary" :age 45 :height 20.0})
                   (gen/hash-map :name gen/string
                                 :age gen/nat
                                 :height gen/double
                                 :opinions #{})]}

   {:title "Make sure the deck is completely shuffled"
    :description "Create a generator of shuffled card decks, where a card is a number from 1 to 52 inclusive."
    :tags ["collections"]
    :tests '[(->> (gen/sample __ 1000)
                  (every? (fn [xs]
                            (and (sequential? xs)
                                 (= (range 1 53) (sort xs))))))
             (->> (gen/sample __ 1000)
                  ;; Almost never generates the same thing
                  (distinct)
                  (count)
                  (< 975))
             (->> (gen/sample __ 1000)
                  ;; Every card sometimes shows up at the top
                  (map first)
                  (distinct)
                  (count)
                  (= 52))
             (->> (gen/sample __ 1000)
                  ;; Every card sometimes shows up at the bottom
                  (map last)
                  (distinct)
                  (count)
                  (= 52))]
    :good-answers '[(gen/shuffle (range 1 53))
                    ((fn self [xs]
                       (if (empty? xs)
                         (gen/return ())
                         (gen/let [x (gen/elements xs)
                                   shuf'd (self (remove #{x} xs))]
                           (cons x shuf'd))))
                     (range 1 53))]
    :bad-answers '[(gen/return (range 1 53))
                   (gen/shuffle (range 52))
                   (gen/shuffle (range 53))]}



   ;;
   ;; Combinator generators
   ;;
   {:title "Numbers that can't even"
    :description "Create a generator that generates odd numbers."
    :tags ["combinators"]
    :tests '[(->> (gen/sample __ 1000)
                  (every? integer?))
             (->> (gen/sample __ 1000)
                  (every? odd?))
             (->> (gen/sample __ 1000)
                  (distinct)
                  (count)
                  (< 50))]
    :good-answers '[(gen/fmap #(inc (* % 2)) gen/int)]
    :bad-answers '[(gen/return 43)
                   (gen/such-that odd? gen/int)]}
   {:title "[TODO: COME UP WITH CLEVER PROBLEM TITLE]"
    :description "Create a generator that sometimes generates keywords and sometimes generates pairs of booleans."
    :tags ["combinators"]
    :tests '[(->> (gen/sample __ 1000)
                  (every? (fn [x] (or (keyword? x)
                                      (and (vector? x)
                                           (= [Boolean Boolean] (map class x)))))))
             (->> (gen/sample __ 1000)
                  ;; Generates each type roughly equally often
                  (map type)
                  (frequencies)
                  (vals)
                  (every? #(< 300 % 700)))
             (->> (gen/sample __ 1000)
                  ;; Generates a good variety of values
                  (distinct)
                  (count)
                  (< 400))
             (->> (gen/sample __ 1000)
                  ;; Generates all possible boolean pairs
                  (filter vector?)
                  (distinct)
                  (count)
                  (= 4))]
    :good-answers '[(gen/one-of [gen/keyword (gen/tuple gen/boolean gen/boolean)])]
    :bad-answers '[(gen/return :foo)
                   (gen/return [true false])
                   (gen/elements [:foo :bar :baz [true false] [true true] [false false]])]}
   {:title "Stringly typed"
    :description "Create a generator of strings of integers, e.g. <code>\"42\"</code> or <code>\"-17\"</code>."
    :tags ["combinators"]
    :tests '[(->> (gen/sample __ 1000)
                  (every? string?))
             (->> (gen/sample __ 1000)
                  (map #(Long/parseLong %))
                  (every? integer?))
             (->> (gen/sample __ 1000)
                  ;; Generates an okay variety
                  (distinct)
                  (count)
                  (< 50))]
    :good-answers '[(gen/fmap str gen/int)]
    :bad-answers '[(gen/return "42")]}

   {:title "Pick a card, any card"
    :description "Create a generator of [xs x], where xs is a non-empty list of integers and x is an element of xs."
    :tags ["combinators"]
    :tests '[(->> (gen/sample __ 1000)
                  ;; Correct shape
                  (every? (fn [[xs x]]
                            (and (seq? xs)
                                 (every? integer? xs)
                                 (integer? x)))))
             (->> (gen/sample __ 1000)
                  ;; The core requirements
                  (every? (fn [[xs x]]
                            (some #(= x %) xs))))
             (->> (gen/sample __ 1000)
                  ;; Should generate a good variety of things
                  (distinct)
                  (count)
                  (< 600))]
    :good-answers '[(gen/let [xs (gen/not-empty (gen/list gen/int))
                              x (gen/elements xs)]
                      [xs x])]
    :bad-answers '[(gen/return [[42] 42])
                   (gen/tuple (gen/list gen/nat) gen/nat)
                   (gen/bind (gen/list gen/int)
                             (fn [xs]
                               (gen/tuple (gen/return xs)
                                          (gen/elements xs))))
                   (gen/such-that
                    (fn [[xs x]] (some #{x} xs))
                    (gen/tuple (gen/list gen/nat) gen/nat))]}

   {:title "It's sort of like the matrix"
    :description "Create a generator of vectors of vectors of integers, where the inner vectors are all the same size."
    :tags ["combinators"]
    :tests '[(->> (gen/sample __ 1000)
                  (every? (fn [v]
                            (and (vector? v)
                                 (every? vector? v)
                                 (every? integer? (apply concat v))))))
             (->> (gen/sample __ 1000)
                  (every? (fn [v]
                            (or (empty? v)
                                (apply = (map count v))))))
             (->> (gen/sample __ 1000)
                  ;; Generates a good variety of values
                  (distinct)
                  (count)
                  (< 900))
             (->> (gen/sample __ 1000)
                  ;; Generates a good variety of inner sizes
                  (apply concat)
                  (map count)
                  (distinct)
                  (count)
                  (< 10))
             (->> (gen/sample __ 1000)
                  ;; Generates a good variety of numbers
                  (apply concat)
                  (apply concat)
                  (distinct)
                  (count)
                  (< 50))]
    :good-answers '[(gen/let [len gen/nat]
                      (gen/vector (gen/vector gen/large-integer len)))
                    (gen/bind gen/nat
                              (fn [len]
                                (gen/vector (gen/vector gen/int len))))]
    :bad-answers '[(gen/return [])
                   (gen/return [[]])
                   (gen/return [[1 2 3]])
                   (gen/return [[1 2 3] [4 5 6] [7 8 9]])
                   (gen/vector (gen/vector gen/int))]}

   {:title "Sets can't contain 42."
    :description "Create a generator of sets of integers that never contain 42."
    :tags ["combinators"]
    :tests '[(->> (gen/sample __ 1000)
                  (every? (fn [xs]
                            (and (set? xs)
                                 (every? integer? xs)))))
             (->> (gen/sample __ 1000)
                  (apply concat)
                  (not-any? #(= % 42)))
             (->> (gen/sample __ 1000)
                  ;; Generates a good variety of values
                  (distinct)
                  (count)
                  (< 800))]
    :good-answers '[(gen/set (gen/such-that #(not= % 42) gen/int))
                    (gen/let [xs (gen/set gen/int)]
                      (disj xs 42))
                    (gen/set (gen/one-of [(gen/large-integer* {:max 41})
                                          (gen/large-integer* {:min 43})]))]
    :bad-answers '[(gen/return #{1 2 3 4})
                   ;; oh dang...is there some other way to make this point?
                   #_
                   (gen/such-that #(not (% 42))
                                  (gen/fmap set (gen/list gen/int)))]}



   ;;
   ;; Extra Credit
   ;;

   {:title "Three-valued logic"
    :description "Create a generator that generates booleans and nil."
    :tags ["simple" "extra-credit"]
    :tests '[(->> (gen/sample __ 1000)
                  (set)
                  (= #{true false nil}))
             (->> (gen/sample __ 1000)
                  ;; Decent distribution
                  (frequencies)
                  (vals)
                  (every? #(< 200 % 600)))]
    :good-answers '[(gen/elements [true false nil])
                    (gen/one-of [gen/boolean (gen/return nil)])]
    :bad-answers '[(gen/return true)
                   (gen/return nil)
                   (gen/elements [true false nil 42])]}
   {:title "Goes up to eleven"
    :description "Create a generator that generates the full range of integers between -111111111 and 111111111, inclusive."
    :tags ["simple" "extra-credit"]
    :tests '[(->> (gen/sample __ 1000)
                  (every? #(and (integer? %) (<= -111111111 % 111111111))))
             (let [xs (gen/sample __ 1000)]
               ;; distributed decently
               (every? (fn [digit-count]
                         (->> xs
                              (map #(Math/abs %))
                              (map str)
                              (map count)
                              (filter #{digit-count})
                              (count)
                              (<= 10)))
                       (range 1 10)))]
    :good-answers '[(gen/large-integer* {:min -111111111 :max 111111111})]
    :bad-answers '[(gen/return 42)
                   (gen/choose 0 222222222)
                   (gen/choose -111111111 11111111)
                   (gen/choose -111111111 1111111111)
                   (gen/large-integer* {:min -11111111 :max 11111111})
                   (gen/large-integer* {:min -111111111 :max 1111111111})
                   gen/int gen/nat gen/pos-int gen/s-pos-int]}

   {:title "Do I really have to do this one?"
    :description "Create a generator of vectors of lists of pairs of maps from ints to ints and keywords, e.g. [([{2 3, 54 1} :heyo] [{} :what]) () ([{-1 1} :a-keyword])]"
    :tags ["collections" "extra-credit"]
    :tests '[(->> (gen/sample (gen/scale #(min % 15) __) 100)
                  (every? vector?))
             (->> (gen/sample (gen/scale #(min % 15) __) 100)
                  (apply concat)
                  (every? seq?))
             (->> (gen/sample (gen/scale #(min % 15) __) 100)
                  (apply concat)
                  (apply concat)
                  (map first)
                  (every? map?))
             (->> (gen/sample (gen/scale #(min % 15) __) 100)
                  ;; generates a variety of maps & keywords
                  (tree-seq sequential? identity)
                  (distinct)
                  (count)
                  (< 100))]
    :good-answers '[(gen/vector (gen/list (gen/tuple (gen/map gen/int gen/int)
                                                     gen/keyword)))]
    :bad-answers '[(gen/return [([{2 3, 54 1} :heyo] [{} :what])
                                ()
                                ([{-1 1} :a-keyword])])]}

   {:title "Up and to the right"
    :description "Create a generator of non-empty lists of strictly-increasing positive integers."
    :tags ["combinators" "extra-credit"]
    :tests '[(->> (gen/sample __ 1000)
                  (every? (fn [xs] (and (sequential? xs)
                                        (seq xs)
                                        (every? integer? xs)
                                        (every? pos? xs)
                                        (apply < xs)))))
             (->> (gen/sample __ 1000)
                  ;; Generates various lengths of lists
                  (map count)
                  (frequencies)
                  (count)
                  (< 10))
             (->> (gen/sample __ 1000)
                  ;; Generates a good variety of values
                  (distinct)
                  (count)
                  (< 900))]
    :good-answers '[(gen/fmap #(reductions + %) (gen/not-empty (gen/list gen/s-pos-int)))
                    (gen/fmap (fn [xs]
                                (map + (sort xs) (range)))
                              (gen/not-empty (gen/list gen/s-pos-int)))
                    (gen/let [xs (gen/not-empty (gen/list (gen/large-integer* {:min 1 :max 1000000000})))]
                      (reductions + xs))]
    :bad-answers '[(gen/return [1 2 3 4])
                   (gen/fmap sort (gen/list gen/s-pos-int))]}

   {:title "Turtles all the way down"
    :description "Create a generator of lists of directions like <code>[:up :left :up :right :down :right]</code>, where each direction is a 90-degree turn from the previous."
    :tags ["combinators" "extra-credit"]
    :tests '[(->> (gen/sample __ 1000)
                  (every? sequential?))
             (->> (gen/sample __ 1000)
                  (some empty?))
             (->> (gen/sample __ 1000)
                  ;; only valid transitions
                  (mapcat #(partition 2 1 %))
                  (set)
                  (= #{[:up :left] [:up :right]
                       [:left :up] [:left :down]
                       [:down :left] [:down :right]
                       [:right :up] [:right :down]}))
             (->> (gen/sample __ 1000)
                  ;; Generates decent list lengths
                  (map count)
                  (distinct)
                  (count)
                  (< 20))
             (->> (gen/sample __ 1000)
                  ;; Each direction shows up as the initial
                  (remove empty?)
                  (map first)
                  (set)
                  (= #{:up :left :right :down}))]
    :good-answers '[(gen/one-of
                     [(gen/return ())
                      (gen/let [[init turn-dirs]
                                (gen/tuple (gen/elements [:up :left :right :down])
                                           (gen/list (gen/elements [:left :right])))]
                        (reduce (fn [ret new-dir]
                                  (conj ret
                                        ({[:up :right] :right
                                          [:up :left] :left
                                          [:right :right] :down
                                          [:right :left] :up
                                          [:down :left] :right
                                          [:down :right] :left
                                          [:left :right] :up
                                          [:left :left] :down}
                                         [(peek ret) new-dir])))
                                [init]
                                turn-dirs))])]
    :bad-answers '[(gen/return [])
                   (gen/return [:left])
                   (gen/list [:up :down :left :right])]}
   {:title "One at a time please"
    :description "Create a generator of lists of non-negative integers beginning with <code>0</code>, such as <code>(0 0 1 0 2 3 4 3 2)</code>, where the number <code>N</code> does not appear until <code>N-1</code> has appeared."
    :tags ["combinators" "extra-credit"]
    :tests '[(->> (gen/sample __ 1000)
                  (every? (fn [xs]
                            (and (sequential? xs)
                                 (seq xs)
                                 (every? integer? xs)
                                 (not-any? neg? xs)))))
             (->> (gen/sample __ 1000)
                  (every? (fn [xs]
                            (let [xs' (distinct xs)]
                              (= xs' (range (count xs')))))))
             (->> (gen/sample __ 1000)
                  ;; Generates a good variety of things
                  (distinct)
                  (count)
                  (< 700))]
    :good-answers '[(gen/let [x gen/nat]
                      ((fn self [ret dec-size]
                         (if (zero? dec-size)
                           (gen/return ret)
                           (gen/let [x (gen/large-integer* {:min 0 :max (->> ret (apply max) (inc))})]
                             (self (conj ret x) (dec dec-size)))))
                       [0] x))]
    :bad-answers '[(gen/return [1 2 3 3 4])
                   (gen/fmap sort (gen/list gen/nat))]}])

(defn read-source
  [filepath {:keys [line column]}]
  (with-open [is (-> filepath (clojure.java.io/input-stream))
              r (java.io.InputStreamReader. is)
              rdr (java.io.LineNumberReader. r)]
    (dotimes [_ (dec line)] (.readLine rdr))
    (dotimes [_ (dec column)] (.read rdr))
    (let [text (StringBuilder.)
          pbr (proxy [java.io.PushbackReader] [rdr]
                (read [] (let [i (proxy-super read)]
                           (.append text (char i))
                           i)))]
      (read {} (java.io.PushbackReader. pbr))
      (let [lines (clojure.string/split (str text) #"\n")]
        (->> (rest lines)
             (map #(subs % (dec column)))
             (cons (first lines))
             (clojure.string/join "\n"))))))

(defn load-problems []
  "Puts all the problems in the db."
  (do
    (insert! :seqs
             {:_id "problems"
              :seq (count the-problems)})
    (doseq [[prob id] (map vector the-problems (rest (range)))
            :let [prob (reduce (fn [prob k]
                                 (update prob k
                                         (fn [forms]
                                           (map (fn [form]
                                                  (if (symbol? form)
                                                    (pr-str form)
                                                    (read-source this-file
                                                                 (meta form))))
                                                forms))))
                               prob
                               [:tests :good-answers :bad-answers])]]
      (insert! :problems
               (assoc prob
                      :_id id
                      :times-solved 0
                      :approved true)))))

(comment
  (defonce check-solution
    ;; "Returns nil for success, or an error message"
    (memoize
     (fn [problem-id tests code-str]
       (:error (foreclojure.problems/run-code* problem-id code-str)))))

  (defn test-example-answers
    []

    (doseq [{:keys [_id bad-answers good-answers tests title]}
            (sort-by :_id
                     (#'foreclojure.fake-mongo/records :problems))]
      (doseq [code-str good-answers]
        (when-let [error (check-solution _id tests code-str)]
          (println "CRAP" _id code-str error)))
      (doseq [code-str bad-answers]
        (when-not (check-solution _id tests code-str)
          (println "CRAP[ASSED]" _id code-str))))
    (println "Done."))

  ;;
  ;; Dev utils
  ;;

  (defn reset-db
    []
    (.delete (java.io.File. "fake-mongo/data-0.edn"))
    (alter-var-root #'foreclojure.fake-mongo/the-db
                    (constantly (com.gfredericks.webscale/create
                                 #'foreclojure.fake-mongo/update-state
                                 {} "fake-mongo")))
    (foreclojure.mongo/prepare-mongo)))


(comment

  ;; working out tests

  (require '[clojure.test.check.generators :as gen])

  (defn avg [x y] (/ (+ x y) 2))

  (defn recommend-bounds
    [gen pred sample-count]
    (let [[the-min the-max]
          (->> (repeatedly 1000 (fn []
                                  (->> (gen/sample gen sample-count)
                                       (filter pred)
                                       (count))))
               (map #(/ % sample-count))
               (apply (juxt min max)))]
      [(avg 0 the-min) (avg the-max 1)]))


  )
