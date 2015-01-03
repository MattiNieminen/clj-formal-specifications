(ns clj-formal-specifications.core)

(defn action?
  "Returns true if action is a valid action with body."
  [action]
  (contains? action :body))

(defn available?
  "Returns true if action is available for execution."
  [action]
  (if (contains? action :available) (:available action) true))

(defn- test-action
  "Throws exceptions if action can not be executed. Used to give reasonable
  error messages for why executing an action has failed."
  [action]
  (cond
     (not (action? action))
       (throw (Exception. "given parameter is not a valid action."))

     (not (available? action))
       (throw (Exception. "action is not available for execution"))

     :else true))

(defn execute
  "Executes action body and returns the evaluated value if an action is
  available and well-formed."
  [action]
  (if (test-action action) (:body action)))
