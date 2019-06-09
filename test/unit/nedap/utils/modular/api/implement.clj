(ns unit.nedap.utils.modular.api.implement
  "Demonstrates `#'nedap.utils.modular.api/implement`"
  (:require
   [clojure.test :refer :all]
   [nedap.utils.modular.api :as sut]
   [unit.nedap.utils.modular.api.example-external-protocol :as example-external-protocol]))

(defprotocol Thing
  :extend-via-metadata true
  (foo [this x]))

(defprotocol MetadataExtensionDisabled
  "A protocol lacking `:extend-via-metadata`."
  (bar [this x]))

(defn foo-impl [this x]
  (+ x x))

(def not-an-impl 2)

(deftest implement
  (testing "metadata is associated, and ns-qualifed"
    (is (= foo-impl (-> (sut/implement {}
                          foo foo-impl)
                        (meta)
                        (get `foo)))))

  (testing "The protocol function can be invoked"
    (is (= 42 (-> (sut/implement {}
                    foo foo-impl)
                  (foo 21)))))

  (testing "swapped order"
    (with-out-str
      (is (thrown? Exception (sut/implement {}
                               foo-impl foo)))))

  (testing "values that don't resolve to a function"
    (with-out-str
      (is (thrown? Exception (sut/implement {}
                               foo not-an-impl)))))

  (testing "made-up symbols"
    (with-out-str
      (is (thrown? Exception (eval '(sut/implement {}
                                      foo-impl oooooommmmmggggg))))))

  (testing "spurious aliases in either side of the mapping"
    (with-out-str
      (is (thrown? Exception (eval '(sut/implement {}
                                      made/up inc)))))

    (with-out-str
      (is (thrown? Exception (eval '(sut/implement {}
                                      foo made/up))))))

  (testing "Implementing incompatible protocols is prevented"
    (is (thrown-with-msg? Exception #"The targeted protocol does not have `:extend-via-metadata` activated."
                          (sut/implement {}
                            bar foo-impl))))

  (testing "Specifyicing protocols (and implementations) belonging to other namespaces, their symbols having an alias prefix"
    (is (= 3000
           (example-external-protocol/do-thing (sut/implement {}
                                                 example-external-protocol/do-thing example-external-protocol/do-thing-impl))))))
