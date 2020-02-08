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
  (clojure.java.io/make-parents "./testdir/a.txt")
  (init (str (-> (java.io.File. "") .getAbsolutePath) "/"))
  (doseq [file ["./testdir/a.txt" "./testdir/b.txt" "./testdir/c.txt" "./testdir/d.txt"]]
    (spit file ""))
  )

(defn cleanup []
  (remove-dir-all (read-config))
  (remove-dir-all "./testdir"))

;; (deftest base-commit-test
;;   (testing "commit test"
;;     (setup)
;;     (let [files ["./testdir/a.txt" "./testdir/b.txt" "./testdir/c.txt" "./testdir/d.txt"]
;;           sqlite-path (str (read-config) "vcljs.sqlite")
;;           db (sqlite-db sqlite-path)]
;;       (add (read-config) files)
;;       (sut/commit (read-config) "test")
;;       (let [added-nodes (j/query db ["select * from nodes where status_id = 0;"])
;;             commited-nodes-in-nodes (j/query db ["select * from nodes where status_id = 1 order by created_at;"])
;;             commited-nodes-in-commited-nodes (j/query db [commited-nodes-in-commited-nodes-sql])]
;;         (is (zero? (count added-nodes)))
;;         (is (= commited-nodes-in-commited-nodes commited-nodes-in-nodes))))
;;     (cleanup)))

(deftest move-commit-test
  (testing "move commit test"
    (setup)
    (let [files ["./testdir/a.txt" "./testdir/b.txt" "./testdir/c.txt" "./testdir/d.txt"]
          sqlite-path (str (read-config) "vcljs.sqlite")
          db (sqlite-db sqlite-path)]
      ;; 1
      (doseq [file files]
        (spit file "hoge"))
      (add (read-config) files)
      (sut/commit (read-config) "test1")
      ;; 2
      (doseq [file files]
        (spit file "fuga"))
      (add (read-config) files)
      (sut/commit (read-config) "test2")
      ;; 3
      (doseq [file files]
        (spit file "poyo"))
      (add (read-config) files)
      (sut/commit (read-config) "test3")
      (let [commit-id-list (map #(:id %)
                                (j/query db ["select id, created_at from commits order by created_at"]))]
        ;; move 1 test
        (sut/move-commit (read-config) (nth commit-id-list 0))
        (doseq [file files]
          (is (= (slurp file)
                 "hoge")))
        ;; move 2 test
        (sut/move-commit (read-config) (nth commit-id-list 1))
        (doseq [file files]
          (is (= (slurp file)
                 "fuga")))
        ;; move 3 test
        (sut/move-commit (read-config) (nth commit-id-list 2))
        (doseq [file files]
          (is (= (slurp file)
                 "poyo")))
        )
      (cleanup)
      )))
