pipeline {
    agent {
        docker {
            image 'maven:3.9.9-eclipse-temurin-17'
            args '-v /var/run/docker.sock:/var/run/docker.sock'
        }
    }

    environment {
        DOCKER_HUB_CREDENTIALS = credentials('dockerhub')
        DOCKER_HUB_REPO = "valentapi16"
        SERVICES = "product-service order-service payment-service user-service shipping-service favourite-service"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Services') {
            steps {
                script {
                    SERVICES.split().each { service ->
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

        stage('Build & Push Images') {
            steps {
                script {
                    SERVICES.split().each { service ->
                        dir(service) {
                            sh "docker build -t ${DOCKER_HUB_REPO}/${service}:dev ."
                            sh "docker push ${DOCKER_HUB_REPO}/${service}:dev"
                        }
                    }
                }
            }
        }
    }
}
