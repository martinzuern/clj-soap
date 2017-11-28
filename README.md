# clj-soap

clj-soap is a SOAP server and client using Apache Axis2.

This version is based on [Zeto's](https://github.com/Zeto-Ltd/clj-soap) version,
which is based on Sean Corfield's version, which is in turn based on [Tetsuya
Takatsuru's version](https://bitbucket.org/taka2ru/clj-soap).  
It includes patches from a range of
[forks](https://github.com/seancorfield/clj-soap/network) to add new features,
options and bug fixes. Shout out to contributors for the @jaimeagudo, @uswitch,
@j1mr10rd4n, @scttnlsn, @jpmonettas, @rentpath forks.

## Usage

### Client

You can call remote SOAP method as following:
```clojure
(require '[clj-soap.client :as soap])

(let [client (soap/client-fn {:wsdl "http://... (URL for WSDL)"})]
  (client :someMethod {:param1 "foo", :param2 "bar", ...}))
```

The response is a Clojure object representation of the response XML.

Optional configuration data can be passed into the `client-fn` function:
```clojure
(let [client (soap/client-fn {:wsdl "http://... (URL for WSDL)"
                              :options {; enables authentication for retrieving the WSDL
                                        ; NB: because of Axis2 internals, this must change
                                        ; the JVM-wide Authenticator default, so be careful!
                                        :wsdl-auth {:username "test"
                                                    :password "test"}
                                        ; enables authentication for all requests
                                        :auth {:username "test"
                                               :password "test"}
                                        ; specify timeout for accessing requests
                                        :timeout 10000
                                        ; enables chunked transfer for requests
                                        :chunked? true
                                        ; by default, SOAP failures result in exceptions
                                        ; setting this value to false makes failures 
                                        ; result in standard responses
                                        :throw-faults false
                                        ; headers specified here will be added 
                                        ; to outgoing requests
                                        :headers {"X-Api-Key" "ssshhh"}}})]
  (client :someMethod {:param1 "foo", :param2 "bar", ...}))
```

In certain situations, Axis2 doesn't detect complex arguments. To workaround
this, complex arguments can be manually specified:

```clojure
(let [client (soap/client-fn {:wsdl "http://... (URL for WSDL)"
                              :options {:complex-args true}})]
  (client :someMethod {:foo [["item" "test1"] :bar ["item" "test2"]]})
```

### Server

To make SOAP service:
```clojure
(require '[clj-soap.server :as soap])

;; Defining service class
(soap/defservice my.some.SoapClass
  (someMethod ^String [^Integer x ^String s]
              (str "x is " x "\ts is " s)))

;; Start SOAP Service
(serve "my.some.SoapClass")
```

`defservice` needs to be AOT-compiled. For example, `lein compile` before
running server.

#### Type Hint

SOAP services need typehints. `String` for arguments and `void` for return
value, if you don't specify typehints.

## License

Copyright (C) 2011 Tetsuya Takatsuru

Distributed under the Eclipse Public License, the same as Clojure.
