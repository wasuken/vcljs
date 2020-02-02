(ns vcljs.cmds.reset-test
  (:require [vcljs.cmds.reset :as sut]
            [vcljs.cmds.commit :refer :all]
            [vcljs.cmds.init :refer :all]
            [vcljs.cmds.add :refer :all]
            [vcljs.db :refer :all]
            [clojure.test :refer :all]
            [vcljs.util :refer :all]))

(defn setup []
  (remove-dir-all (read-config))
  (clojure.java.io/make-parents "./testdir/a.txt")
  (init (str (-> (java.io.File. "") .getAbsolutePath) "/"))
  (doseq [file ["./testdir/a.txt" "./testdir/b.txt" "./testdir/c.txt" "./testdir/d.txt"]]
    (spit file "")))

(defn cleanup []
  (remove-dir-all (read-config))
  (remove-dir-all "./testdir"))

(deftest base-reset-test
  (testing "reset test"
    (setup)
    (let [files ["./testdir/a.txt" "./testdir/b.txt" "./testdir/c.txt" "./testdir/d.txt"]
          sqlite-path (str (read-config) "vcljs.sqlite")
          db (sqlite-db sqlite-path)]
      (doseq [file files]
        (spit file "hoge"))
      (add (read-config) files)
      (commit (read-config) "test")
      (doseq [file files]
        (spit file file))
      (sut/reset (read-config))
      (doseq [file files]
        (is (= (slurp file) "hoge"))))
    (cleanup)))
