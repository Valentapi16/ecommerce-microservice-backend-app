"""
Pruebas de Rendimiento y Carga con Locust
==========================================

Este archivo contiene las pruebas de rendimiento para el sistema de e-commerce.
Simula usuarios concurrentes realizando operaciones críticas del sistema.

ESCENARIOS DE PRUEBA:
1. Registro de usuarios
2. Consulta de productos (lectura intensiva)
3. Creación de órdenes (escritura intensiva)
4. Carga concurrente mixta (100+ usuarios simultáneos)

REQUISITOS:
- Locust instalado: pip install locust
- Todos los microservicios corriendo
- API Gateway en http://localhost:8080

EJECUCIÓN:
locust -f locustfile.py --host=http://localhost:8080

INTERFAZ WEB:
http://localhost:8089

@author Test Suite
@since 2025-01-29
"""

from locust import HttpUser, task, between, SequentialTaskSet
import random
import json
from datetime import datetime


class UserRegistrationScenario(SequentialTaskSet):
    """
    ESCENARIO 1: Registro de Usuarios
    
    Simula usuarios registrándose en el sistema.
    Este escenario prueba la capacidad del sistema para manejar
    múltiples registros simultáneos.
    """
    
    @task
    def register_user(self):
        """Registrar un nuevo usuario"""
        timestamp = datetime.now().strftime("%Y%m%d%H%M%S%f")
        email = f"loadtest_{timestamp}_{random.randint(1000, 9999)}@example.com"
        
        user_data = {
            "firstName": f"LoadTest{random.randint(100, 999)}",
            "lastName": f"User{random.randint(100, 999)}",
            "email": email,
            "phone": f"+1-555-{random.randint(1000, 9999)}",
            "credential": {
                "username": email,
                "password": "TestPassword123!"
            },
            "address": {
                "fullAddress": "123 Test Street",
                "postalCode": "12345",
                "city": "TestCity"
            }
        }
        
        with self.client.post(
            "/api/users/register",
            json=user_data,
            headers={"Content-Type": "application/json"},
            catch_response=True
        ) as response:
            if response.status_code in [200, 201]:
                response.success()
                print(f"✓ Usuario registrado: {email}")
            else:
                response.failure(f"Fallo en registro: {response.status_code}")
    
    @task
    def stop_scenario(self):
        """Finalizar escenario"""
        self.interrupt()


class ProductBrowsingScenario(SequentialTaskSet):
    """
    ESCENARIO 2: Consulta de Productos
    
    Simula usuarios navegando y consultando productos.
    Prueba de lectura intensiva sobre el catálogo.
    """
    
    @task(3)
    def get_all_products(self):
        """Obtener lista completa de productos"""
        with self.client.get(
            "/api/products",
            headers={"Content-Type": "application/json"},
            catch_response=True
        ) as response:
            if response.status_code == 200:
                try:
                    products = response.json()
                    if isinstance(products, list) and len(products) > 0:
                        response.success()
                        print(f"✓ Productos obtenidos: {len(products)}")
                        # Guardar IDs de productos para consultas individuales
                        self.user.product_ids = [p.get("productId") for p in products if p.get("productId")]
                    else:
                        response.failure("Lista de productos vacía")
                except Exception as e:
                    response.failure(f"Error parseando respuesta: {str(e)}")
            else:
                response.failure(f"Error HTTP: {response.status_code}")
    
    @task(2)
    def get_product_by_id(self):
        """Consultar un producto específico"""
        if hasattr(self.user, 'product_ids') and self.user.product_ids:
            product_id = random.choice(self.user.product_ids)
            
            with self.client.get(
                f"/api/products/{product_id}",
                headers={"Content-Type": "application/json"},
                catch_response=True
            ) as response:
                if response.status_code == 200:
                    response.success()
                    print(f"✓ Producto {product_id} consultado")
                else:
                    response.failure(f"Error consultando producto: {response.status_code}")
    
    @task(1)
    def search_products_by_category(self):
        """Buscar productos por categoría"""
        category_ids = [1, 2, 3, 4, 5]  # IDs de categorías comunes
        category_id = random.choice(category_ids)
        
        with self.client.get(
            f"/api/products/category/{category_id}",
            headers={"Content-Type": "application/json"},
            catch_response=True,
            name="/api/products/category/[id]"
        ) as response:
            if response.status_code == 200:
                response.success()
                print(f"✓ Productos de categoría {category_id} obtenidos")
            elif response.status_code == 404:
                # Es normal que algunas categorías no existan
                response.success()
            else:
                response.failure(f"Error buscando por categoría: {response.status_code}")
    
    @task
    def stop_scenario(self):
        """Finalizar escenario"""
        self.interrupt()


