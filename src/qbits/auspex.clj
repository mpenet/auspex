(ns qbits.auspex
  "This provides a clj friendly wrapper for CompletableFuture and adds a
  few utility functions to mimic manifold features.  Shamelessly stole
  code/ideas from the awesome manifold library."
  (:refer-clojure :exclude [future future? realized? loop recur empty])
  (:require [qbits.auspex.function :as f]
            [qbits.auspex.impl :as impl]
            [qbits.auspex.protocols :as p])
  (:import (java.util.concurrent CompletableFuture)))

(set! *warn-on-reflection* true)

(def error! #'p/-error!)
(def error? #'p/-error?)
(def catch #'p/-catch)
(def finally #'p/-finally)
(def complete! #'p/-complete!)
(def success! #'p/-success!)
(def realized? #'p/-realized?)
(def handle #'p/-handle)
(def then #'p/-then)
(def fmap #'p/-fmap)
(def when-complete #'p/-when-complete)
(def cancel! #'p/-cancel!)
(def canceled? #'p/-canceled?)
(def timeout! #'p/-timeout!)
(def wrap #'p/-wrap)
(def empty #'p/-empty)
(def future? #'p/-future?)

(defn future
  "No arg creates an empty/incomplete future, 1 arg creates a future
  that will get the return value of f as realized value on fork-join
  common pool, 2 arg creates a future that will be realized on
  ExecutorService supplied with return value of f as realized value.

  The executor that is set at this stage will continue to be used for
  subsequent steps (then/chain etc) if another one is not specified at
  another level"
  ([] (CompletableFuture.))
  ([f]
   (CompletableFuture/supplyAsync (f/supplier f)))
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
  [^Throwable x]
  (CompletableFuture/failedFuture x))

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

(defn any'
  "Like `any` but faster if you know you're only dealing with future args"
  [fs]
  (CompletableFuture/anyOf (into-array CompletableFuture fs)))

(defn any
  "Returns one value from a collection of futures"
  [xs]
  (any' (map wrap xs)))

(defn one'
  "Like `one` but faster if you know you're only dealing with future args"
  [& fs]
  (any' fs))

(defn one
  "Returns one value from a list of futures"
  [& xs]
  (any' (map wrap xs)))

(defn all'
  "Like `all` buf faster if you know you're only dealing with futures
  args"
  [fs]
  (-> (CompletableFuture/allOf (into-array CompletableFuture fs))
      (then (fn [_]
              (map deref fs)))))

(defn all
  "Takes a collection of values, some of which may be futures, and
   returns a future that will contain a list of realized values"
  [xs]
  (all' (map wrap xs)))

(defn zip'
  "Like zip but faster if you know you're only dealing with futures
  args"
  [& fs]
  (all' fs))

(defn zip
  "Takes a list of values, some of which can be futures and returns a
  future that will contains a list of realized values"
  [& xs]
  (all' (map wrap xs)))

(deftype Recur [args]
  clojure.lang.IDeref
  (deref [_] args))

(defn recur
  "Like recur, but to be used with `qbits.auspex/loop`"
  [& args]
  (Recur. args))

(defn recur?
  [x]
  (instance? Recur x))

(defmacro loop
  "A version of Clojure's loop which allows for asynchronous loops, via
  `qbits.auspex/recur`.  `loop` will always return a CompletableFuture
  Value, even if the body is synchronous.  Note that `loop` does
  **not** coerce values to deferreds, actual `qbits.auspex/future`s
  must be used.

   (loop [i 1e6]
     (chain (future i)
       #(if (zero? %)
          %
          (recur (dec %)))))"

  [bindings & body]
  (let [pairs (partition 2 bindings)
        vars (map first pairs)
        vals (map second pairs)
        var-syms (map (fn [_] (gensym "var")) vars)
        ok (gensym "ok")
        ret (gensym "ret")
        val (gensym "val")]
    `(let [result# (future)]
       ((fn fun# [result# ~@var-syms]
          (clojure.core/loop [~@(interleave vars var-syms)]
            (let [~ret (try ~@body
                            (catch Throwable t#
                              (error! result# t#)))]
              (cond
                (future? ~ret)
                (if (realized? ~ret)
                  (let [~val (try @~ret (catch Throwable t# t#))]
                    (cond
                      (instance? Throwable ~val)
                      (error! result# ~val)

                      (recur? ~val)
                      (~'recur ~@(map
                                  (fn [n] `(nth @~val ~n))
                                  (range (count vars))))

                      :else
                      (success! result# ~val)))
                  (handle ~ret
                          (fn [~ok err#]
                            (cond
                              err#
                              (error! result# err#)

                              (recur? ~ok)
                              (apply fun# result# @~ok)

                              :else
                              (success! result# ~ok)))))

                (recur? ~ret)
                (~'recur ~@(map
                            (fn [n] `(nth @~ret ~n))
                            (range (count vars))))

                :else
                (success! result# ~ret)))))
        result#
        ~@vals)
       result#)))

(defmacro let-flow
  "manifold.`let-flow` port. It doesn't do any fancy binding dependency analysis
  like manifold does, but it's good enough for the common use cases, not to
  mention drastically simpler to implement.

  A version of `let` where deferred values that are let-bound or closed over can
  be treated as if they are realized values. The body will only be executed once
  all of the let-bound values, even ones only used for side effects, have been
  computed."
  [steps & body]
  (let [steps-pairs (partition 2 steps)]
    (reduce (fn [step [x f]]
              (case x
                :when `(when ~f ~step)
                `(chain ~f (fn [~x] ~step))))
            `(do ~@body)
            (reverse steps-pairs))))

(def ex-unwrap
  "Takes input exception and return the original exception cause (if
  any). This unwraps `ExecutionException` and `CompletionException`"
  impl/ex-unwrap)

(defn unwrap
  "Tries to deref a Future, returns a value upon completion or the
  original exception that triggered exceptional termination (as
  opposed to a wrapped exception)"
  [f]
  (try
    @f
    (catch Exception e
      (throw (ex-unwrap e)))))


