(ns tc-workshop.base64-test
  (:require [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [tc-workshop.base64 :refer :all]))

;;
;; I recommend you test the `encode` and the `decode` functions, and note
;; that for each of them the input & output is a byte array (not a string).
;;

(defspec some-spec 100
  (prop/for-all [x (gen/return 42)]
    (= x 42)))
