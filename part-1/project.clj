(defproject foreclojure "2.0.0-rc2"
  :description "4clojure - a website for learning Clojure"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [compojure "1.1.0"]
                 [hiccup "1.0.0"]
                 [clojail "1.0.6"]
                 [lib-noir "0.1.1"]
                 [org.jasypt/jasypt "1.7"]
                 [cheshire "4.0.0"]
                 [useful "0.8.3-alpha4"]
                 [amalloy/ring-gzip-middleware "0.1.2"]
                 [innuendo "0.1.7"]
                 [ring "1.1.1"]
                 [incanter/incanter-core "1.3.0"]
                 [incanter/incanter-charts "1.3.0"]
                 [commons-lang "2.6"]
                 [org.apache.commons/commons-email "1.2"]
                 [org.clojure/data.xml "0.0.5"]
                 [com.gfredericks/webscale "0.1.0"]
                 [org.clojure/test.check "0.9.0"]]
  :plugins [[lein-ring "0.7.1"]]
  :profiles {:dev {:dependencies [[midje "1.3.0" :exclusions [org.clojure/clojure]]]}}
  :checksum-deps true
  :jvm-opts ["-Djava.security.policy=java-security-policy-file"]
  :main foreclojure.core
  :ring {:handler foreclojure.core/app
         :init foreclojure.mongo/prepare-mongo}
  ;; :local-repo "local-m2"
  )
