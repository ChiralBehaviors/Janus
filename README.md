# Janus
Composite Object Framework for Java
___
Got sick and tired of seeing a bunch of really lame, or horrifically complicated composite object frameworks, so I built a lightweight and useful one.

## Build Status
![Build Status](https://github.com/hellblazer/Janus/actions/workflows/maven.yml/badge.svg)
___
Licensed under the Apache Licence version 2.0

Built with Mave 3.83+:

	mvn clean install

Java 20+

## Maven Artifacts
Currently, Janus is in active development and does not publish to maven central.  Rather, periodic snapshots (and releases when they happen)
will be uploaded to the [repo-hell]() repository.  If you would like to use Janus maven artifacts, you'll need to add the following repository
declarations to your pom.xml  The maven coordinates for individual artifacts are found below.
    
    <repositories>
        <repository>
            <id>hell-repo</id>
            <url>https://raw.githubusercontent.com/Hellblazer/repo-hell/main/mvn-artifact</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>

    <pluginRepositories>
        <pluginRepository>
            <id>plugin-hell-repo</id>
            <url>https://raw.githubusercontent.com/Hellblazer/repo-hell/main/mvn-artifact</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </pluginRepository>
    </pluginRepositories>

### Janus Runtime

     <dependency>
         <groupId>com.chiralbehaviors</groupId>
         <artifactId>janus</artifactId>
         <version>0.1.0-SNAPSHOT</version>
     </dependency>