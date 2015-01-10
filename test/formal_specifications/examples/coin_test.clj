(ns formal-specifications.examples.coin-test
  (:require [clojure.test :refer :all]
            [formal-specifications.core :as fspec]
            [formal-specifications.examples.coin :refer :all]))

(defn heads-or-tails?
  "Returns true if coin is either :heads or :tails."
  [coin]
  (some #{coin} [:heads :tails]))

(deftest action-test
  (is (heads-or-tails? (fspec/execute (flip-action)))))
