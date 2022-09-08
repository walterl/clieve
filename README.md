# Clieve

Render [Sieve](http://sieve.info/) scripts from a [Hiccup](https://github.com/weavejester/hiccup)-like [EDN representation](./example.edn).

## Usage

Render Sieve from an EDN file:

    $ clojure -X walterl.clieve/render-file :infile '"example.edn"'
    require ["copy", "fileinto", "imap4flags"];
    # Spam
    if anyof (
        allof (
            header :is "x-spam-flag" "yes",
            address :is :localpart "to" ["admin", "contact", "info", "sales"]
        ),
        header :contains "received" ["compromised@example.org", "spamtrap@example.org"],
        address :matches :all "from" "advertise*@gmail.com",
        address :is :all "from" "invitations@linkedin.com",
        header :matches "subject" "I'd like to add you to my professional network on LinkedIn"
    ) {
        # fileinto "Sievedebug.Spam"
        discard;
        stop;
    }
    if header :is "x-spam-flag" "yes" {
        addflag "\\Seen";
        fileinto "Junk";
        stop;
    } else {
        fileinto "Archive";
    }
    # Forwards
    if address :is :localpart "to" "fwdme" {
        redirect "destination@protonmail.com";
    }
    if address :is :localpart "to" "fwd-and-copy" {
        redirect :copy "destination@protonmail.com";
    }
    if address :is :localpart "from" "fail2ban" {
        if header :matches "subject" "[Fail2Ban] * started on *" {
            addflag "\\Seen";
        }
        fileinto "VPS.Fail2ban";
        stop;
    }
    if address :is :localpart "from" "logwatch" {
        fileinto "Logs";
        stop;
    }

Run the project's tests:

    $ clojure -T:build test

Run the project's CI pipeline and build a JAR:

    $ clojure -T:build ci

Install it locally (requires the `ci` task be run first):

    $ clojure -T:build install

## License

Copyright © 2022 Walter

Distributed under the [Eclipse Public License version 1.0](./LICENSE).
