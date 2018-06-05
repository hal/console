[![TC Build](https://ci.wildfly.org/app/rest/builds/buildType:(id:hal_ConsoleDevelopment)/statusIcon.svg)](https://ci.wildfly.org/viewType.html?buildTypeId=hal_ConsoleDevelopment&guest=1) [![License](https://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html) [![Chat on Gitter](https://badges.gitter.im/hal/console.svg)](https://gitter.im/hal/console)

HAL ist the project name for the WildFly and JBoss EAP management console. It's part of every WildFly and JBoss EAP installation. To get started simply fire up your browser and open http://localhost:9990. 


# Technical Stack

HAL is a client side RIA without server side dependencies. It is a GWT application - which means it's written almost completely in Java. GWT is used to transpile the Java code into a bunch of JavaScript, HTML and CSS files. HAL uses some external JavaScript libraries as well. These dependencies are managed using [bower](https://bower.io/) which is in turn integrated into the Maven build using the [`maven-frontend-plugin`](https://github.com/eirslett/frontend-maven-plugin). Take a look at the [`bower.json`](https://github.com/hal/console/blob/develop/app/bower.json) too see all JavaScript dependencies.

In a nutshell the console uses the following technical stack:

- Java 8
- [GWT](http://www.gwtproject.org/) 
- [GWTP](https://dev.arcbees.com/gwtp/)
- [Elemento](https://github.com/hal/elemento)
- [RxGWT](https://github.com/intendia-oss/rxgwt)
- [PouchDB](https://pouchdb.com/)
- [PatternFly](https://www.patternfly.org/)

# Build

For a full build use 

```bash
mvn clean install
``` 

This includes the GWT compiler, which might take a while. If you just want to make sure that there are no compilation or test failures, you can skip the GWT compiler and use

```bash
mvn clean install -Dgwt.skipCompilation
``` 

## Production Builds

To build a HAL release ready to be used as standalone console, for WildFly or JBoss EAP use one of the following commands:

- Standalone: `mvn clean install -P prod,theme-hal`
- WildFly: `mvn clean install -P prod,theme-wildfly`
- JBoss EAP: `mvn clean install -P prod,theme-eap`

# Run

The GWT development mode starts a local Jetty server. As a one time prerequisite you need to add the URL of the local Jetty server as an allowed origin to your WildFly / JBoss EAP configuration: 

**Standalone Mode**

```bash
/core-service=management/management-interface=http-interface:list-add(name=allowed-origins,value=http://localhost:8888)
reload
```
**Domain Mode**

```bash
/host=master/core-service=management/management-interface=http-interface:list-add(name=allowed-origins,value=http://localhost:8888)
reload --host=master
``` 
 
The main GWT application is located in the `app` folder. To run the console use

```bash
cd app
mvn gwt:devmode
```

This will start the development mode. Wait until you see a message like 

```
00:00:15,703 [INFO] Code server started in 15.12 s ms
```

Then open http://localhost:8888/dev.html in your browser and connect to your WildFly / JBoss EAP instance. 

# Debug

Start the console as described in the previous chapter. GWT uses the [SourceMaps standard](https://docs.google.com/document/d/1U1RGAehQwRypUTovF1KRlpiOFze0b-_2gc6fAH0KY0k/edit?usp=sharing) to map the Java source code to the transpiled JavaScript code. This makes it possible to use the browser development tools for debugging.

In Chrome open the development tools and switch to the 'Sources' tab. Press <kbd>⌘ P</kbd> and type the name of the Java source file you want to open. 

Let's say we want to debug the enable / disable action in the data source column in configuration. Open the class `DataSourceColumn` and put a breakpoint on the first line of method `void setEnabled(ResourceAddress, boolean, SafeHtml)` (should be line 285). Now select a data source like the default 'ExampleDS' data source and press the enable / disable link in the preview. The browser should stop at the specified line and you can use the development tools to inspect and change variables. 

**Inspect Variables**

If you're used to debug Java applications in your favorite IDE, the debugging experience in the browser development tools might feel strange at first. You can inspect simple types like boolean, numbers and strings. Support for native JavaScript types like arrays and objects is also very good. On the other hand Java types like lists or maps are not very well supported. In addition most variable names are suffixed with something like `_0_g$`. We recommend to inspect these variables using the console and call the `toString()` method on the respective object.    

# Develop

To apply changes made to Java code you just need to refresh the browser. GWT will detect the modifications and only transpile the changed sources. 

Changes to other resources require a little bit more effort. To make it easier, you can use the script `app/refresh.sh`. Change to the `app` folder and call `refresh.sh` with one of the following parameters, depending what kind of resource you've modified:

- `less`: Compile LESS stylesheets
- `html`: Update HTML snippets
- `i18n`: Process i18n resource bundles
- `mbui`: Regenerate MBUI resources

After calling the script, refresh the browser to see your changes. 

# Replace Existing Console

If you want to replace the console of an existing WildFly installation use the following steps:

1. `mvn clean install -P prod,theme-wildfly`
1. `cp app/target/hal-console-<version>-resources.jar $WILDFLY_HOME/modules/system/layers/base/org/jboss/as/console/main`
1. Edit `$WILDFLY_HOME/modules/system/layers/base/org/jboss/as/console/main/module.xml` and adjust the `<resources/>` config: `<resource-root path="hal-console-<version>-resources.jar"/>`
