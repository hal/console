[![TC Build](https://ci.wildfly.org/app/rest/builds/buildType:(id:hal_ConsoleDevelopment)/statusIcon.svg)](https://ci.wildfly.org/viewType.html?buildTypeId=hal_ConsoleDevelopment&guest=1) [![Chat on Gitter](https://badges.gitter.im/hal/console.svg)](https://gitter.im/hal/console)

HAL is the project name for the WildFly and JBoss EAP management console. It's part of every WildFly and JBoss EAP installation. To get started simply fire up your browser and open http://localhost:9990. 

# Technical Stack

HAL is a client side RIA without any server side dependencies. It is a GWT application - which means it's written almost
completely in Java. GWT is used to transpile the Java code into a bunch of JavaScript, HTML and CSS files. HAL uses some
external JavaScript libraries as well. These dependencies are managed using [NPM](https://npmjs.org/) which is in turn
integrated into the Maven build using the [`maven-frontend-plugin`](https://github.com/eirslett/frontend-maven-plugin).

In a nutshell the console uses the following technical stack:

- Java 11
- [GWT](https://www.gwtproject.org/)
- [GWTP](https://dev.arcbees.com/gwtp/)
- [Elemento](https://github.com/hal/elemento)
- [RxGWT](https://github.com/intendia-oss/rxgwt)
- [PouchDB](https://pouchdb.com/)
- [PatternFly](https://www.patternfly.org/)

# Build

For a full build use

```shell
mvn verify
``` 

This includes the GWT compiler, which might take a while. If you just want to make sure that there are no compilation or test failures, you can skip the GWT compiler and use

```shell
mvn verify -P skip-gwt
``` 

To build a HAL release ready to be used for WildFly or JBoss EAP use one of the following commands:

- WildFly: `mvn clean install -P prod,theme-wildfly`
- JBoss EAP: `mvn clean install -P prod,theme-eap`

## Profiles

The POM defines the following profiles:

- `native`: Used to build the native binary for the standalone mode
- `prod`: Activates GWT settings for the production build
- `release`: Builds and signs source and JavaDoc JARs
- `skip-gwt`: Skips GWT compilation
- `theme-eap`: Theme for JBoss EAP
- `theme-hal`: Theme for HAL standalone
- `theme-wildfly`: Theme for WildFly

# Run

## Development Mode

The GWT development mode starts a local Jetty server. As a one time prerequisite you need to add the URL of the local
Jetty server as an allowed origin to your WildFly / JBoss EAP configuration:

```shell
/core-service=management/management-interface=http-interface:list-add(name=allowed-origins,value=http://localhost:8888)
reload
```

resp.

```shell
/host=master/core-service=management/management-interface=http-interface:list-add(name=allowed-origins,value=http://localhost:8888)
reload --host=master
``` 

The main GWT application is located in the `app` folder. To run the console in dev mode use

```shell
cd app
mvn gwt:devmode
```

This will start the development mode. Wait until you see a message like

```
00:00:15,703 [INFO] Code server started in 15.12 s ms
```

Then open http://localhost:8888/dev.html in your browser and connect to your WildFly / JBoss EAP instance.

The dev mode allows you to change code and see your changes simply by refreshing the browser. GWT will detect the
modifications and only transpile the changed sources.

## Standalone Mode

HAL can also be started as standalone Java application. The standalone mode is a Quarkus application which uses port
9090. Similar to GWT dev mode, you have to add the URL as allowed origin to your WildFly / JBoss EAP configuration:

```shell
/core-service=management/management-interface=http-interface:list-add(name=allowed-origins,value=http://localhost:9090)
reload
```

resp.

```shell
/host=master/core-service=management/management-interface=http-interface:list-add(name=allowed-origins,value=http://localhost:9090)
reload --host=master
``` 

To build and run the standalone mode use

```shell
mvn package --projects standalone --also-make -P prod,theme-hal
java -jar standalone/target/quarkus-app/quarkus-run.jar
```

Then open http://localhost:9090/ in your browser and connect to your WildFly / JBoss EAP instance.

# Debug

Start the console as described in 'Run / Development Mode'. GWT uses
the [SourceMaps standard](https://docs.google.com/document/d/1U1RGAehQwRypUTovF1KRlpiOFze0b-_2gc6fAH0KY0k/edit?usp=sharing)
to map the Java source code to the transpiled JavaScript code. This makes it possible to use the browser development
tools for debugging.

In Chrome open the development tools and switch to the 'Sources' tab. Press <kbd>⌘ P</kbd> and type the name of the Java
source file you want to open.

Let's say we want to debug the enable / disable action in the data source column in configuration. Open the
class `DataSourceColumn` and put a breakpoint on the first line of
method `void setEnabled(ResourceAddress, boolean, SafeHtml)`. Now select a data source like the default 'ExampleDS' data
source and press the enable / disable link in the preview. The browser should stop at the specified line, and you can
use the development tools to inspect and change variables.

**Inspect Variables**

If you're used to debugging Java applications in your favorite IDE, the debugging experience in the browser development
tools might feel strange at first. You can inspect simple types like boolean, numbers and strings. Support for native
JavaScript types like arrays and objects is also very good. On the other hand Java types like lists or maps are not very
well-supported. In addition, most variable names are suffixed with something like `_0_g$`. We recommend inspecting these
variables using the console and call the `toString()` method on the respective object.

# Scripts

This repository contains various scripts to automate tasks.

## `depgraph.sh`

Generates a visual dependency graph which is part of HAL's official site: https://hal.github.io/

## `format.sh`

Formats the codebase by applying the following maven goals:

- [`license-maven-plugin:format`](https://mycila.carbou.me/license-maven-plugin/#goals)
- [`formatter-maven-plugin:format`](https://code.revelc.net/formatter-maven-plugin/format-mojo.html)
- [`impsort-maven-plugin:sort`](https://code.revelc.net/impsort-maven-plugin/sort-mojo.html)

## `validate.sh`

Validates the codebase by applying the following maven goals:

- [`enforcer:enforce`](https://maven.apache.org/enforcer/maven-enforcer-plugin/enforce-mojo.html)
- [`checkstyle:check`](https://maven.apache.org/plugins/maven-checkstyle-plugin/check-mojo.html)
- [`license-maven-plugin:check`](https://mycila.carbou.me/license-maven-plugin/#goals)
- [`formatter-maven-plugin:validate`](https://code.revelc.net/formatter-maven-plugin/validate-mojo.html)
- [`impsort-maven-plugin:check`](https://code.revelc.net/impsort-maven-plugin/check-mojo.html)

## `versionBump.sh`

Bumps the version to the specified version number.

# Licenses

This project uses the following licenses:

* [Apache License 2.0](https://repository.jboss.org/licenses/apache-2.0.txt)
