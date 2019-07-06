(ns nedap.utils.modular.impl.implement
  (:require
   [nedap.utils.spec.api :refer [check!]]))

(defn protocol-method-var? [v]
  (and (-> v meta :protocol)
       (-> v deref fn?)
       (let [{:keys [on] :as protocol} (-> v meta :protocol deref)]
         (if (-> protocol :extend-via-metadata)
           true
           (throw (ex-info "The targeted protocol does not have `:extend-via-metadata` activated."
                           {:protocol on}))))))

(defn impl-method-var? [v]
  (and (not (-> v meta :protocol))
       (or (-> v deref fn?)
           (->> v deref (instance? clojure.lang.MultiFn)))))

(defn resolves-to-protocol-method?
  "Does `sym` resolve to a function that is an abstract protocol method?"
  [ns sym]
  (some->> sym (ns-resolve ns) protocol-method-var?))

(defn resolves-to-implementation-method?
  "Does `sym` resolve to a function that is not an abstract protocol method?"
  [ns sym]
  (some->> sym (ns-resolve ns) impl-method-var?))

(defn fully-qualify [ns s]
  (if (qualified-symbol? s)
    ;; turn 'component/start into 'com.stuartsierra.component/start:
    (do
      (check! resolve s)
      (symbol (resolve s)))
    (symbol (str ns) (str s))))

(defn implement [obj ns kvs]
  `(do
     (check! some? ~obj)
     (doseq [[protocol-symbol# implementation-symbol#] ~(->> kvs
                                                             (partition 2)
                                                             (mapv (fn [[x y]]
                                                                     [(list 'quote x) (list 'quote y)])))]
       (check! (partial resolves-to-protocol-method? ~ns) protocol-symbol#)
       (check! (partial resolves-to-implementation-method? ~ns) implementation-symbol#))
     (vary-meta ~obj assoc ~@(->> kvs
                                  (partition 2)
                                  (mapv (fn [[x y]]
                                          [(list 'quote (fully-qualify ns x)) y]))
                                  (apply concat)))))
