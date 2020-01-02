(ns vcljs.cmds.status
  (:require [clojure.java.jdbc :as j]
            [clojure.data :refer :all]
            [vcljs.db :refer :all]
            [vcljs.util :refer :all]))

(defn added<->commited->modified-recs [db]
  (let [added-recs (j/query db
                            [(str "select * from nodes"
                                  " where status_id = 0")])
        latest-commited-recs (j/query db
                                      [(str "select * from nodes"
                                            " where id = (select cn1.node_id from commit_nodes as cn1 where node_id = "
                                            " (select max(cn2.id) from commit_nodes as cn2))")])]
    (if (or (zero? (count added-recs))
            (zero? (count latest-commited-recs)))
      []
      (filter #(some (fn [x] (and (= (:filepath x) (:filepath %))
                                  (not (= (:content_hash x)
                                          (:content_hash %))))) latest-commited-recs)
              added-recs))))

(defn added<->adding->modified-recs [adding-path db]
  (let [added-recs (j/query db
                            [(str "select * from nodes"
                                  " where status_id = 0")])]
    (if (zero? (count added-recs))
      []
      (filter #(some (fn [x] (and (= x (:filepath %))
                                  (not (= (sha1-str x (slurp x))
                                          (:content_hash %))))) adding-path)
              added-recs))))

(defn diff-print [result-list]
  (doseq [result result-list]
    (cond (= (:status result) "modified")
          (do
            (print "====[")
            (print (:filepath result))
            (println "]====")
            (print (:status result))
            (print "\t -> ")
            (println (count (remove nil? (first (:diff-result result))))))
          (= (:status result) "deleted")
          (do
            (print "====[")
            (print (:filepath result))
            (println "]====")
            (println (:status result))))))

(defn added-watch-status [target-recs db]
  (let [result-list (atom [])]
    (doseq [target-rec target-recs]
      (let [df (if (.exists (clojure.java.io/file (:filepath target-rec)))
                 (let [diff-result (diff (clojure.string/split (:content target-rec)
                                                               #"\n")
                                         (clojure.string/split (slurp (:filepath target-rec))
                                                               #"\n"))]
                   (cond (zero? (count (remove nil? (first diff-result))))
                         {:status "no change",
                          :filepath (:filepath target-rec)}
                         :else
                         {:status "modified",
                          :filepath (:filepath target-rec),
                          :diff-result diff-result}))
                 {:status "deleted", :filepath (:filepath target-rec)})]
        (reset! result-list (conj @result-list df))))
    @result-list))

;;; 追加したファイルのみ
(defn adding-nodes-status [db]
  (let [added-recs (j/query db
                            [(str "select * from nodes"
                                  " where status_id = 0")])]
    (sort (comp (fn [x y] (compare (:filepath x) (:filepath y))))
          added-recs)))

(defn status [config-path]
  (let [db (sqlite-db (str config-path "vcljs.sqlite"))]
    (diff-print (added-watch-status (j/query db
                                             [(str "select * from nodes"
                                                   " where status_id = 0")])
                                    db))))