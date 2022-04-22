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

(defn quoted
  "Returns a list of quoted string if `s` is a sequence, otherwise a quoted string of `s`."
  [s]
  (if (sequential? s)
    (format "[%s]" (quoted-strs s))
    (quoted-str s)))

(defn flag-kw->str
  "Converts :seen to \"\\\\Seen\"."
  [flag]
  (-> flag
      (name)
      (str/capitalize)
      (->> (str "\\\\"))
      quoted-str))

(defn lines
  "Turns sequence of \n-separated strings into a single sequence of strings."
  [lines]
  (->> lines
      (str/join "\n")
      (str/split-lines)))

(defn comment-line
  "Formats s as a comment line: prefixed with #, ending in a newline."
  [s]
  (str "#"
       (when (not-empty s) (str " "))
       s
       "\n"))

(def ^:dynamic *indent* "    ")

(defn indent
  "Indents all lines in `s` one level (`*indent*`)."
  [s]
  (->> s
       (str/split-lines)
       (map #(str *indent* %))
       (str/join \newline)))
