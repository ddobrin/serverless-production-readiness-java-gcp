#!/bin/bash

# exit if a command returns a non-zero exit code and also print the commands and their args as they are executed.
set -e -x

# Download and install required tools.
npm install -g cdktf-cli

export GCS_BACKEND_BUCKET_NAME=${PROJECT_ID}-cdktf-state
gcloud storage buckets create gs://${GCS_BACKEND_BUCKET_NAME} 2>/dev/null || true 

case $CDKTF_TYPE in
  destroy)
      cdktf destroy application-dev --auto-approve
    ;;
  deploy)
      cdktf deploy application-dev --var='referenceImageName=reference-jit' --var='bffImageName=bff-jit' --var='faultyImageName=faulty-jit' --auto-approve
    ;;
  *)
      cdktf synth *
    ;;
esac



