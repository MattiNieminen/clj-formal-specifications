(ns clj-formal-specifications.core-test
  (:require [clojure.test :refer :all]
            [clj-formal-specifications.core :refer :all]))

(deftest action?-test
  (is (true? (action? {:body (+ 1 1)})))
  (is (false? (action? {:a 1 :b 2}))))

(deftest available?-test
  (testing "without specified availability"
    (is (true? (available? {:body (+ 1 1)}))))
  (testing "with availability specified"
    (is (true? (available? {:available true :body (+ 1 1)})))
    (is (false? (available? {:available false :body (+ 1 1)})))))

(deftest execute-test
  (testing "with proper actions"
    (is (= 2 (execute {:body (+ 1 1)})))
    (is (= 2 (execute {:body (+ 1 1) :available true}))))
  (testing "with actions without executable body"
    (is (thrown? Exception (execute {:available true})))
    (is (thrown? Exception (execute {:available true :no-body (+ 1 1)})))
    (is (thrown? Exception (execute {:no-body (+ 1 1)}))))
  (testing "with unavailable actions"
    (is (thrown? Exception (execute {:available false :body (+ 1 1)})))))
