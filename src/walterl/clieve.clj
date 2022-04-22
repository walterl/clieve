(ns walterl.clieve
  (:require [clojure.string :as str]
            [walterl.clieve.util :as u]))

(defn simple-action
  [x]
  (str x ";"))

(defmulti node->str
  "Coverts a Clieve node into a Sieve string."
  (fn [node] (first node)))

(defmethod node->str :default
  [node]
  (throw (ex-info "Unsupported node" {:node node})))

(defmethod node->str 'comment_
  [[_ & comments]]
  (->> comments
       (u/lines)
       (map u/comment-line)
       (str/join)))

(defmethod node->str 'raw
  [[_ & [x]]]
  (str x))

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

(defn block
  [body]
  (format "{\n%s\n}\n" (node->str body)))

(defmethod node->str 'if
  [[_ cnd then else]]
  (cond-> (format "if %s %s" (node->str cnd) (block then))
    (some? else) (-> (str/trim-newline)
                     (str " else " (block else)))))

(defmethod node->str 'require
  [[_ & exts]]
  (if (= 1 (count exts))
    (format "require %s;\n" (u/quoted-str (first exts)))
    (format "require [%s];\n" (u/quoted-strs exts))))

(defmethod node->str 'fileinto
  [[_ & [dest]]]
  (format "fileinto %s;" (u/quoted-str dest)))

(defmethod node->str 'addflag
  [[_ & [flag]]]
  (format "addflag %s;" (u/flag-kw->str flag)))

(defn transpile
  "Transpiles Clieve source form to Sieve source."
  [src]
  (node->str src))
