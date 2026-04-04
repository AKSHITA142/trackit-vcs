import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.security.MessageDigest;
import commands.BranchCommand;
import commands.CheckoutCommand;
import commands.StashCommand;
import core.HeadManager;
import utils.HashUtil;
import commands.MergeCommand;

public class MiniGit {

    /// init

    public static void initRepository() {
        File repo = new File(".minigit");

        if (repo.exists()) {
            System.out.println("Repository already exists.");
            return;
        }

        boolean created = repo.mkdir();
        if (!created) {
            System.out.println("Failed to initialize repository.");
            return;
        }

        // Create main folders
        new File(".minigit/objects").mkdir();
        new File(".minigit/commits").mkdir();
        new File(".minigit/staging").mkdir();
        new File(".minigit/stash").mkdir();
        new File(".minigit/branches").mkdir();

        // Create HEAD file pointing to main
        File headFile = new File(".minigit/HEAD");
        try (FileWriter writer = new FileWriter(headFile)) {
            writer.write("main");
        } catch (IOException e) {
            System.out.println("Error setting HEAD.");
        }

        // Create main branch file
        File mainBranch = new File(".minigit/branches/main");

        try {
            // Step 1: create initial empty commit
            String initialCommitId = createInitialCommit();
            // Step 2: write commit ID to main branch
            try (FileWriter writer = new FileWriter(mainBranch)) {
                writer.write(initialCommitId);
            }
        } catch (IOException e) {
            System.out.println("Error creating main branch.");
        }

        System.out.println("Repository initialized successfully at: " + repo.getAbsolutePath());
    }

    /**
     * Creates an initial empty commit to avoid empty branches.
     */
    private static String createInitialCommit() throws IOException {
        // Generate a unique commit ID (could be timestamp-based)
        String commitId = "c" + System.currentTimeMillis();

        File commitDir = new File(".minigit/commits/" + commitId);
        commitDir.mkdir();

        // Optional: create meta file to store commit message
        File meta = new File(commitDir, "meta.txt");
        try (FileWriter writer = new FileWriter(meta)) {
            writer.write("Initial commit\n");
        }

        return commitId;
    }

    public static void add(String filename) {

        File file = new File(filename);

        if (!file.exists()) {
            System.out.println("File does not exist: " + filename);
            return;
        }

        File stagingDir = new File(".minigit/staging");

        if (!stagingDir.exists()) {
            System.out.println("Repository not initialized.");
            return;
        }

        File destFile = new File(stagingDir, file.getName());

        try {
            Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Added file: " + filename);
        } catch (IOException e) {
            System.out.println("Error adding file.");
        }
    }

    public static void commit(String message) {

        File stagingDir = new File(".minigit/staging");

        File[] stagedFiles = stagingDir.listFiles();

        if (stagedFiles == null || stagedFiles.length == 0) {
            System.out.println("Nothing to commit.");
            return;
        }

        File commitsDir = new File(".minigit/commits");

        // Build data for hashing
        StringBuilder data = new StringBuilder();
        data.append(message);

        for (File file : stagedFiles) {
            data.append(file.getName());
            try {
                data.append(new String(Files.readAllBytes(file.toPath())));
            } catch (IOException e) {
                System.out.println("Error reading file: " + file.getName());
            }
        }

        String commitId = HashUtil.generateHash(data.toString());

        File newCommit = new File(commitsDir, commitId);
        newCommit.mkdir();

        // Copy files
        for (File file : stagedFiles) {
            File dest = new File(newCommit, file.getName());
            try {
                Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.out.println("Error committing file: " + file.getName());
            }
        }

        // Save meta
        try (FileWriter writer = new FileWriter(new File(newCommit, "meta.txt"))) {
            writer.write("Commit ID: " + commitId + "\n");
            writer.write("Message: " + message + "\n");
        } catch (IOException e) {
            System.out.println("Error saving commit.");
        }

        // Clear staging
        for (File file : stagedFiles) {
            file.delete();
        }

        // Update branch pointer
        String currentBranch = HeadManager.getCurrentBranch();
        File branchFile = new File(".minigit/branches/" + currentBranch);

        try (FileWriter writer = new FileWriter(branchFile)) {
            writer.write(commitId);
        } catch (IOException e) {
            System.out.println("Error updating branch.");
        }

        System.out.println("Commit successful with ID: " + commitId.substring(0, 7));
    }

    // showlog()

    public static void showLog() {

        File commitsDir = new File(".minigit/commits");

        if (!commitsDir.exists()) {
            System.out.println("Repository not initialized.");
            return;
        }

        File[] commits = commitsDir.listFiles();

        if (commits == null || commits.length == 0) {
            System.out.println("No commits found.");
            return;
        }

        Arrays.sort(commits, (a, b) -> b.getName().compareTo(a.getName()));

        for (File commit : commits) {

            File metaFile = new File(commit, "meta.txt");

            String message = "No message";

            try (BufferedReader reader = new BufferedReader(new FileReader(metaFile))) {
                reader.readLine(); // skip commit id line
                message = reader.readLine();
            } catch (IOException e) {
                System.out.println("Error reading commit.");
            }

            String commitId = commit.getName();
            String shortId = commitId.length() > 7 ? commitId.substring(0, 7) : commitId;

            System.out.println("commit " + shortId);
            System.out.println("Message: " + message);
            System.out.println("-------------------------");
        }
    }

