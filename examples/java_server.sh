#!/bin/sh

javac -cp "../target/classes:../lib/msgpack-0.5.0-devel.jar:../lib/jedis-1.5.0.jar:/tmp/javassist.jar:/tmp/slf4j-api.jar:." JavaServer.java
java -cp "../target/classes:../lib/msgpack-0.5.0-devel.jar:../lib/jedis-1.5.0.jar:/tmp/javassist.jar:/tmp/slf4j-api.jar:."  JavaServer
