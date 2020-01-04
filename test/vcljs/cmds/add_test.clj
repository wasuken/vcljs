(ns vcljs.cmds.add-test
  (:require [vcljs.cmds.add :as sut]
            [vcljs.cmds.init :refer [init]]
            [vcljs.util :refer :all]
            [vcljs.db :refer :all]
            [clojure.test :refer :all]
            [clojure.java.jdbc :as j]))

(defn setup []
  (remove-dir-all (read-config))
  (clojure.java.shell/sh "cd" "./testdir")
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

;; (deftest add-cancel-test
;;   (testing "add-cancel command test"
;;     (setup)
;;     (cleanup)))
