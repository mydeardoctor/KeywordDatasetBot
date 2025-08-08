#!/bin/bash

echo "Changing directory to \"target\""
cd ./target

echo "Searching for .zip archives in ${PWD}"
ZIP_ARCHIVES=($(find . -maxdepth 1 -type f -name  "*.zip"))
NUMBER_OF_ZIP_ARCHIVES=${#ZIP_ARCHIVES[@]}

if [[ "${NUMBER_OF_ZIP_ARCHIVES}" -ne 1 ]]; then
    echo "Error! Expected one zip archive," \
         "but found ${NUMBER_OF_ZIP_ARCHIVES}!" >&2
    exit 1
fi

ZIP_ARCHIVE="${ZIP_ARCHIVES[0]}"
echo "Found ${ZIP_ARCHIVE}"

echo "Extracting ${ZIP_ARCHIVE}"
unzip -o "${ZIP_ARCHIVE}" -d .

echo