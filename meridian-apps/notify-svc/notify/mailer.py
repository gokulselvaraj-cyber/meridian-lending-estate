"""Sends email through the bank's SMTP relay (outside this estate)."""
import datetime as dt
import smtplib
from email.message import EmailMessage

import psycopg

from notify import config


def _email_of(customer_id: str) -> str:
    with psycopg.connect(config.DB_DSN) as conn:
        return conn.execute("SELECT email FROM CUSTOMER WHERE customer_id = %s", (customer_id,)).fetchone()[0]


def send_to_customer(customer_id: str, subject: str, body: str, send_on: dt.date | None = None) -> None:
    msg = EmailMessage()
    msg["From"] = "lending@meridian.invalid"
    msg["To"] = _email_of(customer_id)
    msg["Subject"] = subject
    msg.set_content(body)
    with smtplib.SMTP(config.SMTP_HOST, config.SMTP_PORT) as smtp:
        smtp.starttls()
        smtp.send_message(msg)
