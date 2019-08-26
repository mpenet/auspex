# auspex

A small wrapper over java11 `CompletableFuture` that mimics most of
[manifold](https://github.com/ztellman/manifold) `deferred` API,
including `chain`, `catch`, `finally`, `loop/recur`, `zip` and most of
the nice stuff. I intentionally left out let-flow.

It also adds a few interesting features that CompletableFutures
support out of the box, for instance per "step" executor
configuration, cancellation. Futures realization by default run on
Thread/currentThread but you can also specify a custom executor.

``` clj
(-> (a/deferred)
    (a/then inc)
    (a/then inc executor-a)
    (a/then inc executor-b)
    (a/then inc executor-c))
```

You can also use a future as replacement of clojure future via
`(a/future (fn [] ::foo) executor)` it would then run on whatever
ExecutorService you'd choose (there's some sugar for that on
`qbits.auspex.executor`).

Performance:
First indication is that it's quite a bit faster/more efficient already (I
have yet to demonstrate that clearly, but first numbers are
promising).

## Installation

[![Clojars Project](https://img.shields.io/clojars/v/cc.qbits/auspex.svg)](https://clojars.org/cc.qbits/auspex)

## Usage

Run the project's tests (they'll fail until you edit them):

    $ clj -A:test:runner

## Examples

wip

## License

Copyright © 2019 Max Penet

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
