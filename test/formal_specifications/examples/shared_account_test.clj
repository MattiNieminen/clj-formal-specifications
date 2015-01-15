(ns formal-specifications.examples.shared-account-test
  (:require [clojure.test :refer :all]
            [formal-specifications.examples.shared-account :refer :all]
            [formal-specifications.core :as fspec]))

(deftest function-test
  (testing "creating a person"
    (is (= (person :bob 1000) {:id :bob :wallet 1000})))
  (testing "validation of person"
    (is (true? (valid-person? {:id :bob :wallet 100})))
    (is (true? (valid-person? {:id :bob :wallet 0})))
    (is (false? (valid-person? {:id :bob :wallet -1}))))
  (testing "applying functions to person wallet"
    (is (= (apply-to-wallet {:id :bob :wallet 100} 25 +)
           {:id :bob :wallet 125})))
  (testing "creating an account"
    (is (= (account 100 #{:bob :mike}) {:balance 100 :owners #{:bob :mike}}))
    (is (= (account nil #{:bob}) {:balance nil :owners #{:bob}})))
  (testing "validation of account"
    (is (true? (valid-account? {:balance 100 :owners #{:bob :mike}})))
    (is (true? (valid-account? {:balance 0 :owners #{:bob}})))
    (is (false? (valid-account? {:balance -1 :owners #{:bob}})))
    (is (false? (valid-account? {:balance 100 :owners nil})))
    (is (false? (valid-account? {:balance -1}))))
  (testing "applying functions to account balance"
    (is (= (apply-to-balance {:balance 100 :owners #{:bob :mike}} 25 +)
           {:balance 125 :owners #{:bob :mike}}))))

(deftest action-test
  (testing "person was created"
    (is (= (do
             (fspec/execute-init person-1 (create-person :mike 100))
             @person-1)
           {:id :mike :wallet 100}))
    (is (= (do
             (fspec/execute-init person-2 (create-person :bob 0))
             @person-2)
           {:id :bob :wallet 0})))
  (testing "account was created"
    (is (= (do
             (fspec/execute-init account-1 (create-account #{person-2}))
             @account-1)
           {:balance 0 :owners '(:bob)})))
  (testing "it is not possible to deposit money that the person does not have"
    (is (not (fspec/available? (deposit account-1 person-2 1)))))
  (testing "person with money can deposit to an account"
    (is (= (do
             (fspec/execute (deposit account-1 person-1 100))
             @account-1)
           {:balance 100 :owners '(:bob)}))
    (is (= @person-1 {:id :mike :wallet 0})))
  (testing "person who does not own the account can not withdraw"
    (is (false? (fspec/available? (withdraw account-1 person-1 100))))
    (is (true? (fspec/available? (withdraw account-1 person-2 100)))))
  (testing "person who owns the account can withdraw"
    (is (= (do
             (fspec/execute (withdraw account-1 person-2 100))
             @account-1)
           {:balance 0 :owners '(:bob)})))
  (testing "previous tests have moved money from mike to bob"
    (is (= @person-1 {:id :mike :wallet 0}))
    (is (= @person-2 {:id :bob :wallet 100}))))
