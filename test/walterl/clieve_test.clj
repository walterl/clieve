(ns walterl.clieve-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
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
      (is (= "discard;\n"
             (clieve/transpile '(discard)))))
    (testing "keep"
      (is (= "keep;\n"
             (clieve/transpile '(keep)))))
    (testing "stop"
      (is (= "stop;\n"
             (clieve/transpile '(stop)))))))

(deftest transpile-require-command-test
  (testing "single require command"
    (testing "with single extension"
      (is (= "require \"fileinto\";\n"
             (clieve/transpile '(require "fileinto")))))
    (testing "with multiple extensions"
      (is (= "require [\"a\", \"b\"];\n"
             (clieve/transpile '(require ["a" "b"]))))))

  (testing "multiple require commands"
    (testing "with single extension each"
      (is (= "require \"a\";\nrequire \"b\";\n"
             (clieve/transpile '(do (require "a") (require "b"))))))
    (testing "with multiple extensions each"
      (is (= "require [\"a\", \"aa\"];\nrequire [\"b\", \"bb\", \"bbb\"];\n"
             (clieve/transpile '(do (require ["a" "aa"]) (require ["b" "bb" "bbb"]))))))
    (testing "one with a single extension, another with multiple"
      (is (= "require \"a\";\nrequire [\"b\", \"bb\"];\n"
             (clieve/transpile '(do (require "a") (require ["b" "bb"]))))))))

(deftest transpile-fileinto-command-test
  (testing "fileinto"
    (is (= "fileinto \"Junk\";\n"
           (clieve/transpile '(fileinto "Junk"))))
    (testing "with :copy"
      (is (= "fileinto :copy \"Mailing lists\";\n"
             (clieve/transpile '(fileinto :copy "Mailing lists")))))))

(deftest transpile-redirect-command-test
  (testing "redirect"
    (is (= "redirect \"friend@example.com\";\n"
           (clieve/transpile '(redirect "friend@example.com"))))
    (testing "with :copy"
      (is (= "redirect :copy \"backup@example.com\";\n"
             (clieve/transpile '(redirect :copy "backup@example.com")))))))

(deftest transpile-addflag-command-test
  (testing "addflag"
    (is (= "addflag \"\\\\Seen\";\n"
           (clieve/transpile '(addflag :seen))))))

(deftest transpile-if-command-test
  (testing "if command"
    (testing "simple"
      (testing "with single command in body"
        (is (= "if false {\n    stop;\n}\n"
               (clieve/transpile '(if (raw false) (stop))))))
      (testing "with multiple commands in body"
        (is (= "if true {\n    discard;\n    stop;\n}\n"
               (clieve/transpile
                 '(if (raw true)
                    (do
                      (discard)
                      (stop)))))))))
  (testing "if-else"
    (is (= "if true {\n    discard;\n} else {\n    stop;\n}\n"
           (clieve/transpile
             '(if (raw true)
                (discard)
                (stop))))))
  (testing "if-elsif"
    (is (= "if true {\n    discard;\n} elsif false {\n    stop;\n}\n"
           (clieve/transpile
             '(if
                (raw true) (discard)
                (raw false) (stop))))))
  (testing "if-elsif-elsif-else"
    (is (= (str/join "\n"
                     ["if 1 {"
                      "    keep;"
                      "} elsif 2 {"
                      "    discard;"
                      "} elsif 3 {"
                      "    keep;"
                      "} else {"
                      "    stop;"
                      "}"
                      ""])
           (clieve/transpile
             '(if
                (raw 1) (keep)
                (raw 2) (discard)
                (raw 3) (keep)
                (stop))))))
  (testing "nested if-elsif-else-blocks"
    (is (= (str/join "\n"
                     ["if 1 {"
                      "    if 21 {"
                      "        if 31 {"
                      "            discard;" ;; Multiple actions
                      "            stop;"
                      "        }"
                      "    } elsif 22 {"
                      "        keep;"
                      "    }"
                      "} else {"
                      "    discard;"
                      "}"
                      ""])
           (clieve/transpile
             '(if (raw 1)
                (if
                  (raw 21) (if (raw 31) (do (discard) (stop)))
                  (raw 22) (keep))
                (discard)))))))

(deftest transpile-address-command-test
  (testing "address"
    (is (= "address :is :localpart \"to\" \"me\""
           (clieve/transpile '(address :is :localpart "to" "me"))))
    (is (= "address :matches :all \"from\" \"advertise*@gmail.com\""
           (clieve/transpile '(address :matches :all "from" "advertise*@gmail.com"))))))

(deftest transpile-allof-command-test
  (testing "allof"
    (is (= "allof (\n    true\n)"
           (clieve/transpile '(allof (raw true)))))
    (is (= "allof (\n    true,\n    false\n)"
           (clieve/transpile '(allof (raw true) (raw false))))))
  (testing "and alias"
    (is (= "allof (\n    true\n)"
           (clieve/transpile '(and (raw true)))))
    (is (= "allof (\n    true,\n    false\n)"
           (clieve/transpile '(and (raw true) (raw false)))))))

(deftest transpile-anyof-command-test
  (testing "anyof"
    (is (= "anyof (\n    true\n)"
           (clieve/transpile '(anyof (raw true)))))
    (is (= "anyof (\n    true,\n    false\n)"
           (clieve/transpile '(anyof (raw true) (raw false))))))
  (testing "or alias"
    (is (= "anyof (\n    true\n)"
           (clieve/transpile '(or (raw true)))))
    (is (= "anyof (\n    true,\n    false\n)"
           (clieve/transpile '(or (raw true) (raw false)))))))

(deftest transpile-header-command-test
  (testing "header"
    (testing "with single header"
      (testing "and single key"
        (is (= "header :is \"x-some-header\" \"Test key\""
               (clieve/transpile '(header :is "x-some-header" "Test key")))))
      (testing "and multiple keys"
        (is (= "header :is \"x-some-header\" [\"this\", \"THAT\"]"
               (clieve/transpile '(header :is "x-some-header" ["this" "THAT"]))))))
    (testing "with multiple headers"
      (testing "and single key"
        (is (= "header :is [\"x-header1\", \"x-header2\"] \"Test key\""
               (clieve/transpile '(header :is ["x-header1" "x-header2"] "Test key")))))
      (testing "and multiple keys"
        (is (= "header :is [\"x-header1\", \"x-header2\"] [\"this\", \"THAT\"]"
               (clieve/transpile '(header :is ["x-header1" "x-header2"] ["this" "THAT"]))))))))

(deftest transpile-not-command-test
  (testing "not"
    (is (= "not true"
           (clieve/transpile '(not (raw true)))))
    (is (= "not address :is :domain \"from\" \"github.com\""
           (clieve/transpile '(not (address :is :domain "from" "github.com")))))))

(deftest shortcut-fileinto+stop-test
  (is (= "fileinto \"Some.Folder\";\nstop;\n"
         (clieve/transpile '(fileinto+stop "Some.Folder")))))

(deftest shortcut-address-localpart-to-test
  (is (= "address :is :localpart \"to\" \"me\""
         (clieve/transpile '(address-localpart-to "me")))))

(deftest integration-test
  (testing "Real world example:"
    (testing "File spam in \"Junk\" folder"
      (is (= (str/join "\n"
                       ["if header :is \"x-spam-flag\" \"yes\" {"
                        "    addflag \"\\\\Seen\";"
                        "    fileinto \"Junk\";"
                        "    stop;"
                        "}"
                        ""])
             (clieve/transpile
               '(if (header :is "x-spam-flag" "yes")
                  (do
                    (addflag :seen)
                    (fileinto "Junk")
                    (stop)))))))
    (testing "File cron mail under server stuff"
      (is (= (str/join "\n"
                       ["if allof ("
                        "    address :is :localpart \"from\" \"root\","
                        "    header :matches \"subject\" \"Cron <*\""
                        ") {"
                        "    fileinto \"Server.Cron\";"
                        "    stop;"
                        "}"
                        ""])
             (clieve/transpile
               '(if (allof
                      (address :is :localpart "from" "root")
                      (header :matches "subject" "Cron <*"))
                  (do
                    (fileinto "Server.Cron")
                    (stop)))))))
    (testing "File away fail2ban, marking some of them as seen"
      (is (= (str/join "\n"
                       ["if address :is :localpart \"from\" \"fail2ban\" {"
                        "    if header :matches \"subject\" \"[Fail2Ban] noisy-filter\" {"
                        "        addflag \"\\\\Seen\";"
                        "    }"
                        "    fileinto \"Server.Fail2ban\";"
                        "    stop;"
                        "}"
                        ""])
             (clieve/transpile
               '(if (address :is :localpart "from" "fail2ban")
                  (do
                    (if (header :matches "subject" "[Fail2Ban] noisy-filter")
                      (addflag :seen))
                    (fileinto "Server.Fail2ban")
                    (stop)))))))))
