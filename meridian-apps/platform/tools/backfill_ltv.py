"""One-off: fill LOAN.ltv_ratio for loans disbursed before Liquibase change 002-add-ltv-column.

Run once by data-platform in May 2025, by hand. Not scheduled and not part of any service.
Usage: python backfill_ltv.py --dsn postgresql://.../lending --valuations valuations.csv
"""
import argparse
import csv

import psycopg


def main() -> None:
    p = argparse.ArgumentParser()
    p.add_argument("--dsn", required=True)
    p.add_argument("--valuations", required=True, help="CSV: loan_id,property_value at disbursement")
    a = p.parse_args()
    with psycopg.connect(a.dsn) as conn, open(a.valuations, newline="") as f:
        for row in csv.DictReader(f):
            conn.execute("UPDATE LOAN SET ltv_ratio = principal / %s WHERE loan_id = %s AND ltv_ratio IS NULL",
                         (float(row["property_value"]), row["loan_id"]))


if __name__ == "__main__":
    main()
