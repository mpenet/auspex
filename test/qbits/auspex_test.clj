(ns qbits.auspex-test
  (:require [clojure.test :refer :all]
            [qbits.auspex :as a])
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
  (is (a/future? (a/future)))
  (is (thrown? ExecutionException
               @(a/future (fn [] (throw ex))
                          executor)))
  (is (a/future? (a/success-future ::foo)))
  (is (a/future? (a/future (fn [] ::foo)))))

(deftest supply-test
  (is (= ::foo @(doto (a/future)
                  (a/success! ::foo))))
  (is (= ::foo @(a/success-future ::foo)))
  (is (thrown? ExecutionException
               @(a/future (fn [] (throw ex))
                          executor))))
(deftest cancel-test
  (is (a/canceled? (-> (a/future)
                       (a/cancel!))))

  (is (not (a/canceled? (a/future))))

  (is (thrown? CancellationException
               @(-> (a/future)
                    (a/cancel!))))

  (is (= ::foo
         @(-> (a/future)
              (a/cancel!)
              (a/catch
                  (fn [ce]
                    (when (instance? CancellationException ce)
                      ::foo)))))))

(deftest timeout-test
  (is (thrown? ExecutionException
               @(-> (a/future)
                    (a/timeout! 10))))
  (is (= ::bar
         @(-> (a/future)
              (a/timeout! 10 ::bar))))

  (is (= ::foo
         @(-> (a/future)
              (a/timeout! 10)
              (doto (a/success! ::foo)))))

  (is (= ::foo
         @(-> (a/chain (doto (a/future)
                         (a/timeout! 5)))
              (a/catch (fn [e] ::foo)))))

  (is (= ::foo
         @(-> (a/future)
              (a/timeout! 5)
              (a/catch (fn [t] ::foo))))))

(deftest error-test
  (is (thrown? ExecutionException
               @(doto (a/future)
                  (a/error! ex))))

  (is (a/error? (doto (a/future)
                    (a/error! ex))))

  (is (= ::foo
         @(-> (doto (a/future)
                  (a/error! ex))
              (a/catch (fn [_] ::foo)))))

  (is (= ::foo
         @(-> (doto (a/future)
                  (a/error! ex))
              (a/catch ExceptionInfo
                  (fn [_] ::foo)))))

  (is (thrown? ExecutionException
               @(-> (a/future (fn [] (throw (Exception. "meh")))
                              executor)
                    (a/catch ExceptionInfo (fn [_] ::foo)))))

  (let [p (promise)]
    @(-> (doto (a/future)
             (a/error! ex))
         (a/catch ExceptionInfo (fn [_] ::foox))
         (a/finally (fn [] (deliver p ::bar))))
    (is (= ::bar @p)))

  (let [p (promise)]
    (is (= ::foo @(-> (a/future (fn [] (throw ex))
                                executor)
                      (a/catch ExceptionInfo (fn [_] ::foo))
                      (a/finally (fn [] (deliver p ::bar))))))
    (is (= ::bar @p)))

  (let [p (promise)]
    (is (= ::foo
           @(-> (a/future (fn [] (throw ex))
                          executor)
                (a/catch ExceptionInfo (fn [_] ::foo))
                (a/finally (fn [] (deliver p ::bar))
                           executor))))
    (is (= ::bar @p))))

(deftest complete-test
  (is (= ::foo @(doto (a/future)
                  (a/success! ::foo))))

  (let [f (doto (a/future)
            (a/success! ex))]
    @f
    (is (not (a/error? f))))

  (let [f (-> (a/future)
              (a/complete! (fn [] (throw ex)) executor))]
    (try @f (catch Exception e))
    (is (a/error? f)))

  (is (a/realized? (doto (a/future)
                     (a/success! ::foo)))))

