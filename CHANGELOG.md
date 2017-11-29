### Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
-   New option for adding headers to the outgoing request.
-   Parameters not defined in the WSDL will be appended to the request at the
    end.

### Changed
-   **Breaking Change:** Request parameters are now added using a hash map
    instead of plain function params.
-   **Breaking Change:** We'll throw an `IllegalArgumentException` if the
    requested SOAP method isn't listed in the WSDL schema.
-   Fixed: Namespace of root message wasn't determined correctly sometimes. We
    now use the namespace of the "outMessage".
-   Fixed: Complex structures (e.g. objects) were not always handled correctly.
    Added handling for maps and plain objects, throwing on arrays
-   Fixed: When adding arbitrary objects, `setText` was called with non-string
    values
-   Fixed: Coerce timeout to integer
-   Fixed client tests.

### Removed
-   **Breaking Change:** Dropped support for SOAP server.
