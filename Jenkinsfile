pipeline {
    agent any

    environment {
        DOCKER_HUB_CREDENTIALS = credentials('dockerhub')
        DOCKER_HUB_REPO = "docker.io/valentapi16"
        JAVA_HOME = "/usr/lib/jvm/java-17-openjdk-amd64"
        PATH = "${JAVA_HOME}/bin:${env.PATH}"
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
                    def services = [
                        "product-service",
                        "order-service",
                        "payment-service",
                        "user-service",
                        "shipping-service",
                        "favourite-service"
                    ]

                    for (service in services) {
                        echo "🛠️ Compilando ${service}..."
                        dir("${service}") {
                            sh 'mvn clean package -DskipTests'
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
                        "order-service",
                        "payment-service",
                        "user-service",
                        "shipping-service",
                        "favourite-service"
                    ]

                    for (service in services) {
                        echo "🐳 Construyendo imagen Docker para ${service}..."
                        dir("${service}") {
                            sh "docker build -t ${DOCKER_HUB_REPO}/${service}:dev ."
                        }
                    }
                }
            }
        }

        stage('Login DockerHub') {
            steps {
                echo "🔐 Iniciando sesión en Docker Hub..."
                sh "echo ${DOCKER_HUB_CREDENTIALS_PSW} | docker login -u ${DOCKER_HUB_CREDENTIALS_USR} --password-stdin"
            }
        }

        stage('Push Docker Images') {
            steps {
                script {
                    def services = [
                        "product-service",
                        "order-service",
                        "payment-service",
                        "user-service",
                        "shipping-service",
                        "favourite-service"
                    ]

                    for (service in services) {
                        echo "📤 Subiendo imagen de ${service} a Docker Hub..."
                        sh "docker push ${DOCKER_HUB_REPO}/${service}:dev"
                    }
                }
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline completado exitosamente."
        }
        failure {
            echo "❌ Error durante la ejecución del pipeline."
        }
    }
}
