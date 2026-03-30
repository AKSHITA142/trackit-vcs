package commands;

import core.HeadManager;

import java.io.*;
import java.nio.file.*;
import java.util.Arrays;

public class CheckoutCommand {

    // Check if working directory has uncommitted changes
    public static boolean hasUncommittedChanges() {
        File stagingDir = new File(".minigit/staging");
        File workingDir = new File(".");

        File[] workingFiles = workingDir.listFiles();
        if (workingFiles == null) return false;

        for (File file : workingFiles) {
            if (file.getName().equals(".minigit") || !file.isFile()) continue;

            File stagedFile = new File(stagingDir, file.getName());

            try {
                if (!stagedFile.exists()) return true;

                byte[] workingContent = Files.readAllBytes(file.toPath());
                byte[] stagedContent = Files.readAllBytes(stagedFile.toPath());

                if (!Arrays.equals(workingContent, stagedContent)) return true;

            } catch (IOException e) {
                return true;
            }
        }
        return false;
    }

    // Execute branch checkout
    public static void execute(String[] args) {
        if (args.length < 2) {
            System.out.println("Please provide branch name.");
            return;
        }

        String branchName = args[1];
        File branchFile = new File(".minigit/branches/" + branchName);

        if (!branchFile.exists()) {
            System.out.println("Branch does not exist.");
            return;
        }

        if (hasUncommittedChanges()) {
            System.out.println("You have uncommitted changes. Please commit or stash before switching branches.");
            return;
        }

        String commitId = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(branchFile))) {
            commitId = reader.readLine();
        } catch (IOException e) {
            System.out.println("Error reading branch file.");
            return;
        }

        // ✅ If branch has no commits yet, allow checkout (empty working dir)
        if (commitId == null || commitId.isEmpty()) {
            HeadManager.setCurrentBranch(branchName);
            System.out.println("Switched to branch (no commits yet): " + branchName);
            return;
        }

        File commitDir = new File(".minigit/commits/" + commitId);
        if (!commitDir.exists()) {
            System.out.println("Commit not found: " + commitId);
            return;
        }

        // 1️⃣ Remove files not present in commit
        File[] workingFiles = new File(".").listFiles();
        if (workingFiles != null) {
            for (File file : workingFiles) {
                if (file.getName().equals(".minigit") || !file.isFile()) continue;
                File targetFile = new File(commitDir, file.getName());
                if (!targetFile.exists()) {
                    file.delete();
                }
            }
        }

        // 2️⃣ Restore all files from commit
        File[] commitFiles = commitDir.listFiles();
        if (commitFiles != null) {
            for (File file : commitFiles) {
                if (file.getName().equals("meta.txt")) continue;

                File dest = new File(file.getName());
                try {
                    Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    System.out.println("Error restoring file: " + file.getName());
                }
            }
        }

        // 3️⃣ Update HEAD
        HeadManager.setCurrentBranch(branchName);
        System.out.println("Switched to branch: " + branchName);
    }
}
