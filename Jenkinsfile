pipeline {
    agent {
        docker {
            // Use a Maven + JDK 17 image for backend build
            image 'maven:3.9-eclipse-temurin-17'
            args '-v /root/.m2:/root/.m2'
        }
    }

    environment {
        DOCKER_REGISTRY = 'docker.io'
        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    stages {
//         stage('Checkout') {
//             steps {
//                 git branch: 'main', url: 'https://github.com/yourusername/todo-app.git'
//             }
//         }
            stage('Checkout') {
                steps {
                    git branch: 'main',
                        url: 'https://github.com/yourusername/todo-app.git',
                        credentialsId: 'utsav-gitpat'
                }
            }


        stage('Build Backend') {
            steps {
                dir('backend') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Test Backend') {
            steps {
                dir('backend') {
                    sh 'mvn test'
                }
            }
        }

        stage('Build Frontend') {
            agent {
                docker {
                    // Node.js agent for frontend build
                    image 'node:20-alpine'
                    args '-v /root/.npm:/root/.npm'
                }
            }
            steps {
                dir('frontend') {
                    sh 'npm ci --omit=dev'
                    sh 'npm run build'
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                sh 'docker-compose build'
            }
        }

        stage('Deploy') {
            steps {
                sh 'docker-compose down'
                sh 'docker-compose up -d'
            }
        }

        stage('Health Check') {
            steps {
                script {
                    sleep(time: 30, unit: 'SECONDS')
                    sh 'curl -f http://localhost:8081/api/todos/health || exit 1'
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline executed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
        always {
            cleanWs()
        }
    }
}
