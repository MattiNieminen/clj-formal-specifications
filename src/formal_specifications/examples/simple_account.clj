(ns formal-specifications.examples.simple-account
  (:require [formal-specifications.core :as fspec]))

; Standard functions
(defn account
  [amount]
  {:balance amount})

(defn valid-account?
  [acc]
  (not (neg? (:balance acc))))

(defn apply-to-balance
  [acc amount f]
  (update-in acc [:balance] f amount))

; Actions
(fspec/defaction create-account
  []
  {:body (account 0)})

(fspec/defaction deposit-action
  [acc amount]
  {:body (apply-to-balance acc amount +)})

(fspec/defaction withdraw-action
  [acc amount]
  {:available (>= (:balance acc) amount)
   :body (apply-to-balance acc amount -)})


