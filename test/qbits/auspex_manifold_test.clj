(ns qbits.auspex-manifold-test
  (:require [clojure.test :refer :all]
            [qbits.auspex :as a]
            qbits.auspex.manifold
            [manifold.deferred :as d])
  (:import
   (clojure.lang ExceptionInfo)
   (java.util.concurrent
    CancellationException
    ExecutionException)))

(def executor clojure.lang.Agent/pooledExecutor)
(def ex (ex-info "boom" {}))
(def f-inc #(a/future (constantly (inc %))
                      executor))

(deftest create-test
  (is (a/future? (d/deferred)))
  (is (a/future? (d/success-deferred ::foo)))
  (is (a/future? (d/error-deferred ::foo))))

(deftest timeout-test
  (is (thrown? Exception
               @(-> (d/deferred)
                    (a/timeout! 10))))
  (is (= ::bar
         @(-> (d/deferred)
              (a/timeout! 10 ::bar))))

  (is (= ::foo
         @(-> (d/deferred)
              (a/timeout! 10)
              (doto (a/success! ::foo)))))

  (is (= ::foo
         @(-> (a/chain (doto (d/deferred)
                         (a/timeout! 10)))
              (a/catch (fn [e] ::foo)))))

  (is (= ::foo
         @(-> (d/deferred)
              (a/timeout! 10)
              (a/catch (fn [t] ::foo))))))

(deftest error-test
  (is (thrown? Exception
               @(doto (d/deferred)
                  (a/error! ex))))

  (is (thrown? ExceptionInfo
               (a/unwrap
                (doto (d/deferred)
                  (a/error! ex)))))

  (is (= ::foo
         @(-> (doto (d/deferred)
                (a/error! ex))
              (a/catch (fn [_] ::foo)))))

  (is (= ::foo
         @(-> (doto (d/deferred)
                (a/error! ex))
              (a/catch ExceptionInfo
                  (fn [_] ::foo)))))


  (let [p (promise)]
    @(-> (doto (d/deferred)
           (a/error! ex))
         (a/catch ExceptionInfo (fn [_] ::foox))
         (a/finally (fn [] (deliver p ::bar))))
    (is (= ::bar @p)))

  (let [p (promise)]
    (is (= ::foo @(-> (d/error-deferred ex)
                      (a/catch ExceptionInfo (fn [_] ::foo))
                      (a/finally (fn [] (deliver p ::bar))))))
    (is (= ::bar @p)))

  (let [p (promise)]
    (is (= ::foo
           @(-> (d/error-deferred ex)
                (a/catch ExceptionInfo (fn [_] ::foo))
                (a/finally (fn [] (deliver p ::bar))
                           executor))))
    (is (= ::bar @p))))

#_(deftest complete-test
    (is (= ::foo @(doto (d/deferred)
                    (a/success! ::foo))))

    (let [f (-> (a/future)
                (a/complete! (fn [] (throw ex)) executor))]
      (try @f (catch Exception e))
      (is (a/error? f)))

    (is (a/realized? (doto (a/future)
                       (a/success! ::foo)))))

