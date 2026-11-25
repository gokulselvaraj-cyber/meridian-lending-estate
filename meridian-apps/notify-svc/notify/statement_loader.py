"""Loads the nightly statement file written by mainframe job NIGHTLY (STMTGEN1).

The dataset MERIDIAN.LENDING.STMT.DAILY is landed here as /data/stmt/daily.dat by the file transfer.
Fixed-width records, 80 bytes:
  cols  1-10  loan id
  cols 11-22  customer id
  cols 23-35  outstanding balance, 2 implied decimals
  cols 36-48  accrued interest, 2 implied decimals
  cols 49-56  statement date, YYYYMMDD
"""
from notify import config

_by_loan: dict[str, dict] = {}


def _amount(field: str) -> float:
    return int(field.strip() or "0") / 100


def load(path: str = config.STATEMENT_FILE) -> int:
    _by_loan.clear()
    with open(path, encoding="ascii") as f:
        for line in f:
            if len(line.rstrip("\n")) < 56:
                continue
            _by_loan[line[0:10].strip()] = {
                "customer_id": line[10:22].strip(),
                "outstanding": _amount(line[22:35]),
                "accrued_interest": _amount(line[35:48]),
                "statement_date": line[48:56],
            }
    return len(_by_loan)


def latest(loan_id: str) -> dict | None:
    return _by_loan.get(loan_id)
