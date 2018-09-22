# Black Rook Engine

Copyright (c) 2015-2018 Black Rook Software. All rights reserved.  
[http://blackrooksoftware.com/projects.htm?name=engine](http://blackrooksoftware.com/projects.htm?name=engine)  
[https://github.com/BlackRookSoftware/Engine](https://github.com/BlackRookSoftware/Engine)

### NOTICE

This library is currently in **EXPERIMENTAL** status. This library's API
may change many times in different ways over the course of its development!

### Required Libraries

Black Rook Commons 2.31.0+  
[https://github.com/BlackRookSoftware/Common](https://github.com/BlackRookSoftware/Common)

Black Rook Common I/O 2.5.0+  
[https://github.com/BlackRookSoftware/CommonIO](https://github.com/BlackRookSoftware/CommonIO)

Black Rook Common Lang 2.10.0+  
[https://github.com/BlackRookSoftware/CommonLang](https://github.com/BlackRookSoftware/CommonLang)

Black Rook Common ArcheText  
[https://github.com/BlackRookSoftware/ArcheText](https://github.com/BlackRookSoftware/ArcheText)

Black Rook Common FileSystem  
[https://github.com/BlackRookSoftware/FileSystem](https://github.com/BlackRookSoftware/FileSystem)

### Required Java Modules

[java.base](https://docs.oracle.com/javase/10/docs/api/java.base-summary.html)  

### Introduction

Black Rook Engine is a dependency injection system built around several roles for hooking into
a single program and managing device contexts, message passing, and update loops.

### Compiling with Ant

To download the dependencies for this project (if you didn't set that up yourself already), type:

	ant dependencies

A *build.properties* file will be created/appended to with the *dev.base* property set.
	
To compile this library with Apache Ant, type:

	ant compile

To make a JAR of this library, type:

	ant jar

And it will be placed in the *build/jar* directory.

### Other

This program and the accompanying materials
are made available under the terms of the GNU Lesser Public License v2.1
which accompanies this distribution, and is available at
http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html

A copy of the LGPL should have been included in this release (LICENSE.txt).
If it was not, please contact us for a copy, or to notify us of a distribution
that has not included it. 
