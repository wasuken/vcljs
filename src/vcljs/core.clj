(ns vcljs.core
  (:require [clj-sub-command.core :refer [parse-cmds]]
            [vcljs.cmds.init :refer :all]
            [vcljs.cmds.status :refer :all]
            [vcljs.cmds.commit :refer :all]
            [vcljs.util :refer :all]
            [vcljs.cmds.add :refer :all])
  (:gen-class))

(def options
  [["-h" "--help"]])

(def commands
  [["init" "initalize set config Dir in current directory."]
   ["add" "add filepath."]
   ["add-cancel" "cancel added filepath."]
   ["status" "added and commit status."]
   ["commit" "commit files."]
   ["commit-delete" "delete commit."]
   ["reset" "reset modified files to latest commit."]
   ["move-commit" "move other commit."]
   ["c-diff" "each commits diff."]
   ["a-diff" "each commits(one of witch is latest) diff."]])

(defn -main [& args]
  (let [parsed (parse-cmds args options commands)
        cmd (:command parsed)
        ags (:arguments parsed)
        current-path (str (-> (java.io.File. "") .getAbsolutePath) "/")]
    (case cmd
      :init (cond (.exists (clojure.java.io/file (str (read-config) ".vcljs/")))
                  (println "exists config dir.")

                  (and (not (nil? (first ags)))
                       (not (.exists (clojure.java.io/file (first ags)))))
                  (println "not exists dir")

                  (.exists (clojure.java.io/file (str (first ags) ".vcljs/")))
                  (println "exists config dir.")

                  (and (not (nil? (first ags)))
                       (.exists (clojure.java.io/file (first ags))))
                  (init (str (.getAbsolutePath (clojure.java.io/file (first ags))) "/"))

                  :else
                  (init (str (.getAbsolutePath (clojure.java.io/file "")) "/")))
      :add (if-not (.exists (clojure.java.io/file (read-config)))
             (println "config dir not found.")
             (add (read-config) ags))
      :status (if-not (.exists (clojure.java.io/file (read-config)))
                (println "config dir not found.")
                (status (read-config)))
      :commit (if-not (or (.exists (clojure.java.io/file (read-config)))
                          (nil? (first ags)))
                (println "config dir not found or not msg.")
                (commit (read-config) (first ags)))
      ;; :add-cancel (add current-path)
      )))
