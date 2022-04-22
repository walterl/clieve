(ns walterl.clieve)

(defmulti node->str
  "Coverts a Clieve node into a Sieve string."
  (fn [node] (first node)))

(defmethod node->str 'require
  [[_ ext]]
  (format "require [\"%s\"];\n" ext))

(defn transpile
  "Transpiles Clieve source form to Sieve source."
  [src]
  (node->str src))

(comment
  (def src '(require "fileinto"))
  (type (first src))
  (transpile '(require "fileinto"))
  ,)
