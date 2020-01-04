(ns vcljs.cmds.commit-test
  (:require [vcljs.cmds.commit :as sut]
            [vcljs.cmds.init :refer :all]
            [vcljs.cmds.status :refer :all]
            [vcljs.cmds.add :refer :all]
            [vcljs.util :refer :all]
            [vcljs.db :refer :all]
            [clojure.java.jdbc :as j]
            [clojure.test :refer :all]
            [clojure.java.shell :refer [sh]]))

(def commited-nodes-in-commited-nodes-sql
  (str "select * from nodes"
       " where id in (select node_id from commit_nodes where commit_id = "
       "(select id from commits where created_at = "
       "(select max(created_at) from commits) limit 1))"))

(defn setup []
  (remove-dir-all (read-config))
  (init (str (-> (java.io.File. "") .getAbsolutePath) "/"))
  (doseq [file ["./testdir/a.txt" "./testdir/b.txt" "./testdir/c.txt" "./testdir/d.txt"]]
    (spit file ""))
  )

(defn cleanup []
  (remove-dir-all (read-config))
  )

(deftest base-commit-test
  (testing "commit test"
    (setup)
    (let [files ["./testdir/a.txt" "./testdir/b.txt" "./testdir/c.txt" "./testdir/d.txt"]
          sqlite-path (str (read-config) "vcljs.sqlite")
          db (sqlite-db sqlite-path)]
      (add (read-config) files)
      (sut/commit (read-config) "test")
      (let [added-nodes (j/query db ["select * from nodes where status_id = 0;"])
            commited-nodes-in-nodes (j/query db ["select * from nodes where status_id = 1 order by created_at;"])
            commited-nodes-in-commited-nodes (j/query db [commited-nodes-in-commited-nodes-sql])]
        (is (zero? (count added-nodes)))
        (is (= commited-nodes-in-commited-nodes commited-nodes-in-nodes))))
    (cleanup)))

(deftest multi-commit-test
  (testing "multi commit test"
    (setup)
    (let [files ["./testdir/a.txt" "./testdir/b.txt" "./testdir/c.txt" "./testdir/d.txt"]
          sqlite-path (str (read-config) "vcljs.sqlite")
          db (sqlite-db sqlite-path)]
      (add (read-config) files)
      (let [added-nodes (j/query db ["select * from nodes where status_id = 0;"])
            commited-nodes-in-nodes (j/query db ["select * from nodes where status_id = 1 order by created_at;"])
            commited-nodes-in-commited-nodes (j/query db
                                                      [commited-nodes-in-commited-nodes-sql])]
        (sut/commit (read-config) "test")
        (doseq [file files]
          (spit file file))
        (add (read-config) files)
        (sut/commit (read-config) "test2")
        (let [added-nodes2 (j/query db ["select * from nodes where status_id = 0;"])
              commited-nodes-in-nodes2 (j/query db ["select * from nodes where status_id = 1 order by created_at;"])
              commited-nodes-in-commited-nodes2 (j/query db [commited-nodes-in-commited-nodes-sql])]
          (is (> (count commited-nodes-in-nodes2) 0))
          (is (> (count commited-nodes-in-commited-nodes2) 0))
          (is (= commited-nodes-in-commited-nodes2 commited-nodes-in-nodes2))
          (is (not (= commited-nodes-in-nodes commited-nodes-in-nodes2)))
          (is (not (= commited-nodes-in-commited-nodes commited-nodes-in-commited-nodes2)))
          (is (zero? (count added-nodes2))))))
    (cleanup)))
