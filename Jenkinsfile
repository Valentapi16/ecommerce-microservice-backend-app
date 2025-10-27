pipeline {
    agent any
    
    stages {
        stage('Debug - Ver estructura') {
            steps {
                sh '''
                    echo "=== WORKSPACE ==="
                    echo "WORKSPACE: ${WORKSPACE}"
                    pwd
                    
                    echo ""
                    echo "=== Contenido del workspace ==="
                    ls -la
                    
                    echo ""
                    echo "=== Contenido de product-service ==="
                    ls -la product-service/
                    
                    echo ""
                    echo "=== Verificar pom.xml ==="
                    cat product-service/pom.xml | head -20
                    
                    echo ""
                    echo "=== Test Docker mount ==="
                    docker run --rm \
                        -v "${WORKSPACE}/product-service":/usr/src/app \
                        maven:3.9.9-eclipse-temurin-17 \
                        ls -la /usr/src/app
                '''
            }
        }
    }
}