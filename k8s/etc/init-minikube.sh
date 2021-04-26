#!/usr/bin/env zsh

_SCRIPT_PATH="$(cd ${0%/*} && pwd)"
cd $_SCRIPT_PATH

source .settings

echo "This script requires elevated privileges in order to configure files in /etc/resolver/"
sudo echo "elevated privileges enabled"

minikube start -p $_PROFILE --cpus=4 --memory=4g --driver=hyperkit --kubernetes-version=v1.19.10
minikube profile $_PROFILE

# enable ingress and dns in minikube
minikube addons enable ingress
minikube addons enable ingress-dns
sudo mkdir -p /etc/resolver/
cat <<EOF > tmp-resolver
domain grafana.$_PROFILE.example
nameserver $(minikube ip)
search_order 1
timeout 5
EOF
sudo mv tmp-resolver /etc/resolver/minikube-$(minikube profile)-grafana.$_PROFILE.example
cat <<EOF > tmp-resolver
domain prometheus.$_PROFILE.example
nameserver $(minikube ip)
search_order 1
timeout 5
EOF
sudo mv tmp-resolver /etc/resolver/minikube-$(minikube profile)-prometheus.$_PROFILE.example
cat <<EOF > tmp-resolver
domain kafka-0.kafka.$_PROFILE.example
nameserver $(minikube ip)
search_order 1
timeout 5
EOF
sudo mv tmp-resolver /etc/resolver/minikube-$(minikube profile)-kafka-0.kafka.$_PROFILE.example
cat <<EOF > tmp-resolver
domain mongodb.$_PROFILE.example
nameserver $(minikube ip)
search_order 1
timeout 5
EOF
sudo mv tmp-resolver /etc/resolver/minikube-$(minikube profile)-mongodb.$_PROFILE.example

# create the $_PROFILE namespace if it doesn't already exist
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: Namespace
metadata:
  name: $_PROFILE
  labels:
    name: local
EOF
kubectl config set-context minikube-$_PROFILE --namespace=$_PROFILE --cluster=$_PROFILE --user=$_PROFILE

# use the $_PROFILE namespace
kubectl config use-context minikube-$_PROFILE
