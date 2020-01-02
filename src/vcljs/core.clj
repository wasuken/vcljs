(ns vcljs.core
  (:require [clj-sub-command.core :refer [parse-cmds]]
            [vcljs.cmds.init :refer :all]
            [vcljs.cmds.status :refer :all]
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
        current-path (str (-> (java.io.File. "") .getAbsolutePath) "/")]
    (case cmd
      :init (if (.exists (clojure.java.io/file (str (read-config) ".vcljs/")))
              (println "exists config dir.")
              (init current-path))
      :add (if-not (.exists (clojure.java.io/file (read-config)))
             (println "config dir not found.")
             (add (read-config) (:arguments parsed)))
      :status (if-not (.exists (clojure.java.io/file (read-config)))
                (println "config dir not found.")
                (status (read-config)))
      ;; :add-cancel (add current-path)
      )))
