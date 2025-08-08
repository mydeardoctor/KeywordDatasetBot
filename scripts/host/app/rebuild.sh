#!/bin/bash

echo "Changing directory to project root."
cd ../../..

echo "Rebuilding project."
mvn clean verify

echo