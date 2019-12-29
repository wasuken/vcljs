(ns vcljs.cmds.init-test
  (:require [vcljs.cmds.init :as sut]
            [clojure.test :refer :all]))

(defn remove-dir-all [path]
  "danger"
  (do
    (map #(clojure.java.io/delete-file (.getPath %))
         (filter #(.isFile %)
                 (file-seq (clojure.java.io/file path))))
    (map #(clojure.java.io/delete-file (.getPath %))
         (reverse (file-seq (clojure.java.io/file path))))))

(defn setup []
  (remove-dir-all ".vcljs")
  )

(defn cleanup []
  ;; (remove-dir-all ".vcljs")
  )

(deftest init-test
  (testing "subcommand init test"
    (setup)
    (sut/init (-> (java.io.File. "") .getAbsolutePath))
    (is true (.exists (java.io.File. ".vcljs")))
    (is true (.exists (java.io.File. ".vcljs/config.json")))
    (is true (.exists (java.io.File. ".vcljs/vcljs.sqlite")))
    (cleanup)))
