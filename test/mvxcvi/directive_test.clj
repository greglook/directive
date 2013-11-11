(ns mvxcvi.directive-test
  (:require [clojure.test :refer :all]
            [mvxcvi.directive :refer [command execute]]))


(deftest misusage
  (is (thrown? IllegalArgumentException
               (eval '(mvxcvi.directive/command
                        "foo" "desc" :not-valid)) ))
  (is (thrown? IllegalArgumentException
               (eval '(mvxcvi.directive/command
                        "foo" "desc"
                        (init identity)
                        (init [opts] nil)))))
  (is (thrown? IllegalArgumentException
               (eval '(mvxcvi.directive/command
                        "foo" "desc"
                        (action vector)
                        (action [opts args] nil)))))
  (is (thrown? IllegalArgumentException
               (eval '(mvxcvi.directive/command
                        "foo" "desc"
                        (action vector)
                        (command "bar" "desc"))))))


(def test-commands
  (command "test [global opts] <command> [command args]"
    "Test command-line tool description."

    ["-v" "--verbose" "Show extra debugging messages."
     :flag true :default false]
    ["-h" "--help" "Show usage information."
     :flag true :default false]

    (init identity)

    (command "foo <arg>"
      "Foo command description."

      ["--pretty" "Formats the info over multiple lines for easier viewing."
       :flag true :default false]

      (init [opts] (assoc opts :foo true))

      (command "bar"
        "Bar desc."

        (action [opts args] [:test-foo-bar opts args])))

    (command "baz [opts]"
      "Baz command description."

      (action vector))))


(deftest check-command-properties
  (is (= "test" (:name test-commands)))
  (is (= "[global opts] <command> [command args]" (:usage test-commands)))
  (is (= "Test command-line tool description." (:desc test-commands)))
  (is (= 2 (count (:specs test-commands))))
  (is (every? vector? (:specs test-commands)))
  (is (not (nil? (:init test-commands))))
  (is (= 2 (count (:commands test-commands)))))


(deftest command-execution
  (let [foo-help (with-out-str (execute test-commands ["help" "foo"]))
        usage (first (clojure.string/split foo-help #"\n"))]
    (is (= "Usage: test foo <arg>" usage)))
  (let [[kw opts args] (execute test-commands ["foo" "--pretty" "bar" "arg1" "--opt2" "arg3"])]
    (is (= :test-foo-bar kw))
    (is (:pretty opts))
    (is (not (:verbose opts)))
    (is (= ["arg1" "--opt2" "arg3"] args))))
