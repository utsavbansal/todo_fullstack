FROM ollama/ollama:latest

# Install curl for healthchecks
RUN apt-get update && apt-get install -y curl && apt-get clean

# Add startup script
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]


