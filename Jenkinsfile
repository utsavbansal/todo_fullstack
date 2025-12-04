pipeline {
    agent any

    environment {
        BACKEND_URL = "http://localhost:8081/api/todos"
        FRONTEND_URL = "http://localhost"
        AI_URL = "http://localhost:11434/api/tags"
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

//         stage('Build Docker Images') {
//             steps {
//                 sh 'docker compose build'
//             }
//         }

//         stage('Build Docker Images') {
//             steps {
//                 sh 'ls -la'  // debug: make sure we SEE docker-compose.yml
//                 sh 'docker compose -f docker-compose.yml build'
//             }
//         }

stage('Build Docker Images') {
    steps {
        sh 'echo ==== WORKSPACE ===='
        sh 'pwd'
        sh 'ls -R .'
        sh 'docker compose build'
    }
}



        stage('Deploy') {
            steps {
                sh '''
                    echo "🧹 Cleaning stale containers, networks, and volumes..."

                    # Remove all containers whose name includes "todo" or "ollama"
                    docker rm -f $(docker ps -aq --filter "name=todo") || true
                    docker rm -f $(docker ps -aq --filter "name=ollama") || true

                    # Clean orphaned networks and volumes
                    docker network prune -f || true

                    # Bring down any existing setup
                    docker compose down --remove-orphans || true

                    echo "🚀 Starting services..."
                    docker compose up -d
                '''
            }
        }

        stage('Wait for Ollama Model Download') {
            steps {
                script {
                    echo "⏳ Waiting for Ollama to download AI model..."
                    echo "This may take 5-10 minutes on first run..."

                    timeout(time: 20, unit: 'MINUTES') {
                        sh '''
                            # Wait for ollama-setup container to complete
                            while docker ps | grep -q ollama-setup; do
                                echo "Still downloading model..."
                                docker logs ollama-setup --tail 5 2>/dev/null || true
                                sleep 15
                            done

                            echo "✅ Model download complete!"
                        '''
                    }
                }
            }
        }

        stage('Health Check') {
            steps {
                script {
                    echo "⏳ Waiting for services to stabilize..."
                    sleep 30

                    def backendUrl = "http://localhost:8081/api/todos/health"
                    def frontendUrl = "http://localhost"
                    def aiUrl = "http://localhost:11434/api/tags"

                    // Check Backend
                    retry(10) {
                        echo "🔍 Checking Backend..."
                        sh """
                            if ! curl -fs ${backendUrl} > /dev/null; then
                                echo '❌ Backend CHECK FAILED!'
                                docker compose logs backend --tail 50
                                exit 1
                            fi
                            echo '✅ Backend is healthy!'
                        """
                        sleep 5
                    }

                    // Check Frontend
                    retry(10) {
                        echo "🔍 Checking Frontend..."
                        sh """
                            if ! curl -fs ${frontendUrl} > /dev/null; then
                                echo '❌ Frontend CHECK FAILED!'
                                docker compose logs frontend --tail 50
                                exit 1
                            fi
                            echo '✅ Frontend is healthy!'
                        """
                        sleep 5
                    }

                    // Check Ollama AI
                    retry(10) {
                        echo "🔍 Checking Ollama AI Service..."
                        sh """
                            if ! curl -fs ${aiUrl} > /dev/null; then
                                echo '❌ Ollama AI CHECK FAILED!'
                                docker compose logs ollama --tail 50
                                exit 1
                            fi
                            echo '✅ Ollama AI is healthy!'
                        """
                        sleep 5
                    }

                    // Check AI endpoints
                    echo "🔍 Testing AI endpoints..."
                    sh """
                        curl -fs http://localhost:8081/api/ai/health > /dev/null || {
                            echo '❌ AI endpoint not responding!'
                            docker compose logs backend --tail 50
                            exit 1
                        }
                        echo '✅ AI endpoints are working!'
                    """

                    echo "💚 All services are healthy!"
                }
            }
        }

        stage('Display Service URLs') {
            steps {
                script {
                    echo """
                    ╔════════════════════════════════════════════╗
                    ║     🎉 DEPLOYMENT SUCCESSFUL! 🎉          ║
                    ╠════════════════════════════════════════════╣
                    ║ Frontend:  http://localhost               ║
                    ║ Backend:   http://localhost:8081          ║
                    ║ AI API:    http://localhost:11434         ║
                    ║ Database:  localhost:5432                 ║
                    ╚════════════════════════════════════════════╝
                    """
                }
            }
        }
    }

    post {
        success {
            echo "🎉 Deployment Successful with AI Integration!"
        }
        failure {
            echo "❌ Deployment Failed. Checking logs..."
            sh '''
                echo "=== Backend Logs ==="
                docker compose logs backend --tail 100 || true
                echo "=== Frontend Logs ==="
                docker compose logs frontend --tail 100 || true
                echo "=== Ollama Logs ==="
                docker compose logs ollama --tail 100 || true
                echo "=== Postgres Logs ==="
                docker compose logs postgres --tail 100 || true
            '''
        }
        always {
            cleanWs()
        }
    }
}
