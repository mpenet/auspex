(ns qbits.auspex.impl
  (:require [qbits.xi.protocols :as p]
            [qbits.auspex.protocols :as ap]
            [qbits.auspex.function :as f])
  (:import (java.util.concurrent CompletableFuture
                                 Executor
                                 CompletionException
                                 ExecutionException)))

(set! *warn-on-reflection* true)

(defn ex-unwrap
  "Unwraps exceptions if we have a valid ex-cause present"
  [ex]
  (if (or (instance? ExecutionException ex)
          (instance? CompletionException ex))
    (or (ex-cause ex) ex)
    ex))

(extend-type CompletableFuture

  ap/Wrap
  (-wrap [cf] cf)

  p/Future

  p/Success!
  (-success! [cf x]
    (.complete cf x))

  p/Error!
  (-error! [cf e]
    (.completeExceptionally cf e))

  p/Complete!
  (-complete! [cf f executor]
    (.completeAsync cf
                    (f/supplier f)
                    ^Executor executor))

  p/Error?
  (-error? [cf]
    (.isCompletedExceptionally cf))

  p/Catch
  (-catch
    ([cf f]
     (.exceptionally cf (f/function #(f (ex-unwrap %)))))
    ([cf error-class f]
     (.exceptionally cf
                     (f/function (fn [^Throwable t]
                                   (let [ex (ex-unwrap t)]
                                     (if (instance? error-class ex)
                                       (f ex)
                                       (throw ex))))))))
  p/Finally
  (-finally
    ([cf f]
     (p/-when-complete cf
                       (fn [_ _] (f))))
    ([cf f executor]
     (p/-when-complete cf
                       (fn [_ _] (f))
                       ^Executor executor)))
  p/Handle
  (-handle
    ([cs f]
     (.handle cs (f/bifunction f)))
    ([cs f executor]
     (.handleAsync cs
                   (f/bifunction f)
                   ^Executor executor)))

  p/Then
  (-then
    ([cs f]
     (.thenApply cs (f/function f)))
    ([cs f executor]
     (.thenApplyAsync cs
                      (f/function f)
                      ^Executor executor)))
  p/FMap
  (-fmap
    ([cs f]
     (.thenCompose cs (f/function f)))
    ([cs f executor]
     (.thenComposeAsync cs
                        (f/function f)
                        ^Executor executor)))
  p/WhenComplete
  (-when-complete
    ([cf f]
     (.whenComplete cf (f/biconsumer f)))
    ([cf f executor]
     (.whenCompleteAsync cf
                         (f/biconsumer f)
                         ^Executor executor)))
  p/Realized?
  (-realized? [cf]
    (.isDone cf))

  p/Cancel!
  (-cancel! [cf]
    (.cancel cf true)
    cf)

  p/Canceled?
  (-canceled? [cf]
    (.isCancelled cf))

  p/Timeout!
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

(extend-protocol ap/Wrap
  Object
  (-wrap [x] (CompletableFuture/completedFuture x)))
