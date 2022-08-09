(ns qbits.auspex.manifold
  (:require [qbits.auspex.protocols :as p]
            [manifold.deferred :as d])
  (:import (java.util.concurrent CompletableFuture)))

(defn- error
  [e]
  (if (instance? Throwable e)
    e
    (ex-info (format "manifold.deferred error: %s"
                     (ex-message e))
             {:qbits.auspex.manifold/error e})))

(extend-type manifold.deferred.IDeferred

  p/Future
  p/Success!
  (-success! [d x]
    (d/success! d x))

  p/Error!
  (-error! [d e]
    (d/error! d (error e)))

  p/Catch
  (-catch
    ([d f]
     (d/catch d
              (fn [e]
                (f (error e)))))
    ([d error-class f]
     (d/catch d error-class f)))

  p/Finally
  (-finally
    ([d f]
     (p/-finally d f nil))
    ([d f executor]
     (d/finally d f)))

  p/Handle
  (-handle
    ([d f] (p/-handle d f nil))
    ([d f executor]
     (d/on-realized d
                    #(f % nil)
                    #(f nil (error %)))))

  p/Then
  (-then
    ([d f]
     (p/-then d f nil))
    ([d f executor]
     (d/chain' d f)))

  p/FMap
  (-fmap
    ([d f]
     (p/-fmap d f nil))
    ([d f executor]
     (d/chain d f)))

  p/WhenComplete
  (-when-complete
    ([d f]
     (p/-when-complete d f nil))
    ([d f executor]
     (p/-handle d f nil)))

  p/Realized?
  (-realized? [d]
    (d/realized? d))

  p/Timeout!
  (-timeout!
    ([d timeout-ms]
     (d/timeout! d timeout-ms))
    ([d timeout-ms timeout-val]
     (d/timeout! d timeout-ms timeout-val)))

  p/Wrap
  (-wrap [x]
    (let [cf (CompletableFuture.)]
      (d/on-realized x
                     #(.complete cf %)
                     #(.completeExceptionally cf (error %)))
      cf))

  p/Empty
  (-empty [_] (d/deferred)))

(defn wrap
  "Converts `CompletableFuture` to `manifod.deferred`"
  [fut]
  (let [d (d/deferred)]
    (-> fut
        (p/-then #(d/success! d %))
        (p/-catch #(d/error! d %)))
    d))
