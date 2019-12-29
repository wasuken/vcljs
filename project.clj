(defproject vcljs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [clj-sub-command "0.5.1"]
                 [org.clojure/java.jdbc "0.7.11"]
                 [org.xerial/sqlite-jdbc "3.30.1"]
                 [org.clojure/data.json "0.2.7"]]
  :repl-options {:init-ns vcljs.core}
  :main vcljs.core)
