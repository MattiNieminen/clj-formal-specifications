(ns clj-formal-specifications.core)

(defn action?
  [action]
  (contains? action :body))

(defn available?
  [action]
  (if (contains? action :available) (:available action) true))

(defn- test-action
  [action]
  (cond
     (not (action? action))
       (throw (Exception. "given parameter is not a valid action."))

     (not (available? action))
       (throw (Exception. "action is not available for execution"))

     :else true))

(defn execute
  [action]
  (if (test-action action) (:body action)))

