(ns walterl.clieve
  (:require [clojure.string :as str]
            [walterl.clieve.util :as u]))

(defn simple-action
  [x]
  (str x ";"))

(defmulti node->str
  "Coverts a Clieve node into a Sieve string."
  (fn [node] (first node)))

(defmethod node->str 'discard
  [[a]]
  (simple-action a))

(defmethod node->str 'keep
  [[a]]
  (simple-action a))

(defmethod node->str 'stop
  [[a]]
  (simple-action a))

(defmethod node->str 'do
  [[_ & forms]]
  (str/join \newline (map node->str forms)))

(defmethod node->str 'require
  [[_ & exts]]
  (if (= 1 (count exts))
    (format "require %s;\n" (u/quoted-str (first exts)))
    (format "require [%s];\n" (u/quoted-strs exts))))

(defn transpile
  "Transpiles Clieve source form to Sieve source."
  [src]
  (node->str src))
