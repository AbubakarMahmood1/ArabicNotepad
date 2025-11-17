[ArabicNotepad]

ArabicNotepad is a Java-based text editor designed to handle Arabic and markdown content, providing users with basic text formatting, file management, and analytics tools. The application supports CRUD operations for managing files/books, integrates with MySQL for data storage, and offers advanced features like pagination, transliteration, and TF-IDF analytics. It is built for Windows, with potential to expand to a web version in the future, supporting multiple presentation layers.

[Features]

Markdown Support: Basic formatting and text structuring using markdown language.
File Management: Create, read, upload, delete, and discard files and books with ease (CRUD operations).
Database Integration: MySQL is the primary database, with support for multiple databases through the use of facade and abstract factory patterns.

Analytics: Perform text analytics such as Term Frequency-Inverse Document Frequency (TF-IDF) and more to come.
Transliteration: Convert between Arabic script and other writing systems.
Pagination: Efficiently handle large files by displaying content page by page.

Basic Keyboard Shortcuts: Support for commands like:
Ctrl + C: Copy
Ctrl + V: Paste
Ctrl + X: Cut
Ctrl + Z: Undo
Ctrl + Y: Redo

Architecture: Three-layered architecture to separate concerns:
Presentation Layer (UI)
Business Logic Layer
Data Access Layer

Dependency Inversion & Injection: Minimize coupling and promote scalable code design.
Multiple Presentation Layers: Planned expansion to add a web interface alongside the Windows desktop app.

[Installation]

Prerequisites
- Java 22 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher for database management

Steps

1. Clone the repository:
```bash
git clone https://github.com/SoftwareConstructionAndDev/24f-prj-scd-21f-9462-21f9463-21f-9500.git
cd ArabicNotepad
```

2. Set up MySQL database:
```sql
CREATE DATABASE arabic_notepad;
-- Create necessary tables (see schema documentation)
```

3. Configure environment variables for security:

**IMPORTANT SECURITY NOTE**: Never hardcode credentials in configuration files!

Create a `.env` file (use `.env.example` as template):
```bash
cp .env.example .env
```

Edit `.env` with your database credentials:
```bash
# Database Configuration
DB_USERNAME=your_database_username
DB_PASSWORD=your_secure_password
DB_URL=jdbc:mysql://localhost:3306/arabic_notepad
```

**On Linux/Mac**, export environment variables:
```bash
export DB_USERNAME="your_username"
export DB_PASSWORD="your_password"
export DB_URL="jdbc:mysql://localhost:3306/arabic_notepad"
```

**On Windows**, set environment variables:
```cmd
set DB_USERNAME=your_username
set DB_PASSWORD=your_password
set DB_URL=jdbc:mysql://localhost:3306/arabic_notepad
```

Or use Windows System Properties → Environment Variables to set them permanently.

4. Build the project:
```bash
mvn clean package
```

5. Run the application:
```bash
# Run with environment variables
java -jar target/ArabicNotepad-1.0-SNAPSHOT-jar-with-dependencies.jar

# Or using Maven
mvn exec:java -Dexec.mainClass="Main.Main"
```

[Security Configuration]

This application implements several security measures:

1. **Environment Variables for Credentials**
   - Database credentials should NEVER be hardcoded
   - Always use `DB_USERNAME`, `DB_PASSWORD`, and `DB_URL` environment variables
   - The application checks environment variables first, then falls back to config files
   - Production deployment MUST use environment variables

2. **Path Traversal Protection**
   - Book titles are validated and sanitized before file operations
   - Special characters and path separators are blocked
   - File paths are validated to stay within allowed directories

3. **SQL Injection Protection**
   - All database queries use parameterized statements
   - Search inputs are validated and sanitized
   - LIKE patterns are properly escaped

4. **Input Validation**
   - Book titles limited to 255 characters
   - Search queries limited to 500 characters
   - Invalid characters are rejected or sanitized

5. **Resource Management**
   - All database connections use try-with-resources
   - ResultSets and PreparedStatements are properly closed
   - No resource leaks

[Environment Variables Reference]

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `DB_USERNAME` | Yes (Production) | - | MySQL database username |
| `DB_PASSWORD` | Yes (Production) | - | MySQL database password |
| `DB_URL` | Yes (Production) | jdbc:mysql://localhost:3306/arabic_notepad | JDBC connection URL |
| `ENVIRONMENT` | No | DEVELOPMENT | Environment mode (DEVELOPMENT, TESTING, PRODUCTION, REMOTE) |

[Usage]
Creating/Uploading Files: Use the menu options or drag-and-drop to create or upload books.
Text Editing: Use markdown syntax for formatting text.
Analytics: Analyze text using TF-IDF from the analytics panel.

[Future Enhancements]
Web-based interface for broader accessibility.
Advanced analytics features.
Multi-language support and enhanced text processing tools.

[Contributing]

We welcome contributions! Please follow these guidelines:

1. **Security First**: Never commit credentials, API keys, or sensitive data
2. **Code Quality**: Follow existing code style and add tests for new features
3. **Documentation**: Update README and JavaDoc for significant changes
4. **Pull Requests**: Include clear descriptions of changes and testing performed

Security Vulnerability Reporting:
If you discover a security vulnerability, please email security@example.com instead of creating a public issue.

[License]
This project is licensed under the MIT License - see the LICENSE file for details.