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
        
        stage('Build JARs with Maven') {
            steps {
                script {
                    def services = SERVICES.split()
                    services.each { service ->
                        echo "📦 Compiling ${service}..."
                        dir(service) {
                            sh '''
                                # Crear Dockerfile temporal para compilar
                                cat > Dockerfile.build << 'EOF'
FROM maven:3.9.9-eclipse-temurin-17
WORKDIR /build
COPY . .
RUN mvn clean package -DskipTests
EOF
                                
                                # Compilar
                                docker build -f Dockerfile.build -t temp-build-${BUILD_NUMBER} .
                                
                                # Extraer el JAR compilado
                                docker create --name temp-container-${BUILD_NUMBER} temp-build-${BUILD_NUMBER}
                                docker cp temp-container-${BUILD_NUMBER}:/build/target ./
                                docker rm temp-container-${BUILD_NUMBER}
                                docker rmi temp-build-${BUILD_NUMBER}
                                
                                # Limpiar
                                rm Dockerfile.build
                                
                                echo "✅ Compilado exitosamente"
                                ls -la target/*.jar
                            '''
                        }
                    }
                }
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
            echo "✅ ¡ÉXITO COMPLETO! 🎉🎉🎉"
            echo ""
            echo "📦 Imágenes subidas a Docker Hub:"
            script {
                def services = SERVICES.split()
                services.each { service ->
                    echo "   ✓ ${DOCKER_HUB_REPO}/${service}:${VERSION}"
                    echo "   ✓ ${DOCKER_HUB_REPO}/${service}:dev-latest"
                }
            }
        }
        failure {
            echo "❌ Build falló"
        }
    }
}