(ns vcljs.util)

(defn read-config
  ([] (read-config (str (.getAbsolutePath (clojure.java.io/file "")) "/")))
  ([config-dir-path] (cond (or (empty? config-dir-path) (= "/" config-dir-path))
                           (println "config file not found")
                           (.exists (clojure.java.io/file (str config-dir-path ".vcljs")))
                           (str (.getAbsolutePath (clojure.java.io/file (str config-dir-path ".vcljs")))
                                "/")
                           :else (read-config (str (.getParent (clojure.java.io/file config-dir-path)) "/")))))

(defn remove-dir-all [path]
  "danger"
  (when (and (not (nil? path)) (.exists (clojure.java.io/file path)))
    (doall (map #(clojure.java.io/delete-file %)
                (filter #(.isFile %)
                        (file-seq (clojure.java.io/file path)))))
    (doall (map #(clojure.java.io/delete-file %)
                (reverse (file-seq (clojure.java.io/file path)))))))

;;; original => https://gist.github.com/hozumi/1472865
(defn sha1-str [s]
  (->> (-> "sha1"
           java.security.MessageDigest/getInstance
           (.digest (.getBytes s)))
       (map #(.substring
              (Integer/toString
               (+ (bit-and % 0xff) 0x100) 16) 1))
       (apply str)))
