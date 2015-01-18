(ns clj-formal-specifications.examples.coin-test
  (:require [clojure.test :refer :all]
            [clj-formal-specifications.examples.coin :refer :all]
            [clj-formal-specifications.core :as fspec]))

(defn heads-or-tails?
  "Returns true if coin is either :heads or :tails."
  [coin]
  (some #{coin} [:heads :tails]))

(deftest action-test
  (is (heads-or-tails? (fspec/execute (flip-action)))))
