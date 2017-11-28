(ns clj-soap.client-test
  (:require [clojure.test :refer :all]
            [clj-soap.client :as soap-client]))

(deftest test-client-request
  (testing "Validate airport: MUC"
    (let [client (soap-client/client-fn {:wsdl "http://www.webservicex.net/airport.asmx?WSDL"})
          result (client :getAirportInformationByAirportCode {:airportCode "MUC"})]
      (is (= #clojure.data.xml.Element{:attrs   {},
                                       :tag     :getAirportInformationByAirportCodeResponse,
                                       :content (#clojure.data.xml.Element{:attrs   {},
                                                                           :content ("<NewDataSet>\n  <Table>\n    <AirportCode>MUC</AirportCode>\n    <CityOrAirportName>MUNICH</CityOrAirportName>\n    <Country>Germany</Country>\n    <CountryAbbrviation>DE</CountryAbbrviation>\n    <CountryCode>429</CountryCode>\n    <GMTOffset>-1</GMTOffset>\n    <RunwayLengthFeet>9199</RunwayLengthFeet>\n    <RunwayElevationFeet>1737</RunwayElevationFeet>\n    <LatitudeDegree>48</LatitudeDegree>\n    <LatitudeMinute>21</LatitudeMinute>\n    <LatitudeSecond>0</LatitudeSecond>\n    <LatitudeNpeerS>N</LatitudeNpeerS>\n    <LongitudeDegree>11</LongitudeDegree>\n    <LongitudeMinute>47</LongitudeMinute>\n    <LongitudeSeconds>0</LongitudeSeconds>\n    <LongitudeEperW>E</LongitudeEperW>\n  </Table>\n  <Table>\n    <AirportCode>MUC</AirportCode>\n    <CityOrAirportName>MUNICH</CityOrAirportName>\n    <Country>Germany</Country>\n    <CountryAbbrviation>DE</CountryAbbrviation>\n    <CountryCode>429</CountryCode>\n    <GMTOffset>-1</GMTOffset>\n    <RunwayLengthFeet>9199</RunwayLengthFeet>\n    <RunwayElevationFeet>1737</RunwayElevationFeet>\n    <LatitudeDegree>48</LatitudeDegree>\n    <LatitudeMinute>21</LatitudeMinute>\n    <LatitudeSecond>0</LatitudeSecond>\n    <LatitudeNpeerS>N</LatitudeNpeerS>\n    <LongitudeDegree>11</LongitudeDegree>\n    <LongitudeMinute>47</LongitudeMinute>\n    <LongitudeSeconds>0</LongitudeSeconds>\n    <LongitudeEperW>E</LongitudeEperW>\n  </Table>\n</NewDataSet>"),
                                                                           :tag     :getAirportInformationByAirportCodeResult})}
             result)))))

(deftest test-throw-if-method-illegal
  (let [client (soap-client/client-fn {:wsdl "http://www.webservicex.net/airport.asmx?WSDL"})]
    (is (thrown? IllegalArgumentException
                 (client :foo {})))))
