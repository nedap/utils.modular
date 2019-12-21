(ns nedap.utils.modular.impl.implement
  (:require
   [nedap.speced.def :as speced]
   [nedap.utils.spec.api :refer [check!]]))

;; `org.clojure/clojurescript` is a `:provided` dependency,
;; and consumer projects may opt to not have it in their classpaths at all:
(def cljs-available?
  (try
    (require '[cljs.analyzer])
    true
    (catch Exception _
      false)))

(when cljs-available?
  (eval '(speced/defn ^::speced/nilable ^symbol? cljs-resolve
           "Additions over the original CLJS resolve:
  * symbols don't have to be quoted, allowing arbitrary queries
  * var metadata is not dropped."
           [env, ^symbol? sym]
           (let [[var meta] (try
                              (let [var (cljs.analyzer/resolve-var env sym (cljs.analyzer/confirm-var-exists-throw))]
                                [var (cljs.analyzer/var-meta var)])
                              (catch Throwable t
                                [(cljs.analyzer/resolve-var env sym) nil]))]
             (some-> var
                     :name
                     (vary-meta assoc :cljs.analyzer/no-resolve true)
                     (vary-meta merge meta))))))

(def cljs-resolver
  (if cljs-available?
    (-> 'cljs-resolve resolve)
    (fn [& _]
      (throw (ex-info "Trying to compile cljs code without the clojurescript dependency in the classpath"
                      {})))))

(speced/defn ^{::speced/spec (complement map?)}
  do-resolve [^symbol? sym, ^boolean? clj?, ^some? ns, env]
  (if clj?
    (ns-resolve ns env sym)
    (cljs-resolver env sym)))

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

(speced/defn fully-qualify [^boolean? clj?
                            ns
                            ^symbol? s
                            env]
  (if (qualified-symbol? s)
    ;; turn 'component/start into 'com.stuartsierra.component/start:
    (let [resolver (fn [s]
                     (do-resolve s clj? ns env))]
      (check! resolver s)
      (symbol (resolver s)))
    (symbol (if clj?
              (str ns)
              (do
                (check! map? ns)
                (-> ns
                    :name
                    (doto assert)
                    str)))
            (str s))))

(speced/defn implement [^boolean? clj?, obj, ns, ^sequential? kvs, env]
  `(do
     (check! some? ~obj)
     ~(when clj?
        `(doseq [[protocol-symbol# implementation-symbol#] ~(->> kvs
                                                                 (partition 2)
                                                                 (mapv (fn [[x y]]
                                                                         [(list 'quote x) (list 'quote y)])))]
           (check! (partial resolves-to-protocol-method? ~ns) protocol-symbol#)
           (check! (partial resolves-to-implementation-method? ~ns) implementation-symbol#)))
     (vary-meta ~obj assoc ~@(->> kvs
                                  (partition 2)
                                  (mapv (fn [[x y]]
                                          [(list 'quote (fully-qualify clj? ns x env))
                                           y]))
                                  (apply concat)))))
