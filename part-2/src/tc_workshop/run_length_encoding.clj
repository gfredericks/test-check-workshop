(ns tc-workshop.run-length-encoding)

;; Run Length Encoding
;;
;; Description & clojure code from
;; http://rosettacode.org/mw/index.php?title=Run-length_encoding&oldid=215177
;;
;; Given a string containing uppercase characters (A-Z), compress
;; repeated 'runs' of the same character by storing the length of that
;; run, and provide a function to reverse the compression. The output can
;; be anything, as long as you can recreate the input with it.

;; Example:

;;     Input: WWWWWWWWWWWWBWWWWWWWWWWWWBBBWWWWWWWWWWWWWWWWWWWWWWWWBWWWWWWWWWWWWWW
;;         Output: 12W1B12W3B24W1B14W

(defn compress [s]
  (->> (partition-by identity s) (mapcat (juxt count first)) (apply str)))

(defn extract [s]
  (->> (re-seq #"(\d+)([A-Z])" s)
       (mapcat (fn [[_ n ch]] (repeat (Integer/parseInt n) ch)))
       (apply str)))
