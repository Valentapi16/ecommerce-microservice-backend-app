pipeline {
    agent any
    
    environment {
        DOCKER_HUB_CREDENTIALS = credentials('dockerhub')
        DOCKER_HUB_REPO = "valentapi16"
        SERVICES = "product-service order-service payment-service user-service shipping-service favourite-service"
        ENVIRONMENT = "dev"
        VERSION = "dev-${BUILD_NUMBER}"
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo "🚀 Building for DEV environment"
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
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .
COPY product-service product-service
COPY order-service order-service
COPY payment-service payment-service
COPY user-service user-service
COPY shipping-service shipping-service
COPY favourite-service favourite-service
COPY service-discovery service-discovery
COPY cloud-config cloud-config
COPY api-gateway api-gateway
COPY proxy-client proxy-client
COPY src src
RUN mvn clean package -DskipTests -pl product-service,order-service,payment-service,user-service,shipping-service,favourite-service
EOF
                    
                    # Compilar TODO
                    docker build -f Dockerfile.build -t temp-build-${BUILD_NUMBER} .
                    
                    # Extraer los JARs compilados de cada servicio
                    docker create --name temp-container-${BUILD_NUMBER} temp-build-${BUILD_NUMBER}
                    docker cp temp-container-${BUILD_NUMBER}:/build/product-service/target product-service/
                    docker cp temp-container-${BUILD_NUMBER}:/build/order-service/target order-service/
                    docker cp temp-container-${BUILD_NUMBER}:/build/payment-service/target payment-service/
                    docker cp temp-container-${BUILD_NUMBER}:/build/user-service/target user-service/
                    docker cp temp-container-${BUILD_NUMBER}:/build/shipping-service/target shipping-service/
                    docker cp temp-container-${BUILD_NUMBER}:/build/favourite-service/target favourite-service/
                    
                    # Limpiar
                    docker rm temp-container-${BUILD_NUMBER}
                    docker rmi temp-build-${BUILD_NUMBER}
                    rm Dockerfile.build
                    
                    echo "✅ Todos los servicios compilados exitosamente"
                    ls -la product-service/target/*.jar
                    ls -la order-service/target/*.jar
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
        
        stage('Build & Push Docker Images') {
            steps {
                script {
                    def services = SERVICES.split()
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
            echo "✅ ¡ÉXITO TOTAL! 🎉🎉🎉"
            echo ""
            echo "📦 Imágenes subidas a Docker Hub:"
            script {
                def services = SERVICES.split()
                services.each { service ->
                    echo "   ✓ ${DOCKER_HUB_REPO}/${service}:${VERSION}"
                    echo "   ✓ ${DOCKER_HUB_REPO}/${service}:dev-latest"
                }
            }
            echo ""
            echo "🔗 Ver en: https://hub.docker.com/u/${DOCKER_HUB_REPO}"
        }
        failure {
            echo "❌ Build falló"
        }
    }
}