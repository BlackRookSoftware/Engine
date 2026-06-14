# Black Rook Engine

Copyright (c) 2016-2026 Black Rook Software.  
[https://github.com/BlackRookSoftware/Engine](https://github.com/BlackRookSoftware/Engine)

### NOTICE

This library is currently in **EXPERIMENTAL** status. This library's API
may change many times in different ways over the course of its development!

### Required Libraries

Black Rook FileSystem 1.0.0+  
[https://github.com/BlackRookSoftware/FileSystem](https://github.com/BlackRookSoftware/FileSystem)

### Required Java Modules

[java.base](https://docs.oracle.com/javase/10/docs/api/java.base-summary.html)

### Where to Get

* [Maven Central](https://central.sonatype.com/artifact/com.blackrooksoftware/engine)  
* [GitHub Releases](https://github.com/BlackRookSoftware/Engine/releases/latest)

### Introduction

Black Rook Engine is a dependency injection system built around several roles for hooking into
a single program and managing device contexts, message passing, and update loops.


### Why?

It's nice to have a quick thing to manage dependencies and lifecycle for larger, real-time programs.
This is it. 


### Compiling with Maven

To install/compile this library and make all artifacts with Apache Maven, type:

	mvn install

To compile this library, type:

	mvn compile

To make Maven-compatible JARs of this library, type:

	mvn jar:jar

To make Javadocs:

	mvn javadoc:javadoc

To run tests, type:

	mvn test

To generate a coverage report, type:

	mvn test jacoco:report

To clean up everything:

	mvn clean


### Other

This program and the accompanying materials
are made available under the terms of the GNU Lesser Public License v2.1
which accompanies this distribution, and is available at
http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html

A copy of the LGPL should have been included in this release (LICENSE.txt).
If it was not, please contact us for a copy, or to notify us of a distribution
that has not included it. 
