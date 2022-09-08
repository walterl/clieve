(ns walterl.clieve.render-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [walterl.clieve :as clieve]))

(deftest render-comment-test
  (testing "comments"
    (testing "single line"
      (is (= "# This is a comment\n"
             (clieve/render [:comment "This is a comment"]))))
    (testing "multiple lines in a single arg"
      (is (= "# Line 1\n# Line 2\n"
             (clieve/render [:comment "Line 1\nLine 2"]))))
    (testing "multiple lines in separate args"
      (is (= "# Line 1\n# Line 2\n"
             (clieve/render [:comment "Line 1" "Line 2"]))))
    (testing "multiple args with multiple lines each"
      (is (= "# 1a\n# 1b\n# 2a\n# 2b\n# 3\n"
             (clieve/render [:comment "1a\n1b" "2a\n2b" "3"]))))
    (testing "with blank lines"
      (is (= "# Line 1\n#\n# Line 2\n"
             (clieve/render [:comment "Line 1\n\nLine 2"]))))))

(deftest render-simple-commands-test
  (testing "simple terminal actions"
    (testing "discard"
      (is (= "discard;\n"
             (clieve/render [:discard]))))
    (testing "keep"
      (is (= "keep;\n"
             (clieve/render [:keep]))))
    (testing "stop"
      (is (= "stop;\n"
             (clieve/render [:stop]))))))

(deftest render-require-command-test
  (testing "single require command"
    (testing "with single extension"
      (is (= "require \"fileinto\";\n"
             (clieve/render [:require "fileinto"]))))
    (testing "with multiple extensions"
      (is (= "require [\"a\", \"b\"];\n"
             (clieve/render [:require ["a" "b"]])))))

  (testing "multiple require commands"
    (testing "with single extension each"
      (is (= "require \"a\";\nrequire \"b\";\n"
             (clieve/render [:do
                             [:require "a"]
                             [:require "b"]]))))
    (testing "with multiple extensions each"
      (is (= "require [\"a\", \"aa\"];\nrequire [\"b\", \"bb\", \"bbb\"];\n"
             (clieve/render [:do
                             [:require ["a" "aa"]]
                             [:require ["b" "bb" "bbb"]]]))))
    (testing "one with a single extension, another with multiple"
      (is (= "require \"a\";\nrequire [\"b\", \"bb\"];\n"
             (clieve/render [:do
                             [:require "a"]
                             [:require ["b" "bb"]]]))))))

(deftest render-fileinto-command-test
  (testing "fileinto"
    (is (= "fileinto \"Junk\";\n"
           (clieve/render [:fileinto "Junk"])))
    (testing "with :copy"
      (is (= "fileinto :copy \"Mailing lists\";\n"
             (clieve/render [:fileinto :copy "Mailing lists"]))))))

(deftest render-redirect-command-test
  (testing "redirect"
    (is (= "redirect \"friend@example.com\";\n"
           (clieve/render [:redirect "friend@example.com"])))
    (testing "with :copy"
      (is (= "redirect :copy \"backup@example.com\";\n"
             (clieve/render [:redirect :copy "backup@example.com"]))))))

(deftest render-addflag-command-test
  (testing "addflag"
    (is (= "addflag \"\\\\Seen\";\n"
           (clieve/render [:addflag :seen])))))

(deftest render-if-command-test
  (testing "if command"
    (testing "simple"
      (testing "with single command in body"
        (is (= "if false {\n    stop;\n}\n"
               (clieve/render [:if [:raw false] [:stop]]))))
      (testing "with multiple commands in body"
        (is (= "if true {\n    discard;\n    stop;\n}\n"
               (clieve/render
                 [:if [:raw true]
                  [:do
                   [:discard]
                   [:stop]]]))))))
  (testing "if-else"
    (is (= "if true {\n    discard;\n} else {\n    stop;\n}\n"
           (clieve/render
             [:if [:raw true]
              [:discard]
              [:stop]]))))
  (testing "if-elsif"
    (is (= "if true {\n    discard;\n} elsif false {\n    stop;\n}\n"
           (clieve/render
             [:if
              [:raw true] [:discard]
              [:raw false] [:stop]]))))
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
           (clieve/render
             [:if
              [:raw 1] [:keep]
              [:raw 2] [:discard]
              [:raw 3] [:keep]
              [:stop]]))))
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
           (clieve/render
             [:if [:raw 1]
              [:if
               [:raw 21] [:if [:raw 31] [:do [:discard] [:stop]]]
               [:raw 22] [:keep]]
              [:discard]])))))

