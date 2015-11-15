(ns foreclojure.test-check-generators
  "Namespace that generator problems get evaluated in."
  (:require [clojure.test.check.generators :as gen]))

(defn matching-proportion?
  [pred lower-bound upper-bound coll]
  (let [{t true, f false}
        (->> coll
             (map (comp boolean pred))
             (frequencies))
        p (/ t (+ t f))]
    (<= lower-bound p upper-bound)))
