(ns unit.nedap.utils.modular.api.implement
  "Demonstrates `#'nedap.utils.modular.api/implement`"
  (:require
   [clojure.test :refer :all]
   [nedap.utils.modular.api :as sut]
   [unit.nedap.utils.modular.api.example-external-protocol :as example-external-protocol])
  (:import
   (clojure.lang ExceptionInfo)))

(def this-ns *ns*)

(defn aliased? [s]
  (->> (ns-aliases this-ns)
       (keys)
       (some #{s})))

(defprotocol Thing
  :extend-via-metadata true
  (foo [this x]))

(defprotocol MetadataExtensionDisabled
  "A protocol lacking `:extend-via-metadata`."
  (bar [this x]))

(defn foo-impl [this x]
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

  (testing "swapped order"
    (are [impl] (with-out-str
                  (is (thrown-with-msg? ExceptionInfo #"Validation failed" (sut/implement {}
                                                                             impl foo))))

      foo-impl
      foo-alternative-impl))

  (testing "values that don't resolve to a function"
    (with-out-str
      (is (thrown-with-msg? ExceptionInfo #"Validation failed" (sut/implement {}
                                                                 foo not-an-impl)))))

  (testing "made-up symbols"

    ;; no `are` here since `eval '` makes it harder to predict what will actually happen
    (with-out-str
      (is (thrown-with-msg? ExceptionInfo #"Validation failed" (eval '(nedap.utils.modular.api/implement {}
                                                                        foo-impl oooooommmmmggggg)))))

    (with-out-str
      (is (thrown-with-msg? ExceptionInfo #"Validation failed" (eval '(nedap.utils.modular.api/implement {}
                                                                        foo-alternative-impl oooooommmmmggggg))))))

  (testing "Spurious aliases in either side of the mapping"
    (assert (aliased? 'sut))
    (assert (not (aliased? 'made)))

    (with-out-str
      (let [^Exception v (try
                           (eval (list 'do
                                       (list `in-ns (->> this-ns str symbol (list 'quote)))
                                       '(nedap.utils.modular.api/implement {}
                                          made/up inc)))
                           (is false)
                           (catch Exception e
                             e))]
        (is (-> v  .getCause .getMessage #{"Validation failed"}))
        (is (-> v .getCause class #{ExceptionInfo}))))

    (with-out-str
      (let [^Exception v (try
                           (eval (list 'do
                                       (list `in-ns (->> this-ns str symbol (list 'quote)))
                                       '(nedap.utils.modular.api/implement {}
                                          foo made/up)))
                           (is false)
                           (catch Exception e
                             e))]
        (is (-> v .getMessage #{"Validation failed"}))
        (is (-> v class #{ExceptionInfo})))))

  (testing "Implementing incompatible protocols is prevented"
    (are [impl] (thrown-with-msg? ExceptionInfo #"The targeted protocol does not have `:extend-via-metadata` activated."
                                  (sut/implement {}
                                    bar impl))
      foo-impl
      foo-alternative-impl))

  (testing "Specifyicing protocols (and implementations) belonging to other namespaces, their symbols having an alias prefix"
    (is (= 3000
           (example-external-protocol/do-thing (sut/implement {}
                                                 example-external-protocol/do-thing example-external-protocol/do-thing-impl))))))
