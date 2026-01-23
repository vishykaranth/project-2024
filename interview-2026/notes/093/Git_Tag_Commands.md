# Git Tag Commands - Getting Tag Details

## Basic Tag Commands

### 1. List All Tags
```bash
# List all tags
git tag

# List tags with pattern matching
git tag -l "v1.*"

# List tags sorted by version
git tag -l --sort=-version:refname
```

### 2. Show Tag Details

#### Show Specific Tag Information
```bash
# Show tag details (commit, author, date, message)
git show <tagname>

# Show tag details in one line
git show <tagname> --oneline

# Show tag details with full diff
git show <tagname> --stat
```

#### Show Tag with Commit Information
```bash
# Show what commit a tag points to
git rev-parse <tagname>

# Show tag and commit details
git log -1 <tagname>

# Show tag with commit message
git log -1 --pretty=format:"%h - %an, %ar : %s" <tagname>
```

### 3. Detailed Tag Information

#### Show Tag Metadata
```bash
# Show tag object details
git cat-file -p <tagname>

# Show tag type and size
git cat-file -t <tagname>
git cat-file -s <tagname>
```

#### Show Tag Annotation (for annotated tags)
```bash
# Show tag annotation details
git show-ref --tags -d | grep <tagname>

# Show tagger information
git show <tagname> --format="%T %t %s" --no-patch
```

### 4. List Tags with Details

#### List Tags with Commit Info
```bash
# List tags with commit hash and message
git tag -l --format='%(refname:short) %(objectname:short) %(subject)'

# List tags with author and date
git tag -l --format='%(refname:short) - %(taggerdate:short) - %(subject)'

# List tags with full details
git tag -l --format='Tag: %(refname:short)%0aCommit: %(objectname:short)%0aAuthor: %(taggername) <%(taggeremail)>%0aDate: %(taggerdate)%0aMessage: %(subject)%0a'
```

### 5. Compare Tags

```bash
# Show commits between two tags
git log <tag1>..<tag2>

# Show diff between two tags
git diff <tag1> <tag2>

# Show files changed between tags
git diff --name-only <tag1> <tag2>

# Show statistics between tags
git diff --stat <tag1> <tag2>
```

### 6. Tag Information in Different Formats

```bash
# Show tag in JSON-like format
git for-each-ref --format='{"tag":"%(refname:short)","commit":"%(objectname:short)","date":"%(taggerdate)","message":"%(subject)"}' refs/tags/

# Show tags with dates sorted
git tag -l --sort=-taggerdate

# Show tags with creation date
git for-each-ref --sort=-taggerdate --format='%(refname:short) - %(taggerdate:short)' refs/tags/
```

### 7. Verify Tag Signature (for signed tags)

```bash
# Verify tag signature
git tag -v <tagname>

# List all signed tags
git tag -v $(git tag -l)
```

### 8. Find Tags for Specific Commit

```bash
# Show which tags point to current commit
git describe --tags

# Show tags containing a specific commit
git tag --contains <commit-hash>

# Show tags pointing at specific commit
git tag --points-at <commit-hash>
```

## Practical Examples

### Example 1: Get All Tag Details
```bash
# Comprehensive tag information
git for-each-ref --format='Tag: %(refname:short)
Commit: %(objectname:short)
Author: %(taggername) <%(taggeremail)>
Date: %(taggerdate:iso)
Message: %(subject)
---' refs/tags/
```

### Example 2: Get Latest Tag Details
```bash
# Get latest tag
LATEST_TAG=$(git describe --tags --abbrev=0)

# Show latest tag details
git show $LATEST_TAG

# Show latest tag commit
git log -1 $LATEST_TAG
```

### Example 3: Get Tag Details as JSON
```bash
# JSON format
git for-each-ref --format='{
  "tag": "%(refname:short)",
  "commit": "%(objectname:short)",
  "fullCommit": "%(objectname)",
  "tagger": "%(taggername)",
  "email": "%(taggeremail)",
  "date": "%(taggerdate:iso8601)",
  "message": "%(subject)"
}' refs/tags/ | jq .
```

### Example 4: Compare Current Branch with Latest Tag
```bash
# Show commits since latest tag
git log $(git describe --tags --abbrev=0)..HEAD --oneline

# Show what changed since latest tag
git diff $(git describe --tags --abbrev=0)..HEAD --stat
```

## Quick Reference

| Command | Description |
|---------|-------------|
| `git tag` | List all tags |
| `git tag -l "pattern"` | List tags matching pattern |
| `git show <tagname>` | Show tag details |
| `git show-ref --tags` | Show all tag references |
| `git describe --tags` | Show closest tag |
| `git tag --contains <commit>` | Tags containing commit |
| `git tag --points-at <commit>` | Tags pointing at commit |
| `git log <tag1>..<tag2>` | Commits between tags |
| `git diff <tag1> <tag2>` | Diff between tags |
| `git for-each-ref refs/tags/` | Detailed tag information |

## Most Useful Commands

### Get Complete Tag Information
```bash
git show <tagname>
```

### List All Tags with Details
```bash
git tag -l --format='%(refname:short) - %(taggerdate:short) - %(subject)'
```

### Get Latest Tag
```bash
git describe --tags --abbrev=0
```

### Show What Changed Since Tag
```bash
git log $(git describe --tags --abbrev=0)..HEAD
```
