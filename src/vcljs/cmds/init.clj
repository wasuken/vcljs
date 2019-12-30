(ns vcljs.cmds.init
  (:require [clojure.data.json :as json]
            [clojure.java.jdbc :as j]
            [vcljs.db :refer :all]))

(def sql-map {:create {:nodes (str "nodes(id integer primary key,"
                                   "created_at DATETIME DEFAULT CURRENT_TIMESTAMP,"
                                   "filepath text, status integer not null default 0);")
                       :files "files(node_id integer primary key,content text not null, content_hash text not null);"
                       :dirs "dirs(node_id integer primary key);"
                       :status (str "status(id integer primary key, status_name text not null);"
                                    "insert into status(id, status_name) values(0, 'add');"
                                    "insert into status(id, status_name) values(1, 'commit');")
                       :commits (str "commits(id text priamry key,"
                                      "created_at DATETIME DEFAULT CURRENT_TIMESTAMP,"
                                      "commit_message text,"
                                      "branch_id integer);")
                       :branches "branches(id integer primary key, name text);"
                       :version-nodes (str "commit_nodes(commit_id integer, node_id integer,"
                                           "primary key(commit_id, node_id));")
                       }})

(defn create-database [path]
  (let [db (sqlite-db path)]
    (spit path "")
    (doseq [x (vals (:create sql-map))]
      (j/execute! db (str "create table " x)))))

(defn init [dirpath]
  (let [cur-path (str dirpath ".vcljs/")
        config-path (str cur-path "config.json")
        db-path (str cur-path "vcljs.sqlite")]
    (when (.exists (clojure.java.io/file cur-path))
      (println "already create repository."))
    (when (not (.exists (clojure.java.io/file cur-path)))
      (clojure.java.io/make-parents config-path)
      (spit config-path (json/write-str {:current-dir dirpath}))
      (create-database db-path))))
