#!/bin/sh

# javac -cp "../target/redpack-1.0.0.jar:." JavaServer.java
# java -cp "../target/redpack-1.0.0.jar:." JavaServer

javac -cp "../target/classes:." JavaServer.java
java -cp "../target/classes:." JavaServer