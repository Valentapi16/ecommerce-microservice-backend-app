// Jenkinsfile-dev - Versión simplificada
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
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo "🚀 Building for DEV environment"
            }
        }
        
        stage('Verify Environment') {
            steps {
                sh '''
                    echo "=== Environment Info ==="
                    java -version
                    mvn -version
                    docker --version
                '''
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
    }
    
    post {
        always {
            script {
                sh 'docker logout || true'
            }
        }
        success {
            echo "✅ DEV build completed successfully!"
        }
        failure {
            echo "❌ DEV build failed!"
        }
    }
}