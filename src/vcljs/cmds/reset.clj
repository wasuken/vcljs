(ns vcljs.cmds.reset
  (:require [clojure.java.jdbc :as j]
            [vcljs.db :refer :all]
            [vcljs.cmds.status :refer :all]
            [vcljs.util :refer :all]))

;;; 直前のcommitのファイル全てを比較し、hash値が異なれば上書きする。
(defn reset
  [config-path]
  (let [db (sqlite-db (str config-path "vcljs.sqlite"))
        latest-commited-recs (j/query db
                                      [(str "select * from nodes"
                                            " where nodes.id in "
                                            " (select cn1.node_id from commit_nodes as cn1 where cn1.commit_id in "
                                            " (select cn2.commit_id from commit_nodes as cn2 where node_id = "
                                            " (select max(cn3.node_id) from commit_nodes as cn3)))")])]
    (when-not (zero? (count latest-commited-recs))
      (doseq [rec latest-commited-recs]
        (let [now-file-contents (slurp (:filepath rec))]
          (when-not (= (sha1-str (str (:filepath now-file-contents)))
                       (:contents_hash rec))
            (spit (:filepath rec) (:content rec))))))))
