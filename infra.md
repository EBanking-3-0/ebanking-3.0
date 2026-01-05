# Minikube

## if you have smth in you minikube
If you have something running in your minikube, you can use profiles to work separately on different projects.
Now we will create a new profile called `ebank`.
And the default profile will remain untouched. But keep in mind that the default profile will not consume resources while you are working in the `ebank` profile. Good for your poor laptop :)

```bash
minikube start -p ebank
minikube profile ebank
minikube status
```

## if you have a fresh minikube
If you have a fresh minikube, you can just start it.

```bash
minikube start
minikube status
```
