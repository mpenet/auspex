(ns qbits.auspex.impl
  (:require [qbits.auspex.protocols :as p]
            [qbits.auspex.function :as f]
            [qbits.auspex.executor :as executor])
  (:import (java.util.concurrent CompletableFuture
                                 Executor
                                 CompletionException
                                 ExecutionException)))

(set! *warn-on-reflection* true)

(extend-type CompletableFuture
  p/IFuture
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
     (.exceptionally cf (f/function f)))
    ([cf error-class f]
     (.exceptionally cf
                     (f/function (fn [^Throwable t]
                                   (let [ex (cond-> t
                                              ;; unwrap cause exception? or it's a bad idea?
                                              (or (instance? ExecutionException t)
                                                  (instance? CompletionException t))
                                              ex-cause)]
                                     (if (instance? error-class ex)
                                       (f ex)
                                       (throw ex))))))))

  (-finally
    ([cf f]
     (p/-finally cf f (executor/current-thread-executor)))
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

  p/ICancel
  (-cancel! [cf]
    (.cancel cf true)
    cf)

  (-canceled? [cf]
    (.isCancelled cf))

  p/ITimeout
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
