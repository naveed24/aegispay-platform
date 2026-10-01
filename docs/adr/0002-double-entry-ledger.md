# ADR 0002: Immutable double-entry ledger

**Status:** Accepted

Every completed payment creates a ledger transaction containing balanced debit and credit entries. Historical entries are never updated or deleted. Corrections are represented as new reversing transactions.

Invariant:

`sum(debits) == sum(credits)`

This makes financial history auditable and prevents a mutable balance field from becoming the source of truth.
