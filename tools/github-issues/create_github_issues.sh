#!/bin/bash

# Script to create GitHub issues from PREPARED-ISSUES.md
# Usage: bash tools/github-issues/create_github_issues.sh [--dry-run] [--limit N]
# Note: Run this script from the project root directory

set -e

ISSUES_FILE="PREPARED-ISSUES.md"
DRY_RUN=false
LIMIT=0
REPO="Sign-Softer/case-observer"

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        --limit)
            LIMIT="$2"
            shift 2
            ;;
        *)
            echo "Usage: $0 [--dry-run] [--limit N]"
            exit 1
            ;;
    esac
done

# Check prerequisites
if ! command -v gh &> /dev/null; then
    echo "Error: GitHub CLI (gh) is not installed"
    exit 1
fi

if ! gh auth status &> /dev/null; then
    echo "Error: Not authenticated. Run: gh auth login"
    exit 1
fi

if [ ! -f "$ISSUES_FILE" ]; then
    echo "Error: $ISSUES_FILE not found"
    exit 1
fi

echo "Creating GitHub issues from $ISSUES_FILE"
echo "Repository: $REPO"
[ "$DRY_RUN" = true ] && echo "DRY RUN MODE"
echo ""

# Function to get labels
get_labels() {
    local risk="$1"
    local priority="$2"
    local issue_id="$3"
    
    local labels=""
    
    # Priority
    if echo "$priority" | grep -qE "^1|Must fix"; then
        labels="bug,critical"
    elif echo "$priority" | grep -qE "^2"; then
        labels="bug,high"
    elif echo "$priority" | grep -qE "^3"; then
        labels="enhancement,medium"
    else
        labels="enhancement,low"
    fi
    
    # Component
    echo "$issue_id" | grep -q "^BACKEND-" && labels="$labels,backend"
    echo "$issue_id" | grep -q "^FRONTEND-" && labels="$labels,frontend"
    echo "$issue_id" | grep -q "^BUSINESS-" && labels="$labels,business-logic"
    echo "$issue_id" | grep -q "^ADDITIONAL-" && labels="$labels,additional"
    echo "$issue_id" | grep -q "^INTEGRATION-" && labels="$labels,integration"
    
    echo "$labels" | sed 's/^,//' | tr ',' '\n' | sort -u | tr '\n' ',' | sed 's/,$//'
}

# Export variables for Python script
export DRY_RUN
export LIMIT
export REPO

# Use Python for reliable markdown parsing (if available) or use a simpler approach
if command -v python3 &> /dev/null; then
    # Use Python to parse and create issues
    python3 << 'PYTHON_SCRIPT'
import re
import sys
import subprocess
import tempfile
import os

issues_file = "PREPARED-ISSUES.md"
dry_run = os.environ.get("DRY_RUN", "false") == "true"
limit = int(os.environ.get("LIMIT", "0"))
repo = os.environ.get("REPO", "Sign-Softer/case-observer")

def create_label_if_not_exists(repo, label_name, color="0E8A16", description=""):
    """Create a label if it doesn't exist"""
    try:
        result = subprocess.run(
            ["gh", "label", "list", "--repo", repo, "--json", "name"],
            capture_output=True,
            text=True
        )
        existing_labels = []
        if result.returncode == 0:
            import json
            existing_labels = [l["name"] for l in json.loads(result.stdout)]
        
        if label_name not in existing_labels:
            cmd = ["gh", "label", "create", label_name, "--repo", repo, "--color", color]
            if description:
                cmd.extend(["--description", description])
            subprocess.run(cmd, capture_output=True, text=True, check=False)
    except Exception:
        pass  # Ignore errors, continue anyway

