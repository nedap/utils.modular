(ns unit.nedap.utils.modular.api.dependent
  (:require
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [com.stuartsierra.component :as component]
   [nedap.utils.modular.api :as sut]
   [nedap.utils.modular.impl.dependent :as impl.dependent]))

(deftest dependent
  (are [description dependencies rename expected]
       (testing description
         (let [component (sut/dependent {} :on dependencies :renames rename)
               dependency-map (::component/dependencies (meta component))]
           (is (= expected
                  dependency-map))
           true))

    "Without renames"
    [:a]
    nil
    {:a :a}

    "Rename from dependency vector"
    [:a :b]
    {:b :c}
    {:a :a :b :c}

    "Rename additional dependencies from vec"
    [:a]
    {:b :c}
    {:a :a :b :c}

    "Rename single dependency"
    {:a :b}
    {:a :c}
    {:a :c}

    "Rename additional dependencies from map"
    {:a :b}
    {:c :d}
    {:a :b :c :d})

  (testing "Wrong keys in options"
    (is (spec-assertion-thrown? ::impl.dependent/options (sut/dependent {} :on {} :reneames {})))))
