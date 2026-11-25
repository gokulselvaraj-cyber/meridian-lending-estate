# loan-batch

The mainframe side of Meridian Lending: the nightly batch (accrual, late fees, statements), the weekly LTV check and
the CICS loan enquiry. COBOL with DB2 (`EXEC SQL`) on the shared `LOAN` and `CUSTOMER` tables. Owner: mainframe-ops.

| Folder | Holds |
|---|---|
| `cobol/` | batch programs |
| `cics/` | online programs |
| `bms/` | 3270 maps |
| `copybook/` | record layouts |
| `jcl/` | jobs |
| `PARMLIB/` | job parameters |
| `ops/` | run logs |
