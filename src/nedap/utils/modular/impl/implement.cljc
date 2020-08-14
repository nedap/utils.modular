(ns nedap.utils.modular.impl.implement
  (:require
   [nedap.speced.def :as speced]
   [nedap.utils.spec.api :refer [check!]])
  #?(:clj (:import (clojure.lang MultiFn))))

;; `org.clojure/clojurescript` is a `:provided` dependency,
;; and consumer projects may opt to not have it in their classpaths at all:
#?(:clj (def cljs-available?
          (try
            (require '[cljs.analyzer])
            true
            (catch Exception _
              false))))

#?(:clj (when cljs-available?
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
                             (vary-meta merge meta)))))))

#?(:clj (def cljs-resolver
          (if cljs-available?
            (-> 'cljs-resolve resolve)
            (fn [& _]
              (throw (ex-info "Trying to compile cljs code without the clojurescript dependency in the classpath"
                              {}))))))

#?(:clj (speced/defn ^{::speced/spec (complement map?)}
          do-resolve [^symbol? sym, ^boolean? clj?, ^some? ns-name, env]
          (if clj?
            (ns-resolve ns-name env sym)
            (cljs-resolver env sym))))

(defn ns-protocol-method-vars
  "Returns a set of every abstract protocol in `ns`"
  [ns]
  (->> (ns-publics ns)
       (filter (comp :protocol meta second))
       (map second)
       (set)))

(defn impl-method-var?
  "Is `@v` a function that is not an abstract protocol method?"
  [v]
  (and v
       (not (-> v meta :protocol))
       (or (-> v deref fn?)
           (->> v deref (instance? MultiFn)))))

#?(:clj (speced/defn fully-qualify [^boolean? clj?
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
                    (str s)))))

#?(:clj (speced/defn implement [^boolean? clj?, obj, ns, ^sequential? kvs, env]
          `(do
             (check! some? ~obj)
             ~(when clj?
                `(doseq [[protocol-symbol# implementation-symbol#] ~(->> kvs
                                                                         (partition 2)
                                                                         (mapv (fn [[x y]]
                                                                                 [(list 'quote x) (list 'quote y)])))
                         :let [protocol-var#      (ns-resolve ~ns protocol-symbol#)
                               all-protocol-vars# (ns-protocol-method-vars (-> protocol-var# meta :ns))]]
                   (check! all-protocol-vars# protocol-var#)
                   (when-not (-> protocol-var# meta :protocol deref :extend-via-metadata)
                     (throw (ex-info "The targeted protocol does not have `:extend-via-metadata` activated."
                                     {:protocol (-> protocol-var# meta :protocol symbol)})))
                   (check! impl-method-var? (ns-resolve ~ns implementation-symbol#))))
             (vary-meta ~obj assoc ~@(->> kvs
                                          (partition 2)
                                          (mapv (fn [[x y]]
                                                  [(list 'quote (fully-qualify clj? ns x env))
                                                   y]))
                                          (apply concat))))))
