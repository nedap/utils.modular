(defproject com.nedap.staffing-solutions/utils.modular "0.3.0"
  ;; Please keep the dependencies sorted a-z.
  :dependencies [[com.nedap.staffing-solutions/utils.spec "0.6.2"]
                 [org.clojure/clojure "1.10.0"]]

  :description "Utilities for creating modular Clojure systems."

  :url "http://github.com/nedap/utils.modular"

  :min-lein-version "2.0.0"

  :license {:name "EPL-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}

  :signing {:gpg-key "releases-staffingsolutions@nedap.com"}

  :repositories {"releases" {:url      "https://nedap.jfrog.io/nedap/staffing-solutions/"
                             :username :env/artifactory_user
                             :password :env/artifactory_pass}}

  :deploy-repositories [["releases" {:url "https://nedap.jfrog.io/nedap/staffing-solutions/"}]]

  :repository-auth {#"https://nedap.jfrog\.io/nedap/staffing-solutions/"
                    {:username :env/artifactory_user
                     :password :env/artifactory_pass}}

  :target-path "target/%s"

  :monkeypatch-clojure-test false

  :repl-options {:init-ns dev}

  :profiles {:dev {:dependencies [[cider/cider-nrepl "0.16.0" #_"formatting-stack needs it"]
                                  [com.clojure-goes-fast/clj-java-decompiler "0.2.1"]
                                  [criterium "0.4.4"]
                                  [formatting-stack "0.18.2"]
                                  [lambdaisland/deep-diff "0.0-29"]
                                  [org.clojure/test.check "0.10.0-alpha3"]
                                  [org.clojure/tools.namespace "0.3.0-alpha4"]
                                  [org.clojure/tools.reader "1.1.1" #_"formatting-stack needs it"]]
                   :plugins      [[lein-cloverage "1.0.13"]]
                   :source-paths ["dev" "test"]}})
