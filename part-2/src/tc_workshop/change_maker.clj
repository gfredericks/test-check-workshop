(ns tc-workshop.change-maker)

;; Change Maker
;;
;; Description & clojure code from
;; http://rosettacode.org/mw/index.php?title=Count_the_coins&oldid=214417

;; There are four types of common coins in US currency: quarters (25
;; cents), dimes (10), nickels (5) and pennies (1). There are 6 ways to
;; make change for 15 cents:

;;   - A dime and a nickel;
;;   - A dime and 5 pennies;
;;   - 3 nickels;
;;   - 2 nickels and 5 pennies;
;;   - A nickel and 10 pennies;
;;   - 15 pennies.

;; How many ways are there to make change for a dollar using these common
;; coins? (1 dollar = 100 cents).


(def denomination-kind [1 5 10 25])

(defn- cc [amount denominations]
  (cond (= amount 0) 1
        (or (< amount 0) (empty? denominations)) 0
        :else (+ (cc amount (rest denominations))
                 (cc (- amount (first denominations)) denominations))))

(defn count-change
  "Calculates the number of times you can give change with the given denominations."
  [amount denominations]
  (cc amount denominations))
