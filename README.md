# HAL.next

Fresh start of the HAL management console using the latest frameworks / libraries. 

- Java 8
- GWT 3.x (JsInterop, Elemental)
- Latest GWTP build
- PatternFly

## Motivation

HAL.next is a prototype and playground to test new features and evaluate the latest frameworks. The upcoming GWT 3.0 release will introduce many breaking changes for GWT applications. The most important change will be the deprecation of `gwt-user.jar`. This includes features such as the GWT widgets, deferred binding and GWT RPC. 

In order to make the console future proof, it's necessary to rewrite these parts. At the same time this is an opportunity to fix some weak points of the current implementation and add new features. Currently the following features and enhancements are implemented / planned:

- General

    - Place management for finder *and* applications. This enables features like
        - Cross-links between different parts of HAL (configuration ⟷ runtime ⟷ deployment)
        - Applications / finder selections can be bookmarked
        - Search can be re-implemented
    - Switch between applications using the breadcrumb
    - Add deployments using drag & drop
    - Macro recording
    - PatternFly compliant
    - Enhanced form items for lists, properties and booleans
    - Support for capabilities & requirements to show combo boxes with type-ahead support
    - Declarative UI using MBUI and a simple XML format (w/o behaviour). See [LoggingView.xml](app/src/main/resources/org/jboss/hal/client/configuration/subsystem/logging/LoggingView.xml) for an example.
    - Remove deprecated APIs 

- Finder

    - Navigation using cursor keys. Open an application by pressing ↵ (`enter`) and go back with ⌫ (`backspace`)
    - Pin frequently used subsystems to stay at the top
    - Filter items by name *and* by properties like 'enabled' in the data sources column
    - Columns could be provided by extensions

## Running

There are different ways to launch HAL.next and connect to a running WildFly instance. Each one requires to configure the allowed origins of the HTTP management endpoint.
 
- Standalone mode

        /core-service=management/management-interface=http-interface:list-add(name=allowed-origins,value=<url>)
        reload

- Domain mode
 
        /host=master/core-service=management/management-interface=http-interface:list-add(name=allowed-origins,value=<url>)
        reload --host=master
        
### Local / Production Mode

The `standalone` module contains an executable jar which launches a local web server at http://localhost:9090.
  
1. Add http://localhost:9090 as allowed origin
1. `mvn install`
1. `java -jar standalone/target/hal-standalone-<version>.jar`
1. Open http://localhost:9090

### Local / SuperDevMode

Useful during development as it provides browser refresh after code changes. 

1. Add http://localhost:8888 as allowed origin
1. `mvn install -Dgwt.skipCompilation -Pdev` 
1. `cd app`
1. `./devmode.sh`
1. Open http://localhost:8888/hal/dev.html

### Remote

A recent version of HAL.next is also available on the `gh-pages` branch at https://hal.github.io/hal.next/. 

1. Add https://hal.github.io/hal.next/ as allowed origin
1. Open https://hal.github.io/hal.next/

The remote version is served from **https** so you need to secure the management interface as well. Please note that if you're using a self signed key store you might need to open the local management endpoint in the browser and accept the unsafe certificate before you can use it with HAL.next.