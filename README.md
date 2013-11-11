Directive
=========

A simple DSL for building command-line interfaces with trees of subcommands.
This library wraps the `org.clojure/tools.cli` library for option parsing.

## Usage

The `command` macro lets you declare a tree of subcommands, each of which can
have its own options, arguments, and initialization function. Leaf commands
specify an _action_ to take when invoked.

```clojure
(require '[mvxcvi.directive :refer [command execute]])

(def commands
  (command "my-tool [global opts] <command> [command args]"
    "Command-line tool for my awesome project."

    ["--config" "Set path to tool configuration."
     :default config/default-path]
    ["-v" "--verbose" "Show extra debugging messages."
     :flag true :default false]
    ["-h" "--help" "Show usage information."
     :flag true :default false]

    (init config/initialize)

    (command "config <type>"
      "Show configuration information."

      (command "dump"
        "Prints out a raw version of the configuration map."

        ["--pretty" "Formats the info over multiple lines for easier viewing."
         :flag true :default false]

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
