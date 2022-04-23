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

(defmethod node->str 'do
  [[_ & forms]]
  (str/join \newline (map node->str forms)))

;;; Control commands

(defn- block
  [body]
  (format "{\n%s\n}\n" (u/indent (node->str body))))

(defn- add-cond-body-pair
  [s [cond_ body]]
  (if (some? body)
    (format "%s%s %s %s"
            (str/trim-newline s)
            (if (str/blank? s) "if" " elsif")
            (node->str cond_)
            (block body))
    (format "%s else %s"
            (str/trim-newline s)
            (block cond_))))

(defmethod node->str 'if
  [[_ & parts]]
  (reduce add-cond-body-pair "" (partition-all 2 parts)))

(defmethod node->str 'require
  [[_ & [exts]]]
  (format "require %s;\n" (u/string-list exts)))

(defmethod node->str 'stop
  [[a]]
  (simple-action a))

;;; Action commands

(defn- copy-cmd
  [cmd [copy dest]]
  (if (nil? dest)
    (copy-cmd cmd [nil copy])
    (format "%s %s%s;"
            cmd
            (if copy ":copy " "")
            (u/quoted-str dest))))

(defmethod node->str 'fileinto
  [[_ & args]]
  (copy-cmd "fileinto" args))

(defmethod node->str 'redirect
  [[_ & args]]
  (copy-cmd "redirect" args))

(defmethod node->str 'discard
  [[a]]
  (simple-action a))

(defmethod node->str 'keep
  [[a]]
  (simple-action a))

(defmethod node->str 'addflag
  [[_ & [flag]]]
  (format "addflag %s;" (u/flag-kw->str flag)))

;;; Test commands

(defmethod node->str 'address
  [[_ comparitor address-part headers keys_]]
  (format "address %s %s %s %s"
          (u/tagged-arg comparitor)
          (u/tagged-arg address-part)
          (u/string-list headers)
          (u/string-list keys_)))

(defn- test-list
  [tests]
  (format "(\n%s\n)\n"
          (->> tests
               (map node->str)
               (str/join ",\n")
               (u/indent))))

(defmethod node->str 'allof
  [[_ & tests]]
  (str "allof " (test-list tests)))

(defmethod node->str 'allof
  [[_ & tests]]
  (str "allof " (test-list tests)))

(defmethod node->str 'header
  [[_ comparitor headers keys_]]
  (format "header %s %s %s" (u/tagged-arg (name comparitor)) (u/string-list headers) (u/string-list keys_)))

;;; Public API

(defn transpile
  "Transpiles Clieve source form to Sieve source."
  [src]
  (node->str src))
