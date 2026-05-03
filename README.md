# Parks — GitOps-managed parks demo on OpenShift

Single Argo Application that deploys the whole stack into the `parks` namespace:

| Object | Type | Purpose |
|---|---|---|
| `parks` namespace | Namespace | Project boundary |
| `parksmap` SA + RoleBinding | RBAC | Lets the parksmap pod list Routes for backend discovery |
| `mongodb` | Secret | Credentials shared by VM bootstrap + backend pod |
| `mongodb-fedora-disk` | DataVolume | Pulls `quay.io/containerdisks/fedora:39` into a 30Gi PVC |
| `mongodb` | **VirtualMachine** | KubeVirt VM — cloud-init installs MongoDB 7, creates users, enables auth |
| `mongodb` | Service | Selects the VM via `kubevirt.io/domain=mongodb`, exposes 27017 |
| `nationalparks` | Deployment+Service+Route | Spring Boot REST API backed by Mongo |
| `parksmap` | Deployment+Service+Route | Vert.x map UI, auto-discovers backend Routes |

## Apply

```bash
# 1) patch the Application to point at your fork
sed -i '' \
  -e 's|<your-org>/<your-repo>|sansow/openshift-25|' \
  -e 's|<path>|parks/manifests|' \
  parks/argocd/parks-app.yaml

# 2) push to GitHub (after copying parks/ into the repo)
git add parks/
git commit -m "Parks demo (GitOps + VM-backed Mongo)"
git push

# 3) bootstrap on the cluster
oc apply -f parks/argocd/parks-app.yaml
oc get application parks -n openshift-gitops -w
```

ArgoCD reconciles in waves:

1. Namespace + Secret + DataVolume created
2. DataVolume imports Fedora 39 image (~2-3 min)
3. VM starts, cloud-init runs (~2 min more): installs MongoDB 7, creates users, enables auth
4. nationalparks pod starts; readiness probe fails until Mongo is up
5. parksmap pod starts; auto-discovers nationalparks via Route label

Total cold-start: about 5-7 min from `oc apply` to map showing pins.

## One-time cluster prereqs

```bash
# OpenShift Virtualization software emulation (only needed on AWS sandbox without metal nodes)
oc annotate hyperconverged kubevirt-hyperconverged -n openshift-cnv --overwrite \
  kubevirt.kubevirt.io/jsonpatch='[{"op":"add","path":"/spec/configuration/developerConfiguration","value":{"useEmulation":true}}]'

# Bounce virt-controller so the new config is applied
oc delete pods -n openshift-cnv -l kubevirt.io=virt-controller
```

## Verify

```bash
# Pod, VM, Route status
oc get vm,vmi,pods,svc,route -n parks

# Bootstrap marker inside the VM (proves cloud-init finished)
virtctl ssh fedora@mongodb -n parks -- cat /var/log/mongo-bootstrap.done

# End-to-end probe — pod talks to the VM Mongo through the Service
oc -n parks run mongo-probe --rm -it --image=mongo:7 --restart=Never -- \
  mongosh "mongodb://mongodb:mongodb@mongodb.parks.svc:27017/mongodb" \
  --eval "db.parks.countDocuments()"

# Seed the data and read it back through the API
URL=$(oc get route nationalparks -n parks -o jsonpath='{.spec.host}')
curl -ks https://$URL/ws/data/load           # POSTs the parks dataset
curl -ks https://$URL/ws/data/all | jq length

# Open the map
oc get route parksmap -n parks -o jsonpath='{.spec.host}{"\n"}'
```

## Demo moments this enables

1. **`oc get vm,deploy -n parks`** — VM and Deployments listed side by side
2. **`virtctl console mongodb -n parks`** — inside-the-VM shell that's still under K8s RBAC
3. **`oc adm drain $node`** while the map is being clicked — KubeVirt live-migrates the VM, app stays up
4. **`oc edit ksvc weather`** (Phase 3) — change traffic split between revisions, watch which revision answers
5. **One Argo Application reconciles all of it** — show the Argo UI tree

## Switching the database back to a container

If you ever want to A/B containers vs VM, the Service contract is identical — drop in a container Mongo with the same Service name `mongodb` and the same Secret. The nationalparks pod won't notice.
