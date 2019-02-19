# nedap.utils.modular

Utilities for creating modular systems.

Functions related to Component or protocols will go here.

## Synopsis

#### `nedap.utils.modular.api/implement`

`implement` is a safer layer over raw metadata-based protocol extension.

> Metadata-based protocol extension might be a reliable solution against [this](https://github.com/clojure/tools.namespace/tree/bb9d7a1e98cc5a1ff53107966c96af6886eb0f5b#warnings-for-protocols) problem.

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
  
`implement` guards you against all of those. It also provides some sugar (symbols don't have to be quoted) and enforcement (you cannot pass anything other than a symbol; this aims to avoid deeply nested, non-reusable code).

This is how it looks like:

```clojure
(implement {} component/start start component/stop stop)
```

## Installation

```clojure
[com.nedap.staffing-solutions/utils.modular "0.1.1"]
````

## ns organisation

There is exactly 1 namespace meant for public consumption:

* `nedap.utils.modular.api`

By convention, `api` namespaces are deliberately thin so you can browse them comfortably.

## Documentation

Please browse the public namespaces, which are documented, speced and tested.

## License

Copyright © Nedap

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
