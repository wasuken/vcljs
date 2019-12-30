(ns vcljs.db)

(defn sqlite-db [path]
  {:connection-uri (str "jdbc:sqlite:" path)})
