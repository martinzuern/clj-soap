# clj-soap

clj-soap is a SOAP server and client using Apache Axis2.

This version is based on Sean Corfield's version, which is in turn based on
[Tetsuya Takatsuru's version](https://bitbucket.org/taka2ru/clj-soap). It
includes patches from a range of
[forks](https://github.com/seancorfield/clj-soap/network) to add new
features, options and bug fixes. Shout out to contributors for the
@jaimeagudo, @uswitch, @j1mr10rd4n, @scttnlsn, @jpmonettas, @rentpath forks.

## Usage

### Client

You can call remote SOAP method as following:
```clojure
(require '[clj-soap.client :as soap])

(let [client (soap/client-fn {:wsdl "http://... (URL for WSDL)"})]
  (client :someMethod param1 param2 ...))
```

Optional configuration data can be passed into the `client-fn` function:

```clojure
(let [client (soap/client-fn {:wsdl "http://... (URL for WSDL)"
                              :options {:basic-auth {:username "test" :password "test"}
                                        :timeout 10000
                                        :chunked? true
                                        :throw-faults false}})]
  (client :someMethod param1 param2 ...))
```

In certain situations, Axis2 doesn't detect complex arguments. To workaround this,
complex arguments can be manually specified:

```clojure
(let [client (soap/client-fn {:wsdl "http://... (URL for WSDL)"
                              :options {:complex-args true}})]
  (client :someMethod [["item" "test1"] ["item" "test2"]]))
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
`defservice` needs to be AOT-compiled.
For example, `lein compile` before running server.

#### Type Hint

SOAP services need typehints.
`String` for arguments and `void` for return value,
if you don't specify typehints.

## License

Copyright (C) 2011 Tetsuya Takatsuru

Distributed under the Eclipse Public License, the same as Clojure.

