(ns unit.nedap.utils.modular.api.omit-this
  (:require
   [clojure.test :refer :all]
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
