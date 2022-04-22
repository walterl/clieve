# Clieve

Clojure-based DSL for [Sieve](http://sieve.info/) scripts.

## Usage

Invoke a library API function from the command-line:

    $ clojure -X walterl.clieve/transpile :infile script.clv

Run the project's tests:

    $ clojure -T:build test

Run the project's CI pipeline and build a JAR:

    $ clojure -T:build ci

Install it locally (requires the `ci` task be run first):

    $ clojure -T:build install

## License

Copyright © 2022 Walter

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

[Full license](./LICENSE.md)
