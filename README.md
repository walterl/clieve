# Clieve

Clojure-based DSL for [Sieve](http://sieve.info/) scripts.

## Usage

Render Sieve from an EDN file:

    $ clojure -X walterl.clieve/render-file :infile script.edn

Run the project's tests:

    $ clojure -T:build test

Run the project's CI pipeline and build a JAR:

    $ clojure -T:build ci

Install it locally (requires the `ci` task be run first):

    $ clojure -T:build install

## License

Copyright © 2022 Walter

Distributed under the [Eclipse Public License version 1.0](./LICENSE).
