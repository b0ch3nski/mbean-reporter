# mbean-reporter

## build
```bash
gradle clean build
```

## run testapp
```bash
java \
-javaagent:agent/build/libs/agent-0.1.jar \
-cp testapp/build/libs/testapp-0.1-all.jar:api/build/libs/api-0.1.jar:impl/influxdb/build/libs/influxdb-0.1.jar \
-DrootLogLevel=DEBUG \
-Dreporter.dbName=jvm-metrics-test \
-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dcom.sun.management.jmxremote.ssl=false \
-Djava.rmi.server.hostname=127.0.0.1 \
-Dcom.sun.management.jmxremote.port=9999 \
-Dcom.sun.management.jmxremote.rmi.port=9999 \
com.github.b0ch3nski.reporter.testapp.Main
```
