#!/bin/bash

if [[ ( -z "${ARTIFACT_ID}" ) ]]; then
    echo "Error! Some of environment variables are not set!" >&2
    exit 1
fi

if [[ ! -d "../../../target/${ARTIFACT_ID}" ]]; then
    echo "Error! \"../../../target/${ARTIFACT_ID}\" does not exist!" >&2
    exit 1
fi

echo "Changing directory to target/${ARTIFACT_ID}"
cd "../../../target/${ARTIFACT_ID}"

echo "Searching for jars in ${PWD}"
JARS=($(find . -maxdepth 1 -type f -name  "*.jar"))
NUMBER_OF_JARS=${#JARS[@]}

if [[ "${NUMBER_OF_JARS}" -ne 1 ]]; then
    echo "Error! Expected one jar, but found ${NUMBER_OF_JARS}!" >&2
    exit 1
fi

JAR="${JARS[0]}"
echo "Found ${JAR}"

echo "Starting application."
java -jar "${JAR}"

echo