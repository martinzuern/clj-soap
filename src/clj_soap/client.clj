(ns clj-soap.client
  (:require [clojure.tools.logging :as log]
            [clojure.data.xml :as xml]
            [clj-time.core :as t]
            [clj-time.format :as f])
  (:import [org.apache.axis2.client ServiceClient
                                    Options]
           [org.apache.axis2.addressing EndpointReference]
           [org.apache.axis2.transport.http HTTPConstants
                                            HTTPAuthenticator]
           [org.apache.axis2.transport.http.impl.httpclient3 HttpTransportPropertiesImpl$Authenticator]
           [org.apache.axiom.om OMAbstractFactory]
           [org.apache.axis2.description OutOnlyAxisOperation
                                         AxisService]
           [javax.xml.namespace QName]
           [java.net URL
                     Authenticator
                     PasswordAuthentication]
           [java.util ArrayList]
           [org.apache.axis2.context NamedValue]))

(defn axis-service-operations
  [axis-service]
  (iterator-seq (.getOperations axis-service)))

(defn axis-op-name
  [axis-op]
  (some-> axis-op
          (.getName)
          (.getLocalPart)))

(defn axis-op-namespace
  [axis-op]
  (some-> axis-op
          (.getMessage "Out")
          (.getElementQName)
          (.getNamespaceURI)))

(defn axis-op-args
  [axis-op]
  (for [elem (some-> (first (filter #(= "out" (.getDirection %))
                                    (iterator-seq (.getMessages axis-op))))
                     .getSchemaElement .getSchemaType
                     .getParticle .getItems seq)]
    {:name (or (.getName elem) (some-> elem .getWireName .getLocalPart))
     :type (some-> elem .getSchemaType .getName keyword)
     :elem elem}))

(defmulti obj->soap-str (fn [obj argtype] argtype))

(defmethod obj->soap-str :integer [obj argtype] (str obj))
(defmethod obj->soap-str :double [obj argtype] (str obj))
(defmethod obj->soap-str :string [obj argtype] (str obj))
(defmethod obj->soap-str :boolean [obj argtype] (str obj))
(defmethod obj->soap-str :default [obj argtype] (str obj))

(defmulti soap-str->obj (fn [obj argtype] argtype))
(def multi-parser (f/formatter (t/default-time-zone) "YYYY-MM-dd" "YYYY/MM/dd"))

(defmethod soap-str->obj :long [soap-str argtype] (Long/parseLong soap-str))
(defmethod soap-str->obj :integer [soap-str argtype] (Integer/parseInt soap-str))
(defmethod soap-str->obj :double [soap-str argtype] (Double/parseDouble soap-str))
(defmethod soap-str->obj :string [soap-str argtype] soap-str)
(defmethod soap-str->obj :boolean [soap-str argtype] (Boolean/parseBoolean soap-str))
(defmethod soap-str->obj :date [soap-str argtype] (f/parse multi-parser soap-str))
(defmethod soap-str->obj :default [soap-str argtype] soap-str)

(defn make-om-elem
  ([factory tag-name]
   (.createOMElement factory
                     (if (instance? QName tag-name)
                       tag-name
                       (QName. tag-name))))
  ([factory tag-name value-type value]
   (doto (make-om-elem factory tag-name)
     (.setText (if (or (nil? value) (nil? value-type))
                 ; support optional parameters and values without defined types
                 (str value)
                 (obj->soap-str value value-type))))))

(defn map-obj->om-element
  [factory op arg-type arg-key arg-val]
  (let [tag-name (QName. (axis-op-namespace op)
                         (or (:name arg-type) (name arg-key)))]
    (cond
      (seq? arg-val) (throw (UnsupportedOperationException. "XML Arrays not yet supported"))
      (map? arg-val) (let [outer-element (make-om-elem factory tag-name)]
                       (doseq [[key val] arg-val]
                         (.addChild outer-element
                                    (make-om-elem factory
                                                  (QName. (axis-op-namespace op) (name key))
                                                  (:type arg-type)
                                                  val)))
                       outer-element)
      :else (make-om-elem factory tag-name (:type arg-type) val))))

(defn add-request-parameter!
  "Adds a single parameter to the request.
  Uses `map-obj->om-element` if `arg-type` is unknown (i.e. not a primitive) or `complex-args` is enabled.
  Otherwise, it creates a new tag and uses `obj->soap-str` to determine the tag value."
  [op request factory options arg-type arg-key arg-val]
  (.addChild request
             (if (or (nil? (:type arg-type)) (:complex-args options false))
               (map-obj->om-element factory op arg-type arg-key arg-val)
               (doto (.createOMElement
                       factory (QName. (axis-op-namespace op) (:name arg-type)))
                 (.setText (obj->soap-str arg-val (:type arg-type)))))))

