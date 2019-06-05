(ns nedap.utils.modular.api
  (:require
   [clojure.repl]
   [clojure.spec.alpha :as spec]
   [nedap.utils.modular.impl.defmethod :refer [defmethod-source]]
   [nedap.utils.modular.impl.implement :as implement]
   [nedap.utils.spec.api :refer [check!]]))

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
                 metadata-extension-supported?             *clojure-version*)]}
  (implement/implement object *ns* methods))

(defmacro add-method
  "Installs a new method of multimethod associated with dispatch-value."
  {:pre [(-> 'defmethod clojure.repl/source-fn #{defmethod-source})]}
  [multifn dispatch-val f]
  `(. ~(with-meta multifn {:tag 'clojure.lang.MultiFn}) addMethod ~dispatch-val ~f))
