// Jenkinsfile-dev - VERSIÓN SIMPLE Y FUNCIONAL
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
                echo "🚀 Building for DEV environment - Quick iteration"
            }
        }
        
        stage('Verify Tools') {
            steps {
                sh '''
                    echo "=== Verifying tools ==="
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
                        sh """
                            cd ${service}
                            mvn clean package -DskipTests
                        """
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
            sh 'docker logout || true'
        }
        success {
            echo "✅ DEV build completed successfully!"
            echo "Images pushed:"
            script {
                def services = SERVICES.split()
                services.each { service ->
                    echo "  - ${DOCKER_HUB_REPO}/${service}:${VERSION}"
                }
            }
        }
        failure {
            echo "❌ DEV build failed!"
        }
    }
}