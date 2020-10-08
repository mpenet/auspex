(ns qbits.auspex.protocols)

(defprotocol IFuture
  (-success! [cf x]
    "Sets the success value of future to x.")
  (-error! [cf t]
    "If not already completed, set error value to the given
    exception.")
  (-complete! [cf f] [cf f executor]
    "If not already completed, sets the value to the f return value.")
  (-realized? [cf] "Returns true if future is realized.")
  (-error? [cf] "Returns true if future is in error state.")
  (-catch [cf f] [cf error-class f]
    "Returns a new CompletableFuture is completed when this
     CompletableFuture completes, with the result of the given
     function of the exception triggering this CompletableFuture's
     completion when it completes exceptionally; otherwise, if this
     CompletableFuture completes normally, then the returned
     CompletableFuture also completes normally with the same value.  3
     arg version allows to catch a specific error-class only and let
     others error.")
  (-finally [cf f] [cf f executor]
    "Runs side-effectful code after completion, returns original
    future value.")
  (-handle [cf f] [cf f executor]
    "Returns a new CompletionStage that, when this stage completes
     either normally or exceptionally, is executed using the supplied
     executor, with this stage's result and exception as arguments to
     the supplied function.")
  (-then [cf f] [cf f executor]
    "Runs f on future result and returns a new future with result.")
  (-fmap [cf f] [cf f executor]
    "Runs f, fn returning a future, on future result and returns a new
    future with result.")
  (-when-complete [cf f] [cf f executor]
    "Returns a new future with the same result or exception as this
     stage, that executes the given action using this stage's default
     asynchronous execution facility when this stage completes."))

(defprotocol ICancel
  (-canceled? [cf]
    "Returns true if this CompletableFuture was cancelled before it
  completed normally.")
  (-cancel! [cf]
    "If not already completed, completes this CompletableFuture with a
  CancellationException."))

(defprotocol ITimeout
  (-timeout! [cf timeout-ms] [cf timeout-ms timeout-val]
    "Exceptionally completes this CompletableFuture with a
     TimeoutException if not otherwise completed before the given
     timeout.  3 arg will complete with timeout-val on timeout"))
