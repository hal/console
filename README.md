[![TC Build](https://ci.wildfly.org/app/rest/builds/buildType:(id:hal_HalNextDev)/statusIcon.svg)](https://ci.wildfly.org/viewType.html?buildTypeId=hal_HalNextDev&guest=1) [![License](https://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html) [![Chat on Gitter](https://badges.gitter.im/hal/hal.next.svg)](https://gitter.im/hal/hal.next)  
[![Issues in Ready](https://badge.waffle.io/hal/hal.next.svg?label=ready&title=Ready)](http://waffle.io/hal/hal.next) [![Issues in Progress](https://badge.waffle.io/hal/hal.next.svg?label=In%20Progress&title=In%20Progress)](http://waffle.io/hal/hal.next) 

# HAL.next

[![Join the chat at https://gitter.im/hal/hal.next](https://badges.gitter.im/hal/hal.next.svg)](https://gitter.im/hal/hal.next?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

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
    - Topology overview
    - Macro recording
    - PatternFly compliance
    - Enhanced form items for lists, properties and booleans
    - Use capabilities & requirements to generate combo boxes with type-ahead support
    - Declarative UI using MBUI and a simple XML format. See [LoggingView.mbui.xml](app/src/main/resources/org/jboss/hal/client/configuration/subsystem/logging/LoggingView.mbui.xml) for an example.
    - [JavaScript API](https://cdn.rawgit.com/hal/hal.next/esdoc/index.html)
    - [Extensions](Extensions.md)
    - Remove deprecated APIs 

- Finder

    - Navigation using cursor keys. Open an application by pressing ↵ (`enter`) and go back with ⌫ (`backspace`)
    - Pin frequently used subsystems to stay at the top
    - Filter items by name *and* by properties like 'enabled' in the data sources column or 'stopped' in the servers column

## Running

HAL.next should be used with **WildFly 11.x** (it makes use of the new capabilities service). There are different ways to launch HAL.next. Most of them require to configure the allowed origins of the HTTP management endpoint and connect to a running WildFly instance.
 
- Standalone mode

        /core-service=management/management-interface=http-interface:list-add(name=allowed-origins,value=<url>)
        reload

- Domain mode
 
        /host=master/core-service=management/management-interface=http-interface:list-add(name=allowed-origins,value=<url>)
        reload --host=master
        
### Standalone

The module `hal-standalone` contains an executable jar which launches a local web server at http://localhost:9090.
  
1. Add http://localhost:9090 as allowed origin
1. `mvn clean install -P prod,theme-hal`
1. `java -jar standalone/target/hal-standalone-<version>.jar`
1. Open http://localhost:9090

If you don't want to or cannot build locally you can download `hal-standalone.jar` from https://repository.jboss.org/nexus/index.html#nexus-search;quick~hal-standalone. 

### NPM

The module `hal-npm` provides a npm package which launches a local web server at http://localhost:3000.
  
1. Add http://localhost:3000 as allowed origin
1. `mvn clean install -P prod,theme-hal`
1. `cd npm/target/hal-npm-<version>-hal-console/`
1. `npm install`
1. `node server.js`
1. Open http://localhost:3000

The npm package is also available on npmjs.com: https://www.npmjs.com/package/hal-console
 
1. `npm install -g hal-console`
1. `hal-console`

### WildFly Swarm Fraction

The module `hal-fraction` contains the WildFly Swarm fraction `org.jboss.hal.fraction.HalFraction`.
 
1. Add the following dependencies to your POM:

    ```xml
    <dependency>
        <groupId>org.wildfly.swarm</groupId>
        <artifactId>management</artifactId>
    </dependency>
    <dependency>
        <groupId>org.jboss.hal</groupId>
        <artifactId>hal-fraction</artifactId>
    </dependency>
    ```
        
1. Build and start your WildFly Swarm application
1. Add http://localhost:8080 as allowed origin
1. Open http://localhost:8080/hal

### Docker 

The `docker` module provides a docker image with WildFly 11.x and HAL.next.

1. `cd docker`
1. `mvn install`
1. `docker run -p 9990:9990 -it hpehl/hal-console /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0` or 
1. `docker run -p 9990:9990 -it hpehl/hal-console /opt/jboss/wildfly/bin/domain.sh -b 0.0.0.0 -bmanagement 0.0.0.0` 
1. Open http://localhost:9990 and log in with `admin:admin`

The docker image is also available in the public docker repository: https://hub.docker.com/r/hpehl/hal-console/

`docker pull hpehl/hal-console`

### GitHub Pages

Finally HAL.next is also available on the `gh-pages` branch at https://hal.github.io/hal.next/. 

1. Add https://hal.github.io/hal.next/ as allowed origin
1. Open https://hal.github.io/hal.next/

GitHub pages are served from **https** so you need to secure the management interface as well. Please note that if you're using a self signed key store you might need to open the local management endpoint in the browser and accept the unsafe certificate before you can use it with HAL.next.

### SuperDevMode

The SuperDevMode is intended for development as it provides browser refresh after code changes. 

1. Add http://localhost:8888 as allowed origin
1. `mvn install -Dgwt.skipCompilation` 
1. `cd app`
1. `mvn gwt:devmode`
1. Open http://localhost:8888/hal/dev.html
