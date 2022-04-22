(ns walterl.clieve-test
  (:require [clojure.test :refer [deftest is testing]]
            [walterl.clieve :as clieve]))

(deftest transpile-comment-test
  (testing "transpiling comments"
    (testing "single line"
      (is (= "# This is a comment\n"
             (clieve/transpile '(comment_ "This is a comment")))))
    (testing "multiple lines in a single arg"
      (is (= "# Line 1\n# Line 2\n"
             (clieve/transpile '(comment_ "Line 1\nLine 2")))))
    (testing "multiple lines in separate args"
      (is (= "# Line 1\n# Line 2\n"
             (clieve/transpile '(comment_ "Line 1" "Line 2")))))
    (testing "multiple args with multiple lines each"
      (is (= "# 1a\n# 1b\n# 2a\n# 2b\n# 3\n"
             (clieve/transpile '(comment_ "1a\n1b" "2a\n2b" "3")))))
    (testing "with blank lines"
      (is (= "# Line 1\n#\n# Line 2\n"
             (clieve/transpile '(comment_ "Line 1\n\nLine 2")))))))

(deftest transpile-simple-commands-test
  (testing "simple terminal actions"
    (testing "discard"
      (is (= "discard;"
             (clieve/transpile '(discard)))))
    (testing "keep"
      (is (= "keep;"
             (clieve/transpile '(keep)))))
    (testing "stop"
      (is (= "stop;"
             (clieve/transpile '(stop)))))))

(deftest transpile-require-command-test
  (testing "single require command"
    (testing "with single extension"
      (is (= "require \"fileinto\";\n"
             (clieve/transpile '(require "fileinto")))))
    (testing "with multiple extensions"
      (is (= "require [\"a\", \"b\"];\n"
             (clieve/transpile '(require "a" "b"))))))

  (testing "multiple require commands"
    (testing "with single extension each"
      (is (= "require \"a\";\n\nrequire \"b\";\n"
             (clieve/transpile '(do (require "a") (require "b"))))))
    (testing "with multiple extensions each"
      (is (= "require [\"a\", \"aa\"];\n\nrequire [\"b\", \"bb\", \"bbb\"];\n"
             (clieve/transpile '(do (require "a" "aa") (require "b" "bb" "bbb"))))))
    (testing "one with a single extension, another with multiple"
      (is (= "require \"a\";\n\nrequire [\"b\", \"bb\"];\n"
             (clieve/transpile '(do (require "a") (require "b" "bb"))))))))

(deftest transpile-fileinto-command-test
  (testing "fileinto"
    (is (= "fileinto \"Junk\";"
           (clieve/transpile '(fileinto "Junk"))))))

(deftest transpile-addflag-command-test
  (testing "addflag"
    (is (= "addflag \"\\\\Seen\";"
           (clieve/transpile '(addflag :seen))))))

(deftest transpile-if-command-test
  (testing "if command"
    (testing "simple"
      (testing "with single command in body"
        (is (= "if false {\nstop;\n}\n"
               (clieve/transpile '(if (raw false) (stop))))))
      (testing "with multiple commands in body"
        (is (= "if true {\ndiscard;\nstop;\n}\n"
               (clieve/transpile
                 '(if (raw true)
                    (do
                      (discard)
                      (stop))))))))))
