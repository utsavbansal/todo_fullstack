pipeline {
    agent any

    environment {
        BACKEND_URL = "http://localhost:8081/api/todos"
        FRONTEND_URL = "http://localhost"
        AI_URL = "http://localhost:11434/api/tags"
    }

    stages {

        /* -------------------------
         * 1. CHECKOUT CODE
         * ------------------------- */
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/utsavbansal/todo_fullstack.git',
                    credentialsId: 'utsav-gitpat'
            }
        }

        /* -------------------------
         * 2. BUILD BACKEND
         * ------------------------- */
        stage('Build Backend') {
            steps {
                dir('backend') {
                    sh 'mvn clean package -DskipTests'
                    sh 'mvn test'
                }
            }
        }

        /* -------------------------
         * 3. BUILD FRONTEND
         * ------------------------- */
        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    sh 'mkdir -p .npm-cache'
                    sh 'npm ci --cache .npm-cache'
                    sh 'npm run build'
                }
            }
        }

        /* -------------------------
         * 4. BUILD DOCKER IMAGES
         * ------------------------- */
        stage('Build Docker Images') {
            steps {
                sh 'echo ==== WORKSPACE ===='
                sh 'pwd'
                sh 'ls -R .'
                sh 'docker compose build'
            }
        }

        /* -------------------------
         * 5. WAIT FOR MODEL DOWNLOAD
         * -------------------------
         * This is where model downloads happen — BEFORE full deploy
         * ------------------------- */
        stage('Wait for Ollama Model Download') {
            steps {
                script {
                    echo "⏳ Waiting for Ollama model setup container to finish..."

                    timeout(time: 30, unit: 'MINUTES') {
                        sh '''
                            # Wait until ollama-setup container finishes
                            while docker ps | grep -q ollama-setup; do
                                echo "Model still downloading..."
                                docker logs ollama-setup --tail 5 2>/dev/null || true
                                sleep 15
                            done

                            echo "✅ Model download stage completed!"
                        '''
                    }
                }
            }
        }

        /* -------------------------
         * 6. DEPLOY SERVICES
         * ------------------------- */
        stage('Deploy') {
            steps {
                sh '''
                    echo "🧹 Cleaning old containers..."

                    docker rm -f $(docker ps -aq --filter "name=todo") || true
                    docker rm -f $(docker ps -aq --filter "name=ollama") || true

                    docker network prune -f || true

                    docker compose down --remove-orphans || true

                    echo "🚀 Starting services..."
                    docker compose up -d
                '''
            }
        }

        /* -------------------------
         * 7. HEALTH CHECKS
         * ------------------------- */
        stage('Health Check') {
            steps {
                script {
                    echo "⏳ Waiting for services to stabilize..."
                    sleep 20

                    def backendUrl = "http://localhost:8081/api/todos/health"
                    def frontendUrl = "http://localhost"
                    def aiUrl = "http://localhost:11434/api/tags"

                    retry(10) {
                        sh "curl -fs ${backendUrl}"
                    }

                    retry(10) {
                        sh "curl -fs ${frontendUrl}"
                    }

                    retry(10) {
                        sh "curl -fs ${aiUrl}"
                    }

                    echo "🔍Checking backend AI endpoint..."
                    sh "curl -fs http://localhost:8081/api/ai/health"

                    echo "💚 All services are healthy!"
                }
            }
        }

        /* -------------------------
         * 8. SHOW SERVICE URLS
         * ------------------------- */
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

    /* -------------------------
     * POST ACTIONS
     * ------------------------- */
    post {
        success {
            echo "🎉 Deployment Successful with AI Integration!"
        }
        failure {
            echo "❌ Deployment Failed. Printing logs..."
            sh '''
                docker compose logs backend --tail 100 || true
                docker compose logs frontend --tail 100 || true
                docker compose logs ollama --tail 100 || true
                docker compose logs postgres --tail 100 || true
            '''
        }
        always {
            cleanWs()
        }
    }
}
