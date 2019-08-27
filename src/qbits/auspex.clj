(ns qbits.auspex
  "This provides a clj friendly wrapper for CompletableFuture and adds a
  few utility functions to mimic manifold features.  Shamelessly stole
  code/ideas from the awesome manifold library."
  (:refer-clojure :exclude [future future? realized? loop recur])
  (:require [qbits.auspex.protocols :as p]
            [qbits.auspex.function :as f]
            [qbits.auspex.executor :as executor]
            [qbits.auspex.impl :as impl])
  (:import (java.util.concurrent
            CompletableFuture CompletionStage Executor
            AbstractExecutorService)))

(set! *warn-on-reflection* true)

;;; ICancel
(def cancel! p/-cancel!)
(def canceled? p/-canceled?)

;;; ITimeout
(def timeout! p/-timeout!)

;;; IFuture
(def error! p/-error!)
(def error? p/-error?)
(def catch p/-catch)
(def finally p/-finally)
(def complete! p/-complete!)
(def success! p/-success!)
(def realized? p/-realized?)
(def handle p/-handle)
(def then p/-then)
(def fmap p/-fmap)
(def when-complete p/-when-complete)

(defn future
  "No arg creates an empty/incomplete future 1 arg creates a future that
  will get the return value of f as realized value 2 arg creates a
  future that will be realized on ExecutorService supplied with
  return value of f as realized value.

  The executor that is set at this stage will continue to be used for
  subsequent steps (then/chain etc) if another one is not specified at
  another level.
  "
  ([] (CompletableFuture.))
  ([f]
   (CompletableFuture/supplyAsync (f/supplier f)
                                  (executor/current-thread-executor)))
  ([f executor]
   (CompletableFuture/supplyAsync (f/supplier f)
                                  executor)))
(defn success-future
  "Returns a new CompletableFuture that is already completed with the
  given value."
  [x]
  (CompletableFuture/completedFuture x))

(defn error-future
  "Returns a new CompletableFuture that is already completed
  exceptionally with the given exception."
  [x]
  (CompletableFuture/failedFuture x))

(defn future?
  "Returns true if x is a CompletableFuture"
  [x]
  ;; satisfies is (still) horribly slow sadly
  (instance? CompletableFuture x))

(defn- wrap
  [x]
  (cond-> x
    (not (future? x))
    success-future))

(defn chain'
  "Like chain but assumes fns return raw values instead of potential
  futures"
  [x & fns]
  (reduce then (wrap x) fns))

(defn chain
  "Composes functions starting with x as argument triggering calls to
  fns for every step coercing the return values to deferreds if
  necessary and returns a deferred with the final result."
  [x & fns]
  (reduce (fn [fut f]
            (fmap fut
                  (fn [x]
                    (wrap (f x)))))
          (wrap x)
          fns))

(defn chain-futures
  "Like chain but takes a value and functions that will return futures"
  [x & fs]
  (reduce fmap (wrap x) fs))

(defn one
  "Returns one value from a list of futures"
  [& cfs]
  (CompletableFuture/anyOf (into-array CompletableFuture
                                       cfs)))
(defn zip'
  "Like zip but faster if you know you're only dealing with futures
  args"
  [& fs]
  (-> (CompletableFuture/allOf (into-array CompletableFuture
                                           fs))
      (then (fn [cf]
              (map deref fs)))))

(defn zip
  "Takes a list of values, some of which can be futures and returns a
  future that will contains a list of realized values"
  [& xs]
  (apply zip' (map wrap xs)))

(deftype Recur [args]
  clojure.lang.IDeref
  (deref [_] args))

(defn recur
  [& args]
  (Recur. args))

(defn recur?
  [x]
  (instance? Recur x))

(defmacro loop
  "A version of Clojure's loop which allows for asynchronous loops, via `recur`.
  `loop` will always return a deferred value, even if the body is
  synchronous.  Note that `loop` does **not** coerce values to
  deferreds, actual auspex/futures must be used.

   (loop [i 1e6]
     (chain (future i)
       #(if (zero? %)
          %
          (recur (dec %)))))"

  [bindings & body]
  (let [pairs (partition 2 bindings)
        vars (map first pairs)
        vals (map second pairs)
        ret (gensym "ret")]
    `(let [result# (future)]
       ((fn fun# [result# ~@vars]
          (clojure.core/loop [~@(interleave vars vars)]
            (let [~ret (try
                         ~@body
                         (catch Throwable t#
                           (error! result# t#)))]
              (cond
                (future? ~ret)
                (handle ~ret
                        (fn [ok# err#]
                          (cond
                            err#
                            (error! result# err#)

                            (recur? ok#)
                            (apply fun# result# (deref ok#))

                            :else
                            (success! result# ok#))))

                (recur? ~ret)
                (apply fun# result# @~ret)

                :else
                (success! result# ~ret)))))
        result#
        ~@vals)
       result#)))
