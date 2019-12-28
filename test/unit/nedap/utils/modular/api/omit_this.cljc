(ns unit.nedap.utils.modular.api.omit-this
  (:require
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [nedap.utils.modular.api :as sut]
   [unit.nedap.utils.modular.api.omit-this.example-protocol :as example-protocol]))

(def sum (sut/omit-this +))

(def adder
  (sut/implement {}
    example-protocol/sum sum))

(deftest omit-this
  (is (= 3
         (sum adder 1 2)))

  (is (= 3
         (example-protocol/sum adder 1 2))))
