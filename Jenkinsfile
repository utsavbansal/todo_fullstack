pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = 'docker.io'
        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
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
                    args '' // No need to mount WORKSPACE manually
                }
            }
            steps {
                dir('backend') {
                    sh 'mvn clean package -DskipTests'
                    sh 'mvn test'
                }
            }
        }

        stage('Debug Workspace') {
            steps {
                sh 'pwd'
                sh 'ls -l'
            }
        }

//         stage('Build Frontend') {
//             agent {
//                 docker {
//                     image 'node:20-alpine'
//                     args '' // Use default workspace mount
//                 }
//             }
//             steps {
//                 dir('frontend') {
//                     sh 'npm ci --omit=dev'
//                     sh 'npm run build'
//                 }
//             }
//         }

//         stage('Build Frontend') {
//             agent {
//                 docker {
//                     image 'node:20-alpine'
//                     args '' // no volume mounts
//                 }
//             }
//             steps {
//                 dir('frontend') {
//                     // Use a cache folder inside workspace
//                     sh 'mkdir -p .npm-cache'
//                     sh 'npm ci --omit=dev --cache .npm-cache'
//                     sh 'npm run build'
//                 }
//             }
//         }

            stage('Build Frontend') {
                agent {
                    docker {
                        image 'node:20-alpine'
                    }
                }
                steps {
                    dir('frontend') {
                        sh 'mkdir -p .npm-cache'
                        // Install dev dependencies this time
                        sh 'npm ci --cache .npm-cache'
                        sh 'npm run build'
                    }
                }
            }




//         stage('Build Docker Images') {
//             steps {
//                 sh 'docker-compose build'
//             }
//         }

        stage('Build Docker Images') {
            steps {
                sh 'docker compose build'
            }
        }


//         stage('Deploy') {
//             steps {
//                 sh 'docker-compose down'
//                 sh 'docker-compose up -d'
//             }
//         }

        stage('Deploy') {
            steps {
                sh 'docker compose down'
            }
        }



//         stage('Health Check') {
//             steps {
//                 script {
//                     sleep(time: 30, unit: 'SECONDS')
//                     sh 'curl -f http://localhost:8081/api/todos/health || exit 1'
//                 }
//             }
//         }
//     }

        stage('Health Check') {
            steps {
                script {
                    echo "Waiting for stack to start..."
                    sleep 10

                    def backendUrl = "http://localhost:8080/api/todos"
                    def frontendUrl = "http://localhost:5173"

                    echo "Checking Backend API..."
                    sh """
                        if ! curl -fs ${backendUrl} > /dev/null; then
                            echo 'Backend Health Check FAILED!'
                            docker compose logs backend
                            exit 1
                        fi
                    """

                    echo "Checking Frontend..."
                    sh """
                        if ! curl -fs ${frontendUrl} > /dev/null; then
                            echo 'Frontend Health Check FAILED!'
                            docker compose logs frontend
                            exit 1
                        fi
                    """

                    echo "All services are healthy!"
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
