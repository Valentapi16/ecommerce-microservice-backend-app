pipeline {
    agent any

    tools {
        maven 'MAVEN-3.9.9'
    }

    environment {
        DOCKER_HUB_CREDENTIALS = credentials('dockerhub')
        DOCKER_HUB_REPO = "valentapi16"
        SERVICES = "product-service order-service payment-service user-service shipping-service favourite-service"
    }

    stages {
        stage('Checkout') {
            steps {
                echo "🔄 Clonando repositorio..."
                checkout scm
            }
        }

        stage('Build Maven') {
            steps {
                script {
                    SERVICES.split().each { service ->
                        echo "🛠️ Compilando ${service}..."
                        dir("${service}") {
                            sh "mvn clean package -DskipTests"
                        }
                    }
                }
            }
        }

        stage('Docker Login') {
            steps {
                sh '''
                    echo "${DOCKER_HUB_CREDENTIALS_PSW}" | docker login -u "${DOCKER_HUB_CREDENTIALS_USR}" --password-stdin
                '''
            }
        }

        stage('Build & Push Docker Images') {
            steps {
                script {
                    SERVICES.split().each { service ->
                        echo "🐳 Construyendo imagen de ${service}..."
                        dir("${service}") {
                            sh "docker build -t ${DOCKER_HUB_REPO}/${service}:dev ."
                            sh "docker push ${DOCKER_HUB_REPO}/${service}:dev"
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo "✅ Todas las imágenes fueron construidas y enviadas correctamente."
        }
        failure {
            echo "❌ Falló el pipeline. Revisar logs."
        }
    }
}