def get_labels(risk, priority, issue_id):
    labels = []
    
    # Priority-based labels (using simple names)
    if re.match(r"^1", priority) or "Must fix" in priority:
        labels.extend(["bug", "critical"])
    elif re.match(r"^2", priority):
        labels.extend(["bug", "high"])
    elif re.match(r"^3", priority):
        labels.extend(["enhancement", "medium"])
    else:
        labels.extend(["enhancement", "low"])
    
    # Component labels
    if issue_id.startswith("BACKEND-"):
        labels.append("backend")
    elif issue_id.startswith("FRONTEND-"):
        labels.append("frontend")
    elif issue_id.startswith("BUSINESS-"):
        labels.append("business-logic")
    elif issue_id.startswith("ADDITIONAL-"):
        labels.append("additional")
    elif issue_id.startswith("INTEGRATION-"):
        labels.append("integration")
    
    return sorted(set(labels))

def get_category_from_issue_id(issue_id):
    """Determine category from issue ID prefix"""
    if issue_id.startswith("BACKEND-"):
        return "Backend"
    elif issue_id.startswith("FRONTEND-"):
        return "Frontend"
    elif issue_id.startswith("BUSINESS-"):
        return "Business Logic"
    elif issue_id.startswith("ADDITIONAL-"):
        return "Additional"
    elif issue_id.startswith("INTEGRATION-"):
        return "Integration"
    return "General"

def get_use_case_from_section(content, issue_start_pos):
    """Find which use case section this issue belongs to"""
    # Look backwards from issue position to find the use case header
    before_issue = content[:issue_start_pos]
    
    # Find the last "## Use Case" header
    use_case_match = re.search(r'## Use Case \d+: (.+?)(?=\n|$)', before_issue)
    if use_case_match:
        return use_case_match.group(1).strip()
    
    # Check for cross-cutting issues
    if "Cross-Cutting Issues" in before_issue:
        # Find the subsection
        if "Authentication & Security" in before_issue[-500:]:
            return "Cross-Cutting: Authentication & Security"
        elif "Error Handling" in before_issue[-500:]:
            return "Cross-Cutting: Error Handling"
        elif "Performance" in before_issue[-500:]:
            return "Cross-Cutting: Performance"
    
    return "General"

