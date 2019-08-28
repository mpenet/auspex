(ns qbits.auspex.function
  (:import
   (java.util.function Function
                       BiFunction
                       BiConsumer
                       Supplier
                       Consumer)))

(set! *warn-on-reflection* true)

(defn supplier
  [f]
  (reify Supplier
    (get [_] (f))))

(defn function
  ^Function
  [f]
  (reify Function
    (apply [_ x] (f x))))

(defn bifunction
  ^BiFunction
  [f]
  (reify BiFunction
    (apply [_ x y]
      (f x y))))

(defn consumer
  ^Consumer
  [f]
  (reify Consumer
    (accept [_ x] (f x))))

(defn biconsumer
  ^BiConsumer
  [f]
  (reify BiConsumer
    (accept [_ x y]
      (f x y))))
