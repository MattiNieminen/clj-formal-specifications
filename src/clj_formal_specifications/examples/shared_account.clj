(ns clj-formal-specifications.examples.shared-account
  (:require [clj-formal-specifications.core :as fspec]))

; Standard functions
(defn person
  [id amount]
  {:id id :wallet amount})

(defn valid-person?
  [p]
  (not (neg? (:wallet p))))

(defn apply-to-wallet
  "Calls f with the :wallet of p and the amount and updates the :wallet with
  the returned value."
  [p amount f]
  (update-in p [:wallet] f amount))

(defn account
  [amount owner-ids]
  {:balance amount :owners owner-ids})

(defn valid-account?
  [acc]
  (and (not (neg? (:balance acc))) (not (empty? (:owners acc)))))

(defn apply-to-balance
  "Calls f with the :balance of acc and the amount and updates the :balance
  with the returned value."
  [acc amount f]
  (update-in acc [:balance] f amount))

; Actions
(fspec/defaction create-person
  [id amount]
  {:body (person id amount)})

(fspec/defaction create-account
  "Creates an account with owners from the :id of owners-refs, if owner-refs
  is a collection and not empty."
  [owner-refs]
  {:available (and (coll? owner-refs) (not (empty? owner-refs)))
   :body (account 0 (map (comp :id deref) owner-refs))})

(fspec/defaction withdraw
  "Transfers money from an account to a person, if the person owns the
  account and the account has enough balance."
  [account-ref person-ref amount]
  {:available (and (>= (:balance @account-ref) amount)
                   (some #{(:id @person-ref)} (:owners @account-ref)))
   :body (dosync (alter account-ref apply-to-balance amount -)
                 (alter person-ref apply-to-wallet amount +))})

(fspec/defaction deposit
  "Transfers money from a person to an account, if the person has enough
  money."
  [account-ref person-ref amount]
  {:available (>= (:wallet @person-ref) amount)
   :body (dosync (alter account-ref apply-to-balance amount +)
                 (alter person-ref apply-to-wallet amount -))})
