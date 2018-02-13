# mbean-reporter
Lightweight JVM MBean metrics reporter that can be easily extended to support any database.

## dependencies
Only `org.slf4j:slf4j-api` must be available on classpath.

## modules

### mbean-reporter-agent
Java Agent based application that connects to JVM and loads `mbean-reporter-api` implementations using Service Provider
mechanism. This makes it very portable, as it can be attached to your already compiled and packaged app **without source
code modifications**.

As soon as agent is loaded (which is before `main()` method), it starts to periodically query local **MBean Server** for
metrics and sends them to database of your choice (each 10 seconds by default).

### mbean-reporter-api
Separated API package that provides necessary interface `MetricsDatabase` for databases implementations, `Measurement`
definition, configuration reader and simple HTTP client.

Configuration is created from environment variables and Java execution properties (override supported). Variable name
must start with `reporter` prefix followed by dot/underscore and configuration key name (all case insensitive), for
example:
* environment variable `REPORTER_NAME=test` will result in `name=test` configuration
* execution property `-Dreporter.dataBase=test` will result in `database=test` configuration

Configuration keys available by default in `mbean-reporter-agent`:
* `interval` - reporting period in seconds, default: 10
* `dbImpl` - fully qualified class name of `MetricsDatabase` implementation, default:
`com.github.b0ch3nski.reporter.persistence.InfluxDB`

### mbean-reporter-influxdb
Example implementation of `mbean-reporter-api` for InfluxDB. Following configuration is supported:
* `dbUrl` - URL of InfluxDB instance, default: `http://localhost:8086`
* `dbName` - Database name where metrics will be sent, default: `jvm-metrics`

**WARNING**: HTTPS and user authentication is not yet supported!

## building
Development builds:
```bash
gradle clean build
```

Release builds:
```bash
gradle clean build -Pbuild.type=release
```

## usage
```bash
java \
-javaagent:mbean-reporter-agent-0.2.jar \
-cp <<YOUR_CLASSPATH>>:mbean-reporter-api-0.2.jar:mbean-reporter-influxdb-0.2.jar \
your.package.your.main.class
```

### example
For testing purposes, one might try running `mbean-reporter-testapp` which is producing meaningless
`metrics.com.github.b0ch3nski.reporter.testapp.Main.test_test.more_tests` metric using `io.dropwizard.metrics`:
```bash
docker run -d -p 8086:8086 --name=influxdb influxdb:alpine; \
java \
-javaagent:agent/build/libs/mbean-reporter-agent-0.2.jar \
-cp testapp/build/libs/mbean-reporter-testapp-0.2.jar:api/build/libs/mbean-reporter-api-0.2.jar:impl/influxdb/build/libs/mbean-reporter-influxdb-0.2.jar \
-DrootLogLevel=DEBUG \
com.github.b0ch3nski.reporter.testapp.Main
```

After a while, dummy metric should be visible in **InfluxDB** alongside other standard JVM metrics, like
`java.lang.Memory.HeapMemoryUsage.used`:
```bash
curl -Gi "http://localhost:8086/query?pretty=true" --data-urlencode "db=jvm-metrics" --data-urlencode "q=SHOW MEASUREMENTS"
curl -Gi "http://localhost:8086/query?pretty=true" --data-urlencode "db=jvm-metrics" --data-urlencode "q=SELECT * FROM \"metrics.com.github.b0ch3nski.reporter.testapp.Main.test_test.more_tests.Count\""
```

Simple data visualization is possible using **Grafana**:
```bash
docker run -d -p 3000:3000 --link=influxdb -e INFLUXDB_HOST=influxdb --name=grafana appcelerator/grafana
```

For additional verification, one can enable remote JMX interface to compare results with live view using `JConsole` or
`VisualVM`:
```bash
-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dcom.sun.management.jmxremote.ssl=false \
-Djava.rmi.server.hostname=127.0.0.1 \
-Dcom.sun.management.jmxremote.port=9999 \
-Dcom.sun.management.jmxremote.rmi.port=9999
```
