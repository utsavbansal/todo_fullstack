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

        stage('Build Docker Images') {
            steps {
                sh 'echo ==== WORKSPACE ===='
                sh 'pwd'
                sh 'ls -la'
                sh 'docker compose build'
            }
        }

        stage('Clean Previous Deployment') {
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
                '''
            }
        }

        stage('Start Infrastructure') {
            steps {
                sh '''
                    echo "🚀 Starting Database and Ollama services first..."
                    docker compose up -d postgres ollama

                    echo "⏳ Waiting for database to be ready..."
                    sleep 10
                '''
            }
        }

        stage('Download AI Model') {
            steps {
                script {
                    echo "⏳ Starting Ollama model download..."
                    echo "This may take 5-10 minutes on first run..."

                    timeout(time: 20, unit: 'MINUTES') {
                        sh '''
                            # Start the ollama-setup service to download model
                            docker compose up -d ollama-setup

                            # Wait for ollama-setup container to complete
                            echo "Waiting for model download to complete..."
                            while docker ps | grep -q ollama-setup; do
                                echo "📥 Still downloading model..."
                                docker logs ollama-setup --tail 5 2>/dev/null || true
                                sleep 15
                            done

                            # Check if download was successful
                            if docker ps -a --filter "name=ollama-setup" --filter "exited=0" | grep -q ollama-setup; then
                                echo "✅ Model download complete!"
                            else
                                echo "❌ Model download failed!"
                                docker logs ollama-setup
                                exit 1
                            fi
                        '''
                    }
                }
            }
        }

        stage('Verify AI Model') {
            steps {
                script {
                    echo "🔍 Verifying AI model is available..."
                    retry(5) {
                        sh '''
                            # Check if model is listed in Ollama
                            if docker exec todo-ollama ollama list | grep -q "llama3.2"; then
                                echo "✅ AI model verified!"
                            else
                                echo "❌ Model not found, retrying..."
                                sleep 10
                                exit 1
                            fi
                        '''
                    }
                }
            }
        }

        stage('Deploy Application') {
            steps {
                sh '''
                    echo "🚀 Starting Backend and Frontend services..."
                    docker compose up -d backend frontend

                    echo "⏳ Waiting for services to initialize..."
                    sleep 20
                '''
            }
        }

        stage('Health Check') {
            steps {
                script {
                    echo "⏳ Running comprehensive health checks..."

                    def backendUrl = "http://localhost:8081/api/todos/health"
                    def frontendUrl = "http://localhost"
                    def aiUrl = "http://localhost:11434/api/tags"

                    // Check Postgres
                    echo "🔍 Checking Database..."
                    retry(5) {
                        sh """
                            if ! docker exec todo-postgres pg_isready -U todouser -d tododb > /dev/null; then
                                echo '❌ Database not ready!'
                                exit 1
                            fi
                            echo '✅ Database is healthy!'
                        """
                        sleep 3
                    }

                    // Check Ollama AI
                    echo "🔍 Checking Ollama AI Service..."
                    retry(10) {
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

                    // Check Backend
                    echo "🔍 Checking Backend..."
                    retry(10) {
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

                    // Check AI endpoints
                    echo "🔍 Testing AI endpoints..."
                    retry(5) {
                        sh """
                            if ! curl -fs http://localhost:8081/api/ai/health > /dev/null; then
                                echo '❌ AI endpoint not responding!'
                                docker compose logs backend --tail 50
                                exit 1
                            fi
                            echo '✅ AI endpoints are working!'
                        """
                        sleep 3
                    }

                    // Check Frontend
                    echo "🔍 Checking Frontend..."
                    retry(10) {
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
                    ╠════════════════════════════════════════════╣
                    ║ Test AI:   curl localhost:8081/api/ai/health ║
                    ╚════════════════════════════════════════════╝
                    """
                }
            }
        }
    }

    post {
        success {
            echo "🎉 Deployment Successful with AI Integration!"
            sh '''
                echo "📊 Container Status:"
                docker ps --filter "name=todo" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
            '''
        }
        failure {
            echo "❌ Deployment Failed. Checking logs..."
            sh '''
                echo "=== Container Status ==="
                docker ps -a --filter "name=todo"
                docker ps -a --filter "name=ollama"

                echo ""
                echo "=== Backend Logs ==="
                docker compose logs backend --tail 100 || true

                echo ""
                echo "=== Frontend Logs ==="
                docker compose logs frontend --tail 100 || true

                echo ""
                echo "=== Ollama Logs ==="
                docker compose logs ollama --tail 100 || true

                echo ""
                echo "=== Ollama Setup Logs ==="
                docker compose logs ollama-setup --tail 100 || true

                echo ""
                echo "=== Postgres Logs ==="
                docker compose logs postgres --tail 100 || true
            '''
        }
        always {
            cleanWs()
        }
    }
}