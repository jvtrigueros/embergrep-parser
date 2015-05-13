(defproject ember-grep "0.1.0-SNAPSHOT"
  :description "Use the Ember Grep API to download the content."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.json "0.2.6"]
                 [clj-http "1.1.2"]]
  :main ^:skip-aot ember-grep.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
