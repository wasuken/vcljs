(ns vcljs.cmds.add-test
  (:require [vcljs.cmds.add :as sut]
            [vcljs.cmds.init :refer [init]]
            [vcljs.cmds.commit :refer :all]
            [vcljs.util :refer :all]
            [vcljs.db :refer :all]
            [clojure.test :refer :all]
            [clojure.java.jdbc :as j]
            [org.satta.glob :refer [glob]]))

(defn setup []
  (remove-dir-all (read-config))
  (init (str (-> (java.io.File. "") .getAbsolutePath) "/"))
  (let [files ["./testdir/a.txt" "./testdir/b.txt" "./testdir/c.txt" "./testdir/d.txt"
               "./testdir/hoge"]]
    (clojure.java.io/make-parents "./testdir/a.txt")
    (doseq [file files]
      (spit file "")))
  )

(defn cleanup []
  (remove-dir-all (read-config))
  (remove-dir-all "./testdir")
  )

(deftest base-add-test
  (testing "add file test"
    (setup)
    (let [files ["./testdir/a.txt" "./testdir/b.txt" "./testdir/c.txt" "./testdir/d.txt"
                 "./testdir/hoge"]
          sqlite-path (str (read-config) "vcljs.sqlite")
          db (sqlite-db sqlite-path)]
      (sut/add (read-config) files))
    (cleanup)))

(deftest add-pattern-test
  (testing "add file test"
    (setup)
    (let [patterns ["testdir/*"]
          sqlite-path (str (read-config) "vcljs.sqlite")
          db (sqlite-db sqlite-path)]
      (sut/add (read-config) patterns))
    (cleanup)))

(deftest add-cancel-test
  (testing "add-cancel command test"
    (setup)
    (let [files (map #(.getAbsolutePath (clojure.java.io/file %)) ["testdir/a.txt" "testdir/b.txt"])
          sqlite-path (str (read-config) "vcljs.sqlite")
          db (sqlite-db sqlite-path)]
      (sut/add (read-config) files)
      (sut/add-cancel (read-config) [(first files)])
      (is (= (count (map #(:filepath %)
                         (j/query db ["select filepath from nodes"])))
             1))
      (doseq [actual (map #(:filepath %) (j/query db ["select filepath from nodes"]))
              expected [(second files)]]
        (is (= actual
               expected))))
    (cleanup)))

(deftest add->commit->add-test
  (testing "add->commit->add"
    (setup)
    (let [files (map #(.getAbsolutePath (clojure.java.io/file %)) ["testdir/a.txt" "testdir/b.txt"])
          sqlite-path (str (read-config) "vcljs.sqlite")
          db (sqlite-db sqlite-path)]
      (sut/add (read-config) files)
      (commit (read-config) "test")
      (doseq [file files]
        (spit file "hogehogehogoehgohgoeh"))
      (sut/add (read-config) files)
      (is (= (count files)
             (count (j/query db ["select * from nodes where status_id = 0"])))))
    (cleanup)))
