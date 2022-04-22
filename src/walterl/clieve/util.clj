(ns walterl.clieve.util
  (:require [clojure.string :as str]))

(defn quoted-str
  "Returns `s` with escaped double quotes, and surrounded with double quotes."
  [s]
  (-> s
      (str/replace #"\"" "\\\"")
      (->> (format "\"%s\""))))

(defn quoted-strs
  "Returns quoted strings `ss`, separated with \", \"."
  [ss]
  (->> ss
       (map quoted-str)
       (str/join ", ")))

(defn flag-kw->str
  "Converts :seen to \"\\\\Seen\"."
  [flag]
  (-> flag
      (name)
      (str/capitalize)
      (->> (str "\\\\"))
      quoted-str))
