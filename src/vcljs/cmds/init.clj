(ns vcljs.cmds.init
  (:require [clojure.data.json :as json]
            [clojure.java.jdbc :as j]
            [vcljs.db :refer :all]))

(def sql-map {:create {:nodes (str "nodes(id integer primary key,"
                                   "created_at DATETIME DEFAULT CURRENT_TIMESTAMP,"
                                   "content text not null, content_hash text not null unique,"
                                   "filepath text, status_id integer not null default 0);")
                       :status (str "status(id integer primary key, status_name text not null);"
                                    "insert into status(id, status_name) values(0, 'add');"
                                    "insert into status(id, status_name) values(1, 'commit');")
                       :commits (str "commits(id text priamry key,"
                                     "created_at DATETIME DEFAULT CURRENT_TIMESTAMP,"
                                     "message text not null,"
                                     "branch_id integer not null default 0);")
                       :branches (str "branches(id integer primary key, name text not null);"
                                      "insert into branches(id, name) values(0, 'default');")
                       :commit-nodes (str "commit_nodes(commit_id text, node_id integer,"
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
    (clojure.java.io/make-parents config-path)
    (spit config-path (json/write-str {:current-dir dirpath}))
    (create-database db-path)))
