package core;

import java.io.*;

public class BranchManager {

    public static void createBranch(String branchName) {

        File branchDir = new File(".minigit/branches");

        if (!branchDir.exists()) {
            branchDir.mkdirs();
        }

        File newBranch = new File(branchDir, branchName);

        if (newBranch.exists()) {
            System.out.println("Branch already exists.");
            return;
        }

        // Get latest commit
        String latestCommit = getLatestCommit();

        try (FileWriter writer = new FileWriter(newBranch)) {
            writer.write(latestCommit);
            System.out.println("Branch created: " + branchName);
        } catch (IOException e) {
            System.out.println("Error creating branch.");
        }
    }

    public static String getLatestCommit() {

        File commitsDir = new File(".minigit/commits");
        File[] commits = commitsDir.listFiles();

        if (commits == null || commits.length == 0) {
            return "";
        }

        // Return last commit (simple approach)
        return commits[commits.length - 1].getName();
    }
}