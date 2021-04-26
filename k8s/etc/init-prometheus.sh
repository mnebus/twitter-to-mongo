#!/usr/bin/env zsh

_SCRIPT_PATH="$(cd ${0%/*} && pwd)"
cd $_SCRIPT_PATH

source .environment-settings

#### add prometheus
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
cat <<EOF | helm upgrade --install prometheus prometheus-community/kube-prometheus-stack --values -
alertmanager:
  enabled: false
grafana:
  defaultDashboardsEnabled: false
  ingress:
    enabled: true
    hosts:
      - grafana.$_PROFILE.example
EOF

# add kafka-exporter
cat <<EOF | helm upgrade --install kafka-exporter prometheus-community/prometheus-kafka-exporter --values -
prometheus:
  serviceMonitor:
    enabled: true
    namespace: $_PROFILE
    additionalLabels:
      release: prometheus
kafkaServer:
  - kafka:9092
EOF

# add ingress for prometheus
cat <<EOF | kubectl apply -f -
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: prometheus-ingress
spec:
  rules:
    - host: prometheus.$_PROFILE.example
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: prometheus-kube-prometheus-prometheus
                port:
                  number: 9090
EOF

kubectl apply -f dashboards/grafana-dashboard.yml
kubectl apply -f dashboards/kubernetes-namespace-pods-dashboard.yml
kubectl apply -f dashboards/kubernetes-pods-dashboard.yml
