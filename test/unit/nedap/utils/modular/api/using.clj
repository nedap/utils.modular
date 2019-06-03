(ns unit.nedap.utils.modular.api.using
  (:require
   [clojure.test :refer :all]
   [com.stuartsierra.component :as component]
   [nedap.utils.modular.api :as sut]))

(deftest using
  (are [dependencies rename expected]
       (let [component (sut/using {} dependencies rename)
             dependency-map (::component/dependencies (meta component))]
         (= expected
            dependency-map))

    [:a]
    nil ; without renames
    {:a :a}

    [:a :b]
    {:b :c}
    {:a :a :b :c}

    [:a]
    {:b :c} ; additional dependencies
    {:a :a :b :c}

    {:a :b}
    {:a :c}
    {:a :c}

    {:a :b}
    {:c :d} ; additional dependencies
    {:a :b :c :d}))
