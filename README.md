# Virtual Collection Registry

[Production instance](http://clarin.ids-mannheim.de/vcr/app/public)

## Documentation
This application is currently documented on the [CLARIN trac](https://trac.clarin.eu/wiki/VirtualCollectionRegistry).

## Development

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

## History

### Developers
* Oliver Schonefeld, original developer.
* Twan Goosen, contributor to the 1.0 release.
* Willem Elbers, current maintainer

### GitHub migration

In Januari 2016 the Virtual collection Registry was migrated from the svn repository into github. The scripts used for this migration are available in the `scripts` directory.