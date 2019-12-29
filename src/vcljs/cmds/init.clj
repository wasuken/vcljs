(ns vcljs.cmds.init
  (:require [clojure.data.json :as json]
            [clojure.java.jdbc :as j]))

(def sql-map {:create {:nodes (str "nodes(id integer primary key,"
                                   "created_at DATETIME DEFAULT CURRENT_TIMESTAMP,"
                                   "filepath text);")
                       :add-files "add_files(node_id integer primary key,content text);"
                       :add-dirs "add_dirs(node_id integer primary key);"
                       :commit-files "commit_files(node_id integer primary key,content text);"
                       :commit-dirs "commit_dirs(node_id integer primary key);"
                       :versions (str "versions(id integer priamry key,"
                                      "created_at DATETIME DEFAULT CURRENT_TIMESTAMP,"
                                      "commit_message text,"
                                      "branch_id integer);")
                       :branches "branches(id integer primary key, name text);"
                       :version-nodes (str "version_nodes(version_id integer, node_id integer,"
                                           "primary key(version_id, node_id));")
                       }})

(defn sqlite-db [path]
  {:connection-uri (str "jdbc:sqlite:" path)})

(defn create-database [path]
  (let [db (sqlite-db path)]
    (spit path "")
    (doseq [x (vals (:create sql-map))]
      (j/execute! db (str "create table " x)))))

(defn init [dirpath]
  (let [cur-path (str dirpath ".vcljs/")
        config-path (str cur-path "config.json")
        db-path (str cur-path "vcljs.sqlite")]
    (clojure.java.io/make-parents config-path)
    (spit config-path (json/write-str {:current-dir dirpath}))
    (create-database db-path)))
