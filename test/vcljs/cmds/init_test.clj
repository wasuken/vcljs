(ns vcljs.cmds.init-test
  (:require [vcljs.cmds.init :as sut]
            [vcljs.util :refer :all]
            [clojure.test :refer :all]))

(defn setup []
  (remove-dir-all (read-config))
  )

(defn cleanup []
  (remove-dir-all (read-config))
  )

(deftest init-current-test
  (testing "subcommand init test"
    (setup)
    (let [cur-dir-path (str (-> (java.io.File. "") .getAbsolutePath) "/")]
      (sut/init cur-dir-path)
      (is (.exists (java.io.File. (str cur-dir-path ".vcljs"))))
      (is (.exists (java.io.File. (str cur-dir-path ".vcljs/config.json"))))
      (is (.exists (java.io.File. (str cur-dir-path ".vcljs/vcljs.sqlite")))))
    (cleanup)))

;; (deftest when-config-dir-already-exists-test
;;   (testing "subcommand init test(when config dir already exists test)"
;;     (setup)
;;     (sut/init (-> (java.io.File. "") .getAbsolutePath))
;;     (sut/init (-> (java.io.File. "") .getAbsolutePath))
;;     (is true true)
;;     (cleanup)))
