(ns mvxcvi.directive
  (:require
    [clojure.string :as str]
    [clojure.tools.cli :as cli]))


;; UTILITY FUNCTIONS

(defn fail
  "Indicates a failure has occurred."
  ([] false)
  ([msg]
   (binding [*out* *err*]
     (println msg))
   false))


(defn- usage-str
  [branch cmd summary]
  (let [{:keys [usage desc commands]} cmd]
    (str "Usage: " (str/join " " branch) " " usage "\n\n"
         desc
         (when summary
           (str "\n\n" summary))
         (when (seq commands)
           (str "\n\n Subcommand   Description\n"
                    " ----------   -----------\n"
                (->> commands
                     (map #(format " %10s   %s" (:name %) (:desc %)))
                     (str/join \newline)))))))


;; COMMAND DEFINITION

; Command nodes look like this:
#_
{:name "cmd"
 :usage "[global opts] <command> [command args]"
 :desc "Command-line tool for ..."
 :specs [["--some-option" "Path to foo" ...]]
 :init (fn [opts] ...)
 :action (fn [opts args] ...)
 :commands [{:name ...} ...]}


(defn- element-fn
  "Resolves an element as a function body or symbolic reference."
  [[element & body]]
  (when element
    (if (symbol? (first body))
      (first body)
      `(fn ~@body))))


(defn- make-command
  "Constructs a command node map from the given elements."
  [cmd-name usage desc specs init-element action-element command-elements]
  (->>
    {:name cmd-name
     :usage usage
     :desc desc
     :specs (vec specs)
     :init (element-fn init-element)
     :action (element-fn action-element)
     :commands (vec command-elements)}
    (remove (comp nil? second))
    (into {})))


(defmacro command
  "Macro to simplify building readable command trees.

  The result of this macro is a compile-time map value representing the declared
  tree of commands. This map can be used to interpret and take actions specified
  by a sequence of arguments using the `execute` function."
  [usage desc & more]
  (let [[cmd-name usage] (str/split usage #" " 2)
        [specs more]     (split-with vector? more)
        elements         (group-by first (filter list? more))
        init-elements    (elements 'init)
        action-elements  (elements 'action)
        command-elements (elements 'command)]
    (when-not (every? list? more)
      (throw (IllegalArgumentException.
               (str "Non-list elements in '" cmd-name "' command definition: "
                    (remove list? more)))))
    (when (> (count init-elements) 1)
      (throw (IllegalArgumentException.
               (str "Multiple `init` elements in '" cmd-name
                    "' command definition: " init-elements))))
    (when (> (count action-elements) 1)
      (throw (IllegalArgumentException.
               (str "Multiple `action` elements in '" cmd-name
                    "' command definition: " action-elements))))
    (when (and command-elements action-elements)
      (throw (IllegalArgumentException.
               (str "Both `command` and `action` elements in '" cmd-name
                    "' command definition: " command-elements action-elements))))
    (make-command cmd-name usage desc specs
                  (first init-elements)
                  (first action-elements)
                  command-elements)))



;; COMMAND EXECUTION

(defn- parse-command-args
  [opts specs args]
  (if-not (empty? specs)
    (update-in
      (cli/parse-opts args specs :in-order true)
      [:options] (partial merge opts))
    {:options opts
     :arguments args}))


(defn- execute-action
  [usage action opts args]
  (cond
    (:help opts)
    (do (println usage) true)

    action
    (action opts args)

    :else
    (fail
      (str
        (when (seq args)
          (println "Unrecognized arguments:" (str/join " " args) "\n"))
        usage))))


(defn execute
  "Parses a sequence of arguments using a command map. The action function
  associated with a given leaf command will be called with a merged options map
  and any remaining arguments. Returns true on a successful run, false on
  failure, or whatever the result of the command action is."
  ([cmd args]
   (execute cmd {} args))

  ([cmd opts args]
   (execute cmd [] opts args))

  ([cmd branch opts args]
   (let [{:keys [options arguments summary errors]}
         (parse-command-args opts (:specs cmd) args)]
     (cond
       errors
       (fail (str/join \newline errors))

       (and (empty? branch) (= "help" (first args)))
       (recur cmd branch
              (assoc options :help true)
              (next args))

       :else
       (let [branch (conj branch (:name cmd))
             usage (usage-str branch cmd summary)
             opts ((or (:init cmd) identity) opts)]
         (if-let [subcommand (first (filter #(= (first args) (:name %)) (:commands cmd)))]
           (recur subcommand branch opts (next args))
           (execute-action usage (:action cmd) opts args)))))))
