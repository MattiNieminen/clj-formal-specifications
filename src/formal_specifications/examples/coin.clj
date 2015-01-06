(ns formal-specifications.examples.coin
  (:require [formal-specifications.core :as fspec]))

(fspec/defaction flip-action
  "Function returning an action for coin flip. The body of the action returns
  either :heads or :tails. Availability is omitted (always true)."
  []
  {:body (nth [:heads :tails] (rand-int 2))})
