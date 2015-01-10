(ns formal-specifications.examples.simple-account
  (:require [formal-specifications.core :as fspec]))

; Pure functions
(defn account
  [amount]
  {:balance amount})

(defn valid-account?
  [acc]
  (not (neg? (:balance acc))))

(defn apply-to-balance
  [acc amount f]
  (update-in acc [:balance] f amount))

; Functions that return actions and utilize state (refs)
(fspec/defaction create-account
  []
  {:body (account 0)})

(fspec/defaction deposit-action
  [acc amount]
  {:body (dosync (alter acc apply-to-balance amount +))})

(fspec/defaction withdraw-action
  [acc amount]
  {:available (>= (:balance @acc) amount)
   :body (dosync (alter acc apply-to-balance amount -))})


