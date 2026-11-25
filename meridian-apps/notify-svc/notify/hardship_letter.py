"""Drafts a hardship letter for a customer in arrears, for an agent to review before it is sent."""
from pathlib import Path

import psycopg

from notify import config, llm_client

TEMPLATE = Path(__file__).resolve().parent.parent / "prompts" / "hardship_letter.txt"


def draft(customer_id: str, loan_id: str, months_in_arrears: int) -> str:
    with psycopg.connect(config.DB_DSN) as conn:
        name, national_id = conn.execute(
            "SELECT full_name, national_id FROM CUSTOMER WHERE customer_id = %s", (customer_id,)).fetchone()
    prompt = TEMPLATE.read_text().format(full_name=name, national_id=national_id, loan_id=loan_id,
                                         months_in_arrears=months_in_arrears)
    return llm_client.complete(prompt)