    // Helper method to get the last commit ID for current branch
    private static String getLastCommitId() throws IOException {
        String currentBranch = HeadManager.getCurrentBranch();
        File branchFile = new File(".minigit/branches/" + currentBranch);

        if (!branchFile.exists()) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(branchFile))) {
            return reader.readLine();
        }
    }

    // Helper method to check if file content has changed
    private static boolean fileHasChanged(String filename, String commitId) throws IOException {
        if (commitId == null)
            return false;

        File workingFile = new File(filename);
        File committedFile = new File(".minigit/commits/" + commitId + "/" + filename);

        if (!workingFile.exists() || !committedFile.exists()) {
            return false;
        }

        byte[] workingContent = Files.readAllBytes(workingFile.toPath());
        byte[] committedContent = Files.readAllBytes(committedFile.toPath());

        return !Arrays.equals(workingContent, committedContent);
    }

    // status

    public static void status() {

        File repo = new File(".minigit");
        if (!repo.exists()) {
            System.out.println("Repository not initialized.");
            return;
        }

        System.out.println("=== MiniGit Status ===\n");

        // Current Branch
        String currentBranch = HeadManager.getCurrentBranch();
        System.out.println("Current Branch: " + currentBranch + "\n");

        try {
            // Get last commit files
            String lastCommitId = getLastCommitId();
            File lastCommitDir = lastCommitId != null ? new File(".minigit/commits/" + lastCommitId) : null;
            Set<String> committedFiles = new HashSet<>();

            if (lastCommitDir != null && lastCommitDir.exists()) {
                File[] files = lastCommitDir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (!f.getName().equals("meta.txt")) {
                            committedFiles.add(f.getName());
                        }
                    }
                }
            }

            File stagingDir = new File(".minigit/staging");
            File workingDir = new File(".");

            // Staged Files
            System.out.println("Staged Files:");
            File[] stagedFiles = stagingDir.listFiles();

            Set<String> stagedNames = new HashSet<>();

            if (stagedFiles != null && stagedFiles.length > 0) {
                for (File file : stagedFiles) {
                    System.out.println("- " + file.getName());
                    stagedNames.add(file.getName());
                }
            } else {
                System.out.println("No files staged.");
            }

            // Modified Files (tracked but changed)
            System.out.println("\nModified Files:");
            boolean foundModified = false;

            for (String filename : committedFiles) {
                File workingFile = new File(workingDir, filename);

                // Skip if it's staged
                if (stagedNames.contains(filename))
                    continue;

                // Check if file exists and has changed
                if (workingFile.exists()) {
                    try {
                        if (fileHasChanged(filename, lastCommitId)) {
                            System.out.println("- " + filename);
                            foundModified = true;
                        }
                    } catch (IOException e) {
                        // Skip files with read errors
                    }
                }
            }

            if (!foundModified) {
                System.out.println("No files modified.");
            }

            // Untracked Files
            System.out.println("\nUntracked Files:");
            File[] workingFiles = workingDir.listFiles();

            boolean foundUntracked = false;

            if (workingFiles != null) {
                for (File file : workingFiles) {

                    if (file.getName().equals(".minigit"))
                        continue;
                    if (file.getName().endsWith(".java"))
                        continue;
                    if (file.getName().endsWith(".class"))
                        continue;
                    if (file.isDirectory())
                        continue;

                    // Untracked = not staged AND not in committed files
                    if (!stagedNames.contains(file.getName()) && !committedFiles.contains(file.getName())) {
                        System.out.println("- " + file.getName());
                        foundUntracked = true;
                    }
                }
            }

            if (!foundUntracked) {
                System.out.println("No untracked files.");
            }

        } catch (IOException e) {
            System.out.println("Error reading status.");
        }
    }

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("No command Provided");
            return;
        }
        String command = args[0];

        switch (command) {
            case "init":
                initRepository();
                break;
            case "add":
                if (args.length < 2) {
                    System.out.println("Please provide a file name");
                } else {
                    add(args[1]);
                }
                break;
            case "commit":
                if (args.length < 2) {
                    System.out.println("Please provide a commit message");
                } else {
                    commit(args[1]);
                }
                break;
            case "status":
                System.out.println("Checking repository status...");
                status();
                break;
            case "log":
                showLog();
                break;

            case "branch":
                BranchCommand.execute(args);
                break;
            case "checkout":
                CheckoutCommand.execute(args);
                break;
            case "merge":
                MergeCommand.execute(args);
                break;
            case "stash":
                if (args.length > 1 && args[1].equals("apply")) {
                    StashCommand.apply();
                } else {
                    StashCommand.save();
                }
                break;

            default:
                System.out.println("Unknown command: " + command);
        }
    }
}