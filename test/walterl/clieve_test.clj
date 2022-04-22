(ns walterl.clieve-test
  (:require [clojure.test :refer [deftest is testing]]
            [walterl.clieve :as clieve]))

(deftest a-test
  (testing "single require action"
    (testing "with single extension"
      (is (= "require \"fileinto\";\n"
             (clieve/transpile '(require "fileinto")))))
    (testing "with multiple extensions"
      (is (= "require [\"a\", \"b\"];\n"
             (clieve/transpile '(require "a" "b"))))))
  (testing "multiple require actions"
    (testing "with single extension each"
      (is (= "require \"a\";\n\nrequire \"b\";\n"
             (clieve/transpile '(do (require "a") (require "b"))))))
    (testing "with multiple extensions each"
      (is (= "require [\"a\", \"aa\"];\n\nrequire [\"b\", \"bb\", \"bbb\"];\n"
             (clieve/transpile '(do (require "a" "aa") (require "b" "bb" "bbb"))))))
    (testing "one with a single extension, another with multiple"
      (is (= "require \"a\";\n\nrequire [\"b\", \"bb\"];\n"
             (clieve/transpile '(do (require "a") (require "b" "bb"))))))))