(ns nedap.utils.modular.impl.delegator
  (:require
   [clojure.set :as set]
   [clojure.spec.alpha :as spec]
   [nedap.speced.def :as speced]
   [com.stuartsierra.component :as component]
   [nedap.utils.spec.api :refer [check!]]))

(speced/defn ^boolean? component? [x]
  (speced/satisfies? component/Lifecycle x))

(speced/defn protocol-method-var? [ns ^symbol? sym]
  (let [v (ns-resolve ns ^symbol? sym)]
    (and (-> v meta :protocol)
         (-> v deref fn?)
         (let [{:keys [on] :as protocol} (-> v meta :protocol deref)]
           (if (-> protocol :extend-via-metadata)
             true
             (throw (ex-info "The targeted protocol does not have `:extend-via-metadata` activated."
                             {:protocol on})))))))

(spec/def ::function-map
  (spec/map-of symbol? fn?))

(speced/defn ^::function-map proxy-functions
  [^::function-map function-map]
  (reduce-kv (fn [memo fn-key fn-impl]
               (assoc memo fn-key (speced/fn [{:nedap.utils.modular.api/keys [^some? target]} & args]
                                    (apply fn-impl target args))))
             {}
             function-map))

(speced/defn extract-protocol-fns
  [^some? ns ^some? obj]
  {:post [(check! (spec/coll-of (partial protocol-method-var? ns), :kind set?, :min-count 1) %)]}
  (->> (keys (meta obj))
       (filter #(and (symbol? %) (ns-resolve ns %)))
       (set)))

(speced/defn ^map? proxy-undelegated-fns
  "Wraps all undelegated functions on `this` so that the first argument is `target` instead of `this`"
  [{:nedap.utils.modular.api/keys [^some? target] :as ^map? this} ns]
  (->> [target this]
       (map (partial extract-protocol-fns ns))
       (reduce set/difference)
       (select-keys (meta target))
       (proxy-functions)
       (vary-meta this merge)))
