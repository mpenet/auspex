(ns qbits.auspex.impl
  (:require [qbits.auspex.protocols :as p]
            [qbits.auspex.function :as f]
            [qbits.auspex.executor :as executor])
  (:import (java.util.concurrent CompletableFuture
                                 Executor
                                 CompletionException
                                 ExecutionException)))

(set! *warn-on-reflection* true)

(defn relevant-ex
  [ex]
  (cond-> ex
    (or (instance? ExecutionException ex)
        (instance? CompletionException ex))
    ex-cause))

(extend-type CompletableFuture
  p/Future
  (-success! [cf x]
    (.complete cf x))

  (-error! [cf e]
    (.completeExceptionally cf e))

  (-complete! [cf f executor]
    (.completeAsync cf
                    (f/supplier f)
                    ^Executor executor))

  (-error? [cf]
    (.isCompletedExceptionally cf))

  (-catch
    ([cf f]
     (.exceptionally cf (f/function #(f (relevant-ex %)))))
    ([cf error-class f]
     (.exceptionally cf
                     (f/function (fn [^Throwable t]
                                   (let [ex (relevant-ex t)]
                                     (if (instance? error-class ex)
                                       (f ex)
                                       (throw ex))))))))

  (-finally
    ([cf f]
     (p/-when-complete cf
                       (fn [_ _] (f)))
    ([cf f executor]
     (p/-when-complete cf
                       (fn [_ _] (f))
                       ^Executor executor)))

  (-handle
    ([cs f]
     (.handle cs (f/bifunction f)))
    ([cs f executor]
     (.handleAsync cs
                   (f/bifunction f)
                   ^Executor executor)))
  (-then
    ([cs f]
     (.thenApply cs (f/function f)))
    ([cs f executor]
     (.thenApplyAsync cs
                      (f/function f)
                      ^Executor executor)))

  (-fmap
    ([cs f]
     (.thenCompose cs (f/function f)))
    ([cs f executor]
     (.thenComposeAsync cs
                        (f/function f)
                        ^Executor executor)))

  (-when-complete
    ([cf f]
     (.whenComplete cf (f/biconsumer f)))
    ([cf f executor]
     (.whenCompleteAsync cf
                         (f/biconsumer f)
                         ^Executor executor)))

  (-realized? [cf]
    (.isDone cf))

  p/Cancel
  (-cancel! [cf]
    (.cancel cf true)
    cf)

  (-canceled? [cf]
    (.isCancelled cf))

  p/Timeout
  (-timeout!
    ([cf timeout-ms]
     (.orTimeout cf
                 timeout-ms
                 java.util.concurrent.TimeUnit/MILLISECONDS))
    ([cf timeout-ms timeout-val]
     (.completeOnTimeout cf
                         timeout-val
                         timeout-ms
                         java.util.concurrent.TimeUnit/MILLISECONDS))))
