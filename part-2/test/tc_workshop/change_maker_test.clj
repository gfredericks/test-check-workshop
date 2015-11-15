(ns tc-workshop.change-maker-test
  (:require [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [tc-workshop.change-maker :refer :all]))

(defspec some-spec 100
  (prop/for-all [x (gen/return 42)]
    (= x 42)))
