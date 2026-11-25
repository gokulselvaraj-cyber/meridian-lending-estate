"""Reminders after a missed payment: on days 1, 7 and 30 after the PaymentMissed event."""
import datetime as dt

import psycopg

from notify import config, interest, mailer, statement_loader

REMINDER_DAYS = (1, 7, 30)


def start(loan_id: str, customer_id: str, installment_no: int, amount_due: float, due_date: str) -> None:
    """Schedule the three reminders for one missed installment."""
    missed_on = dt.date.today()
    for day in REMINDER_DAYS:
        _schedule(loan_id, customer_id, installment_no, amount_due, missed_on + dt.timedelta(days=day), day)


def _loan(loan_id: str) -> dict:
    with psycopg.connect(config.DB_DSN) as conn:
        row = conn.execute("SELECT outstanding, annual_rate, status FROM LOAN WHERE loan_id = %s",
                           (loan_id,)).fetchone()
    return {"outstanding": float(row[0]), "annual_rate": float(row[1]), "status": row[2]}


def _schedule(loan_id, customer_id, installment_no, amount_due, send_on, day) -> None:
    # A real deployment hands this to the scheduler; here the message is built when it is due.
    loan = _loan(loan_id)
    stmt = statement_loader.latest(loan_id)
    accrued = stmt["accrued_interest"] if stmt else 0.0
    so_far = interest.interest_so_far(loan["outstanding"], loan["annual_rate"], 30)
    body = (f"Installment {installment_no} of {amount_due:,.2f} is overdue (reminder for day {day}).\n"
            f"Interest accrued on your statement: {accrued:,.2f}. Estimated interest this month: {so_far:,.2f}.")
    mailer.send_to_customer(customer_id, "Your payment is overdue", body, send_on=send_on)
