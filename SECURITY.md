# Security Policy

## Overview

ArabicNotepad takes security seriously. This document outlines our security policies, implemented protections, and how to report vulnerabilities.

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |

## Security Features

### 1. Credential Management

**Problem**: Hardcoded database credentials in configuration files
**Solution**: Environment variable-based credential management

- **Environment Variables**: `DB_USERNAME`, `DB_PASSWORD`, `DB_URL`
- **Priority**: Environment variables override configuration files
- **Production**: MUST use environment variables only
- **Warning**: Application logs warning if using file-based credentials in production

**Implementation**: `config/DBConfig.java`

```java
// Checks environment variables first, then falls back to properties
@Override
public String getProperty(String key) {
    String envValue = getFromEnvironment(key);
    if (envValue != null) {
        return envValue;
    }
    // Fall back to properties file
    return properties.getProperty(key);
}
```

### 2. Path Traversal Protection

**Problem**: User-provided book titles used directly in file paths
**Solution**: Comprehensive path validation and sanitization

- **Validation**: Book titles checked for path separators (`/`, `\`, `..`)
- **Sanitization**: Invalid characters removed or replaced
- **Length Limits**: Maximum 255 characters for filenames
- **Canonical Path Checking**: Ensures files stay within allowed directories

**Implementation**: `util/PathSecurityUtil.java`

Key methods:
- `validateBookTitle()` - Validates title constraints
- `sanitizeFilename()` - Removes/replaces invalid characters
- `createSafeFile()` - Creates files with validated paths
- `isPathWithinDirectory()` - Prevents directory traversal

**Protected Operations**:
- `LocalStorageBookDAO.addBook()` - Line 191-220
- `LocalStorageBookDAO.updateBook()` - Line 237-255
- `ArabicNotepadUI.onCreateBookAction()` - Line 280-291

### 3. SQL Injection Protection

**Status**: Already well-implemented with additional improvements

**Existing Protection**:
- All queries use `PreparedStatement` with parameterized queries
- No string concatenation in SQL statements

**New Protection - LIKE Query Sanitization**:
- Special LIKE wildcards (`%`, `_`, `\`) are now escaped
- Search text length validation (max 500 characters)
- Explicit ESCAPE clause in SQL

**Implementation**: `util/SQLSecurityUtil.java`

```java
// Escapes %, _, and \ characters
public static String escapeLikePattern(String input) {
    String escaped = input.replace("\\", "\\\\");
    escaped = escaped.replace("%", "\\%");
    escaped = escaped.replace("_", "\\_");
    return escaped;
}
```

**Protected Methods**:
- `MySQLBookDAO.searchBooksByContent()` - Line 314-359

### 4. Input Validation

**Book Titles**:
- Cannot be null or empty
- Maximum 255 characters
- No path separators (`/`, `\`)
- No path traversal sequences (`..`)
- No reserved Windows names (CON, PRN, AUX, etc.)

**Search Queries**:
- Maximum 500 characters
- LIKE wildcards properly escaped
- Invalid patterns rejected with empty results

**Implementation Locations**:
- UI Layer: `ArabicNotepadUI.onCreateBookAction()`
- DAO Layer: `LocalStorageBookDAO.addBook()`, `updateBook()`
- Search: `MySQLBookDAO.searchBooksByContent()`

### 5. Resource Management

**Problem**: ResultSet not in try-with-resources (potential leak)
**Solution**: Nested try-with-resources for all JDBC objects

**Fixed**:
- `MySQLBookDAO.getBookByName()` - Line 132-155

```java
// Before (LEAK):
ResultSet rs = pstmt.executeQuery();

// After (SAFE):
try (ResultSet rs = pstmt.executeQuery()) {
    // Use rs
}
```

### 6. Serialization Safety

**Status**: Already properly implemented

Both DTOs have proper `serialVersionUID`:
- `dto.Book` - Line 9
- `dto.Page` - Line 8

## Security Best Practices for Developers

### Do's ✅

1. **Always use environment variables** for sensitive configuration
2. **Validate all user input** before processing
3. **Use parameterized queries** for all SQL operations
4. **Close resources** with try-with-resources
5. **Log security events** (failed validations, suspicious input)
6. **Sanitize file paths** before file operations
7. **Limit input lengths** to prevent DoS

### Don'ts ❌

1. **Never hardcode credentials** in source files
2. **Never concatenate SQL** with user input
3. **Never trust user input** for file paths
4. **Never commit `.env` files** to version control
5. **Never ignore validation errors** silently
6. **Never expose stack traces** to end users
7. **Never use default/weak passwords** in examples

## Vulnerability Reporting

If you discover a security vulnerability in ArabicNotepad:

1. **Do NOT** create a public GitHub issue
2. **Email** security@example.com with details:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fix (if any)
3. Allow **48-72 hours** for initial response
4. We will coordinate disclosure timeline

## Security Checklist for Deployment

Before deploying to production:

- [ ] Environment variables set (`DB_USERNAME`, `DB_PASSWORD`, `DB_URL`)
- [ ] No hardcoded credentials in configuration files
- [ ] Database uses strong passwords
- [ ] Application runs with minimal necessary permissions
- [ ] File storage directories have appropriate permissions
- [ ] Logging configured to capture security events
- [ ] Regular security updates applied to dependencies
- [ ] MySQL configured with secure defaults
- [ ] Backups configured and tested
- [ ] Network access restricted to necessary ports only

## Security Audit Log

| Date | Version | Change | Severity |
|------|---------|--------|----------|
| 2025-11-17 | 1.0 | Removed hardcoded database credentials | Critical |
| 2025-11-17 | 1.0 | Added path traversal protection | Critical |
| 2025-11-17 | 1.0 | Fixed ResultSet resource leak | Medium |
| 2025-11-17 | 1.0 | Added LIKE pattern sanitization | Medium |
| 2025-11-17 | 1.0 | Enhanced input validation | Medium |

## Dependencies Security

Run regular security scans:

```bash
# Check for known vulnerabilities in dependencies
mvn dependency-check:check

# Update dependencies
mvn versions:display-dependency-updates
```

Key dependencies and versions:
- MySQL Connector/J: 9.0.0
- SLF4J: 2.0.16
- Logback: 1.5.12
- JUnit Jupiter: 5.11.3

## Additional Resources

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [OWASP SQL Injection Prevention](https://cheatsheetseries.owasp.org/cheatsheets/SQL_Injection_Prevention_Cheat_Sheet.html)
- [OWASP Path Traversal](https://owasp.org/www-community/attacks/Path_Traversal)
- [Java Security Guidelines](https://www.oracle.com/java/technologies/javase/seccodeguide.html)

## Contact

For security concerns: security@example.com
For general questions: See README.md

---

Last Updated: 2025-11-17
Version: 1.0
