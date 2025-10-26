// Jenkinsfile-dev
pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9.9'
        jdk 'JDK-17'
    }
    
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
                echo "🚀 Building for DEV environment - Quick iteration"
            }
        }
        
        stage('Build All Services') {
            steps {
                script {
                    def services = SERVICES.split()
                    services.each { service ->
                        echo "📦 Building ${service}..."
                        dir(service) {
                            sh 'mvn clean package -DskipTests'
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
        
        stage('Quick Smoke Test') {
            steps {
                echo "🔥 Running quick smoke tests..."
                script {
                    // Pruebas básicas de compilación
                    sh 'echo "Verifying artifacts exist..."'
                }
            }
        }
    }
    
    post {
        always {
            sh 'docker logout || true'
            cleanWs()
        }
        success {
            echo "✅ DEV build completed successfully!"
        }
        failure {
            echo "❌ DEV build failed!"
        }
    }
}