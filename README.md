# utils.modular

Utilities for creating modular systems: functions related to [Component](https://github.com/stuartsierra/component), or protocols.

## Installation

```clojure
[com.nedap.staffing-solutions/utils.modular "2.1.0-alpha3"]
````

## Synopsis

#### `nedap.utils.modular.api/implement`

`implement` is a safer layer over raw metadata-based protocol extension.

> Metadata-based protocol extension has recently proven to be a reliable solution against [this](https://github.com/clojure/tools.namespace/tree/bb9d7a1e98cc5a1ff53107966c96af6886eb0f5b#warnings-for-protocols) problem.

In plain Clojure you might be tempted to do:

```clojure
(defn start [this] ...)

(defn stop [this] ...)

(def my-component
  "A com.stuartsierra/component implementing some functionality"
  ^{`component/start start
    `component/stop stop}
  {})
```

However, several things might go wrong:

* What if the protocol lacks a `:extend-via-metadata` directive?
* What if you're running Clojure < 1.10?
* What if the `component/stop` quoted symbol does not get expanded to its fully-qualified name?
  * Would happen if you forget the `:require` in your `(ns ...)` declaration
* What if `component/start` does not resolve to a protocol function?
  * e.g. to a function of not emitted by `defprotocol`
* What if `start` does not evaluate to a function?
  * i.e. any other kind of value

`implement` guards you against all of those, preventing the lack of errors (or opaque errors, at best) that you'd get otherwise.

It also provides some sugar (symbols don't have to be quoted) and enforcement (you cannot pass anything other than a symbol; this aims to avoid deeply nested, non-reusable code).

This is how it looks like:

```clojure
(implement {}
  component/start start
  component/stop  stop)
```

#### `nedap.utils.modular.api/add-method`

`clojure.core/defmethod` does the following:

> Creates and installs a new method of multimethod associated with dispatch-value.

`add-method` does the same exact thing, but skipping the `Creates` part. i.e., it merely associates an existing function to a multimethod.

This has multiple advantages:

* One can code with plain defns, making things more homogeneous
  * And decoupled, reusable
* Said defns can be [speced.def](https://github.com/nedap/speced.def) ones
* Importantly, one should understand that `defmethod` is a side-effect, and as such should be controlled.
  * Better to `add-method` in a [Component](https://github.com/stuartsierra/component) `start` definition.
  
#### `nedap.utils.modular.api/dependent`

Helper fn for `com.stuartsierra.component/using` which takes a dependency collection and optionally
a map with renames. Note that the dependencies can be passed as a vector or a map.

```clojure
(dependent (my-component/new)
           :on my-component/dependencies
           :renames {:internal ::my-component/external})
```

This allows the user to keep using the `my-component/dependencies`-def while maintaining the flexibility to rename
some keys.

#### `nedap.utils.modular.api/omit-this [f]`

Creates a replacement for `f` which drops the first argument, presumed to be of \"this\" type.
Apt for protocol extensions, when `f` is an arbitrary function which may not participate in our protocols at all.

Refer to its test for an example.

## ClojureScript compatibility

All the offered API is compatible with vanilla ClojureScript (i.e. non-self-compiled).

However, `implement` offers weaker guarantees in its cljs version, since cljs has fewer introspection capabilities, particularly at macroexpansion time.

At the same time, as long as your cljs code is defined as a .cljc file _and_ it is compiled for the two possible targets (JVM, js),
then the JVM target will provide the guarantees that cljs cannot provide. i.e. cross-compilation can act as a "linter",
even if only using in production just a single target. 

## ns organisation

There is exactly 1 namespace meant for public consumption:

* `nedap.utils.modular.api`

By convention, `api` namespaces are deliberately thin so you can browse them comfortably.

## Documentation

Please browse the public namespaces, which are documented, speced and tested.

## License

Copyright © Nedap

This program and the accompanying materials are made available under the terms of the [Eclipse Public License 2.0](https://www.eclipse.org/legal/epl-2.0).
