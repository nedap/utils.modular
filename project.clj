(defproject com.nedap.staffing-solutions/utils.modular "0.1.0"
  :description "Utilities for creating modular Clojure systems."
  :url ""
  :repositories {"releases"       {:url      "https://nedap.jfrog.io/nedap/staffing-solutions/"
                                   :username :env/artifactory_user
                                   :password :env/artifactory_pass}}
  :deploy-repositories [["releases" {:url "https://nedap.jfrog.io/nedap/staffing-solutions/"
                                     :sign-releases false}]]
  :repository-auth {#"https://nedap.jfrog\.io/nedap/staffing-solutions/"
                    {:username :env/artifactory_user
                     :password :env/artifactory_pass}}
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[com.nedap.staffing-solutions/utils.spec "0.1.1"]
                 [org.clojure/clojure "1.10.0"]])
