<a href="https://ci.wildfly.org/viewType.html?buildTypeId=hal_HalNextDev&guest=1"><img src="https://ci.wildfly.org/app/rest/builds/buildType:(id:hal_HalNextDev)/statusIcon"/></a>

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
    - Topology overview
    - Macro recording
    - PatternFly compliance
    - Enhanced form items for lists, properties and booleans
    - Use capabilities & requirements to generate combo boxes with type-ahead support
    - Declarative UI using MBUI and a simple XML format. See [LoggingView.xml](app/src/main/resources/org/jboss/hal/client/configuration/subsystem/logging/LoggingView.xml) for an example.
    - Remove deprecated APIs 

- Finder

    - Navigation using cursor keys. Open an application by pressing ↵ (`enter`) and go back with ⌫ (`backspace`)
    - Pin frequently used subsystems to stay at the top
    - Filter items by name *and* by properties like 'enabled' in the data sources column or 'stopped' in the servers column
    - Columns could be provided by extensions

## Running

HAL.next requires **WildFly 11.x** (it makes use of the new capabilities service)! There are different ways to launch HAL.next and connect to a running WildFly instance. Most of them require to configure the allowed origins of the HTTP management endpoint.
 
- Standalone mode

        /core-service=management/management-interface=http-interface:list-add(name=allowed-origins,value=<url>)
        reload

- Domain mode
 
        /host=master/core-service=management/management-interface=http-interface:list-add(name=allowed-origins,value=<url>)
        reload --host=master
        
### Standalone

The `standalone` module contains an executable jar which launches a local web server at http://localhost:9090.
  
1. Add http://localhost:9090 as allowed origin
1. `mvn install`
1. `java -jar standalone/target/hal-standalone-<version>.jar`
1. Open http://localhost:9090

If you don't want to or cannot build locally you can download `hal-standalone.jar` from https://repository.jboss.org/nexus/index.html#nexus-search;quick~hal-standalone. 

### NPM

The `npm` module provides a npm package which launches a local web server at http://localhost:3000.
  
1. Add http://localhost:3000 as allowed origin
1. `mvn install`
1. `cd npm`
1. `npm install`
1. `node server.js`
1. Open http://localhost:3000

The package is also available on npmjs.com: https://www.npmjs.com/package/hal-next
 
1. `npm install -g hal-next`
1. `hal-next`

### Docker 

The `docker` module is used to build a docker image with WildFly 11.0.0.Alpha1 and HAL.next.

1. `cd docker`
1. `mvn install`
1. `docker run -p 9990:9990 -it hpehl/hal-next /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0` or 
1. `docker run -p 9990:9990 -it hpehl/hal-next /opt/jboss/wildfly/bin/domain.sh -b 0.0.0.0 -bmanagement 0.0.0.0` 
1. Open http://localhost:9990 and log in with `admin:admin`

The docker image is also available in the public docker repository: https://hub.docker.com/r/hpehl/hal-next/

### GitHub Pages

Finally HAL.next is also available on the `gh-pages` branch at https://hal.github.io/hal.next/. 

1. Add https://hal.github.io/hal.next/ as allowed origin
1. Open https://hal.github.io/hal.next/

GitHub pages are served from **https** so you need to secure the management interface as well. Please note that if you're using a self signed key store you might need to open the local management endpoint in the browser and accept the unsafe certificate before you can use it with HAL.next.

### SuperDevMode

The SuperDevMode is intended for development as it provides browser refresh after code changes. 

1. Add http://localhost:8888 as allowed origin
1. `mvn install -Dgwt.skipCompilation -Pdev` 
1. `cd app`
1. `./devmode.sh`
1. Open http://localhost:8888/hal/dev.html
