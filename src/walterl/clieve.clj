(ns walterl.clieve
  (:require
    [clojure.edn :as edn]
    [walterl.clieve.render :as render]))

(defn render
  "Render Hiccup-like Sieve AST"
  [root]
  (render/render root))

(defn render-file
  "Render Hiccup-like Sieve AST from given EDN filename."
  [{:keys [infile]}]
  (-> infile
      (slurp)
      (edn/read-string)
      (render)
      (print)))
