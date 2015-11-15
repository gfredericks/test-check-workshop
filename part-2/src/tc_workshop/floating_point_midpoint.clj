(ns tc-workshop.floating-point-midpoint)

;; Idea stolen from a talk by David MacIver on Hypothesis, the
;; property-based testing library for python

(defn mid
  "Given two doubles, returns their mid-point."
  [x y]
  (/ (+ x y) 2))
