# mbean-reporter-testapp

## build
```bash
mvn clean package
```

## run
```bash
java \
-javaagent:../target/mbean-reporter-0.1.jar \
-jar target/mbean-reporter-testapp-0.1-SNAPSHOT-shaded.jar \
-DrootLogLevel=DEBUG \
-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dcom.sun.management.jmxremote.ssl=false \
-Djava.rmi.server.hostname=127.0.0.1 \
-Dcom.sun.management.jmxremote.port=9999 \
-Dcom.sun.management.jmxremote.rmi.port=9999
```
