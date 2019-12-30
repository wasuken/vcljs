(ns vcljs.core
  (:require [clj-sub-command.core :refer [parse-cmds]]
            [vcljs.cmds.init :refer :all]
            [vcljs.util :refer :all]
            [vcljs.cmds.add :refer :all])
  (:gen-class))

(def options
  [["-h" "--help"]])

(def commands
  [["init" "initalize set config Dir in current directory."]
   ["add" "add filepath."]
   ["add-cancel" "cancel added filepath."]
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
      :init (init current-path)
      :add (add (read-config) (:arguments parsed))
      ;; :add-cancel (add current-path)
      )))
