#!/usr/bin/env zsh

_SCRIPT_PATH="$(cd ${0%/*} && pwd)"
cd $_SCRIPT_PATH

kubectl apply -f manifest.yml