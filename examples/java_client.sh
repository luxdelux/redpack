#!/bin/sh

# javac -cp "../target/redpack-1.0.0.jar:." JavaClient.java
# java -cp "../target/redpack-1.0.0.jar:." JavaClient

javac -cp "../target/classes:." JavaClient.java
java -cp "../target/classes:." JavaClient