# Security

Do not commit production secrets, payment credentials, signing keys or customer financial data.

The repository contains development-only credentials for local Docker Compose. They must never be reused outside local development.

Production deployments should:
- source secrets from a managed secret store
- terminate TLS at the edge and use TLS for service traffic where required
- restrict internal endpoints using network policy and workload identity
- rotate webhook signing secrets
- enforce merchant authentication and authorization
- encrypt sensitive data at rest
- enable audit logging and vulnerability scanning

Report security issues privately to the repository owner instead of opening a public issue.
