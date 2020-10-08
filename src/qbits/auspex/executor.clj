(ns qbits.auspex.executor
  (:import
   (java.util.concurrent Executor
                         Executors
                         ForkJoinPool)))

(set! *warn-on-reflection* true)

(defn current-thread-executor
  "Returns an executor that will run task in calling thread"
  []
  (reify Executor
    (execute [this r]
      (.run r))))

(defn fork-join-executor
  "Returns forkJoin commonPool Executor"
  []
  (ForkJoinPool/commonPool))

(defn work-stealing-executor
  "Creates a thread pool that maintains enough threads to support the
  given parallelism level, and may use multiple queues to reduce
  contention. Arity 1 will have parallelism = available processors"
  ([]
   (Executors/newWorkStealingPool))
  ([parallelism]
   (Executors/newWorkStealingPool (int parallelism))))

(defn cached-executor
  "Creates a thread pool that creates new threads as needed, but will
  reuse previously constructed threads when they are available"
  []
  (Executors/newCachedThreadPool))

(defn single-executor
  "Creates an Executor that uses a single worker thread operating off an
  unbounded queue."
  []
  (Executors/newSingleThreadExecutor))

(defn fixed-size-executor
  "Returns a new fixed size executor of size `num-threads`."
  [{:keys [num-threads thread-factory]
    :or {thread-factory (Executors/defaultThreadFactory)}}]
  (Executors/newFixedThreadPool (int num-threads)
                                thread-factory))

(defn clojure-future-executor
  "Returns the thread pool used by clojure.core/future."
  []
  (clojure.lang.Agent/soloExecutor))

(defn current-thread
  "Returns current thread"
  []
  (Thread/currentThread))
