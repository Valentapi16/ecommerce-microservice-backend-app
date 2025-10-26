// Jenkinsfile-dev - Versión sin contenedor anidado
pipeline {
    agent any
    
    tools {
        maven 'MAVEN-3.9.9'
    }
    
    environment {
        DOCKER_HUB_CREDENTIALS = credentials('dockerhub')
        DOCKER_HUB_REPO = "valentapi16"
        SERVICES = "product-service order-service payment-service user-service shipping-service favourite-service"
        ENVIRONMENT = "dev"
        VERSION = "dev-${BUILD_NUMBER}"
        // Forzar Java 17
        JAVA_HOME = "/opt/java/openjdk"
    }
    
    stages {
        stage('Setup Java 17') {
            steps {
                sh '''
                    # Verificar si existe Java 17, si no, usar el del sistema
                    if [ -d "/usr/lib/jvm/java-17-openjdk-amd64" ]; then
                        export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
                    elif [ -d "/usr/lib/jvm/temurin-17-jdk-amd64" ]; then
                        export JAVA_HOME="/usr/lib/jvm/temurin-17-jdk-amd64"
                    fi
                    java -version
                '''
            }
        }
        
        stage('Checkout') {
            steps {
                checkout scm
                echo "🚀 Building for DEV environment - Quick iteration"
            }
        }
        
        stage('Build All Services with Docker Maven') {
            steps {
                script {
                    def services = SERVICES.split()
                    services.each { service ->
                        echo "📦 Building ${service} using Docker Maven container..."
                        dir(service) {
                            sh '''
                                docker run --rm \
                                    -v "$(pwd)":/app \
                                    -v "$HOME/.m2":/root/.m2 \
                                    -w /app \
                                    maven:3.9.9-eclipse-temurin-17 \
                                    mvn clean package -DskipTests
                            '''
                        }
                    }
                }
            }
        }
        
        stage('Docker Login') {
            steps {
                sh 'echo $DOCKER_HUB_CREDENTIALS_PSW | docker login -u $DOCKER_HUB_CREDENTIALS_USR --password-stdin'
            }
        }
        
        stage('Build & Push Docker Images') {
            steps {
                script {
                    def services = SERVICES.split()
                    services.each { service ->
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
            echo "✅ DEV build completed successfully!"
        }
        failure {
            echo "❌ DEV build failed!"
        }
    }
}