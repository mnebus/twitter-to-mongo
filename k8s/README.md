# How to use/deploy to kubernetes locally
## Requirements
 * minikube
   * `minikube start --memory 9216 --cpus 5 --disk-size 50g --driver hyperkit --kubernetes-version v1.17.17`
 * helm 3

### Run the bootstrap script
You should only need to do this one time prior to the first time you run the application.
After you have started a minikube instance with the `minikube start` command, run the following:
```shell
./bootstrap.sh
```
This will:
 * create a new context and namespace in minikube
 * install kafka and mongodb
 * generate the twitter secret
> It might also be handy to create yourself an alias for using this context like this:
> `alias kcl-twitter2mongo='kubectl config use-context minikube-twitter2mongo'`

### Run the application
1. Build the required docker images in minikube's docker cache
```shell
./build_images.sh
```
2. Deploy/run the application
```shell
./run.sh
```
3. Stop the application
```shell
./stop.sh
```
4. Completely tear down the entire installation
```shell
./nuke.sh
```
> You will need to run `./bootstrap.sh` again after the nuke

### Connect to mongo running in k8s
Get the mongo root password
```shell
export MONGODB_ROOT_PASSWORD=$(kubectl get secret --namespace twitter2mongo mongo-mongodb -o jsonpath="{.data.mongodb-root-password}" | base64 --decode)
```

Port forward:
```shell
kubectl port-forward --namespace twitter2mongo svc/mongo-mongodb 27017:27017
```

Use uri: `mongodb://root:${MONGODB_ROOT_PASSWORD}@127.0.0.1:27017/twitter?authSource=admin&retryWrites=true&w=majority`

### TODO - update this with info about the dns ingress names (no port-forward required) 