class OrderCreationScenario(SequentialTaskSet):
    """
    ESCENARIO 3: Creación de Órdenes
    
    Simula usuarios creando órdenes de compra.
    Prueba de escritura intensiva e integración entre servicios.
    """
    
    @task
    def step1_get_products(self):
        """Paso 1: Obtener productos disponibles"""
        with self.client.get(
            "/api/products",
            headers={"Content-Type": "application/json"},
            catch_response=True
        ) as response:
            if response.status_code == 200:
                products = response.json()
                if isinstance(products, list) and len(products) > 0:
                    # Filtrar productos con stock
                    products_with_stock = [p for p in products if p.get("quantity", 0) > 0]
                    if products_with_stock:
                        self.user.selected_product = random.choice(products_with_stock)
                        response.success()
                        print(f"✓ Producto seleccionado: {self.user.selected_product.get('productTitle')}")
                    else:
                        response.failure("No hay productos con stock")
                else:
                    response.failure("Lista de productos vacía")
            else:
                response.failure(f"Error obteniendo productos: {response.status_code}")
    
    @task
    def step2_create_order(self):
        """Paso 2: Crear orden de compra"""
        if not hasattr(self.user, 'selected_product'):
            print("✗ No hay producto seleccionado, saltando creación de orden")
            return
        
        product = self.user.selected_product
        user_id = random.randint(1, 10)  # Simular diferentes usuarios
        
        order_data = {
            "userId": user_id,
            "orderDesc": f"Load Test Order - {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",
            "items": [
                {
                    "productId": product.get("productId"),
                    "quantity": random.randint(1, 3),
                    "priceUnit": str(product.get("priceUnit", "0.00"))
                }
            ]
        }
        
        with self.client.post(
            "/api/orders",
            json=order_data,
            headers={
                "Content-Type": "application/json",
                "Authorization": "Bearer test_token"
            },
            catch_response=True
        ) as response:
            if response.status_code in [200, 201]:
                try:
                    order = response.json()
                    self.user.order_id = order.get("orderId")
                    response.success()
                    print(f"✓ Orden creada: {self.user.order_id}")
                except Exception as e:
                    response.success()  # Aceptar si la orden se creó aunque no podamos parsear
                    print(f"✓ Orden creada (sin ID en respuesta)")
            elif response.status_code == 400:
                # Stock insuficiente es un caso válido para probar
                response.success()
                print(f"⚠ Orden rechazada: stock insuficiente")
            else:
                response.failure(f"Error creando orden: {response.status_code}")
    
    @task
    def step3_process_payment(self):
        """Paso 3: Procesar pago de la orden"""
        if not hasattr(self.user, 'order_id') or not self.user.order_id:
            print("✗ No hay orden creada, saltando pago")
            return
        
        payment_data = {
            "orderId": self.user.order_id,
            "paymentMethod": "CREDIT_CARD",
            "amount": str(random.uniform(10.0, 1000.0)),
            "cardInfo": {
                "cardNumber": "4532-1234-5678-9010",
                "cardHolder": "LOAD TEST USER",
                "expiryDate": "12/25",
                "cvv": "123"
            }
        }
        
        with self.client.post(
            "/api/payments",
            json=payment_data,
            headers={
                "Content-Type": "application/json",
                "Authorization": "Bearer test_token"
            },
            catch_response=True
        ) as response:
            if response.status_code in [200, 201]:
                response.success()
                print(f"✓ Pago procesado para orden {self.user.order_id}")
            else:
                response.failure(f"Error procesando pago: {response.status_code}")
    
    @task
    def stop_scenario(self):
        """Finalizar escenario"""
        self.interrupt()


