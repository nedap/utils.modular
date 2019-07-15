(ns unit.nedap.utils.modular.api.omit-this.example-protocol)

(defprotocol Foo
  :extend-via-metadata true
  (sum [this x y]
    "The sum of two numbers"))
