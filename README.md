# Virtual Collection Registry [![Build Status](https://travis-ci.org/clarin-eric/VirtualCollectionRegistry.svg?branch=milestone-1.2)](https://travis-ci.org/clarin-eric/VirtualCollectionRegistry)

Instances:

* [Production instance](https://collections.clarin.eu/)
* [Beta instance](https://beta-vcr.clarin.eu)

## Documentation
This application is currently documented on the [CLARIN trac](https://trac.clarin.eu/wiki/VirtualCollectionRegistry).

Information regarding development and deployment as well as licencing information can be found in the [documentation directory](doc).

## Modules

The modules are related as follows:
```
┌──────────┐  ┌──────────┐           ┌──────────────┐
│          │  │          │           │              │
│ Frontend │  │ REST API │           │ OAI Provider │
│          │  │          │           │              │
└─────┬────┘  └────┬─┬───┘           └────────┬─────┘
      │            │ │                        │
      │            │ │ ┌────────────────┐     │
      │            │ │ │                │     │
      │            │ └─┤ Serialization  │     │
      │            │   │                │     │
      │            │   └────────────────┘     │
      │            │                          │
      │            │ ┌──────┐                 │
      │            └─┤      │                 │
      │              │ CORE ├─────────────────┘
      └──────────────┤      │
                     └───┬──┘
                         │
                         │
                         │
                     ┌───┴───┐
                     │       │
                     │ Model │
                     │       │
                     └───────┘
```
Ref: https://asciiflow.com/#/

## Development

### Compilation

The project is build using maven.

#### JPA Criteria Metamodel
https://docs.jboss.org/hibernate/orm/5.6/topical/html_single/metamodelgen/MetamodelGenerator.html

#### Unit testing and coverage

Generate a coverage report by running the `verify` target:
```
mvn clean compile test verify
```
Open the report with `./open_coverage_report.bash`, which will open: `target/site/jacoco/index.html`.

### Servlet Container

#### Tomcat

Apache tomcat 8+ is the prefered servlet container. The mysl jdbc driver
must be provided in the tomcat libs directory.

### MySQL

A running MySQL server is required. By using docker this is trivial to run for development purposes.

#### Docker 

```
docker run -d \
    --name vcr-mysql \
    -e MYSQL_ROOT_PASSWORD=vcr-root \
    -e MYSQL_DATABASE=vcr \
    -e MYSQL_USER=vcr-user \
    -e MYSQL_PASSWORD=vcr-password \
    -p 3306:3306 
    mysql:latest
```

Update the tomcat `context.xml` with the information provided for the MYSQL_* environment variables and the ip address of the docker host.
The application uses the database according to the configuration in [persistence.xml](src/main/resources/META-INF/persistence.xml) (Java Persistence API).

## History

### Developers
* Oliver Schonefeld, original developer.
* Twan Goosen, contributor to the 1.0 release.
* Willem Elbers, current maintainer

### GitHub migration

In Januari 2016 the Virtual collection Registry was migrated from the svn repository into github. The scripts used for this migration are available in the `scripts` directory.
