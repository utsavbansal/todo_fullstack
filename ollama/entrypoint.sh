#!/bin/sh

# Start Ollama server in background
ollama serve &
SERVER_PID=$!

# Wait for server to be ready
echo "Waiting for Ollama server..."
until curl -sf http://localhost:11434/api/tags >/dev/null 2>&1; do
  echo "Still waiting for Ollama to start..."
  sleep 2
done
echo "Ollama is ready."

# Check if model exists
if ollama list | grep -q "llama3.2"; then
  echo "Model already installed — skipping download."
else
  echo "Model not found. Pulling llama3.2..."
  ollama pull llama3.2:latest
  echo "Model downloaded."
fi

# Keep the server running
wait $SERVER_PID