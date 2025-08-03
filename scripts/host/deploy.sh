#!/bin/bash

cd ../common
set -a && source .env && set +a

cd ../host
set -a && source .env && set +a
