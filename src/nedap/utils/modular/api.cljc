(ns nedap.utils.modular.api
  (:require
   #?(:clj [nedap.utils.modular.impl.defmethod :refer [clj-defmethod-source cljs-defmethod-source]])
   #?(:clj [nedap.utils.modular.impl.implement :as implement])
   [clojure.repl]
   [clojure.spec.alpha :as spec]
   [nedap.utils.modular.impl.dependent :as dependent]
   [nedap.utils.spec.api :refer [check!]]))

(spec/def ::method-pair (spec/cat :protocol-method    symbol?
                                  :function-reference symbol?))

(defn metadata-extension-supported? [clojure-version]
  (or (-> clojure-version :minor long (> 9))
      (-> clojure-version :major long (> 1))))

#?(:clj
   (defmacro implement
     "Returns a copy of `object` that implements `methods` via metadata (Clojure 1.10 feature).

  In order to foster clear code, it is enforced that `methods` are expressed as symbols (and not as inline functions).

  It is verified at runtime that the symbols do resolve to protocol functions and implementation functions (respectively).

  This run-time checking yields a superior solution to plain metadata-based protocol extension, where symbols may contain typos.

  Check this library's README for notes on ClojureScript compatibility."
     {:style/indent 1}
     [object & methods]
     {:pre [(check! (spec/coll-of ::method-pair :min-count 1) (partition 2 methods)
                    metadata-extension-supported?             *clojure-version*)]}
     (let [clj? (-> &env :ns nil?)
           caller-ns (if clj?
                       *ns*
                       (-> &env :ns))]
       (implement/implement clj?
         object
         caller-ns
         methods
         &env))))

#?(:clj
   (defmacro add-method
     "Installs a new method of multimethod associated with dispatch-value."
     [multifn dispatch-val f]
     (let [clj? (-> &env :ns nil?)]

       (assert (if clj?
                 (-> 'defmethod clojure.repl/source-fn #{clj-defmethod-source})
                 (do
                   (require 'cljs.core)
                   (-> 'cljs.core/defmethod clojure.repl/source-fn #{cljs-defmethod-source}))))

       (if clj?
         `(. ~(with-meta multifn {:tag 'clojure.lang.MultiFn}) addMethod ~dispatch-val ~f)
         `(cljs.core/-add-method ~(with-meta multifn {:tag 'cljs.core/MultiFn}) ~dispatch-val ~f)))))

(defn dependent
  "A replacement for `#'com.stuartsierra.component/using`,
  in which renames can be summed to non-renamed dependencies."
  [component & {:keys [on renames] :as opts}]
  (dependent/dependent component opts))

(defn omit-this
  "Creates a replacement for `f` which drops the first argument, presumed to be of \"this\" type.
  Apt for protocol extensions, when `f` is an arbitrary function which may not participate in our protocols at all."
  [f]
  (fn [_ & args]
    (apply f args)))
