(ns nedap.utils.modular.impl.dependent
  (:require
   [clojure.set :as set]
   [clojure.spec.alpha :as spec]
   [com.stuartsierra.component :as component]
   [nedap.speced.def :as speced]))

(spec/def ::component       #(#?(:clj  speced/satisfies?
                                 :cljs satisfies?) component/Lifecycle %))
(spec/def ::dependency-map  (spec/map-of keyword? keyword?))
(spec/def ::dependency-vec  (spec/coll-of keyword? :kind vector?))

(spec/def :options/on      (spec/or :m ::dependency-map :v ::dependency-vec))
(spec/def :options/renames (spec/nilable ::dependency-map))
(spec/def :options/keys    (spec/keys :req-un [:options/on] :opt-un [:options/renames]))

(speced/def-with-doc :options/closed
  "Restrict keys passed as options to catch typos"
  #(empty? (set/difference (set (keys %)) #{:on :renames})))

(spec/def ::options (spec/and :options/keys :options/closed))

(speced/defn dependent
  [^::component component ^::options options]
  (let [{:keys [on renames]} options
        dependencies (if (sequential? on)
                       (zipmap on on)
                       on)]
    (component/using component
                     (merge dependencies renames))))
