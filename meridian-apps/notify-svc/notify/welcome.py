"""The welcome email after a loan is paid out."""
from notify import mailer


def send_welcome(customer_id: str, loan_id: str, amount: float) -> None:
    mailer.send_to_customer(customer_id, "Welcome to Meridian Lending",
                            f"Your loan {loan_id} for {amount:,.2f} has been paid out.")
