(ns formal-specifications.examples.shared-account
  (:require [formal-specifications.core :as fspec]))

; Standard functions
(defn person
  [id amount]
  {:id id :wallet amount})

(defn valid-person?
  [p]
  (not (neg? (:wallet p))))

(defn apply-to-wallet
  [p amount f]
  (update-in p [:wallet] f amount))

(defn account
  [amount owner-ids]
  {:balance amount :owners owner-ids})

(defn valid-account?
  [acc]
  (and (not (neg? (:balance acc))) (not (empty? (:owners acc)))))

(defn apply-to-balance
  [acc amount f]
  (update-in acc [:balance] f amount))

; Actions
(fspec/defaction create-person
  [id amount]
  {:body (person id amount)})

(fspec/defaction create-account
  [owner-refs]
  {:available (and (coll? owner-refs) (not (empty? owner-refs)))
   :body (account 0 (map (comp :id deref) owner-refs))})

(fspec/defaction withdraw
  [account-ref person-ref amount]
  {:available (and (>= (:balance @account-ref) amount)
                   (some #{(:id @person-ref)} (:owners @account-ref)))
   :body (dosync (alter account-ref apply-to-balance amount -)
                 (alter person-ref apply-to-wallet amount +))})

(fspec/defaction deposit
  [account-ref person-ref amount]
  {:available (>= (:wallet @person-ref) amount)
   :body (dosync (alter account-ref apply-to-balance amount +)
                 (alter person-ref apply-to-wallet amount -))})