(deftest render-address-command-test
  (testing "address"
    (is (= "address :is :localpart \"to\" \"me\""
           (clieve/render [:address :is :localpart "to" "me"])))
    (is (= "address :matches :all \"from\" \"advertise*@gmail.com\""
           (clieve/render [:address :matches :all "from" "advertise*@gmail.com"])))))

(deftest render-allof-command-test
  (testing "allof"
    (is (= "allof (\n    true\n)"
           (clieve/render [:allof [:raw true]])))
    (is (= "allof (\n    true,\n    false\n)"
           (clieve/render [:allof [:raw true] [:raw false]]))))
  (testing "and alias"
    (is (= "allof (\n    true\n)"
           (clieve/render [:and [:raw true]])))
    (is (= "allof (\n    true,\n    false\n)"
           (clieve/render [:and [:raw true] [:raw false]])))))

(deftest render-anyof-command-test
  (testing "anyof"
    (is (= "anyof (\n    true\n)"
           (clieve/render [:anyof [:raw true]])))
    (is (= "anyof (\n    true,\n    false\n)"
           (clieve/render [:anyof [:raw true] [:raw false]]))))
  (testing "or alias"
    (is (= "anyof (\n    true\n)"
           (clieve/render [:or [:raw true]])))
    (is (= "anyof (\n    true,\n    false\n)"
           (clieve/render [:or [:raw true] [:raw false]])))))

(deftest render-header-command-test
  (testing "header"
    (testing "with single header"
      (testing "and single key"
        (is (= "header :is \"x-some-header\" \"Test key\""
               (clieve/render [:header :is "x-some-header" "Test key"]))))
      (testing "and multiple keys"
        (is (= "header :is \"x-some-header\" [\"this\", \"THAT\"]"
               (clieve/render [:header :is "x-some-header" ["this" "THAT"]])))))
    (testing "with multiple headers"
      (testing "and single key"
        (is (= "header :is [\"x-header1\", \"x-header2\"] \"Test key\""
               (clieve/render [:header :is ["x-header1" "x-header2"] "Test key"]))))
      (testing "and multiple keys"
        (is (= "header :is [\"x-header1\", \"x-header2\"] [\"this\", \"THAT\"]"
               (clieve/render [:header :is ["x-header1" "x-header2"] ["this" "THAT"]])))))))

(deftest render-not-command-test
  (testing "not"
    (is (= "not true"
           (clieve/render [:not [:raw true]])))
    (is (= "not address :is :domain \"from\" \"github.com\""
           (clieve/render [:not [:address :is :domain "from" "github.com"]])))))

(deftest shortcut-fileinto+stop-test
  (is (= "fileinto \"Some.Folder\";\nstop;\n"
         (clieve/render [:fileinto+stop "Some.Folder"]))))

(deftest shortcut-address-localpart-to-test
  (is (= "address :is :localpart \"to\" \"me\""
         (clieve/render [:address-localpart-to "me"]))))

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
             (clieve/render
               [:if [:header :is "x-spam-flag" "yes"]
                [:do
                 [:addflag :seen]
                 [:fileinto "Junk"]
                 [:stop]]]))))
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
             (clieve/render
               [:if [:allof
                     [:address :is :localpart "from" "root"]
                     [:header :matches "subject" "Cron <*"]]
                [:do
                 [:fileinto "Server.Cron"]
                 [:stop]]]))))
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
             (clieve/render
               [:if [:address :is :localpart "from" "fail2ban"]
                [:do
                 [:if [:header :matches "subject" "[Fail2Ban] noisy-filter"]
                  [:addflag :seen]]
                 [:fileinto "Server.Fail2ban"]
                 [:stop]]]))))))