def parse_issues():
    with open(issues_file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Split by issue separator (--- on its own line)
    issues = re.split(r'\n---\n+', content)
    
    issue_count = 0
    for issue_text in issues:
        # Extract issue ID and title
        match = re.search(r'^- \*\*([A-Z]+-[0-9]+)\*\*: (.+)$', issue_text, re.MULTILINE)
        if not match:
            continue
        
        issue_id = match.group(1)
        title = match.group(2).strip()
        
        # Find use case by searching in the full content
        issue_pos = content.find(issue_text)
        use_case = get_use_case_from_section(content, issue_pos)
        category = get_category_from_issue_id(issue_id)
        
        # Extract fields - they're on the same line after the colon
        impact_match = re.search(r'^- \*\*Impact\*\*: (.+?)(?=\n- \*\*|\n---|$)', issue_text, re.MULTILINE | re.DOTALL)
        impact = impact_match.group(1).strip() if impact_match else ""
        
        risk_match = re.search(r'^- \*\*Risk\*\*: (.+?)(?=\n- \*\*|\n---|$)', issue_text, re.MULTILINE | re.DOTALL)
        risk = risk_match.group(1).strip() if risk_match else ""
        
        priority_match = re.search(r'^- \*\*Fix Priority\*\*: (.+?)(?=\n- \*\*|\n---|$)', issue_text, re.MULTILINE | re.DOTALL)
        priority = priority_match.group(1).strip() if priority_match else ""
        
        # Extract priority level for display
        priority_level = "CRITICAL"
        if "HIGH" in risk.upper():
            priority_level = "HIGH"
        elif "MEDIUM" in risk.upper():
            priority_level = "MEDIUM"
        elif "LOW" in risk.upper():
            priority_level = "LOW"
        
        # Extract code reference (everything between "Code reference:" and "Suggested solution:")
        # Find the positions
        code_ref_marker = "- **Code reference**:"
        solution_marker = "- **Suggested solution**:"
        
        code_ref_start_idx = issue_text.find(code_ref_marker)
        solution_start_idx = issue_text.find(solution_marker)
        
        code_ref = ""
        if code_ref_start_idx != -1:
            # Extract from after the marker to before solution marker
            start_pos = code_ref_start_idx + len(code_ref_marker)
            if solution_start_idx != -1:
                end_pos = solution_start_idx
            else:
                # Find next "---" separator
                end_pos = issue_text.find("\n---", code_ref_start_idx)
                if end_pos == -1:
                    end_pos = len(issue_text)
            
            code_ref = issue_text[start_pos:end_pos].strip()
            # Remove leading newlines
            code_ref = re.sub(r'^\n+', '', code_ref)
        
        # Extract solution (everything after "Suggested solution:" until next --- or end)
        solution = ""
        if solution_start_idx != -1:
            # Extract from after the marker
            start_pos = solution_start_idx + len(solution_marker)
            # Find next "---" separator
            end_pos = issue_text.find("\n---", solution_start_idx)
            if end_pos == -1:
                end_pos = len(issue_text)
            
            solution = issue_text[start_pos:end_pos].strip()
            # Remove leading newlines
            solution = re.sub(r'^\n+', '', solution)
        
        issue_count += 1
        if limit > 0 and issue_count > limit:
            break
        
        # Build body with pleasant format
        body = f"**Issue ID:** {issue_id}\n\n"
        body += f"**Use Case:** {use_case}\n\n"
        body += f"**Category:** {category}\n\n"
        body += f"**Priority:** {priority_level}\n\n"
        body += "---\n\n"
        
        if impact:
            body += f"## Impact\n{impact}\n\n"
        if risk:
            body += f"## Risk\n{risk}\n\n"
        if priority:
            body += f"## Fix Priority\n{priority}\n\n"
        if code_ref:
            body += f"## Code Reference\n\n{code_ref}\n\n"
        if solution:
            body += f"## Suggested Solution\n\n{solution}\n"
        
        labels = get_labels(risk, priority, issue_id)
        issue_title = f"{issue_id}: {title}"
        
        print(f"[{issue_count}] {issue_title}")
        print(f"  Labels: {', '.join(labels)}")
        
        if not dry_run:
            # Create labels if they don't exist (with appropriate colors)
            label_colors = {
                "bug": "d73a4a",
                "enhancement": "a2eeef",
                "critical": "b60205",
                "high": "fbca04",
                "medium": "0e8a16",
                "low": "c2e0c6",
                "backend": "0052cc",
                "frontend": "7057ff",
                "business-logic": "c5def5",
                "additional": "f9d0c4",
                "integration": "d4c5f9"
            }
            
            for label in labels:
                color = label_colors.get(label, "0e8a16")
                create_label_if_not_exists(repo, label, color)
            
            with tempfile.NamedTemporaryFile(mode='w', delete=False, suffix='.md', encoding='utf-8') as f:
                f.write(body)
                temp_file = f.name
            
            try:
                # Build label arguments
                label_args = []
                for label in labels:
                    label_args.extend(["--label", label])
                
                result = subprocess.run(
                    ["gh", "issue", "create",
                     "--repo", repo,
                     "--title", issue_title,
                     "--body-file", temp_file] + label_args,
                    capture_output=True,
                    text=True
                )
                if result.returncode == 0:
                    print("  ✓ Created")
                else:
                    print(f"  ✗ Failed: {result.stderr}")
            finally:
                os.unlink(temp_file)
        else:
            print("  [DRY RUN]")
        
        print()
    
    print(f"Done! Processed {issue_count} issues.")

if __name__ == "__main__":
    parse_issues()
PYTHON_SCRIPT
else
    echo "Python3 not found. Please install Python3 or use a different method."
    exit 1
fi
