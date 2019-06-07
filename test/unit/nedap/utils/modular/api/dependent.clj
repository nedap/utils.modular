(ns unit.nedap.utils.modular.api.dependent
  (:require
   [clojure.test :refer :all]
   [com.stuartsierra.component :as component]
   [nedap.utils.modular.api :as sut])
  (:import (clojure.lang ExceptionInfo)))

(deftest dependent
  (are [description dependencies rename expected]
       (testing description
        (let [component (sut/dependent {} :on dependencies :renames rename)
              dependency-map (::component/dependencies (meta component))]
          (= expected
             dependency-map)))

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

  ;; TODO use spec-assertion-thrown?
  ;; https://github.com/nedap/utils.spec/pull/55
  (testing "Wrong keys in options"
    (is (thrown? ExceptionInfo (with-out-str (sut/dependent {} :on {} :reneames {}))))))
