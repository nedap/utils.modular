(ns unit.nedap.utils.modular.api.add-method
  (:require
   [clojure.test :refer :all]
   [nedap.speced.def :as speced]
   [nedap.utils.modular.api :as sut]))

(defmulti handle class)

(defn handle-integer [i]
  (+ i i))

(speced/defn handle-string [^string? s]
  (str s s))

(sut/add-method handle Long handle-integer)

(sut/add-method handle String handle-string)

(deftest works
  (are [input e] (= e
                    (handle input))
    2   4
    "2" "22"))
