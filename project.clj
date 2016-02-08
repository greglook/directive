(defproject mvxcvi/directive "0.5.0-SNAPSHOT"
  :description "Clojure library to allow declarative command-line interface construction."
  :url "https://github.com/greglook/directive"
  :license {:name "Public Domain"
            :url "http://unlicense.org/"}

  :deploy-branches ["master"]

  :dependencies
  [[org.clojure/clojure "1.8.0"]
   [org.clojure/tools.cli "0.3.3"]]

  :profiles
  {:coverage
   {:plugins
    [[lein-cloverage "1.0.2"]]}})
