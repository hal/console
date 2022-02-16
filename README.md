[![Verify Codebase](https://github.com/hal/console/actions/workflows/verify.yml/badge.svg)](https://github.com/hal/console/actions/workflows/verify.yml) [![project chat](https://img.shields.io/badge/zulip-join_chat-brightgreen.svg)](https://wildfly.zulipchat.com/#narrow/stream/174373-hal) [![Chat on Gitter](https://badges.gitter.im/hal/console.svg)](https://gitter.im/hal/console)

HAL is the project name for the WildFly and JBoss EAP management console. It's part of every WildFly and JBoss EAP installation. To get started simply fire up your browser and open http://localhost:9990.

# Technical Stack

HAL is a client side RIA without any server side dependencies. It is a GWT application - which means it's written almost completely in Java. GWT is used to transpile the Java code into a bunch of JavaScript, HTML and CSS files. HAL uses some external JavaScript libraries as well. These dependencies are managed using [NPM](https://npmjs.org/) which is in turn integrated into the Maven build using the [`maven-frontend-plugin`](https://github.com/eirslett/frontend-maven-plugin).

In a nutshell the console uses the following technical stack:

- Java 11
- [GWT](https://www.gwtproject.org/)
- [GWTP](https://dev.arcbees.com/gwtp/)
- [Elemento](https://github.com/hal/elemento)
- [RxGWT](https://github.com/intendia-oss/rxgwt)
- [PouchDB](https://pouchdb.com/)
- [PatternFly](https://www.patternfly.org/)

For more information on how to build, debug and run the console, take a look at the HAL community site at https://hal.github.io.

# Contributing

This is an open source project. That means that everybody can [contribute](CONTRIBUTING.md). It's not hard to get started. So start contributing today!

# Licenses

This project uses the following licenses:

* [Apache License 2.0](https://repository.jboss.org/licenses/apache-2.0.txt)
