(defproject cc.qbits/auspex "0.1.0-alpha6-SNAPSHOT"
  :description "Mini wrapper over java-11 CompletableFuture with a manifold deferred after-taste"
  :url "https://github.com/mpenet/auspex"
  :deploy-repositories [["snapshots" :clojars] ["releases" :clojars]]
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]]
  :source-paths ["src"]
  :cljfmt {:remove-multiple-non-indenting-spaces? true}
  :global-vars {*warn-on-reflection* true})
