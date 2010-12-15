#!/bin/sh

javac -cp "../target/redpack-1.0.0.jar:." JavaClient.java
java -cp "../target/redpack-1.0.0.jar:." JavaClient
