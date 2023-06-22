# qbits.auspex 


This provides a clj friendly wrapper for CompletableFuture and adds a
  few utility functions to mimic manifold features.  Shamelessly stole
  code/ideas from the awesome manifold library.



## `->Recur`
``` clojure

(->Recur args)
```

<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L127-L129)</sub>
## `Recur`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L127-L129)</sub>
## `all`
``` clojure

(all xs)
```


Takes a collection of values, some of which may be futures, and
   returns a future that will contain a list of realized values
<br><sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L109-L113)</sub>
## `all'`
``` clojure

(all' fs)
```


Like `all` buf faster if you know you're only dealing with futures
  args
<br><sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L101-L107)</sub>
## `any`
``` clojure

(any xs)
```


Returns one value from a collection of futures
<br><sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L86-L89)</sub>
## `any'`
``` clojure

(any' fs)
```


Like `any` but faster if you know you're only dealing with future args
<br><sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L81-L84)</sub>
## `cancel!`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L24-L24)</sub>
## `canceled?`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L25-L25)</sub>
## `catch`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L15-L15)</sub>
## `chain`
``` clojure

(chain x & fns)
```


Composes functions starting with x as argument triggering calls to
  fns for every step coercing the return values to deferreds if
  necessary and returns a deferred with the final result.
<br><sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L64-L74)</sub>
## `chain'`
``` clojure

(chain' x & fns)
```


Like chain but assumes fns return raw values instead of potential
  futures
<br><sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L58-L62)</sub>
## `chain-futures`
``` clojure

(chain-futures x & fs)
```


Like chain but takes a value and functions that will return futures
<br><sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L76-L79)</sub>
## `complete!`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L17-L17)</sub>
## `empty`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L28-L28)</sub>
## `error!`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L13-L13)</sub>
## `error-future`
``` clojure

(error-future x)
```


Returns a new CompletableFuture that is already completed
  exceptionally with the given exception.
<br><sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L52-L56)</sub>
## `error?`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L14-L14)</sub>
## `ex-unwrap`

Takes input exception and return the original exception cause (if
  any). This unwraps `ExecutionException` and `CompletionException`
<br><sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L209-L212)</sub>
## `finally`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L16-L16)</sub>
## `fmap`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L22-L22)</sub>
## `future`
``` clojure

(future)
(future f)
(future f executor)
```


No arg creates an empty/incomplete future, 1 arg creates a future
  that will get the return value of f as realized value on fork-join
  common pool, 2 arg creates a future that will be realized on
  ExecutorService supplied with return value of f as realized value.

  The executor that is set at this stage will continue to be used for
  subsequent steps (then/chain etc) if another one is not specified at
  another level
<br><sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L31-L45)</sub>
## `future?`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L29-L29)</sub>
## `handle`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L20-L20)</sub>
## `let-flow`
``` clojure

(let-flow steps & body)
```


Macro.


manifold.`let-flow` port. It doesn't do any fancy binding dependency analysis
  like manifold does, but it's good enough for the common use cases, not to
  mention drastically simpler to implement.

  A version of `let` where deferred values that are let-bound or closed over can
  be treated as if they are realized values. The body will only be executed once
  all of the let-bound values, even ones only used for side effects, have been
  computed.
<br><sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L191-L207)</sub>
## `loop`
``` clojure

(loop bindings & body)
```


Macro.


A version of Clojure's loop which allows for asynchronous loops, via
  `qbits.auspex/recur`.  `loop` will always return a CompletableFuture
  Value, even if the body is synchronous.  Note that `loop` does
  **not** coerce values to deferreds, actual `qbits.auspex/future`s
  must be used.

   (loop [i 1e6]
     (chain (future i)
       #(if (zero? %)
          %
          (recur (dec %)))))
<br><sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L140-L189)</sub>
## `one`
``` clojure

(one & xs)
```


Returns one value from a list of futures
<br><sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L96-L99)</sub>
## `one'`
``` clojure

(one' & fs)
```


Like `one` but faster if you know you're only dealing with future args
<br><sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L91-L94)</sub>
## `realized?`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L19-L19)</sub>
## `recur`
``` clojure

(recur & args)
```


Like recur, but to be used with `qbits.auspex/loop`
<br><sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L131-L134)</sub>
## `recur?`
``` clojure

(recur? x)
```

<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L136-L138)</sub>
## `success!`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L18-L18)</sub>
## `success-future`
``` clojure

(success-future x)
```


Returns a new CompletableFuture that is already completed with the
  given value.
<br><sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L46-L50)</sub>
## `then`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L21-L21)</sub>
## `timeout!`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L26-L26)</sub>
## `unwrap`
``` clojure

(unwrap f)
```


Tries to deref a Future, returns a value upon completion or the
  original exception that triggered exceptional termination (as
  opposed to a wrapped exception)
<br><sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L214-L222)</sub>
## `when-complete`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L23-L23)</sub>
## `wrap`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L27-L27)</sub>
## `zip`
``` clojure

(zip & xs)
```


Takes a list of values, some of which can be futures and returns a
  future that will contains a list of realized values
<br><sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L121-L125)</sub>
## `zip'`
``` clojure

(zip' & fs)
```


Like zip but faster if you know you're only dealing with futures
  args
<br><sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex.clj#L115-L119)</sub>
# qbits.auspex.executor 





## `cached-executor`
``` clojure

(cached-executor)
(cached-executor thread-factory)
```


Creates a thread pool that creates new threads as needed, but will
  reuse previously constructed threads when they are available
