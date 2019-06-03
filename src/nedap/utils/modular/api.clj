(ns nedap.utils.modular.api
  (:require
   [clojure.repl]
   [clojure.spec.alpha :as spec]
   [com.stuartsierra.component :as component]
   [nedap.utils.modular.impl.defmethod :refer [defmethod-source]]
   [nedap.utils.modular.impl.implement :as implement]
   [nedap.utils.spec.api :refer [check!]]
   [nedap.utils.speced :as speced]))

(spec/def ::method-pair (spec/cat :protocol-method symbol? :function-reference symbol?))

(defn metadata-extension-supported? [clojure-version]
  (or (-> clojure-version :minor (> 9))
      (-> clojure-version :major (> 1))))

(defmacro implement
  "Returns a copy of `object` that implements `methods` via metadata (Clojure 1.10 feature).

  In order to foster clear code, it is enforced that `methods` are expressed as symbols (and not as inline functions).

  It is verified at runtime that the symbols do resove to protocol functions and implementation functions (respectively).

  This run-time checking yields a superior solution to plain metadata-based protocol extension, where symbols may contain typos."
  {:style/indent 1}
  [object & methods]
  {:pre [(check! (spec/coll-of ::method-pair :min-count 1) (partition 2 methods)
                 metadata-extension-supported? *clojure-version*)]}
  (implement/implement object *ns* methods))

(defmacro add-method
  "Installs a new method of multimethod associated with dispatch-value."
  {:pre [(-> 'defmethod clojure.repl/source-fn #{defmethod-source})]}
  [multifn dispatch-val f]
  `(. ~(with-meta multifn {:tag 'clojure.lang.MultiFn}) addMethod ~dispatch-val ~f))

(spec/def ::component       #(speced/satisfies? component/Lifecycle %))
(spec/def ::dependency-map  (spec/map-of keyword? keyword?))
(spec/def ::dependency-vec  (spec/coll-of keyword? :kind vector?))
(spec/def ::dependency-coll (spec/or ::dependency-map ::dependency-vec))

(defn using
  "Extension of `com.stuartsierra.component/using` with an optional map
   as third argument with additional overwrites of the provided dependencies"
  ([^::component component
    ^::dependency-coll dependencies]
   (using component dependencies nil))

  ([^::component component
    ^::dependency-coll dependencies
    ^::dependency-map rename-map]
   (let [dependencies (if (map? dependencies)
                        dependencies
                        (zipmap dependencies dependencies))]
     (component/using component
                      (merge dependencies rename-map)))))
