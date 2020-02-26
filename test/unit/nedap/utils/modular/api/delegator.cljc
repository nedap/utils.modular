(ns unit.nedap.utils.modular.api.delegator
  (:require
   [clojure.test :refer [deftest is testing]]
   [nedap.speced.def :as speced]
   [nedap.utils.modular.api :refer [implement] :as sut]))

(speced/defprotocol Protocol
  "Protocol with 2 functions used in testing"
  (fn-1 [this] "first function")
  (fn-2 [this] "second function"))

(defn make-record-call
  [fn-name]
  (speced/fn record-call [{::keys [^some? call-record]} & _args]
    (swap! call-record update fn-name (fnil inc 0))
    true))

(def record-fn-1 (make-record-call 'fn-1))
(def record-fn-2 (make-record-call 'fn-2))

(defn proxy-fn-1
  [{::sut/keys [target] :as this}]
  (record-fn-1 this)
  (fn-1 target))

(defn new-target []
  (implement {::call-record (atom {})}
    --fn-1 record-fn-1
    --fn-2 record-fn-2))

(speced/defn ^long get-call-count [{::keys [^some? call-record]}, ^symbol? fn-name]
  (get @call-record fn-name 0))

(deftest works
  (let [target    (new-target)
        delegator (-> (sut/delegate {} :to target
                               --fn-1 record-fn-1)
                      (merge {::call-record (atom {})}))]
    (testing "target methods work on delegator"
      (is (fn-1 delegator))
      (is (fn-2 delegator)))

    (testing "calling non-delegated methods pass the target"
      (let [delegator-count (get-call-count delegator 'fn-2)
            target-count    (get-call-count target 'fn-2)]

        (fn-2 delegator)
        (is (= delegator-count
               (get-call-count delegator 'fn-2)))
        (is (= (inc target-count)
               (get-call-count target 'fn-2)))))

    (testing "calling target doesn't call delegator"
      (let [delegator-count (get-call-count delegator 'fn-1)
            target-count    (get-call-count target 'fn-1)]
        (fn-1 target)
        (is (= delegator-count
               (get-call-count delegator 'fn-1)))
        (is (= (inc target-count)
               (get-call-count target 'fn-1)))))

    (testing "proxying using ::delegator/target"
      (let [proxy (-> (sut/delegate {} :to target
                               --fn-1 proxy-fn-1)
                      (merge {::call-record (atom {})}))
            proxy-count  (get-call-count proxy 'fn-1)
            target-count (get-call-count target 'fn-1)]

        (fn-1 proxy)
        (is (= (inc proxy-count)
               (get-call-count proxy 'fn-1)))
        (is (= (inc target-count)
               (get-call-count target 'fn-1)))))

    (testing "nesting delegators"
      (let [nested (-> (sut/delegate {} :to delegator
                                --fn-1 proxy-fn-1)
                       (merge {::call-record (atom {})}))]
        (testing "calling non-delegated methods"
          (let [nested-count    (get-call-count nested 'fn-2)
                delegator-count (get-call-count delegator 'fn-2)
                target-count    (get-call-count target 'fn-2)]

            (fn-2 nested)
            (is (= nested-count
                   (get-call-count nested 'fn-2)))
            (is (= delegator-count
                   (get-call-count delegator 'fn-2)))
            (is (= (inc target-count)
                   (get-call-count target 'fn-2)))))))))
