(ns clj-formal-specifications.examples.simple-account
  (:require [clj-formal-specifications.core :refer :all]))

; Standard functions
(defn account
  [amount]
  {:balance amount})

(defn valid-account?
  [acc]
  (not (neg? (:balance acc))))

(defn apply-to-balance
  "Calls f with the :balance of acc and the amount and updates the :balance
  with the returned value."
  [acc amount f]
  (update-in acc [:balance] f amount))

; Actions
(defaction create-account
  []
  {:body (account 0)})

(defaction deposit-action
  [acc amount]
  {:body (apply-to-balance acc amount +)})

(defaction withdraw-action
  "Decreaces the balance of an account unless the balance would become
  negative."
  [acc amount]
  {:available (>= (:balance acc) amount)
   :body (apply-to-balance acc amount -)})
