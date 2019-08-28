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
