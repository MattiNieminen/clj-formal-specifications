(ns formal-specifications.core)

(defmacro defaction
  "Like defn in style, but is used to define functions that return executable
  formal specification actions. action-map must be a map, which will be the
  return value of by the defined function with all the values wrapper into
  closures."
  ([name args action-map]
   {:pre [(map? action-map)]}
   `(defn ~name ~args ~(reduce-kv #(assoc %1 %2 `(fn [] ~%3)) {} action-map)))
  ([name doc-string args action-map]
   {:pre [(map? action-map) (string? doc-string)]}
   `(defn ~name ~doc-string ~args
      ~(reduce-kv #(assoc %1 %2 `(fn [] ~%3)) {} action-map))))

(defn action?
  "Returns true if action is a valid action with body."
  [action]
  (and (contains? action :body) (fn? (:body action))))

(defn available?
  "Returns true if action is available for execution."
  [action]
  (if (contains? action :available)
    (if (fn? (:available action)) ((:available action)) false)
    true))

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
  "If action is available and well-formed, executes its body and returns the
  result."
  [action]
  (if (test-action action) ((:body action))))
