(ns vcljs.cmds.add
  (:require [clojure.java.jdbc :as j]
            [vcljs.db :refer :all]
            [org.satta.glob :refer [glob]]))

(defn insert-node-list [path-list db]
  (let [result (atom [])]
    (doseq [path (map #(.getAbsolutePath (clojure.java.io/file %)) path-list)]
      (let [id (:generated_key (j/insert! db :nodes
                                          {:filepath path}))]
        (reset! result (conj @result {:id id, :filepath path}))))
    @result))

(defn insert-list-add [path-list db]
  (let [nodes-list (insert-node-list path-list db)
        already-filepaths (map #(:filepath %)
                               (j/query db ["select filepath from nodes"]))
        filtered-nodes (remove #(clojure.string/includes? already-filepaths
                                                          (:filepath %))
                               nodes-list)
        dirs (filter #(.isDirectory
                       (clojure.java.io/file (:filepath %))) filtered-nodes)
        files (remove #(.isDirectory
                        (clojure.java.io/file (:filepath %))) filtered-nodes)]
    (j/insert-multi! db
                     :add_dirs
                     (map #(hash-map :node_id (:id %))
                          dirs))
    (j/insert-multi! db
                     :add_files
                     (map #(hash-map :node_id (:id %),
                                     :content (slurp (:filepath %)))
                          files))))

(defn insert-add-from-pattern [patterns db]
  (insert-list-add (mapcat #(map (fn [x] (.getAbsolutePath x)) (glob %)) patterns) db))

(defn add [config-dir-path arguments]
  (try
    (if-not (.exists (clojure.java.io/file ".vcljs/"))
      (println "please running init command.")
      (let [db (sqlite-db (str config-dir-path "vcljs.sqlite"))]
        (insert-list-add (filter #(.exists (clojure.java.io/file %)) arguments) db)
        (insert-add-from-pattern (remove #(.exists (clojure.java.io/file %)) arguments) db)))
    ;; これよくないやろ...。
    (catch Exception e (println "caught exception: " (.getMessage e)))))
