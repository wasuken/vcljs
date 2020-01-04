(ns vcljs.cmds.commit
  (:require [clojure.java.jdbc :as j]
            [vcljs.db :refer :all]
            [vcljs.cmds.status :refer :all]
            [vcljs.util :refer :all]))

(defn subsequent-commit-target-node-id-list [db commit-id]
  (let [added-recs (j/query db
                            [(str "select * from nodes"
                                  " where status_id = 0")])
        latest-commited-recs (j/query db
                                      [(str "select * from nodes"
                                            " where id in (select node_id from commit_nodes where commit_id = "
                                            "(select id from commits where created_at = "
                                            "(select max(created_at) from commits) limit 1))")])
        commit-nodes-list (atom [])]
    (when-not  (or (zero? (count added-recs))
                   (zero? (count latest-commited-recs)))
      (doseq [latest-commited-rec latest-commited-recs]
        (let [find-commited-pair-added (some #(= (:filepath %) (:filepath latest-commited-rec))
                                             added-recs)]
          (cond (not (.exists (clojure.java.io/file (:filepath latest-commited-rec))))
                nil
                find-commited-pair-added
                (reset! commit-nodes-list (conj @commit-nodes-list
                                                {:node_id (:id find-commited-pair-added)
                                                 :commit_id commit-id}))
                :else
                (reset! commit-nodes-list (conj @commit-nodes-list
                                                {:node_id (:id latest-commited-rec)
                                                 :commit_id commit-id})))
          (when-not (zero? (count @commit-nodes-list))
            (j/insert-multi! db :commit_nodes @commit-nodes-list)))))))

;;; add->commit
;;; add(a.txt, b.txt)->commit
(defn commit [config-dir-path msg]
  (let [db (sqlite-db (str config-dir-path "vcljs.sqlite"))]
    (try
      (let [added-recs (j/query db ["select * from nodes where status_id = 0"])]
        (if (zero? (count added-recs))
          (println "nothing added files.")
          (let [commit-id (sha1-str (str msg
                                         (.format (java.text.SimpleDateFormat.
                                                   "yyyy/MM/dd hh:mm:ss")
                                                  (java.util.Date.))
                                         (clojure.string/join "," (map #(:filepath %)
                                                                       added-recs))))]
            (j/insert! db :commits {:id commit-id,
                                    :message msg})
            (if (> (count (j/query db ["select * from commits;"])) 1)
              (subsequent-commit-target-node-id-list db commit-id)
              (j/insert-multi! db :commit_nodes
                               (map #(hash-map :node_id (:id %)
                                               :commit_id commit-id)
                                    added-recs)))
            (j/update! db :nodes {:status_id 1} ["status_id = 0"]))))
      (catch Exception e (println "throw Exception:" (.getMessage e))))))

;;; hoge
