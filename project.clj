(require '[clojure.edn :as edn])

(defonce tools-deps (delay (edn/read-string (slurp "deps.edn"))))

(defn deps
  []
  (some->> @tools-deps
           :deps
           (map (fn [[coord {:keys [mvn/version exclusions]}]]
                  [coord version :exclusions exclusions]))))

(defn repositories []
  (:mvn/repos @tools-deps))

(defproject cc.qbits/auspex "0.1.0-alpha2"
  :description "Mini wrapper over java-11 CompletableFuture with a manifold deferred after-taste"
  :url "https://github.com/mpenet/auspex"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies ~(deps)
  :source-paths ["src"]
  :global-vars {*warn-on-reflection* true})
