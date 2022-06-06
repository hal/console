[![Verify Codebase](https://github.com/hal/console/actions/workflows/verify.yml/badge.svg)](https://github.com/hal/console/actions/workflows/verify.yml) [![Maven Central](https://img.shields.io/maven-central/v/org.jboss.hal/hal-console)](https://search.maven.org/search?q=g:org.jboss.hal) [![project chat](https://img.shields.io/badge/zulip-join_chat-brightgreen.svg)](https://wildfly.zulipchat.com/#narrow/stream/174373-hal) [![Chat on Gitter](https://badges.gitter.im/hal/console.svg)](https://gitter.im/hal/console)

HAL is the project name for the WildFly and JBoss EAP management console. It's part of every WildFly and JBoss EAP installation. To get started simply fire up your browser and open http://localhost:9990.

In addition, you can start HAL in [standalone mode](https://hal.github.io/documentation/get-started/#standalone-mode) and connect to arbitrary WildFly and JBoss EAP instances. [Native binaries](https://hal.github.io/documentation/get-started/#native-binary) are available for Linux, macOS and Windows. [Container images](https://hal.github.io/documentation/get-started/#container) are available at https://quay.io/repository/halconsole/hal and the latest HAL version is hosted at https://hal.github.io/console/.  

# Technical Stack

HAL is a client side RIA without any server side dependencies. It is a GWT application - which means it's written almost completely in Java. GWT is used to transpile the Java code into a bunch of JavaScript, HTML and CSS files.

In a nutshell the console uses the following technical stack:

- [Java 11](https://jdk.java.net/java-se-ri/11)
- [GWT](https://www.gwtproject.org/)
- [GWTP](https://github.com/ArcBees/GWTP)
- [Elemento](https://github.com/hal/elemento)
- [PouchDB](https://pouchdb.com/)
- [PatternFly](https://www.patternfly.org/)
- [Maven](https://maven.apache.org/) and [Parcel](https://parceljs.org/) 

# Build

To start from scratch, use the following commands to clone and build HAL:

```shell
git clone git@github.com:hal/console.git
cd console
./mvnw verify
```

For more information on how to [build](https://hal.github.io/development/build-run/#build), [debug](https://hal.github.io/development/build-run/#debug) and [run](https://hal.github.io/documentation/get-started/) the console, take a look at the HAL community site at https://hal.github.io.

# Issue Tracking

Bugs and features are tracked within the HAL Jira project at https://issues.jboss.org/browse/HAL

# Contributing

This is an open source project. That means that everybody can contribute. It's not hard to get started. So start [contributing](CONTRIBUTING.md) today!

# Branches

All recent development happens in the branch `main`. There are additional branches mainly used for maintenance:

| Branch | Description                                       |
|--------|---------------------------------------------------|
| main   | Main branch used for development                  |
| 3.3.x  | Branch used for WildFly 23.x - 26.x and EAP 7.4.x |
| 3.2.x  | Branch used for WildDly 17.x - 22.x and EAP 7.3.x |
| 3.1.x  | Branch used for WildDly 16.x and EAP 7.2.x        |
| 3.0.x  | Branch used for WildFly 13.x - 15.x and EAP 7.2.x |

See [branches](https://hal.github.io/development/branches/) for all details. 

# Licenses

This project uses the following licenses:

* [Apache License 2.0](https://repository.jboss.org/licenses/apache-2.0.txt)
