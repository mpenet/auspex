(ns qbits.auspex.executor
  (:import
   (java.util.concurrent
    Executor
    Executors
    ForkJoinPool)))

(defn current-thread-executor
  []
  (reify Executor
    (execute [this r]
      (.run r))))

(defn fork-join-executor
  []
  (ForkJoinPool/commonPool))

(defn fixed-size-executor
  [{:keys [num-threads thread-factory]
    :or {tread-factory (Executors/defaultThreadFactory)}}]
  (Executors/newFixedThreadPool (int num-threads)
                                thread-factory))

(defn clj-future-executor
  []
  (clojure.lang.Agent/soloExecutor))
