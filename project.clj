(defproject com.nedap.staffing-solutions/utils.modular "0.3.1"
  :description "Utilities for creating modular Clojure systems."

  :url "https://github.com/nedap/utils.modular"

  :repositories {"releases" {:url      "https://nedap.jfrog.io/nedap/staffing-solutions/"
                             :username :env/artifactory_user
                             :password :env/artifactory_pass}}

  :signing { :gpg-key "releases-staffingsolutions@nedap.com"}

  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[com.nedap.staffing-solutions/utils.spec "0.6.1"]
                 [org.clojure/clojure "1.10.0"]]

  :profiles {:dev {:plugins [[lein-cloverage "1.0.13"]]}})
