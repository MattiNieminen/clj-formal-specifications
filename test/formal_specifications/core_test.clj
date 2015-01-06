(ns formal-specifications.core-test
  (:require [clojure.test :refer :all]
            [formal-specifications.core :refer :all]))

(defaction available-action
  []
  {:available true
   :body "Hello world"})

(defaction unavailable-action
  []
  {:available false
   :body "Hello world"})

(defaction action-without-availability
  []
  {:body "Hello world"})

(defaction action-with-documentation
  "This is documentation."
  []
  {:available true
   :body "Hello world"})

(defaction action-without-body
  []
  {:available true
   :no-body "Hello world"})

(defaction empty-action
  []
  {})

; Used for testing that body and availability is evaluated only when needed.
(defaction inc-refs
  [body-counter available-counter]
  {:available (dosync (alter available-counter inc) true)
   :body (dosync (alter body-counter inc))})

(def body-counter (ref 0))
(def available-counter (ref 0))

(deftest test-action?
  (testing "with proper actions"
    (is (true? (action? (available-action))))
    (is (true? (action? (unavailable-action))))
    (is (true? (action? (action-without-availability))))
    (is (true? (action? (action-with-documentation)))))
  (testing "with broken actions"
    (is (false? (action? (action-without-body))))
    (is (false? (action? (empty-action))))
    (is (false? (action? {:body "nonfunction body"})))))

(deftest test-available?
  (testing "with specified availability"
    (is (true? (available? (available-action))))
    (is (true? (available? (action-with-documentation)))))
  (testing "without availability"
    (is (true? (available? (action-without-availability)))))
  (testing "with unavailable actions"
    (is (false? (available? (unavailable-action)))))
  (testing "with broken actions"
    (is (true? (available? (action-without-body))))
    (is (true? (available? (empty-action))))
    (is (false? (available? {:available "nonfunction availability"})))))

(deftest test-execute
  (testing "with proper actions"
    (is (= "Hello world" (execute (available-action))))
    (is (= "Hello world" (execute (action-without-availability))))
    (is (= "Hello world" (execute (action-with-documentation)))))
  (testing "with actions without executable body"
    (is (thrown? Exception (execute (action-without-body))))
    (is (thrown? Exception (execute (empty-action))))
    (is (thrown? Exception (execute {:body "nonfunction body"}))))
  (testing "with unavailable actions"
    (is (thrown? Exception (execute (unavailable-action))))))

(deftest evaluation-test
  (testing ":body and :available are not called when calling action?"
    (is (true? (action? (inc-refs body-counter available-counter))))
    (is (= 0 @body-counter))
    (is (= 0 @available-counter)))
  (testing ":body is not called when calling available?"
    (is (true? (available? (inc-refs body-counter available-counter))))
    (is (= 0 @body-counter))
    (is (= 1 @available-counter))))
