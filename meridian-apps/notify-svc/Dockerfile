FROM python:3.11-slim
WORKDIR /app
COPY pyproject.toml .
COPY notify ./notify
COPY prompts ./prompts
RUN pip install --no-cache-dir .
CMD ["notify-svc"]
