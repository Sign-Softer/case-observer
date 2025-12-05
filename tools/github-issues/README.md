# GitHub Issues Creation Tools

This directory contains tools for automatically creating GitHub issues from prepared markdown files.

## Files

- **`create_github_issues.sh`**: Bash script that parses `PREPARED-ISSUES.md` and creates GitHub issues
- **`GITHUB_ISSUES_README.md`**: Detailed usage guide for the script

## Quick Start

1. **Prepare issues** using the Cursor rule `prepare-issues-for-github` - issues will be written to `PREPARED-ISSUES.md` in the project root

2. **Test with dry run**:
   ```bash
   bash tools/github-issues/create_github_issues.sh --dry-run --limit 1
   ```

3. **Create issues**:
   ```bash
   bash tools/github-issues/create_github_issues.sh --limit 5
   ```

## Issue Format

Issues must be prepared in `PREPARED-ISSUES.md` following the format specified in `.cursor/rules/prepare-issues-for-github.mdc`.

Each issue should include:
- Issue ID (e.g., `BACKEND-002`)
- Impact description
- Risk level (CRITICAL/HIGH/MEDIUM/LOW)
- Fix Priority
- Code reference (with file path and line numbers)
- Suggested solution

## Cursor Rule

Use the `prepare-issues-for-github` rule to prepare issues. The AI will automatically format them correctly and write to `PREPARED-ISSUES.md`.

