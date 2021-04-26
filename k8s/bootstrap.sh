#!/usr/bin/env zsh

_SCRIPT_PATH="$(cd ${0%/*} && pwd)"
cd $_SCRIPT_PATH


source etc/.environment-settings

etc/init-minikube.sh

etc/init-service-dependencies.sh

etc/init-twitter-secret.sh

etc/init-prometheus.sh
