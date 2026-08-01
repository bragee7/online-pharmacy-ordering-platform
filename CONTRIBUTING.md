# Contributing to Online Pharmacy Ordering Platform

Thanks for wanting to contribute! This project is maintained with an academic but
production‑grade discipline. Please follow the rules below.

## Code of Conduct

Be respectful and constructive. This is a shared capstone codebase.

## Getting Started

1. Fork / clone the repository.
2. Read `README.md` and `Problem_Statement.md` first.
3. Set up the environment (Maven Wrapper, MySQL 8, Node 18+).
4. Create a branch off `main`:

```bash
git checkout -b feat/your-feature
```

## Git Rules

- **Conventional Commits** are required:
  `feat:`, `fix:`, `test:`, `docs:`, `chore:`, `refactor:`, `style:`, `perf:`.
  Example: `feat: implement shopping cart`, `fix: prevent negative inventory`.
- Branch naming: `feat/…`, `fix/…`, `docs/…`.
- Commit meaningful units of work — **no meaningless commits**.
- Never commit `.env`, secrets, `target/` or `node_modules/`.

## Development Workflow

1. **Backend first.** Make changes under `backend/src`, update Flyway migrations in
   `backend/src/main/resources/db/migration` when the schema changes.
2. **Keep controllers thin** — business logic lives in `service/`.
3. **Never expose JPA entities** — communicate via `dto/`.
4. **Never trust the frontend** for pricing, authorization, or stock.
5. **Add tests** for new business logic with JUnit 5 / Mockito.
6. **Frontend** changes must integrate through the real REST APIs (no mocked flows).
7. Verify:

```bash
cd backend && mvnw.cmd test          # all tests green
cd frontend && npm run build         # production build passes
```

## Pull Request Checklist

- [ ] Backend compiles and `mvnw test` passes
- [ ] Frontend builds (`npm run build`) without warnings
- [ ] Flyway migration added for any schema change
- [ ] DTOs used at API boundaries; entities not leaked
- [ ] New business rules are covered by tests
- [ ] No secrets/hard‑coded keys; env vars used instead
- [ ] Docs updated (README, API docs) where user‑facing behaviour changed

## Style Notes

- Java: 4‑space indent, meaningful names, no dead code/unused imports.
- SQL: UPPERCASE keywords, single migration per logical change.
- JSX: default Vite conventions, components under `frontend/src/components`.

## Questions

Open an issue or discuss in your review meeting. Keep History clean — rebase rather than
merge-bomb.