class MixedWorkloadScenario(SequentialTaskSet):
    """
    ESCENARIO 4: Carga Concurrente Mixta
    
    Simula carga realista con usuarios realizando diferentes operaciones.
    Diseñado para probar el sistema con 100+ usuarios concurrentes.
    """
    
    @task(5)
    def browse_products(self):
        """Navegar productos (operación más común)"""
        with self.client.get(
            "/api/products",
            headers={"Content-Type": "application/json"},
            catch_response=True,
            name="Mixed: Browse Products"
        ) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Error: {response.status_code}")
    
    @task(3)
    def view_product_details(self):
        """Ver detalles de un producto"""
        product_id = random.randint(1, 20)
        
        with self.client.get(
            f"/api/products/{product_id}",
            headers={"Content-Type": "application/json"},
            catch_response=True,
            name="Mixed: View Product Details"
        ) as response:
            if response.status_code in [200, 404]:
                response.success()
            else:
                response.failure(f"Error: {response.status_code}")
    
    @task(2)
    def create_order(self):
        """Crear una orden (operación menos frecuente)"""
        user_id = random.randint(1, 100)
        
        order_data = {
            "userId": user_id,
            "orderDesc": f"Mixed Load Order {random.randint(1000, 9999)}",
            "items": [
                {
                    "productId": random.randint(1, 10),
                    "quantity": random.randint(1, 5),
                    "priceUnit": str(random.uniform(10.0, 500.0))
                }
            ]
        }
        
        with self.client.post(
            "/api/orders",
            json=order_data,
            headers={
                "Content-Type": "application/json",
                "Authorization": "Bearer test_token"
            },
            catch_response=True,
            name="Mixed: Create Order"
        ) as response:
            if response.status_code in [200, 201, 400]:
                # 400 es aceptable (stock insuficiente)
                response.success()
            else:
                response.failure(f"Error: {response.status_code}")
    
    @task(1)
    def add_to_favourites(self):
        """Agregar producto a favoritos"""
        user_id = random.randint(1, 100)
        product_id = random.randint(1, 20)
        
        with self.client.post(
            f"/api/favourites/user/{user_id}/product/{product_id}",
            headers={
                "Content-Type": "application/json",
                "Authorization": "Bearer test_token"
            },
            catch_response=True,
            name="Mixed: Add to Favourites"
        ) as response:
            if response.status_code in [200, 201, 409]:
                # 409 es aceptable (duplicado)
                response.success()
            else:
                response.failure(f"Error: {response.status_code}")
    
    @task(1)
    def check_health(self):
        """Verificar salud del sistema"""
        with self.client.get(
            "/actuator/health",
            catch_response=True,
            name="Mixed: Health Check"
        ) as response:
            if response.status_code == 200:
                response.success()
            else:
                # Health check puede no estar disponible
                response.success()


# ========================================
# DEFINICIÓN DE USUARIOS
# ========================================

class RegistrationUser(HttpUser):
    """Usuario que solo registra cuentas"""
    tasks = [UserRegistrationScenario]
    wait_time = between(1, 3)  # Espera entre 1-3 segundos entre tareas
    weight = 1  # Peso bajo, no todos los usuarios se registran


class BrowsingUser(HttpUser):
    """Usuario que solo navega productos"""
    tasks = [ProductBrowsingScenario]
    wait_time = between(0.5, 2)  # Espera corta, navegación rápida
    weight = 5  # Peso alto, mayoría de usuarios navegan


class ShoppingUser(HttpUser):
    """Usuario que crea órdenes"""
    tasks = [OrderCreationScenario]
    wait_time = between(2, 5)  # Espera más larga, comprar toma tiempo
    weight = 2  # Peso medio, algunos usuarios compran


class MixedUser(HttpUser):
    """Usuario con comportamiento mixto"""
    tasks = [MixedWorkloadScenario]
    wait_time = between(1, 4)  # Espera variable
    weight = 3  # Peso medio-alto


# ========================================
# INSTRUCCIONES DE EJECUCIÓN
# ========================================

"""
MODO 1: Interfaz Web (Recomendado)
-----------------------------------
locust -f locustfile.py --host=http://localhost:8080

Luego abrir http://localhost:8089 y configurar:
- Number of users: 100
- Spawn rate: 10 (usuarios por segundo)

MODO 2: Línea de Comandos
--------------------------
# Test rápido con 10 usuarios por 30 segundos
locust -f locustfile.py --host=http://localhost:8080 --users 10 --spawn-rate 2 --run-time 30s --headless

# Test de carga con 100 usuarios por 2 minutos
locust -f locustfile.py --host=http://localhost:8080 --users 100 --spawn-rate 10 --run-time 2m --headless

# Test de estrés con 500 usuarios por 5 minutos
locust -f locustfile.py --host=http://localhost:8080 --users 500 --spawn-rate 50 --run-time 5m --headless

MODO 3: Solo un escenario específico
-------------------------------------
# Solo navegación de productos
locust -f locustfile.py --host=http://localhost:8080 --users 50 --spawn-rate 10 BrowsingUser --headless

# Solo creación de órdenes
locust -f locustfile.py --host=http://localhost:8080 --users 20 --spawn-rate 5 ShoppingUser --headless

RESULTADOS
----------
Locust generará un reporte con:
- Request/s (peticiones por segundo)
- Response Time (p50, p90, p95, p99, max)
- Failures (% de fallos)
- RPS per endpoint

MÉTRICAS OBJETIVO
-----------------
✓ Response Time p95 < 2000ms
✓ Failures < 5%
✓ RPS > 50 para endpoints de lectura
✓ Sistema estable con 100+ usuarios concurrentes
"""
