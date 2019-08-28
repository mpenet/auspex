# auspex

It's a wip/experiment for now, expect breakage.

A small wrapper over java11 `CompletableFuture` that mimics most of
[manifold](https://github.com/ztellman/manifold) `deferred` API,
including `chain`, `catch`, `finally`, `loop/recur`, `zip` and most of
the nice stuff. I intentionally left out let-flow.

It also adds a few interesting features that CompletableFutures
support out of the box, for instance per "step" executor
configuration, cancellation. Futures realization by default run on
Thread/currentThread but you can also specify a custom executor.

You can also use a future as replacement of clojure future via
`(a/future (fn [] ::foo) executor)` it would then run on whatever
ExecutorService you'd choose (there's some sugar for that on
`qbits.auspex.executor`).

Performance:
First indication is that it's quite a bit faster/more efficient already (I
have yet to demonstrate that clearly, but first numbers are
promising).

## Usage

Some examples of usage

```clj
(require '[qbits.auspex :as a])

(let [f (a/future)]
  (a/success! f ::foo) -> true
  @f
  ;; returns ::foo
  )

(let [f (a/future)]
  (a/error! f (ex-info "Oh no" {})) -> true
  @f
  ;; returns ExceptionInfo Oh no
 )


(let [f (a/future)]
  (a/handle f (fn [x err]
                (prn x err)))
  (a/success! f ::foo)

  ;; prints nil ::foo
  )

(let [f (-> (a/future (fn [] 0))
            (a/then inc clojure.lang.Agent/soloExecutor)
            (a/then inc clojure.lang.Agent/pooledExecutor))]

  @f
  ;; similar as before but with steps running on different executors
  )

(let [f0 (a/future)
      f (a/chain f0
                 inc
                 inc
                 inc)]

  ;; chain returns a separate future, feeding f0 will set f0 to 0, f
  ;; will be the future with the composition result
  (a/success! f0 0)
  @f
  ;; returns 3

  @f0
  ;; returns 0
  )

(let [f0 (a/future)
      f (-> (a/chain f0
                  inc
                  (fn [x] (throw (ex-info "Oh no" {})))
                  inc)
            (a/catch (fn [_] "no big deal")))]

  (a/success! f0 0)
  @f
  ;; prints no big deal
  )

(let [f0 (a/future)
      f (-> (a/chain f0
                  inc
                  (fn [x] (throw (ex-info "Oh no" {})))
                  inc)
            (a/catch clojure.lang.ExceptionInfo (fn [_] 10))
            (a/finally (fn [] (prn "...and done"))))]

  (a/success! f0 0)
  @f
  ;; returns 10
  ;; prints ...and done
  )


@(a/zip (a/success-future 1)
        2
        (a/success-future 3))
;; returns (1 2 3)


@(a/one (a/future)
        (a/success-future 2)
        (a/success-future 3))
;; returns 2


@(a/timeout! (a/future (fn []
                         (Thread/sleep 50)
                         ::foo)
                       clojure.lang.Agent/soloExecutor)
             10
             ::timed-out)
;; returns ::timed-out


@(a/loop [x []]
   (if (< (count x) 5)
     (a/recur (conj x (count x)))
     x))
;; returns [0 1 2 3 4]

@(a/loop [x 0]
   (a/chain x
            inc
            #(if (< % 5)
               (a/recur %)
               %)))
;; returns 5

```

## Installation

[![Clojars Project](https://img.shields.io/clojars/v/cc.qbits/auspex.svg)](https://clojars.org/cc.qbits/auspex)

## License

Copyright © 2019 Max Penet

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.