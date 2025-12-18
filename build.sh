export VERSION=4.0.0

docker build -t djljuarez/keycloak-providers:$VERSION .
docker push djljuarez/keycloak-providers:$VERSION
