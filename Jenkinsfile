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
        
        stage('Docker Login') {
            steps {
                sh 'echo $DOCKER_HUB_CREDENTIALS_PSW | docker login -u $DOCKER_HUB_CREDENTIALS_USR --password-stdin'
            }
        }
        
        stage('Build & Push Docker Images (con Maven interno)') {
            steps {
                script {
                    def services = SERVICES.split()
                    services.each { service ->
                        echo "📦 Building ${service}..."
                        sh """
                            cd ${service}
                            # El Dockerfile ya tiene Maven, solo hacemos build
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
            echo "✅ ¡Todas las imágenes construidas y subidas!"
            script {
                def services = SERVICES.split()
                services.each { service ->
                    echo "   ✓ ${DOCKER_HUB_REPO}/${service}:${VERSION}"
                }
            }
        }
        failure {
            echo "❌ Build falló"
        }
    }
}