(deftest consume-test
  (let [fs [(d/success-deferred 1) (d/success-deferred 2) (d/success-deferred 3)]]
    (is (seq @(a/zip fs)))
    (is (every? a/realized? fs)))
  (is (number? @(a/one (d/success-deferred 1)
                       (d/success-deferred 2)
                       (d/success-deferred 3))))

  (let [p (promise)]
    (a/handle (d/success-deferred ::foo)
              (fn [x err] (deliver p x)))
    (is (= @p ::foo)))

  (let [p (promise)
        f (a/future)]
    (a/handle f (fn [x err]
                  (deliver p err)))
    (a/error! f ex)
    (is (= @p ex)))

  (let [p (promise)]
    (a/handle (d/success-deferred ::foo)
              (fn [x err] (deliver p x))
              executor)
    (is (= @p ::foo)))

  (let [p (promise)
        f (doto (d/deferred)
            (a/error! ex))]
    (a/handle f
              (fn [x err]
                (deliver p err))
              executor)
    (is (= @p ex)))

  (is (= 3 @(-> (d/success-deferred 1)
                (a/then inc)
                (a/then inc))))

  (is (= 11 @(-> (d/success-deferred 1)
                 (a/then (fn [_] (throw ex)))
                 (a/catch (fn [ex]
                            10))
                 (a/then inc))))

  (is (= 11 @(-> (d/success-deferred 1)
                 (a/then (fn [_] (throw ex)))
                 (a/catch ExceptionInfo (fn [ex] 10))
                 (a/then inc))))

  (is (= 3 @(-> (d/success-deferred 1)
                (a/then inc executor)
                (a/then inc executor))))

  (is (= 3 @(-> (d/success-deferred 1)
                (a/fmap f-inc)
                (a/fmap f-inc))))

  (is (= 3 @(-> (d/success-deferred 1)
                (a/fmap f-inc
                        executor)
                (a/fmap f-inc
                        executor))))

  (let [p (promise)]
    (is (= 1 @(-> (d/success-deferred 1)
                  (a/when-complete (fn [_ _]
                                     (deliver p ::foo))))))
    (is @p ::foo))

  (let [p (promise)]
    (is (= 1 @(-> (d/success-deferred 1)
                  (a/when-complete (fn [_ _]
                                     (deliver p ::foo))
                                   executor))))
    (is @p ::foo)))

(deftest utils-test
  (is (= 3 @(a/chain (d/success-deferred 1)
                     inc
                     inc)))

  (is (= 3 @(a/chain 1
                     inc
                     inc)))

  (is (= 3 @(a/chain-futures (d/success-deferred 1)
                             f-inc
                             f-inc)))

  (is (= 3 @(a/chain-futures 1
                             f-inc
                             f-inc)))

  (is (= 6 @(a/chain (d/success-deferred 1)
                     inc
                     f-inc
                     inc
                     f-inc
                     inc)))

  (is (= 6 @(a/chain 1
                     inc
                     f-inc
                     inc
                     f-inc
                     inc)))

  (is (= [1 2 3]
         @(a/zip (d/success-deferred 1)
                 2
                 (d/success-deferred 3)))))

(deftest loop-recur-test
  (is (= 5
         @(a/loop [x 0]
            (d/chain x
                     inc
                     #(if (< % 5)
                        (a/recur %)
                        %)))))

  (is (thrown? ExecutionException
               @(a/loop [x 0]
                  (d/chain x
                           (fn [_] (throw ex))
                           inc
                           #(if (< % 5)
                              (a/recur %)
                              %)))))
  (is (= ::foo
         @(a/loop [x 0]
            (-> (d/chain x
                         (fn [_] (throw ex))
                         inc
                         #(if (< % 5)
                            (a/recur %)
                            %))
                (a/catch ExceptionInfo
                    (fn [_] ::foo)))))))

(deftest let-flow-test
  (is (= 1
         @(a/let-flow [x 1]
                      x)))

  (is (= 1
         @(a/let-flow [x (d/success-deferred 1)]
                      x)))

  (is (= 2
         @(a/let-flow [x (d/success-deferred 1)]
                      (inc x))))

  (is (= [0 1 2]
         @(a/let-flow [x (d/success-deferred 0)
                       :when (= x 0)
                       y (+ x 1)
                       z (d/chain y (fn [_] (inc y)))]
                      [x y z]))))

(a/chain (d/success-deferred 0)
         (fn [x]
           (when (= x 0)
             (a/chain (+ x 1)
                      (fn [y]
                        (a/chain (d/chain y
                                          (fn [_]
                                            (inc y)))
                                 (fn [z] [x y z])))))))

(deftest all-any-test
  (are [pred result f input] (pred result (deref (f input)))
    =         [1 2 3]  a/all  [1 2 3]
    contains? #{1 2 3} a/any  [1 2 3]))
