pipeline {
    agent any

    environment {
        BACKEND_URL = "http://localhost:8081/api/todos"
        FRONTEND_URL = "http://localhost:3000"  // FIXED PORT
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
            steps {
                dir('backend') {
                    sh 'mvn clean package -DskipTests'
                    sh 'mvn test'
                }
            }
        }

        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    sh 'mkdir -p .npm-cache'
                    sh 'set -e && npm ci --cache .npm-cache'
                    sh 'npm run build'
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                sh 'docker compose build'
            }
        }

//         stage('Deploy') {
//             steps {
//                 sh 'docker compose down || true'
//                 sh 'docker compose up -d'
//             }
//         }


// stage('Deploy') {
//     steps {
//         sh '''
//             echo "Cleaning stale containers..."
//             docker rm -f todo-postgres || true
//
//             docker compose down --remove-orphans || true
//             docker compose up -d
//         '''
//     }
// }


stage('Deploy') {
    steps {
        sh '''
            echo "🧹 Cleaning stale containers, networks, and volumes..."

            # Remove all containers whose name includes "todo"
            docker rm -f $(docker ps -aq --filter "name=todo") || true

            # Also clean orphaned networks and volumes
            docker network prune -f || true
            docker volume prune -f || true

            docker compose down --remove-orphans || true
            docker compose up -d
        '''
    }
}



//         stage('Health Check') {
//             steps {
//                 script {
//                     echo "Waiting for services to start..."
//
//                     retry(10) {
//                         sleep 5
//                         echo "Checking Backend..."
//                         sh "curl -fs ${BACKEND_URL} > /dev/null"
//                     }
//
//                     retry(10) {
//                         sleep 5
//                         echo "Checking Frontend..."
//                         sh "curl -fs ${FRONTEND_URL} > /dev/null"
//                     }
//
//                     echo "All services healthy!"
//                 }
//             }
//         }
//
//

stage('Health Check') {
    steps {
        script {
            echo "Waiting for services to stabilize..."
            sleep 20

            def backendUrl = "http://localhost:8081/api/todos"
            def frontendUrl = "http://localhost"

            retry(5) {
                echo "Checking Backend..."
                sh """
                    if ! curl -fs ${backendUrl} > /dev/null; then
                        echo 'Backend CHECK FAILED!'
                        docker compose logs backend
                        exit 1
                    fi
                """
                sleep 5
            }

            retry(5) {
                echo "Checking Frontend..."
                sh """
                    if ! curl -fs ${frontendUrl} > /dev/null; then
                        echo 'Frontend CHECK FAILED!'
                        docker compose logs frontend
                        exit 1
                    fi
                """
                sleep 5
            }

            echo "💚 All services are healthy!"
        }
    }
}




    }

    post {
        success {
            echo '🎉 Deployment Successful!'
            sh 'docker compose logs'
        }
        failure {
            echo '❌ Deployment Failed. Logs Below:'
            sh 'docker compose logs || true'
        }
        always {
            cleanWs()
        }
    }
}
