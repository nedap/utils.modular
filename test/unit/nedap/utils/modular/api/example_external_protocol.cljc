(ns unit.nedap.utils.modular.api.example-external-protocol)

(defprotocol Example
  :extend-via-metadata true
  (do-thing [this]))

(defn do-thing-impl [this]
  3000)
