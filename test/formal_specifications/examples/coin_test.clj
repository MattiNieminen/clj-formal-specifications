(ns formal-specifications.examples.coin-test
  (:require [clojure.test :refer :all]
            [formal-specifications.core :as spec]
            [formal-specifications.examples.coin :refer :all]))

(defn heads-or-tails?
  "Returns true of coin is either :heads or :tails."
  [coin]
  (some #(= coin %) [:heads :tails]))

(deftest flip-action-test
  (is (heads-or-tails? (spec/execute (flip-action)))))
