# info.shillem
__WARNING__: _At the moment I consider this project mainly a work in progress. Classes and methods have often been subject to change and therefore things could break if you decide to make use of this project in yours._

This project is made up of a series of OSGi plugins that should make things easier when developing Java-based projects.\
What follows is a list of such plugins accompanies by a brief description of their potential use.

## info.shillem.util
It provides a few utility classes to deal with strings, numbers, lists etc.\
However it mostly provides a framework for implementing DTO and DAO patterns.

### Dependencies
- `com.fasterxml.jackson`

## info.shillem.domino
It's designed having the Domino enviroment in mind and it expands on DTO and DAO patters provided by `info.shillem.util` package.\
The goal is to:

* provide CRUD operations while leveraging the common DTO language implemented
* relegate all `lotus.domino` references and calls to the lowest level in the layer stack
* provide a better management when it comes to using `lotus.domino` classes _without thinking about recycling for the most part_
* keep performances as close as possible to the original `lotus.domino` APIs.

### Dependencies
- `com.fasterxml.jackson`
- `info.shillem.util`

## info.shillem.sql
It's designed having the SQL enviroment in mind and it expands on DTO and DAO patters provided by `info.shillem.util` package.\
The goal is to:

* provide ~~C~~ R ~~UD~~ operations while leveraging the common DTO language implemented
* manage SQL connections as sufficiently efficiently as possible

### Dependencies
- `org.apache.commons.dbcp2`
- `org.apache.commons.logging`
- `org.apache.commons.pool2`
- `info.shillem.util`

## info.shillem.rest
It's designed having the REST enviroment in mind and it expands on DTO and DAO patters provided by `info.shillem.util` package.\
This package is currently a work in progress.

### Dependencies
- `com.fasterxml.jackson`
- `org.apache.httpclient`
- `org.apache.httpcore`
- `info.shillem.util`

## XSP Environment
See [XSP README.md](osgi/http/README.md)

## DOTS Environment
See [DOTS README.md](osgi/dots/README.md)