<br><sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/executor.clj#L32-L38)</sub>
## `clojure-future-executor`
``` clojure

(clojure-future-executor)
```


Returns the thread pool used by clojure.core/future.
<br><sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/executor.clj#L55-L58)</sub>
## `current-thread`
``` clojure

(current-thread)
```


Returns current thread
<br><sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/executor.clj#L60-L63)</sub>
## `current-thread-executor`
``` clojure

(current-thread-executor)
```


Returns an executor that will run task in calling thread
<br><sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/executor.clj#L11-L16)</sub>
## `fixed-size-executor`
``` clojure

(fixed-size-executor {:keys [num-threads thread-factory], :or {thread-factory (Executors/defaultThreadFactory)}})
```


Returns a new fixed size executor of size `num-threads`.
<br><sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/executor.clj#L48-L53)</sub>
## `fork-join-executor`
``` clojure

(fork-join-executor)
```


Returns forkJoin commonPool Executor
<br><sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/executor.clj#L18-L21)</sub>
## `single-executor`
``` clojure

(single-executor)
(single-executor thread-factory)
```


Creates an Executor that uses a single worker thread operating off an
  unbounded queue.
<br><sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/executor.clj#L40-L46)</sub>
## `thread-factory`
``` clojure

(thread-factory {:keys [fmt priority daemon]})
```

<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/executor.clj#L65-L76)</sub>
## `work-stealing-executor`
``` clojure

(work-stealing-executor)
(work-stealing-executor parallelism)
```


Creates a thread pool that maintains enough threads to support the
  given parallelism level, and may use multiple queues to reduce
  contention. Arity 1 will have parallelism = available processors
<br><sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/executor.clj#L23-L30)</sub>
# qbits.auspex.function 





## `biconsumer`
``` clojure

(biconsumer f)
```

<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/function.clj#L35-L40)</sub>
## `bifunction`
``` clojure

(bifunction f)
```

<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/function.clj#L22-L27)</sub>
## `consumer`
``` clojure

(consumer f)
```

<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/function.clj#L29-L33)</sub>
## `function`
``` clojure

(function f)
```

<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/function.clj#L16-L20)</sub>
## `supplier`
``` clojure

(supplier f)
```

<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/function.clj#L11-L14)</sub>
# qbits.auspex.impl 





## `ex-unwrap`
``` clojure

(ex-unwrap ex)
```


Unwraps exceptions if we have a valid ex-cause present
<br><sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/impl.clj#L11-L17)</sub>
# qbits.auspex.manifold 





## `wrap`
``` clojure

(wrap fut)
```


Converts `CompletableFuture` to `manifod.deferred`
<br><sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/manifold.clj#L94-L101)</sub>
# qbits.auspex.protocols 





## `-cancel!`
``` clojure

(-cancel! _)
```

<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L27-L28)</sub>
## `-canceled?`
``` clojure

(-canceled? _)
```

<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L30-L31)</sub>
## `-catch`
``` clojure

(-catch _ f)
(-catch _ f pattern)
```

<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L9-L10)</sub>
## `-complete!`
``` clojure

(-complete! _ f executor)
```

<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L18-L19)</sub>
## `-empty`
``` clojure

(-empty x)
```

<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L45-L46)</sub>
## `-error!`
``` clojure

(-error! _ val)
```

<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L21-L22)</sub>
## `-error?`
``` clojure

(-error? _)
```

<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L24-L25)</sub>
## `-finally`
``` clojure

(-finally _ f)
(-finally _ f pattern)
```

<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L12-L13)</sub>
## `-fmap`
``` clojure

(-fmap _ f)
(-fmap _ f executor)
```

<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L6-L7)</sub>
## `-future?`
``` clojure

(-future? x)
```

<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L51-L52)</sub>
## `-handle`
``` clojure

(-handle _ f)
(-handle _ f executor)
```

<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L36-L37)</sub>
## `-realized?`
``` clojure

(-realized? _)
```

<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L39-L40)</sub>
## `-success!`
``` clojure

(-success! _ val)
```

<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L15-L16)</sub>
## `-then`
``` clojure

(-then _ f)
(-then _ f executor)
```

<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L3-L4)</sub>
## `-timeout!`
``` clojure

(-timeout! _ timeout-ms)
(-timeout! _ timeout-ms timeout-val)
```

<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L33-L34)</sub>
## `-when-complete`
``` clojure

(-when-complete _ f)
(-when-complete _ f executor)
```

<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L42-L43)</sub>
## `-wrap`
``` clojure

(-wrap x)
```

<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L48-L49)</sub>
## `Cancel!`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L27-L28)</sub>
## `Canceled?`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L30-L31)</sub>
## `Catch`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L9-L10)</sub>
## `Complete!`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L18-L19)</sub>
## `Empty`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L45-L46)</sub>
## `Error!`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L21-L22)</sub>
## `Error?`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L24-L25)</sub>
## `FMap`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L6-L7)</sub>
## `Finally`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L12-L13)</sub>
## `Future`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L51-L52)</sub>
## `Handle`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L36-L37)</sub>
## `Realized?`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L39-L40)</sub>
## `Success!`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L15-L16)</sub>
## `Then`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L3-L4)</sub>
## `Timeout!`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L33-L34)</sub>
## `WhenComplete`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L42-L43)</sub>
## `Wrap`
<sub>[source](https://github.com/mpenet/auspex/blob/master/src/qbits/auspex/protocols.clj#L48-L49)</sub>
