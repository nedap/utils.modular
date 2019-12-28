(ns nedap.utils.modular.impl.defmethod)

(def clj-defmethod-source
  "The source code of `defmethod`.
  Helps ensuring that `add-method` does the same exact thing as `defmethod`."

  "(defmacro defmethod\n  \"Creates and installs a new method of multimethod associated with dispatch-value. \"
  {:added \"1.0\"}\n  [multifn dispatch-val & fn-tail]
  `(. ~(with-meta multifn {:tag 'clojure.lang.MultiFn}) addMethod ~dispatch-val (fn ~@fn-tail)))")

(def cljs-defmethod-source
  "The source code of `defmethod`.
  Helps ensuring that `add-method` does the same exact thing as `defmethod`."

  "(core/defmacro defmethod
  \"Creates and installs a new method of multimethod associated with dispatch-value. \"
  [multifn dispatch-val & fn-tail]
  `(-add-method ~(with-meta multifn {:tag 'cljs.core/MultiFn}) ~dispatch-val (fn ~@fn-tail)))")
