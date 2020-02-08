(ns vcljs.cmds.status-test
  (:require [vcljs.cmds.commit :refer :all]
            [vcljs.cmds.init :refer :all]
            [vcljs.cmds.status :as sut]
            [vcljs.cmds.add :refer :all]
            [vcljs.util :refer :all]
            [vcljs.db :refer :all]
            [clojure.java.jdbc :as j]
            [clojure.test :refer :all]
            [clojure.java.shell :refer [sh]]))

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

(deftest added<->adding->modified-recs
  (testing "added<->adding->modified-recs test"
    (setup)
    (let [sqlite-path (str (read-config) "vcljs.sqlite")
          db (sqlite-db sqlite-path)
          files ["./testdir/a.txt" "./testdir/b.txt" "./testdir/c.txt" "./testdir/d.txt"]]
      (add (read-config) files)
      (doseq [file files]
        (spit file "hoge"))
      (is (= (map #(:filepath %)
                  (sut/added<->adding->modified-recs (map #(.getAbsolutePath (clojure.java.io/file %)) files)
                                                     db))
             (map #(.getAbsolutePath (clojure.java.io/file %)) files))))
    (commit (read-config) "test1")
    (cleanup)))
