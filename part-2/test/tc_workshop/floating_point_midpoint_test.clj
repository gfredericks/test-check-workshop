(ns tc-workshop.floating-point-midpoint-test
  (:require [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [tc-workshop.floating-point-midpoint :refer :all]))

(defspec some-spec 100
  (prop/for-all [x (gen/return 42)]
    (= x 42)))
