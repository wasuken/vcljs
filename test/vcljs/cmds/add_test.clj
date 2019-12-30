(ns vcljs.cmds.add-test
  (:require [vcljs.cmds.add :as sut]
            [vcljs.cmds.init :refer [init]]
            [vcljs.util :refer :all]
            [vcljs.db :refer :all]
            [clojure.test :refer :all]
            [clojure.java.jdbc :as j]))

(defn setup []
  (remove-dir-all (read-config))
  (init (str (-> (java.io.File. "") .getAbsolutePath) "/"))
  )

(defn cleanup []
  (remove-dir-all (read-config))
  )

(deftest base-add-test
  (testing "add file test"
    (setup)
    (let [files ["./testdir/a.txt" "./testdir/b.txt" "./testdir/c.txt" "./testdir/d.txt"
                 "./testdir/hoge"]
          sqlite-path (str (read-config) "vcljs.sqlite")
          db (sqlite-db sqlite-path)]
      (sut/add (read-config) files)
      (is (= (count (j/query db ["select * from nodes"])) 5))
      (is (= (count (j/query db ["select * from add_files"])) 4))
      (is (= (count (j/query db ["select * from add_dirs"])) 1))
      (doseq [path (map #(:filepath %)
                        (j/query db ["select * from nodes"]))]
        (is (.exists (clojure.java.io/file path)))))
    (cleanup)))

(deftest add-pattern-test
  (testing "add file test"
    (setup)
    (let [patterns ["testdir/*"]
          sqlite-path (str (read-config) "vcljs.sqlite")
          db (sqlite-db sqlite-path)]
      (sut/add (read-config) patterns)
      (is (= (count (j/query db ["select * from nodes"])) 5))
      (is (= (count (j/query db ["select * from add_files"])) 4))
      (is (= (count (j/query db ["select * from add_dirs"])) 1))
      (doseq [path (map #(:filepath %)
                        (j/query db ["select * from nodes"]))]
        (is (.exists (clojure.java.io/file path)))))
    (cleanup)))
