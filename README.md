Directive
=========

[![Build Status](https://travis-ci.org/greglook/directive.svg?branch=master)](https://travis-ci.org/greglook/directive)
[![Coverage Status](https://coveralls.io/repos/greglook/directive/badge.png?branch=master)](https://coveralls.io/r/greglook/directive?branch=master)

A simple DSL for building command-line interfaces with trees of subcommands.
This library wraps the `org.clojure/tools.cli` library for option parsing.

## Installation

Library releases are [published on Clojars](https://clojars.org/mvxcvi/directive).

To use this version with Leiningen, add the following dependency to your project
definition:

```clojure
[mvxcvi/directive "0.4.2"]
```

## Usage

The `command` macro lets you declare a tree of subcommands, each of which can
have its own options, arguments, and initialization function. Leaf commands
specify an _action_ to take when invoked.

```clojure
(require '[mvxcvi.directive :refer [command execute]])

(def commands
  ; Commands start with the command name, followed by usage information.
  (command "my-tool [global opts] <command> [command args]"
    ; Second is a longer description string.
    "Command-line tool for my awesome project."

    ; Next are option specs, passed to clojure.tools.cli.
    ["--config" "Set path to tool configuration."
     :default my-config/default-path]
    ["-v" "--verbose" "Show extra debugging messages." :default false]
    ["-h" "--help" "Show usage information."]
    ; NOTE: don't use `:default false` for help flags, because it will break
    ; the implicit 'cmd help <args>' handler.

    ; Functions may be given as symbolic references like this or defined inline
    ; as below.
    (init my-config/initialize)

    ; Subcommands are just nested `command` nodes.
    (command "config <type>"
      "Show configuration information."

      (command "dump"
        "Prints out a raw version of the configuration map."

        [nil "--pretty" "Formats the info over multiple lines for easier viewing."]

        ; Leaf commands have actions, functions which run on the option map and
        ; any non-option arguments.
        (action [opts args]
          (if (:pretty opts)
            (pprint opts args)
            (prn opts args)))))

    (command "foo <action> [args]"
      "Low-level commands dealing with foos."

      (init [opts]
        (assoc opts :foo true))

      (action do-foo-action))))

(defn -main [& args]
  (execute commands args))
```

Initialization functions are given the current option map and must return an
updated version of the map in addition to causing any necessary side-effects.
Actions take the option map and a vector of the remaining arguments. Either may
be specified as a symbol or as an inline function body.

## License

This is free and unencumbered software released into the public domain.
See the UNLICENSE file for more information.
