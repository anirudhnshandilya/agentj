# Contributing to AgentJ

Thanks for helping make Java agent development simpler.

## Local setup

- JDK 21+
- Maven 3.9+, or the included Maven Wrapper

Run the full test suite:

```bash
./mvnw test
```

Package the reactor:

```bash
./mvnw package
```

Windows PowerShell:

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

## Pull requests

Keep public APIs small and understandable. Prefer plain Java types and narrow interfaces. Add or update tests for behavioral changes and update the README when introducing user-visible behavior.

Before opening a PR:

- run the test suite;
- check that generated build output is not committed;
- keep changes focused;
- explain compatibility or API changes clearly.

## Issues

Please include the Java version, AgentJ version, module/provider involved, and a minimal reproduction where possible.

For security vulnerabilities, follow [`SECURITY.md`](SECURITY.md) rather than opening a public issue.
