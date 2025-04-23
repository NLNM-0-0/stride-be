# Pull official Python image
FROM python:3.13-slim

WORKDIR /usr/backend

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        build-essential \
        gcc \
        netcat-openbsd && \
    rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --upgrade pip \
  && pip install -r requirements.txt

COPY . .

EXPOSE 8085

CMD ["uvicorn", "main:route_service", "--host", "0.0.0.0", "--port", "8085", "--reload"]