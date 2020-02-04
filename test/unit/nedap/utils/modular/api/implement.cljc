(ns unit.nedap.utils.modular.api.implement
  "Demonstrates `#'nedap.utils.modular.api/implement`"
  (:require
   #?(:clj [nedap.utils.modular.api :as sut])
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [nedap.speced.def :as speced]
   [nedap.utils.modular.impl.implement :as sut.impl]
   [unit.nedap.utils.modular.api.example-external-protocol :as example-external-protocol])
  #?(:clj (:import (clojure.lang ExceptionInfo)))
  #?(:cljs (:require-macros [nedap.utils.modular.api :as sut])))

(def this-ns *ns*)

#?(:clj
   (defn aliased? [s]
     (->> (ns-aliases this-ns)
          (keys)
          (some #{s}))))

(defprotocol Thing
  :extend-via-metadata true
  (foo [this x]))

(defprotocol MetadataExtensionDisabled
  "A protocol lacking `:extend-via-metadata`."
  (bar [this x]))

(speced/defn foo-impl [this x]
  (+ x x))

(defmulti foo-alternative-impl (constantly :foo-alternative-impl))

(defmethod foo-alternative-impl :foo-alternative-impl [this x]
  (* x x))

(def not-an-impl 2)

(deftest implement
  (testing "metadata is associated, and ns-qualifed"
    (are [impl] (= impl
                   (-> (sut/implement {}
                         foo impl)
                       (meta)
                       (get `foo)))
      foo-impl
      foo-alternative-impl))

  (testing "The protocol function can be invoked"
    (are [impl result] (= result (-> (sut/implement {}
                                       foo impl)
                                     (foo 21)))
      foo-impl             42
      foo-alternative-impl 441))

  #?(:clj
     (testing "swapped order"
       (are [impl] (spec-assertion-thrown? `sut.impl/protocol-method-var? (sut/implement {}
                                                                            impl foo))
         foo-impl
         foo-alternative-impl)))

  #?(:clj
     (testing "values that don't resolve to a function"
       (is (spec-assertion-thrown? `sut.impl/impl-method-var? (sut/implement {}
                                                                foo not-an-impl)))))

  #?(:clj
     (testing "made-up symbols"

       ;; no `are` here since `eval '` makes it harder to predict what will actually happen
       (is (spec-assertion-thrown? `sut.impl/protocol-method-var? (eval '(nedap.utils.modular.api/implement {}
                                                                           foo-impl oooooommmmmggggg))))

       (is (spec-assertion-thrown? `sut.impl/protocol-method-var? (eval '(nedap.utils.modular.api/implement {}
                                                                           foo-alternative-impl oooooommmmmggggg))))))

  (testing "Spurious aliases in either side of the mapping"
    #?(:clj
       (assert (aliased? 'sut)))
    #?(:clj
       (assert (not (aliased? 'made))))

    #?(:clj
       ;; cant use spec-assertion-thrown? because it throws a CompilerException
       (with-out-str
         (let [#?(:clj  ^Exception v
                  :cljs v) (try
                             (eval (list 'do
                                         (list `in-ns (->> this-ns str symbol (list 'quote)))
                                         '(nedap.utils.modular.api/implement {}
                                            made/up inc)))
                             (is false)
                             (catch #?(:clj  Exception
                                       :cljs js/Error) e
                               e))]
           (is (-> v .getCause .getMessage #{"Validation failed"}))
           (is (-> v .getCause type #{#?(:clj  ExceptionInfo
                                         :cljs js/Error)})))))

    #?(:clj
       (is (spec-assertion-thrown? `sut.impl/impl-method-var? (eval (list 'do
                                                                          (list `in-ns (->> this-ns str symbol (list 'quote)))
                                                                          '(nedap.utils.modular.api/implement {}
                                                                             foo made/up)))))))

  #?(:clj
     (testing "Implementing incompatible protocols is prevented"
       (are [impl] (thrown-with-msg? #?(:clj ExceptionInfo
                                        :cljs js/Error) #"The targeted protocol does not have `:extend-via-metadata` activated."
                                     (sut/implement {}
                                       bar impl))
         foo-impl
         foo-alternative-impl)))

  (testing "Specifying protocols (and implementations) belonging to other namespaces, their symbols having an alias prefix"
    (is (= 3000
           (example-external-protocol/do-thing (sut/implement {}
                                                 example-external-protocol/do-thing example-external-protocol/do-thing-impl))))))
