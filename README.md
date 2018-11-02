# FunStart4j
A Java Webstart Alternative technology with support for JNLPs.

Since Java WebStart is being deprecated (https://stackoverflow.com/questions/46904795/java-web-start-support-in-java-9-and-beyond) FunStart4j is intended to work with your existing JNLPs to run your WebStart app just by downloading a jar and pointing it at your JNLP file.

### Building

Run

```
./gradlew build
```

This will produce a build/libs/FunStart4j.jar.
Note:  This is a fat-jar with all dependencies included, so it can be moved and used from any location.


### Running a Webstart Application
To run a Webstart Application, pass in the URI of the JNLP as the last argument:

For example:

```
java -jar build/libs/FunStart4j.jar https://worldwind.arc.nasa.gov/java/latest/webstart/AirspaceBuilder.jnlp
```

### To pass properties and JVM configuration to the Webstart Application
Prepend properties and JVM configuration with -J.  For example:

For example this will set the favorite.day property, the favorite.car property, the Maximum Heap size to 1024 megabytes, and print out garbage collection details for the Webstart Application:

```
java -jar build/libs/FunStart4j.jar -J-Dfavorite.day=Saturday -J-Dfavorite.car="Nissan 350Z" -J-Xmx1024m -J-XX:+PrintGCDetails https://worldwind.arc.nasa.gov/java/latest/webstart/AirspaceBuilder.jnlp
```
