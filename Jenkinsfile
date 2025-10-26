pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub') // ID de la credencial en Jenkins
        DOCKER_USER = 'Valentapi16'
        TAG = 'dev'
    }

    stages {
        stage('Checkout') {
            steps {
                echo '🔄 Clonando repositorio...'
                checkout scm
            }
        }

        stage('Build Maven') {
            steps {
                script {
                    def services = [
                        "product-service",
                        "category-service",
                        "order-service",
                        "payment-service",
                        "user-service",
                        "gateway-service"
                    ]

                    for (service in services) {
                        echo "🛠️ Compilando ${service}..."
                        dir("${service}") {
                            sh "mvn clean package -DskipTests"
                        }
                    }
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    def services = [
                        "product-service",
                        "category-service",
                        "order-service",
                        "payment-service",
                        "user-service",
                        "gateway-service"
                    ]

                    for (service in services) {
                        echo "🐳 Construyendo imagen de ${service}..."
                        sh """
                            docker build -t ${DOCKER_USER}/${service}:${TAG} ${service}/
                        """
                    }
                }
            }
        }

        stage('Login DockerHub') {
            steps {
                echo '🔐 Iniciando sesión en Docker Hub...'
                sh "echo ${DOCKERHUB_CREDENTIALS_PSW} | docker login -u ${DOCKERHUB_CREDENTIALS_USR} --password-stdin"
            }
        }

        stage('Push Docker Images') {
            steps {
                script {
                    def services = [
                        "product-service",
                        "category-service",
                        "order-service",
                        "payment-service",
                        "user-service",
                        "gateway-service"
                    ]

                    for (service in services) {
                        echo "🚀 Subiendo imagen de ${service} a Docker Hub..."
                        sh "docker push ${DOCKER_USER}/${service}:${TAG}"
                    }
                }
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline ejecutado correctamente. Todas las imágenes fueron subidas a Docker Hub.'
        }
        failure {
            echo '❌ Error durante la ejecución del pipeline.'
        }
    }
}
