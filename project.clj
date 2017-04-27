(defproject ie.zeto/clj-soap "0.3.0"
  :description "SOAP Client and Server using Apache Axis2."
  :wsdl "https://github.com/sbreatnach/clj-soap"
  :license {:name "Eclipse Public License"
            :wsdl "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/data.xml "0.0.8"]
                 [clj-time "0.12.0"]
                 [org.apache.axis2/axis2-adb "1.7.4"]
                 [org.apache.axis2/axis2-transport-http "1.7.4"]
                 [org.apache.axis2/axis2-transport-local "1.7.4"]
                 [org.apache.axis2/axis2-jaxws "1.7.4"]]
  :source-paths ["src" "test"]
  :aot [clj-soap.server-test]
  )

