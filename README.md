# clj-formal-specifications

A library for creating and executing formal specifications. Written in Clojure.

This library exposes all of Clojure for writing good formal specifications.
This is achieved with a tradeoff: on the other hand, this library is close to
a domain-specific language for writing formal specifications and nothing more.
However, this library enforces writing pure functions with nothing but standard
Clojure. By encapsulating the parts related to formal specifications into their
own layer, this approach makes it possible to reuse the functions created
during the specification stage later in the actual implementation.

If you are just starting with formal specifications, read [this article from
Wikipedia](http://en.wikipedia.org/wiki/Formal_specification).

## Usage

Formal specifications created with this library consist of atomic actions.
Actions are defined using a ```defaction``` macro:

```clojure
(defaction name doc-string? args action-map)
```

The ```defaction``` macro mimics ```defn``` in style, but requires that the
body (action-map) must be a map. The map must contain ```:body```, and it may
optionally contain ```:available```. Both ```:body``` and ```:available```
must be normal forms that may refer to the args given to the action.

Below is an example of an action describing a throw of set of dice:

```clojure
(defaction dice-throw
  "Throws n six-sided dice and returns their sum"
  [n]
  {:available (pos? n)
   :body (reduce + (take n (repeatedly #(inc (rand-int 6)))))})
```
This action is available when n is larger than zero (you can't throw less than
one die) and generates n integers between one and six and sums them.

Actions are just functions until they are evaluated with their parameters. This
means that when actions are executed or otherwise manipulated, they must be
given in a form together with their parameters. One of the functions that can
be used together with an action is ```action?```, which is used to check
whether or not object is a proper action:

```clojure
; Wrong! Don't do this!
(action dice-throw)

; Correct! Returns true!
(action? (dice-throw 2))
```

The key ```:available``` is used to enable or disable the action in a certain
situations. It is possible to check if an action can be executed by calling
function ```available?``` together with the action and its parameters.

```clojure
; Returns true
(available? (dice-throw 5))

; Returns false
(available? (dice-throw 0))
```

This returns a boolean describing whether or not the action is available
for execution. Under the hood, the function merely evaluates the value of
```:available``` with given parameters. If an action does not contain
```:available```, it is considered always available for execution.

When an action is created, it is expanded into a function where
```:available``` and ```:body``` are transformed into closures. This
methodology is used to achieve two goals:

* Side-effects are allowed in actions as they are not executed just by
evaluating the action. For example, just by calling ```available``` for
certain action does not evaluate its ```:body```.

* Closures allow ```:body``` and ```:available``` to refer to the argument
list of the action even after they are transformed into functions without
arguments.

Because actions are not executed when evaluating the action expression, there
is a special function for executing the actions called ```execute```. It works
like this:

```clojure
; Returns a number between 5 and 30
(execute (dice-throw 5))
```

This call will find the closure from ```:body``` and executes it. It may
also throw an exception if an action is not properly defined or it is
not available; it is not possible to execute an action when it is not
available.

The usual use case is to execute actions with arguments returned from
executing other actions. To avoid chaining calls to ```execute```, this library
offers some helper functions to store the values returned by executing actions.
First of all, it is mandatory to create a "store" for the data using a function
called ```execute-init```. This function simply creates a var pointing to a
ref, executes the given action and stores its return value into the newly
created ref.

It is also possible to give ```execute-init``` a validator
function, which is given to the ref as a normal validator function. The
validator is a great way to make sure that the availability of action is
defined correctly for every action; if a change to a ref fails due to the
validator, the current version of the formal specification is incomplete as it
can lead to an unwanted state of the application. In summary,
```execute-init``` is called like this:

```clojure
(execute-init var-name action-expr validator?)
```

After the ref is created, the value can be changed just by calling
```execute``` with the ref added to the function call. For example, consider
the scenario where a set of dice is thrown where the amount of dice is the
result of the previous throw:

```clojure
(defn valid-dice-throw?
  "Returns true if the result of the dice throw is larger than one"
  [result]
  (pos? result))

; Start with one dice
(execute-init throw-result (dice-throw 1) valid-dice-throw?)
; Throw another set of dice and save the result
(execute (dice-throw @throw-result) throw-result)
; Once more
(execute (dice-throw @throw-result) throw-result)
```

Sometimes, for example when embedding formal specifications to other
applications, it is necessary to separate actions from normal functions
and refs created with ```execute-init``` from other refs. For this reason,
```defaction``` and ```execute-init``` macros insert metadata to their
returned results. The metadata of an action contains a key/value pair
```:action true```. Similarly, refs creates with ```execute-init``` have
```:spec-ref true``` in their metadata:

```clojure
; Retuns a huge map like {... :action true  ...}
(meta #'dice-throw)

; Returns {:spec-ref true}
(meta throw-result)
```

For now, that is all. See [example specifications]
(https://github.com/MattiNieminen/formal-specifications/tree/master/src/formal_specifications/examples)
and their [tests]
(https://github.com/MattiNieminen/formal-specifications/tree/master/test/formal_specifications/examples)
 for further reference.

## Tips for writing good formal specifications

* It is usually a good idea to use namespaces just as you would use them in
normal Clojure project to give structure to your specifications.

* Choose correct level of abstraction according to the use of the
specification: if you wish to create scenarios to validate the specification
with non-technical stakeholders, don't focus on the implementation level
details, and avoid writing specifications about parts of the software that can
be described unambiguously with natural languages. You can always do those
things later if the specification is transformed into the implementation.

* In case you need to to modify multiple refs in one action, dont use
```execute``` with refs. Instead, make actions that take refs as parameters and
wrap your ```:body``` in ```(dosync ...)```. See [shared account example]
(https://github.com/MattiNieminen/formal-specifications/blob/master/src/formal_specifications/examples/shared_account.clj)
 for details. Remember: actions should be atomic in terms of the specification!

* Layer your specifications properly: write normal pure functions and favor
higher-order functions. Then, write actions into a separate layer that utilizes
the pure functions. This way most of the logic is reusable. If your transaction
logic gets complicated, you may consider using another layer for just the
transactions. This approach results in three-tier architecture: pure functions,
atomic transactions and actions.

* Consider writing unit tests for both actions and the pure functions. Testing
pure functions assures that no mistakes have been made in the behaviour of the
functions (normal unit testing). Testing actions effectively creates a "use
case" from the specification, that can be tested when the specification
changes. Imagine writing "regression tests" for the specifications. Cool!

## License

Copyright © 2015 Matti Nieminen

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
