(ns walterl.clieve.render
  (:require [clojure.string :as str]
            [walterl.clieve.util :as u]))

(defmulti render-node
  "Renders `node`, dispatched on the first element"
  (fn [node] (first node)))

(defmethod render-node :default
  [node]
  (throw (ex-info "Unsupported node" {:node node})))

(defmethod render-node :comment
  [[_ & comments]]
  (->> comments
       (u/lines)
       (map u/comment-line)
       (str/join)))

(defmethod render-node :raw
  [[_ & [x]]]
  (str x))

(defmethod render-node :do
  [[_ & forms]]
  (str/join (map render-node forms)))

;;; Control commands

(defn- block
  [body]
  (format "{\n%s\n}\n" (u/indent (render-node body))))

(defn- add-cond-body-pair
  [s [cond_ body]]
  (if (some? body)
    (format "%s%s %s %s"
            (str/trim-newline s)
            (if (str/blank? s) "if" " elsif")
            (render-node cond_)
            (block body))
    (format "%s else %s"
            (str/trim-newline s)
            (block cond_))))

(defmethod render-node :if
  [[_ & parts]]
  (reduce add-cond-body-pair "" (partition-all 2 parts)))

(defmethod render-node :require
  [[_ & [exts]]]
  (format "require %s;\n" (u/string-list exts)))

(defn- simple-action
  [x]
  (str (name x) ";\n"))

(defmethod render-node :stop
  [[a]]
  (simple-action a))

;;; Action commands

(defn- copy-cmd
  [cmd [copy dest]]
  (if (nil? dest)
    (copy-cmd cmd [nil copy])
    (format "%s %s%s;\n"
            cmd
            (if copy ":copy " "")
            (u/quoted-str dest))))

(defmethod render-node :fileinto
  [[_ & args]]
  (copy-cmd "fileinto" args))

(defmethod render-node :redirect
  [[_ & args]]
  (copy-cmd "redirect" args))

(defmethod render-node :discard
  [[a]]
  (simple-action a))

(defmethod render-node :keep
  [[a]]
  (simple-action a))

(defmethod render-node :addflag
  [[_ & [flag]]]
  (format "addflag %s;\n" (u/flag-kw->str flag)))

;;; Test commands

(defmethod render-node :address
  [[_ comparitor address-part headers keys_]]
  (format "address %s %s %s %s"
          (u/tagged-arg comparitor)
          (u/tagged-arg address-part)
          (u/string-list headers)
          (u/string-list keys_)))

(defn- test-list
  [tests]
  (format "(\n%s\n)"
          (->> tests
               (map render-node)
               (str/join ",\n")
               (u/indent))))

(defmethod render-node :allof
  [[_ & tests]]
  (str "allof " (test-list tests)))

(defmethod render-node :and
  [[_ & args]]
  (render-node (into [:allof] args)))

(defmethod render-node :anyof
  [[_ & tests]]
  (str "anyof " (test-list tests)))

(defmethod render-node :or
  [[_ & args]]
  (render-node (into [:anyof] args)))

(defmethod render-node :header
  [[_ comparitor headers keys_]]
  (format "header %s %s %s" (u/tagged-arg (name comparitor)) (u/string-list headers) (u/string-list keys_)))

(defmethod render-node :not
  [[_ test]]
  (str "not " (render-node test)))

;;; Shortcut commands

(defmethod render-node :fileinto+stop
  ;; Expands to (do (fileinto x) (stop))
  [[_ & args]]
  (render-node [:do (into [:fileinto] args) [:stop]]))

(defmethod render-node :address-localpart-to
  ;; Expands to (address :is "localpart" "to" x)
  [[_ x]]
  (render-node [:address :is :localpart "to" x]))

;;; Public API

(defn render
  "Renders given node to Sieve source."
  [node]
  (render-node node))
