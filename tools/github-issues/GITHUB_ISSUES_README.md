# Creating GitHub Issues from PREPARED-ISSUES.md

This guide explains how to create GitHub issues from the `PREPARED-ISSUES.md` file.

## Prerequisites

1. **GitHub CLI installed**: Make sure `gh` is installed and in your PATH
   ```bash
   gh --version
   ```

2. **Authenticated with GitHub**: 
   ```bash
   gh auth login
   ```

3. **Repository access**: Make sure you have write access to `Sign-Softer/case-observer`

## Usage

### Test with Dry Run (Recommended First Step)

Test the script without creating any issues:

```bash
bash tools/github-issues/create_github_issues.sh --dry-run --limit 1
```

This will:
- Parse the markdown file
- Show what the first issue would look like
- **NOT create any issues**

### Create Issues in Batches

Start with a small batch to test:

```bash
# Create first 5 issues
bash tools/github-issues/create_github_issues.sh --limit 5
```

If successful, create more:

```bash
# Create next 10 issues
bash tools/github-issues/create_github_issues.sh --limit 10
```

### Create All Issues

Once you're confident it works:

```bash
# Create all issues (no limit)
bash tools/github-issues/create_github_issues.sh
```

## Script Features

- **Automatic Label Assignment**: Issues are automatically labeled based on:
  - Priority (critical, high, medium, low)
  - Component (backend, frontend, business-logic, etc.)
  - Risk level

- **Issue Format**: Each issue includes:
  - Issue ID and title
  - Impact description
  - Risk assessment
  - Fix priority
  - Code references
  - Suggested solutions

## Troubleshooting

### "gh: command not found"
- Install GitHub CLI: https://cli.github.com/
- On Windows, you may need to use Git Bash or WSL

### "Not authenticated"
```bash
gh auth login
```

### "Permission denied"
- Make sure you have write access to the repository
- Check with: `gh repo view Sign-Softer/case-observer`

### Issues not parsing correctly
- The script uses pattern matching to extract issues
- If an issue format doesn't match, it will be skipped
- Check the output for any skipped issues
- Ensure `PREPARED-ISSUES.md` follows the format specified in `.cursor/rules/prepare-issues-for-github.mdc`

## Alternative: Manual Creation

If the script doesn't work, you can create issues manually using:

```bash
gh issue create \
  --repo Sign-Softer/case-observer \
  --title "BACKEND-002: Password validation not enforced" \
  --body "Issue description here" \
  --label "bug,critical,backend"
```

## Notes

- The script processes issues in order
- If an issue creation fails, the script continues with the next one
- Check GitHub for created issues after running the script
- You can delete test issues if needed: `gh issue delete <issue-number>`

