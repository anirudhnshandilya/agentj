# Changelog

All notable changes to AgentJ are documented here.

## Unreleased

### Added

- `agentj-jev`: Java-native integration for TypeSafe AI System One / Jev.
- Typed Jev `choice`, `score`, and `noul` question primitives.
- Structured probabilities and confidence in Jev responses.
- `LocalJevProvider` for deterministic, zero-key development and demo flows.
- `examples/hybrid-agent`: typed Jev decisions followed by deterministic Java workflow logic.
- Automated tests for Jev request/response handling, question validation, and the local provider.
- CI packaging of the full Maven reactor plus a zero-key hybrid-example smoke run.
- Dependabot configuration for Maven dependencies and GitHub Actions.

### Changed

- Root project description now reflects AgentJ's broader AI runtime direction.
- README now presents the zero-key decision demo as the fastest way to explore the Jev integration.
- Architecture documentation now separates generative-model, tool, and decision-model responsibilities.

## 0.1.0

Initial public release with Java tools, OpenAI integration, MCP support, memory, tracing, CLI, and the end-to-end MCP example.
