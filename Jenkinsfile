pipeline {
    agent none

    environment {
        DOCKER_REGISTRY = 'docker.io'
        IMAGE_TAG = "${BUILD_NUMBER}"
        WORKSPACE_DIR = "${env.WORKSPACE}"
    }

    stages {

        stage('Checkout') {
            agent any
            steps {
                git branch: 'main',
                    url: 'https://github.com/utsavbansal/todo_fullstack.git',
                    credentialsId: 'utsav-gitpat'
            }
        }

        stage('Build Backend') {
            agent {
                docker {
                    image 'maven:3.9-eclipse-temurin-17'
                    args "-v ${env.WORKSPACE_DIR}:${env.WORKSPACE_DIR} -w ${env.WORKSPACE_DIR}/backend"
                }
            }
            steps {
                dir('backend') {
                    sh 'mvn clean package -DskipTests'
                    sh 'mvn test'
                }
            }
        }

        stage('Build Frontend') {
            agent {
                docker {
                    image 'node:20-alpine'
                    args "-v ${env.WORKSPACE_DIR}:${env.WORKSPACE_DIR} -w ${env.WORKSPACE_DIR}/frontend"
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
            agent any
            steps {
                sh 'docker-compose build'
            }
        }

        stage('Deploy') {
            agent any
            steps {
                sh 'docker-compose down'
                sh 'docker-compose up -d'
            }
        }

        stage('Health Check') {
            agent any
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
