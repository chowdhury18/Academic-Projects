#!/bin/bash

PROJECT_NAME="mcc-fall-2018-g10"
CURRENT_DIR=$(pwd)
RED='\033[0;31m'
NC='\033[0m' # No Color
GREEN='\e[32m'
YELLOW='\e[33m'

deploy_all_backend()
{
  init_firebase_cloud_functions
  pushd $CURRENT_DIR/backend
  firebase login
  firebase deploy --project $PROJECT_NAME
  popd
}

init_firebase_cloud_functions()
{
  echo "Deploying firebase cloud functions"
  pushd $CURRENT_DIR/backend/functions
  npm install
  popd
}

deploy_android_application()
{
  init
  echo -e "${YELLOW}Make sure your handphone is connected via USB. We will build and install it.${NC}"
  sleep 1
  echo "Copying the google-services.json"
  cp $CURRENT_DIR/google-services.json $CURRENT_DIR/frontend/ChatApp/app/
  echo "Building APK..."
  build_android_debug
  echo -e "${GREEN}result apk location: $CURRENT_DIR/frontend/ChatApp/app/build/outputs/apk/debug/app-debug.apk${NC}"
  sleep 1
  echo "Deploying APK to the device..."
  install_debug_apk
}

build_android_debug()
{
  pushd $CURRENT_DIR/frontend/ChatApp/
  ./gradlew assembleDebug
  popd
}

install_debug_apk()
{
  adb install $CURRENT_DIR/frontend/ChatApp/app/build/outputs/apk/debug/app-debug.apk
}

help()
{
  echo "deploy script help"
  echo "------------------"
  echo "To deploy ONLY backend                  : ./deploy.sh backend"
  echo "To deploy ONLY Android                  : ./deploy.sh android"
  echo "To deploy BOTH backend and android      : ./deploy.sh all"
}

init()
{
  echo "Checking requirements..."
  if [ -z "${ANDROID_HOME}" ]
  then
    echo -e "${RED}Android SDK is not found in environment variable ANDROID_HOME${NC}"
    exit 0
  fi
  echo "Checking requirements finished"
}

## Main script start here
echo "Build and Deploy Script: Group 10"
what_to_deploy=$1
if [ "$what_to_deploy" = "backend" ]
then
  deploy_all_backend
elif [ "$what_to_deploy" = "android" ]
then
  deploy_android_application
  echo "android"
elif [ "$what_to_deploy" = "all" ]
then
  deploy_all_backend
  deploy_android_application
else
  help
fi
