# MiniGit Test Plan

## Commands to Test:

1. `init` - Initialize repository
2. `add` - Stage files
3. `commit` - Create commits
4. `status` - Show repository status
5. `log` - Show commit history
6. `branch` - Create/list branches
7. `checkout` - Switch branches
8. `merge` - Merge branches
9. `stash` - Save/restore changes
10. `reset` - Reset to specific commit

---

## Phase 1: Basic Functionality (Individual Tests)

### Test 1.1: Init

```bash
minigit init
# Expected: Repository initialized
# Check: .minigit/ folder created with all subdirectories
```

### Test 1.2: Add & Commit

```bash
echo "Hello" > file1.txt
minigit add file1.txt
minigit commit "First commit"
# Expected: file1.txt stored in commit
# Check: .minigit/commits/[hash]/ contains file1.txt
```

### Test 1.3: Status (Modified Files)

```bash
echo "World" >> file1.txt
minigit status
# Expected:
# - Staged Files: (none)
# - Modified Files: file1.txt
# - Untracked Files: (none)
```

### Test 1.4: Status (Untracked Files)

```bash
echo "New" > file2.txt
minigit status
# Expected:
# - Staged Files: (none)
# - Modified Files: file1.txt
# - Untracked Files: file2.txt
```

### Test 1.5: Log

```bash
minigit log
# Expected: Shows commit with message "First commit"
```

---

## Phase 2: Branching Functionality

### Test 2.1: Create Branch

```bash
minigit branch feature
minigit branch
# Expected: Shows "main" and "feature" branches
```

### Test 2.2: Checkout Branch

```bash
minigit checkout feature
minigit status
# Expected: Shows "Current Branch: feature"
```

### Test 2.3: Commit on Different Branch

```bash
echo "Feature" > feature.txt
minigit add feature.txt
minigit commit "Feature work"
minigit log
# Expected: Shows "Feature work" commit on feature branch
```

### Test 2.4: Switch Back to Main

```bash
minigit checkout main
minigit status
# Expected:
# - file1.txt still there
# - feature.txt NOT there (different branch)
# - Current Branch: main
```

---

## Phase 3: Merge Functionality

### Test 3.1: Merge Feature into Main

```bash
minigit merge feature
minigit status
# Expected:
# - feature.txt now in main branch
# - Can see both file1.txt and feature.txt
```

### Test 3.2: Log After Merge

```bash
minigit log
# Expected: Shows both "First commit" and "Feature work"
```

---

## Phase 4: Stash Functionality

### Test 4.1: Stash Changes

```bash
echo "Temporary" > temp.txt
minigit add temp.txt
minigit stash
minigit status
# Expected: temp.txt disappears from working directory
```

### Test 4.2: Apply Stash

```bash
minigit stash apply
minigit status
# Expected: temp.txt reappears in staging
```

---

## Phase 5: Reset Functionality

### Test 5.1: Create Multiple Commits

```bash
echo "v1" > version.txt
minigit add version.txt
minigit commit "Version 1"

echo "v2" > version.txt
minigit add version.txt
minigit commit "Version 2"

minigit log
# Expected: Shows both commits, can see commit IDs
```

### Test 5.2: Reset to First Commit

```bash
minigit reset [commit_id_1]
minigit status
# Expected: version.txt contains "v1"
```

### Test 5.3: Verify Reset Worked

```bash
cat version.txt
# Expected: Output is "v1"
```

---

## Phase 6: Complex Integration Tests

### Test 6.1: Full Workflow

```bash
# 1. Create 2 branches
minigit branch develop

# 2. Make commits on main
echo "Main1" > main.txt
minigit add main.txt
minigit commit "Main work 1"

# 3. Switch to develop
minigit checkout develop

# 4. Make commits on develop
echo "Dev1" > dev.txt
minigit add dev.txt
minigit commit "Dev work 1"

# 5. Merge back to main
minigit checkout main
minigit merge develop

# 6. Check status
minigit status
# Expected: Both main.txt and dev.txt present
```

### Test 6.2: Stash on Different Branch

```bash
minigit checkout develop

# Add unstaged changes
echo "Temp" > temp2.txt

# Switch branch (should fail or handle gracefully)
minigit checkout main
```

### Test 6.3: Reset After Merge

```bash
# Get oldest commit ID from log
minigit log

# Reset to initial commit
minigit reset [initial_commit_id]

# Check only initial files exist
minigit status
```

---

## Issues to Check:

- [ ] Status shows modified files correctly (comparing with last commit)
- [ ] Status shows untracked files correctly (not in commits)
- [ ] Commits create complete snapshots (all files copied)
- [ ] Switching branches restores correct files
- [ ] Merge combines files from both branches
- [ ] Reset restores entire commit state
- [ ] Stash saves and applies correctly
- [ ] Log shows all commits in order
- [ ] No files are accidentally deleted
- [ ] Branch switching doesn't lose uncommitted changes
- [ ] Multiple commits on same branch work correctly
