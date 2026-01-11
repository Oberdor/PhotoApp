# Research Index

## PhotoApp code quality audit
**Question:** What are the key code quality issues and hotspots in the PhotoApp codebase that should be addressed first in a refactor plan?
**Finding:** Initial scan shows a conventional Spring Boot structure but with very large controllers/services, mixed responsibilities, some naming/REST inconsistencies, and effectively no tests. Domain logic and persistence concerns are often tightly coupled.
**Details:** See `research/photoapp-code-quality-audit.md`.
