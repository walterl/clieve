(ns walterl.clieve-test
  (:require [clojure.test :refer [deftest is testing]]
            [walterl.clieve :as clieve]))

(deftest a-test
  (testing "require action"
    (is (= "require [\"fileinto\"];\n"
           (clieve/transpile '(require "fileinto"))))))
