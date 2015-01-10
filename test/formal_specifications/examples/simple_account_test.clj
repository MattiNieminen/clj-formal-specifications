(ns formal-specifications.examples.simple-account-test
  (:require [clojure.test :refer :all]
            [formal-specifications.examples.simple-account :refer :all]
            [formal-specifications.core :as fspec]))

(deftest pure-function-test
  (testing "creating an account"
    (is (= (account 100) {:balance 100}))
    (is (= (account nil) {:balance nil})))
  (testing "validation of account"
    (is (true? (valid-account? {:balance 100})))
    (is (true? (valid-account? {:balance 0})))
    (is (false? (valid-account? {:balance -1}))))
  (testing "applying functions to account balance"
    (is (= (apply-to-balance {:balance 100} 25 +) {:balance 125}))))

(def account-1 (ref (fspec/execute (create-account))
                    :validator valid-account?))

(deftest action-test
  (testing "account was created"
    (is (= @account-1 {:balance 0})))
  (testing "account balance can be modified"
    (is (true? (fspec/available? (deposit-action account-1 100))))
    (is (= (do
             (fspec/execute (deposit-action account-1 100))
             @account-1)
           {:balance 100}))
    (is (= (do
             (fspec/execute (withdraw-action account-1 75))
             @account-1)
           {:balance 25})))
  (testing "larger amount than the balance of the account can't be withdrawn"
    (is (false? (fspec/available? (withdraw-action account-1 100))))))
