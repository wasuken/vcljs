(ns vcljs.cmds.add
  (:require [clojure.java.jdbc :as j]
            [vcljs.db :refer :all]
            [vcljs.util :refer :all]
            [vcljs.cmds.status :refer [added<->adding->modified-recs]]
            [org.satta.glob :refer [glob]]))

(defn path-str->fullpath [str]
  (.getAbsolutePath (clojure.java.io/file str)))

(defn insert-node-list [path-list db]
  (let [already-added (j/query db [(str "select filepath, content_hash from nodes"
                                        " where status_id = 0")])
        filtered-nodes (remove (fn [x] (nil? (some (fn [y] (= x (:filepath y))) already-added)))
                               (remove #(.isDirectory (clojure.java.io/file %)) path-list))]
    (j/insert-multi! db :nodes
                     (map #(let [content (slurp %)]
                             (hash-map :content content,
                                       :filepath %,
                                       :content_hash (sha1-str (str % content))))
                          filtered-nodes))
    (doseq [mod-rec (added<->adding->modified-recs path-list db)]
      (let [content (slurp (:filepath mod-rec))]
        (j/update! db :nodes
                   {:content content,
                    :content_hash (sha1-str (str (:filepath mod-rec)
                                                 content))}
                   ["id = ?" (:id mod-rec)])))))

(defn insert-add-from-pattern [patterns db]
  (insert-node-list
   (mapcat #(map (fn [x] (.getAbsolutePath x)) (glob %)) patterns) db))

(defn add [config-dir-path arguments]
  (let [db (sqlite-db (str config-dir-path "vcljs.sqlite"))]
    (if (and (= (count arguments) 1) (= (first arguments) "."))
      (insert-node-list (drop 1 (file-seq (clojure.java.io/file "./"))) db)
      (do
        (insert-node-list (map path-str->fullpath
                               (filter #(.exists (clojure.java.io/file %)) arguments)) db)
        (insert-add-from-pattern (remove #(.exists (clojure.java.io/file %)) arguments) db)))))

(defn add-cancel [config-dir-path arguments]
  (try
    (let [db (sqlite-db (str config-dir-path "vcljs.sqlite"))])
    (catch Exception e (println "caught exception: " (.getMessage e)))))
