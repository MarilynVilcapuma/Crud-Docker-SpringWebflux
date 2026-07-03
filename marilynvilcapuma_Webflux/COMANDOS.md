# 🚀 Guía de comandos — Docker, Docker Compose y Kubernetes

Proyecto: **CRUD Spring WebFlux (Persona + Matrícula)**
Todos los comandos se ejecutan desde la carpeta del proyecto `marilynvilcapuma_Webflux`.

> 🔧 ADAPTAR para otro proyecto: reemplaza `marilynvilcapuma` por tu nombre,
> los puertos (9096/9097 compose, 8093/30093/8094 kubernetes) y los endpoints
> (`/v1/api/persona`, `/v1/api/matricula`) por los del nuevo proyecto.

---

## 1️⃣ Docker — construir y subir la imagen a Docker Hub

```powershell
# Construir la imagen (repetir solo si cambias código)
docker build -t marilynvilcapuma_webflux:latest .

# Etiquetar con tu usuario de Docker Hub
docker tag marilynvilcapuma_webflux:latest marilynvilcapuma/marilynvilcapuma_webflux:latest

# Subir a Docker Hub (requiere docker login la primera vez)
docker push marilynvilcapuma/marilynvilcapuma_webflux:latest
```

---

## 2️⃣ Docker Compose — probar el stack completo (app + mysql + nginx)

```powershell
docker compose up -d          # levanta los 3 contenedores en segundo plano
docker compose ps             # ver estado de los contenedores
docker compose logs -f app    # ver logs de la app (Ctrl+C para salir)
```

Probar en el navegador:

| Acceso | URL |
|---|---|
| Directo a la app | http://localhost:9096/v1/api/persona |
| Directo a la app (transaccional) | http://localhost:9096/v1/api/matricula |
| A través de nginx (proxy) | http://localhost:9097/v1/api/persona |

```powershell
docker compose down           # detener y eliminar contenedores + red
```

---

## 3️⃣ Kubernetes — desplegar (aplicar EN ESTE ORDEN)

```powershell
kubectl apply -f k8s/marilynvilcapuma-namespace.yml    # 1° namespace (espacio de trabajo)
kubectl apply -f k8s/marilynvilcapuma-secret.yml       # 2° secret (credenciales en base64)
kubectl apply -f k8s/marilynvilcapuma-deployment.yml   # 3° deployment (3 réplicas, puerto 8093)
kubectl apply -f k8s/marilynvilcapuma-service.yml      # 4° service (NodePort 80 → 30093)
```

> La imagen se descarga automáticamente desde Docker Hub
> (`marilynvilcapuma/marilynvilcapuma_webflux:latest`), por eso el push del paso 1️⃣ va primero.

---

## 4️⃣ Kubernetes — verificar el despliegue

```powershell
kubectl get all -n marilynvilcapuma                    # pods + deployment + service juntos
kubectl get pods -n marilynvilcapuma -w                # esperar a que los 3 pods estén Running (Ctrl+C)
kubectl get secret -n marilynvilcapuma                 # confirmar que el secret existe
kubectl describe deployment marilynvilcapuma-deployment -n marilynvilcapuma   # detalle del deployment
kubectl logs -l app=marilynvilcapuma -n marilynvilcapuma --tail=20            # logs de los pods
```

Probar por el **NodePort** en el navegador: http://localhost:30093/v1/api/persona

---

## 5️⃣ Port-forward — el puente local (rúbrica: puerto 8094)

```powershell
kubectl port-forward service/marilynvilcapuma-service 8094:80 -n marilynvilcapuma
```

Dejarlo corriendo y probar en el navegador (pantallazo de la rúbrica):

👉 **http://localhost:8094/v1/api/persona**

Detener con `Ctrl+C`.

Flujo del tráfico: `8094 (port-forward) → 80 (service) → 8093 (pod)` — el NodePort `30093` es el acceso alternativo directo.

---

## 6️⃣ Limpieza

```powershell
# Kubernetes: borra namespace + secret + deployment + service de una sola vez
kubectl delete namespace marilynvilcapuma

# Docker Compose: detiene y elimina contenedores + red
docker compose down

# Eliminar imágenes locales del proyecto (la de Docker Hub NO se borra)
docker rmi marilynvilcapuma_webflux:latest marilynvilcapuma/marilynvilcapuma_webflux:latest mysql:8.0 nginx:alpine
```

> ⚠️ NO eliminar las imágenes del sistema de Kubernetes de Docker Desktop:
> `kindest/node`, `docker/desktop-*`, `envoyproxy/envoy` — sin ellas el clúster deja de funcionar.
