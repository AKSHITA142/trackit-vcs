package commands;

import core.HeadManager;

import java.io.*;
import java.nio.file.*;
import java.util.Arrays;

public class CheckoutCommand {

    public static boolean hasUncommittedChanges() {

    File stagingDir = new File(".minigit/staging");
    File workingDir = new File(".");

    File[] workingFiles = workingDir.listFiles();
    if (workingFiles == null) return false;

    for (File file : workingFiles) {

        if (file.getName().equals(".minigit")) continue;
        if (file.getName().endsWith(".java")) continue;
        if (file.getName().endsWith(".class")) continue;
        if (!file.isFile()) continue;

        File stagedFile = new File(stagingDir, file.getName());

        try {
            // If file not staged → untracked or modified
            if (!stagedFile.exists()) {
                return true;
            }

            byte[] workingContent = Files.readAllBytes(file.toPath());
            byte[] stagedContent = Files.readAllBytes(stagedFile.toPath());

            if (!Arrays.equals(workingContent, stagedContent)) {
                return true;
            }

        } catch (IOException e) {
            return true;
        }
    }

    return false;
}

    public static void execute(String[] args) {

        if (args.length < 2) {
            System.out.println("Please provide branch name.");
            return;
        }

        if (hasUncommittedChanges()) {
          System.out.println("You have uncommitted changes. Please commit or stash before switching branches.");
          return;
        }

        String branchName = args[1];

        File branchFile = new File(".minigit/branches/" + branchName);

        if (!branchFile.exists()) {
            System.out.println("Branch does not exist.");
            return;
        }

        String commitId = "";

        // Read commit ID
        try (BufferedReader reader = new BufferedReader(new FileReader(branchFile))) {
            commitId = reader.readLine();
        } catch (IOException e) {
            System.out.println("Error reading branch.");
            return;
        }

        if (commitId == null || commitId.isEmpty()) {
            System.out.println("Branch has no commits yet.");
            return;
        }

        File commitDir = new File(".minigit/commits/" + commitId);

        if (!commitDir.exists()) {
            System.out.println("Commit not found.");
            return;
        }

        // ✅ ONLY restore files (NO deletion)
        for (File file : commitDir.listFiles()) {

        // Skip metadata
        if (file.getName().equals("meta.txt")) continue;

        // ❗ VERY IMPORTANT: Only restore non-java files
        if (file.getName().endsWith(".java")) continue;
        if (file.getName().endsWith(".class")) continue;

        File dest = new File(file.getName());

        try {
            Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("Error restoring file: " + file.getName());
        }
    }

        // Update HEAD
        HeadManager.setCurrentBranch(branchName);

        System.out.println("Switched to branch: " + branchName);
    }
}