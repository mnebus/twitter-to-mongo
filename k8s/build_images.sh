#!/usr/bin/env zsh

_SCRIPT_PATH="$(cd ${0%/*} && pwd)"
cd $_SCRIPT_PATH

source etc/.environment-settings
eval $(minikube -p $_PROFILE docker-env)
cd $_SCRIPT_PATH/.. && ./mvnw clean package -Dpackaging=docker

