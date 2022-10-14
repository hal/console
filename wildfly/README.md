# WildFly Images

This folder contains scripts and Dockerfiles to build and run WildFly images for all WildFly versions >= 10.0.0.Final. The images build on top of [quay.io/wildfly/wildfly](https://quay.io/repository/wildfly/wildfly) and are hosted at [quay.io/halconsole/wildfly](https://quay.io/repository/halconsole/wildfly). 

The images are mainly intended for the development and testing of HAL. If you're looking for (Jakarta EE) application development with WildFly, the official [WildFly images](https://quay.io/organization/wildfly) might be a better match. 

The images add an admin user `admin:admin` and [allowed origins](https://docs.wildfly.org/26/wildscribe/core-service/management/management-interface/http-interface/index.html#attr-allowed-origins) for

- http://localhost:8888 (used by GWT dev mode)
- http://localhost:9090 (used by HAL standalone)
- http://hal:9090 (used by the HAL test suite)
- http://hal.github.io (latest online console)
- https://hal.github.io (latest online console)

The allowed origins are meant to run [HAL](https://hal.github.io) in [standalone mode](https://hal.github.io/documentation/get-started/#standalone-mode) and connect to the running WildFly instances.

In addition, the images contain a `standalone-<config>-insecure.xml`  configuration for each `standalone-<config>.xml` variant and a `host-insecure.xml` configuration. These configurations disable the authentication of the management interface and are used by the HAL [test suite](https://github.com/hal/manatoko) to run automatic Selenium tests w/o worrying about browser authentication popups getting in the way. 

## Scripts

Most scripts accept a WildFly version as `<major>[.<minor>]` with `major` being mandatory >= 10 and `minor` being optional >= 0 and <= 9. If the version is omitted, the latest WildFly development version will be used. This image is built from https://github.com/wildfly/wildfly and is updated daily. 

In addition, all scripts support the following flags:

```shell
-h, --help      Prints help information
-v, --version   Prints version information
--no-color      Uses plain text output
```

### Port Mappings

The WildFly images publish the HTTP and management endpoints. The port mappings depend on the WildFly version and whether only a major version `mm` or an additional minor version `n` is specified:

- version omitted
	- 8080 → 8080
	- 9990 → 9990
- major version only
  - 8080 → 80`<mm>`
  - 9900 → 99`<mm>`
- major and minor version
  - 8080 → 8`<mm><n>`
  - 9900 → 9`<mm><n>`

So for WildFly 27, the port mappings are 8027 and 9927, whereas, for WildFly 26.1, the port mappings are 8261 and 9261.   

### `start-wildfly.sh [version] [<parameters>]`

Starts a WildFly standalone server for the specified version. Parameters are passed to the `standalone.sh` script of WildFly. Use `-d` or `--domain` to start WildFly in domain mode. The domain mode uses a modified default configuration using `auto-start=false` for all servers. Parameters are passed to the `domain.sh` script.  

Example:

```shell
start-wildfly.sh
start-wildfly.sh --domain 19
start-wildfly.sh 26.1 -c standalone-microprofile.xml
start-wildfly.sh 27 -c standalone-insecure.xml
```

### `stop-wildfly.sh [version]`

Stops the specified WildFly version. Use `-d` or `--domain` to stop a WildFly server running in domain mode. 

Example:

```shell
stop-wildfly.sh
stop-wildfly.sh --domain 19
stop-wildfly.sh 26.1
stop-wildfly.sh 27
```

### `cli-wildfly.sh [version] [<parameters>]`

Connects to the CLI of the specified WildFly version. Parameters are passed to the main class of `wildfly-cli-client.jar`. 

Example:

```shell
cli-wildfly.sh 26 --file=commands.txt
```

### `hal-wildfly.sh [version]`

Opens HAL in the default browser for the specified WildFly version.

### Remaining scripts

`build-wildfly.sh`, `push-wildfly.sh` and `bulk-*.sh` are used to build and push WildFly images. 
