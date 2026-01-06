#!/bin/bash

eval $(minikube -p ebank docker-env)
export PATH=$(pwd)/tools/shims:$PATH
skaffold dev
