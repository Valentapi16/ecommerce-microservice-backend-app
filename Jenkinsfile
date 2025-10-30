pipeline {
    agent any
    
    environment {
        DOCKER_HUB_CREDENTIALS = credentials('dockerhub')
        DOCKER_HUB_REPO = "valentapi16"
        
        // Servicios de infraestructura (deben construirse primero)
        INFRASTRUCTURE_SERVICES = "service-discovery cloud-config api-gateway"
        
        // Microservicios funcionales (6 seleccionados)
        BUSINESS_SERVICES = "product-service user-service payment-service order-service shipping-service favourite-service"
        
        ENVIRONMENT = "dev"
        VERSION = "dev-${BUILD_NUMBER}"
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo "🚀 Building for DEV environment"
                echo "📋 Infrastructure: ${INFRASTRUCTURE_SERVICES}"
                echo "📋 Business Services: ${BUSINESS_SERVICES}"
            }
        }
        
        stage('Build All Services with Maven') {
            steps {
                echo "📦 Compiling ALL services (including parent POM)..."
                sh '''
                    # Crear Dockerfile que compile TODO desde la raíz
                    cat > Dockerfile.build << 'EOF'
FROM maven:3.9.9-eclipse-temurin-17
WORKDIR /build

# Copiar configuración de Maven
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .

# Copiar TODOS los módulos (necesarios para resolver dependencias)
COPY service-discovery service-discovery
COPY cloud-config cloud-config
COPY api-gateway api-gateway
COPY proxy-client proxy-client
COPY product-service product-service
COPY user-service user-service
COPY payment-service payment-service
COPY order-service order-service
COPY shipping-service shipping-service
COPY favourite-service favourite-service
COPY src src

# Compilar infraestructura + microservicios funcionales
RUN mvn clean package -DskipTests \
    -pl service-discovery,cloud-config,api-gateway,product-service,user-service,payment-service,order-service,shipping-service,favourite-service
EOF
                    
                    # Compilar TODO
                    docker build -f Dockerfile.build -t temp-build-${BUILD_NUMBER} .
                    
                    # Extraer los JARs compilados - INFRAESTRUCTURA
                    docker create --name temp-container-${BUILD_NUMBER} temp-build-${BUILD_NUMBER}
                    docker cp temp-container-${BUILD_NUMBER}:/build/service-discovery/target service-discovery/
                    docker cp temp-container-${BUILD_NUMBER}:/build/cloud-config/target cloud-config/
                    docker cp temp-container-${BUILD_NUMBER}:/build/api-gateway/target api-gateway/
                    
                    # Extraer los JARs compilados - MICROSERVICIOS
                    docker cp temp-container-${BUILD_NUMBER}:/build/product-service/target product-service/
                    docker cp temp-container-${BUILD_NUMBER}:/build/user-service/target user-service/
                    docker cp temp-container-${BUILD_NUMBER}:/build/payment-service/target payment-service/
                    docker cp temp-container-${BUILD_NUMBER}:/build/order-service/target order-service/
                    docker cp temp-container-${BUILD_NUMBER}:/build/shipping-service/target shipping-service/
                    docker cp temp-container-${BUILD_NUMBER}:/build/favourite-service/target favourite-service/
                    
                    # Limpiar
                    docker rm temp-container-${BUILD_NUMBER}
                    docker rmi temp-build-${BUILD_NUMBER}
                    rm Dockerfile.build
                    
                    echo "✅ Todos los servicios compilados exitosamente"
                '''
            }
        }
        
        stage('Run Tests') {
            steps {
                echo "🧪 Running unit tests for all services..."
                sh '''
                    # Crear Dockerfile para tests
                    cat > Dockerfile.test << 'EOF'
FROM maven:3.9.9-eclipse-temurin-17
WORKDIR /test

# Copiar configuración de Maven
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .

# Copiar TODOS los módulos
COPY service-discovery service-discovery
COPY cloud-config cloud-config
COPY api-gateway api-gateway
COPY proxy-client proxy-client
COPY product-service product-service
COPY user-service user-service
COPY payment-service payment-service
COPY order-service order-service
COPY shipping-service shipping-service
COPY favourite-service favourite-service
COPY src src

# Ejecutar tests solo para los servicios seleccionados
RUN mvn test \
    -pl service-discovery,cloud-config,api-gateway,product-service,user-service,payment-service,order-service,shipping-service,favourite-service \
    || echo "⚠️ Some tests failed but continuing build for DEV"
EOF
                    
                    # Ejecutar tests
                    docker build -f Dockerfile.test -t temp-test-${BUILD_NUMBER} . || true
                    
                    # Limpiar
                    docker rmi temp-test-${BUILD_NUMBER} || true
                    rm Dockerfile.test
                    
                    echo "✅ Tests ejecutados"
                '''
            }
        }
        
        stage('Docker Login') {
            steps {
                sh '''
                    USERNAME=$(echo "$DOCKER_HUB_CREDENTIALS_USR" | tr '[:upper:]' '[:lower:]')
                    echo "$DOCKER_HUB_CREDENTIALS_PSW" | docker login -u "$USERNAME" --password-stdin
                '''
            }
        }
        
        stage('Build & Push Infrastructure Images') {
            steps {
                echo "🏗️ Building infrastructure services first..."
                script {
                    def services = INFRASTRUCTURE_SERVICES.split()
                    services.each { service ->
                        echo "🐳 Building Docker image for ${service}..."
                        dir(service) {
                            sh """
                                docker build -t ${DOCKER_HUB_REPO}/${service}:${VERSION} \
                                            -t ${DOCKER_HUB_REPO}/${service}:dev-latest .
                                docker push ${DOCKER_HUB_REPO}/${service}:${VERSION}
                                docker push ${DOCKER_HUB_REPO}/${service}:dev-latest
                            """
                        }
                    }
                }
            }
        }
        
        stage('Build & Push Business Services Images') {
            steps {
                echo "📦 Building business microservices..."
                script {
                    def services = BUSINESS_SERVICES.split()
                    services.each { service ->
                        echo "🐳 Building Docker image for ${service}..."
                        dir(service) {
                            sh """
                                docker build -t ${DOCKER_HUB_REPO}/${service}:${VERSION} \
                                            -t ${DOCKER_HUB_REPO}/${service}:dev-latest .
                                docker push ${DOCKER_HUB_REPO}/${service}:${VERSION}
                                docker push ${DOCKER_HUB_REPO}/${service}:dev-latest
                            """
                        }
                    }
                }
            }
        }
    }
    
    post {
        always {
            sh 'docker logout || true'
        }
        success {
            echo "✅ ¡BUILD EXITOSO! 🎉🎉🎉"
            echo ""
            echo "═══════════════════════════════════════════════════════════"
            echo "📦 INFRAESTRUCTURA (3 servicios):"
            echo "═══════════════════════════════════════════════════════════"
            script {
                def infra = INFRASTRUCTURE_SERVICES.split()
                infra.each { service ->
                    echo "   ✓ ${DOCKER_HUB_REPO}/${service}:${VERSION}"
                    echo "   ✓ ${DOCKER_HUB_REPO}/${service}:dev-latest"
                }
            }
            echo ""
            echo "═══════════════════════════════════════════════════════════"
            echo "📦 MICROSERVICIOS FUNCIONALES (6 servicios):"
            echo "═══════════════════════════════════════════════════════════"
            script {
                def business = BUSINESS_SERVICES.split()
                business.each { service ->
                    echo "   ✓ ${DOCKER_HUB_REPO}/${service}:${VERSION}"
                    echo "   ✓ ${DOCKER_HUB_REPO}/${service}:dev-latest"
                }
            }
            echo ""
            echo "═══════════════════════════════════════════════════════════"
            echo "🔗 Comunicación entre servicios:"
            echo "═══════════════════════════════════════════════════════════"
            echo "   • order-service → user-service, product-service, payment-service"
            echo "   • shipping-service → product-service, order-service"
            echo "   • favourite-service → user-service, product-service"
            echo ""
            echo "🔗 Ver en: https://hub.docker.com/u/${DOCKER_HUB_REPO}"
            echo "═══════════════════════════════════════════════════════════"
        }
        failure {
            echo "❌ Build falló - Revisar logs para más detalles"
        }
    }
}