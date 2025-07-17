export VERSION=1.0.7

podman build -t djljuarez/keycloak-providers:$VERSION .
podman push djljuarez/keycloak-providers:$VERSION
