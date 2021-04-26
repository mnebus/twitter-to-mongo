#!/usr/bin/env zsh

_SCRIPT_PATH="$(cd ${0%/*} && pwd)"
cd $_SCRIPT_PATH

source .environment-settings

#########################################################
# This script installs mongodb and kafka and
# exposes them externally
#########################################################

# TODO - mongo complains if you try to upgrade it without a root password
# maybe we need to skip it if it's already installed
helm repo add bitnami https://charts.bitnami.com/bitnami
cat <<EOF | helm upgrade --install mongo bitnami/mongodb --values -
service:
  type: NodePort
  nodePort: 32017
  nameOverride: mongo-mongodb
auth:
  rootPassword: letMeIn!
EOF

cat <<EOF | helm upgrade --install kafka bitnami/kafka --values -
externalAccess:
  enabled: true
  service:
    type: NodePort
    nodePorts:
      - 30094
    annotations:
      external-dns.alpha.kubernetes.io/hostname: "{{ .targetPod }}.kafka.$_PROFILE.example"
    domain: "kafka-0.kafka.$_PROFILE.example"
serviceAccount:
  create: true
rbac:
  create: true
listeners:
  - INTERNAL://:9093
  - CLIENT://:9092
  - EXTERNAL://:9094
extraEnvVars:
  - name: KAFKA_CFG_ADVERTISED_LISTENERS
    value: INTERNAL://\$(MY_POD_NAME).kafka-headless.$_PROFILE.svc.cluster.local:9093,CLIENT://\$(MY_POD_NAME).kafka-headless.$_PROFILE.svc.cluster.local:9092,EXTERNAL://\$(MY_POD_NAME).kafka.$_PROFILE.example:30094
EOF

ingressStatus=$(kubectl get deployments -A -ojson -o 'jsonpath={.items[?(@.metadata.name == "ingress-nginx-controller")].status.readyReplicas}')
while [ -z $ingressStatus ] || [ $ingressStatus -lt 1 ]
do
  echo "Waiting for ingress controller to become ready"
  sleep 1
  ingressStatus=$(kubectl get deployments -A -ojson -o 'jsonpath={.items[?(@.metadata.name == "ingress-nginx-controller")].status.readyReplicas}')
done

## The below Ingress configs both go to dead ports.
## We need them so the minikube ingress-dns plugin will resolve the names
## to the node IP so external mongo clients can connect on port 32017
## and the kafka clients on 30094
cat <<EOF | kubectl apply --validate=false -f -
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: mongodb-ingress
spec:
  rules:
    - host: mongodb.$_PROFILE.example
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: mongo-mongodb
                port:
                  number: 8080
EOF

cat <<EOF | kubectl apply --validate=false -f -
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: kafka-ingress
spec:
  rules:
    - host: kafka-0.kafka.$_PROFILE.example
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: kafka
                port:
                  number: 8080
EOF
