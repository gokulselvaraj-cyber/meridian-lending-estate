"""Settings, all from the environment."""
import os

KAFKA_BOOTSTRAP = os.environ.get("KAFKA_BOOTSTRAP", "localhost:9092")
DB_DSN = os.environ.get("LOAN_DB_DSN", "postgresql://localhost:5432/lending")  # user and password come from PG* env vars
SMTP_HOST = os.environ.get("SMTP_HOST", "smtp.meridian.invalid")
SMTP_PORT = int(os.environ.get("SMTP_PORT", "587"))
AI_GATEWAY_URL = os.environ.get("AI_GATEWAY_URL", "https://ai-gateway.meridian.invalid")
STATEMENT_FILE = os.environ.get("STATEMENT_FILE", "/data/stmt/daily.dat")

TOPIC_LOAN_DISBURSED = "lending.loan.disbursed.v1"
TOPIC_PAYMENT_MISSED = "lending.payment.missed.v1"