(defn make-request
  [op options args]
  (let [factory (OMAbstractFactory/getOMFactory)
        request (.createOMElement
                  factory (QName. (axis-op-namespace op) (axis-op-name op)))
        op-args (axis-op-args op)]

    ;; Handle all defined parameters
    (doseq [arg-type op-args]
      (let [arg-key (:name arg-type)
            arg-val ((keyword arg-key) args)]
        (add-request-parameter! op request factory options arg-type arg-key arg-val)))

    ;; Handle all undefined parameters
    (let [op-args-keys         (map #(keyword (:name %)) op-args)
          args-without-defined (apply dissoc args op-args-keys)]
      (doseq [[arg-key arg-val] args-without-defined]
        (add-request-parameter! op request factory options nil arg-key arg-val)))

    (log/trace "Invoking SOAP Operation:" (.getName op) "Request:" request)
    request))

(defn get-result
  [retelem]
  (let [result-xml (str retelem)]
    (log/trace "SOAP Operation Response:" result-xml)
    (xml/parse-str result-xml)))

(defn client-call
  [client op options args]
  (let [request (make-request op options args)]
    (locking client
      (if (isa? (class op) OutOnlyAxisOperation)
        (.sendRobust client (.getName op) request)
        (get-result
          (.sendReceive client (.getName op) request))))))

(defn client-proxy
  [client options]
  (->> (for [op (axis-service-operations (.getAxisService client))]
         [(keyword (axis-op-name op))
          (fn [args] (client-call client op options args))])
       (into {})))

(defn make-client
  [url & [{:keys [auth throw-faults timeout chunked? wsdl-auth headers]
           :or   {throw-faults true
                  chunked?     false}}]]
  (let [options (doto (Options.)
                  (.setTo (EndpointReference. url))
                  (.setProperty HTTPConstants/CHUNKED (str chunked?))
                  (.setExceptionToBeThrownOnSOAPFault throw-faults))]

    ;; if WSDL is password-protected, must enable access for URLConnection/connect
    ;; which is used internally by Axis2
    (when wsdl-auth
      (let [url-authenticator (proxy [Authenticator] []
                                (getPasswordAuthentication []
                                  (PasswordAuthentication. (:username wsdl-auth)
                                                           (char-array (:password wsdl-auth)))))]
        (Authenticator/setDefault url-authenticator)))

    ;; support authentication when making SOAP requests
    (when auth
      (let [req-authenticator (doto (HttpTransportPropertiesImpl$Authenticator.)
                                (.setUsername (:username auth))
                                (.setPassword (:password auth)))]
        (.setProperty options HTTPConstants/AUTHENTICATE req-authenticator)))

    ;; enable connection timeouts
    (when timeout
      (let [timeout-int (int timeout)]
        (doto options
          (.setTimeOutInMilliSeconds timeout-int)
          (.setProperty HTTPConstants/SO_TIMEOUT timeout-int)
          (.setProperty HTTPConstants/CONNECTION_TIMEOUT timeout-int))))

    ;; enable custom headers
    (when headers
      (let [headers-list (ArrayList.)]
        (doseq [[k v] headers]
          (.add headers-list (NamedValue. (str k) (str v))))
        (.setProperty options HTTPConstants/HTTP_HEADERS headers-list)))

    ;; ensure all created operation clients also have the same set of options
    (doto (ServiceClient. nil (AxisService/createClientSideAxisService
                                (URL. url) nil nil options))
      (.setOverrideOptions options))))

(defn client-fn
  "Creates SOAP client proxy function which must be invoked with keywordised
  version of the SOAP function and any additional arguments as keywordized hash.
  e.g. (client :GetData {:foo \"test1\" :bar \"test2\"}).
  A map of options are required for generating the function.
  Either :base-client must be supplied (created with make-client) or the :wsdl
  URL string with :options data."
  [{:keys [wsdl options base-client]}]
  (let [; either base client must be supplied or URL with optional data
        client (or base-client (make-client wsdl options))
        px     (client-proxy client options)]
    (fn [method-name args]
      (if-let [operation (px method-name)]
        (operation args)
        (throw (IllegalArgumentException. (str "Cannot find SOAP method '" method-name "'. "
                                               "Available methods are: " (keys px))))))))
