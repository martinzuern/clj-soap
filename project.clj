(defproject io.xapix/clj-soap "1.0.0"
  :description "SOAP Client using Apache Axis2."
  :url "https://gitlab.com/xapix/engineering/clj-soap"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.logging "0.4.0"]
                 [org.clojure/data.xml "0.0.8"]
                 [clj-time "0.14.2"]
                 [org.apache.axis2/axis2-adb "1.7.6"]
                 [org.apache.axis2/axis2-transport-http "1.7.6"]
                 [org.apache.axis2/axis2-transport-local "1.7.6"]
                 [org.apache.axis2/axis2-jaxws "1.7.6"]]
  :source-paths ["src" "test"]
  )

