(ns unit.nedap.utils.modular.api.add-method
  (:require
   #?(:clj [nedap.utils.modular.api :as sut])
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [nedap.speced.def :as speced])
  #?(:cljs (:require-macros [nedap.utils.modular.api :as sut])))

(defn handle-integer [i]
  (+ i i))

(speced/defn handle-string [^string? s]
  (str s s))

(defmulti handle (fn [x]
                   (cond
                     (number? x) ::number
                     (string? x) ::string
                     true        (assert false))))

(sut/add-method handle ::number handle-integer)

(sut/add-method handle ::string handle-string)

(deftest works
  (are [input e] (= e
                    (handle input))
    2   4
    "2" "22"))
