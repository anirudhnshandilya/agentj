# 0.1.0 Launch Checklist

This checklist is for maintainers preparing the first public release.

## Local verification

```bash
./mvnw clean test
./mvnw package
```

Windows PowerShell:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd package
```

## GitHub repository

- Confirm `main` contains the intended release commit.
- Confirm the CI workflow is green.
- Add repository topics such as `java`, `ai-agents`, `llm`, `mcp`, and `developer-tools` if they accurately describe the project.
- Create GitHub Release `v0.1.0` using `docs/RELEASE_0.1.0.md` as the release notes source.
- Keep the repository description short and concrete: `Small, Java-native runtime for building tool-using AI agents.`

## Release hygiene

- Do not commit API keys, `.env` files, logs, or build output.
- Keep the README runnable and synchronized with the public API.
- Prefer small, reviewable follow-up releases over a large 0.x API surface.
