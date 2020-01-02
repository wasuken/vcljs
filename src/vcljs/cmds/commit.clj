(ns vcljs.cmds.commit
  (:require [clojure.java.jdbc :as j]
            [vcljs.db :refer :all]
            [vcljs.util :refer :all]))

;;; add->commit
;;; add(a.txt, b.txt)->commit
(defn commit [msg]
  (try
    (let [added-recs (j/query db ["select * from nodes where status_id = 0"])
        dirpath (read-config)
        db (sqlite-db (str (read-config) "vcljs.sqlite"))]
    (cond (zero? (count added-recs))
          (println "nothing added files."))
    (j/update! db :nodes {status_id 1} ["status_id = 0"]))
    (catch Exception e (println "throw Exception:" (.getMessage e)))))
