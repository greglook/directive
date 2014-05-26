(defproject mvxcvi/directive "0.4.2"
  :description "Clojure library to allow declarative command-line interface construction."
  :url "https://github.com/greglook/directive"
  :license {:name "Public Domain"
            :url "http://unlicense.org/"}

  :dependencies
  [[org.clojure/clojure "1.6.0"]
   [org.clojure/tools.cli "0.3.1"]]

  :profiles
  {:coverage
   {:plugins
    [[lein-cloverage "1.0.2"]]}})