(deftest consume-test
  (let [fs [(a/success-future 1) (a/success-future 2) (a/success-future 3)]]
    (is (seq  @(a/zip fs)))
    (is (every? a/realized? fs)))
  (is (= 1 @(a/one (a/success-future 1)
                   (a/success-future 2)
                   (a/success-future 3))))

  (let [p (promise)]
    (a/handle (a/success-future ::foo)
              (fn [x err] (deliver p x)))
    (is (= @p ::foo)))

  (let [p (promise)
        f (a/future)]
    (a/handle f (fn [x err]
                  (deliver p err)))
    (a/error! f ex)
    (is (= @p ex)))

  (let [p (promise)]
    (a/handle (a/success-future ::foo)
              (fn [x err] (deliver p x))
              executor)
    (is (= @p ::foo)))

  (let [p (promise)
        f (doto (a/future)
                (a/error! ex))]
    (a/handle f
              (fn [x err]
                (deliver p err))
              executor)
    (is (= @p ex)))

  (is (= 3 @(-> (a/success-future 1)
                (a/then inc)
                (a/then inc))))

  (is (= 11 @(-> (a/success-future 1)
                 (a/then (fn [_] (throw ex)))
                 (a/catch (fn [ex]
                            ;; (prn :X (type ex))
                            10))
                 (a/then inc))))

  (is (= 11 @(-> (a/success-future 1)
                 (a/then (fn [_] (throw ex)))
                 (a/catch ExceptionInfo (fn [ex] 10))
                 (a/then inc))))

  (is (= 3 @(-> (a/success-future 1)
                (a/then inc executor)
                (a/then inc executor))))

  (is (= 3 @(-> (a/success-future 1)
                (a/fmap f-inc)
                (a/fmap f-inc))))

  (is (= 3 @(-> (a/success-future 1)
                (a/fmap f-inc
                        executor)
                (a/fmap f-inc
                        executor))))

  (let [p (promise)]
    (is (= 1 @(-> (a/success-future 1)
                  (a/when-complete (fn [_ _]
                                     (deliver p ::foo))))))
    (is @p ::foo))

  (let [p (promise)]
    (is (= 1 @(-> (a/success-future 1)
                  (a/when-complete (fn [_ _]
                                     (deliver p ::foo))
                                   executor))))
    (is @p ::foo)))

(deftest utils-test
  (is (= 3 @(a/chain (a/success-future 1)
                     inc
                     inc)))

  (is (= 3 @(a/chain 1
                     inc
                     inc)))

  (is (= 3 @(a/chain-futures (a/success-future 1)
                             f-inc
                             f-inc)))

  (is (= 3 @(a/chain-futures 1
                             f-inc
                             f-inc)))

  (is (= 6 @(a/chain (a/success-future 1)
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
         @(a/zip' (a/success-future 1)
                  (a/success-future 2)
                  (a/success-future 3))))

  (is (= [1 2 3]
         @(a/zip (a/success-future 1)
                 2
                 (a/success-future 3)))))

(deftest loop-recur-test
  (is (= [0 1 2 3 4]
         @(a/loop [x []]
            (if (< (count x) 5)
              (a/recur (conj x (count x)))
              x))))

  (is (thrown? ExceptionInfo
               @(loop [x []]
                  (throw ex))))

  (is (thrown? ExceptionInfo
               @(loop [x []]
                  (a/recur (throw ex)))))

  (is (thrown? ExceptionInfo
               @(loop [x (throw ex)])))

  (is (= 5
         @(a/loop [x 0]
            (a/chain x
                     inc
                     #(if (< % 5)
                        (a/recur %)
                        %)))))

  (is (thrown? ExecutionException
               @(a/loop [x 0]
                  (a/chain x
                           (fn [_] (throw ex))
                           inc
                           #(if (< % 5)
                              (a/recur %)
                              %)))))
  (is (= ::foo
         @(a/loop [x 0]
            (-> (a/chain x
                         (fn [_] (throw ex))
                         inc
                         #(if (< % 5)
                            (a/recur %)
                            %))
                (a/catch ExceptionInfo
                    (fn [_] ::foo)))))))
