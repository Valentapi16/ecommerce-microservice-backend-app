pipeline {
    agent any

    environment {
        REGISTRY = "docker.io/Valentapi16"
        DOCKER_CREDENTIALS = credentials('dockerhub')
        SERVICES = "product-service order-service user-service payment-service inventory-service gateway-service"
    }

    stages {

        stage('Checkout') {
            steps {
                echo "🔄 Clonando repositorio..."
                checkout([$class: 'GitSCM',
                    branches: [[name: '*/develop']],
                    userRemoteConfigs: [[url: 'https://github.com/Valentapi16/ecommerce-microservice-backend-app.git']]
                ])
            }
        }

        stage('Build Maven') {
            steps {
                script {
                    withEnv(["JAVA_HOME=${tool 'jdk17'}", "PATH+JDK=${tool 'jdk17'}/bin"]) {
                        for (service in SERVICES.split(' ')) {
                            echo "🛠️ Compilando ${service}..."
                            dir("${service}") {
                                sh 'mvn clean package -DskipTests'
                            }
                        }
                    }
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    for (service in SERVICES.split(' ')) {
                        echo "🐳 Construyendo imagen Docker para ${service}..."
                        sh """
                            docker build -t ${REGISTRY}/${service}:dev -f ${service}/Dockerfile .
                        """
                    }
                }
            }
        }

        stage('Login DockerHub') {
            steps {
                echo "🔐 Iniciando sesión en Docker Hub..."
                sh "echo ${DOCKER_CREDENTIALS_PSW} | docker login -u ${DOCKER_CREDENTIALS_USR} --password-stdin"
            }
        }

        stage('Push Docker Images') {
            steps {
                script {
                    for (service in SERVICES.split(' ')) {
                        echo "🚀 Subiendo imagen de ${service} a Docker Hub..."
                        sh "docker push ${REGISTRY}/${service}:dev"
                    }
                }
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline ejecutado correctamente. Todas las imágenes fueron subidas a Docker Hub."
        }
        failure {
            echo "❌ Error durante la ejecución del pipeline."
        }
    }
}
