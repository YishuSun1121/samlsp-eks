# Endpoint


# Create SAML Identity Provider

```
curl -H "Content-Type: application/json" -X POST \
  -d '{"alias":"ssocircle","metadataSource":"HTTP","metadataUrl":"https://idp.ssocircle.com/meta-idp.xml"}' \
    http://localhost:8080/mgr/saml-identity-providers
    
curl -H "Content-Type: application/json" -X POST \
  -d '{"alias":"ssocircle","metadataSource":"HTTP","metadataUrl":"https://idp.ssocircle.com/meta-idp.xml"}' \
    https://sp.authright.com/mgr/saml-identity-providers    
```


```
curl -H "Content-Type: application/json" -X POST \
  -d '{"alias":"ssocircle","metadataSource":"XML","metadataXmlContent":"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\">"}' \
    http://localhost:8080/mgr/saml-identity-providers
```

# Query SAML Identity Provider

```
curl -X GET http://localhost:8080/mgr/saml-identity-providers

curl -X GET https://sp.authright.com/mgr/saml-identity-providers
```

# Delete SAML Identity Provider by ID

```
curl -X DELETE http://localhost:8080/mgr/saml-identity-providers/7805f448-84c7-4361-ba49-f5f6fc10f060

curl -X DELETE https://sp.authright.com/mgr/saml-identity-providers/7805f448-84c7-4361-ba49-f5f6fc10f060
```


# Docker

## build image

```shell
docker build --rm -t saml2sp .
docker tag saml2sp:latest 272778956077.dkr.ecr.us-east-1.amazonaws.com/sprepo:latest
aws ecr get-login-password --region us-east-1 --profile=profile1 | docker login --username AWS --password-stdin 272778956077.dkr.ecr.us-east-1.amazonaws.com
docker push 272778956077.dkr.ecr.us-east-1.amazonaws.com/sprepo:latest
```

## run

```shell
docker run --rm -e "PROFILE=ec2-nextjump" saml2sp
```

# EKS

1. update kubeconfig

```shell
#aws eks update-kubeconfig --region us-east-1 --name sp-eks --profile=profile1 --role-arn arn:aws:iam::272778956077:role/eksClusterRole
aws eks update-kubeconfig --region us-east-1 --name sp-eks --profile=profile1
```

2. get svc

```shell
kubectl get svc
```

```shell
kubectl apply -f saml2sp-deployment.yaml
kubectl apply -f saml2sp-service.yaml
kubectl get all -n saml2sp
kubectl -n saml2sp describe service saml2sp-service
kubectl exec -it saml2sp-deployment-87f8b7c4b-z4rl9 -n saml2sp -- /bin/sh


```