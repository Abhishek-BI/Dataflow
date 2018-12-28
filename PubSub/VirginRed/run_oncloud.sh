#!/bin/bash

PROJECT=virgin-red-test2
BUCKET=virgin-red-test2/ETL
MAIN=com.google.main.main

echo "project=$PROJECT  bucket=$BUCKET  main=$MAIN"

mvn compile -e exec:java \
 -Dexec.mainClass=$MAIN \
      -Dexec.args="--project=$PROJECT \
      --stagingLocation=gs://$BUCKET/staging/ \
      --tempLocation=gs://$BUCKET/tmp/ \
      --dataflowJobFile=gs://$BUCKET/templates/FulldataCSV \
      --runner=TemplatingDataflowPipelineRunner"
