# ArabicNotepad

A Java (Swing) desktop notepad focused on **Arabic-first writing**, **Markdown preview**, and **document analytics**. ArabicNotepad stores documents as “books” composed of “pages” and supports both **local file storage** and a **MySQL-backed** storage implementation.

> Status: active development / coursework project. The UI and core layers are implemented; see “Build status” below for the current Maven dependency caveat.

## Features

- **Book-based editing**: Create, import, export, and delete documents (“books”).
- **Pagination model**: Large documents are represented and navigated as pages.
- **Markdown support**: Editing with a rendered Markdown view.
- **Search**: Search books by content.
- **Text analytics**: TF‑IDF plus additional analysis options exposed in the UI.
- **Transliteration utilities**: Helpers for Arabic script transliteration.
- **Multiple environments**: Development/Testing/Production/Remote configuration sets.
- **Security hardening**: Input validation, path traversal protection, and SQL injection protections.

## Architecture

ArabicNotepad follows a layered design:

- **UI (Swing)**: `ui.*` and `ui.components.*`
- **Business layer**: `bl.*` (`BookFacade`, `BookService`)
- **Data access layer**: `dao.*` (DAO interface + implementations)

Storage backends are abstracted via `BookDAO` and `BookDAOFactory`. Current implementations include:

- `dao.LocalStorageBookDAO` (Markdown files on disk)
- `dao.MySQLBookDAO` (MySQL database)

The entry point is `src/main/java/Main/Main.java`.

## Tech Stack

- Java **22**
- Maven
- Swing UI
- MySQL (optional; required for DB backend)
- Lucene / Stanford CoreNLP (analysis/search helpers)

## Build Status (Important)

At the time of this README update, `mvn test` fails in a fresh environment because the dependency
`alkhalil:AlKhalilMorphoSys2:1.0.0` is **not available from Maven Central**, so Maven cannot resolve it.

If you have access to that library, you can:

- Install it into your local Maven repository, or
- Add a reachable repository that hosts it, or
- Temporarily remove/replace that dependency for CI builds.

## Getting Started

### Prerequisites

- Java 22+
- Maven 3.6+

Optional (for MySQL backend):

- MySQL 8+

### Clone

```bash
git clone https://github.com/AbubakarMahmood1/ArabicNotepad.git
cd ArabicNotepad
```

### Configure (DB credentials)

Credentials must not be committed. Use environment variables (recommended) or a local `.env` file.

```bash
cp .env.example .env
```

Environment variables:

- `DB_USERNAME`
- `DB_PASSWORD`
- `DB_URL` (example: `jdbc:mysql://localhost:3306/arabic_notepad`)
- `ENVIRONMENT` (optional): `DEVELOPMENT`, `TESTING`, `PRODUCTION`, `REMOTE`

### Build

```bash
mvn clean package
```

### Run

```bash
mvn exec:java -Dexec.mainClass="Main.Main"
```

Or run the assembled JAR:

```bash
java -jar target/ArabicNotepad-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Project Layout

```text
src/
  main/java/
    Main/                 # App entry point
    ui/                   # Swing UI
    bl/                   # Business layer (facade/service)
    dao/                  # Storage backends
    dto/                  # Book/Page model
    util/                 # Markdown + analysis + security utilities
  main/resources/
    development|testing|production|remote/  # Environment configs
  test/java/              # JUnit tests
```

## Security Notes

- **No hardcoded credentials**: Use env vars / `.env`.
- **Path traversal protections**: Book titles are validated to prevent unsafe file paths.
- **SQL injection mitigations**: Parameterized statements + input constraints.

If you discover a security issue, please open a GitHub Security Advisory or follow `SECURITY.md`.

## Contributing

Issues and PRs are welcome.

- Keep changes focused and add tests where appropriate.
- Do not commit secrets (`.env` is intentionally ignored).

## License

MIT — see `LICENSE`.
