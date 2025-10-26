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
                        echo "📦 Compiling ${service} with Maven..."
                        sh """
                            docker run --rm \
                                -v "${WORKSPACE}/${service}":/usr/src/app \
                                -v "${HOME}/.m2":/root/.m2 \
                                -w /usr/src/app \
                                maven:3.9.9-eclipse-temurin-17 \
                                mvn clean package -DskipTests
                        """
                    }
                }
            }
        }
        
        stage('Docker Login') {
            steps {
                script {
                    sh '''
                        USERNAME=$(echo "$DOCKER_HUB_CREDENTIALS_USR" | tr '[:upper:]' '[:lower:]')
                        echo "$DOCKER_HUB_CREDENTIALS_PSW" | docker login -u "$USERNAME" --password-stdin
                    '''
                }
            }
        }
        
        stage('Build & Push Docker Images') {
            steps {
                script {
                    def services = SERVICES.split()
                    services.each { service ->
                        echo "🐳 Building Docker image for ${service}..."
                        sh """
                            cd ${service}
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
    
    post {
        always {
            script {
                sh 'docker logout || true'
            }
        }
        success {
            echo "✅ ¡ÉXITO! Todas las imágenes construidas y subidas!"
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