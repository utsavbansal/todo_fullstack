#!/bin/sh

# Start Ollama server in background
ollama serve &
SERVER_PID=$!

# Wait for server to be ready
echo "Waiting for Ollama server..."
until curl -s http://localhost:11434/api/tags >/dev/null 2>&1; do
  sleep 2
done
echo "Ollama is ready."

# Only pull the model if it DOES NOT EXIST in the volume
if ! ollama list | grep -q "llama3.2"; then
  echo "Model not found. Pulling llama3.2..."
  ollama pull llama3.2:latest
  echo "Model downloaded."
else
  echo "Model already installed — skipping download."
fi

wait $SERVER_PID
