package commands;

import core.HeadManager;

import java.io.*;
import java.nio.file.*;

public class MergeCommand {

    public static void execute(String[] args) {

        if (args.length < 2) {
            System.out.println("Please provide branch name to merge.");
            return;
        }

        String targetBranch = args[1];
        String currentBranch = HeadManager.getCurrentBranch();

        if (targetBranch.equals(currentBranch)) {
            System.out.println("Cannot merge same branch.");
            return;
        }

        //  Get commit ID of target branch
        File branchFile = new File(".minigit/branches/" + targetBranch);

        if (!branchFile.exists()) {
            System.out.println("Branch not found.");
            return;
        }

        String commitId = "";

        try (BufferedReader reader = new BufferedReader(new FileReader(branchFile))) {
            commitId = reader.readLine();
        } catch (IOException e) {
            System.out.println("Error reading branch.");
            return;
        }

        if (commitId == null || commitId.isEmpty()) {
            System.out.println("Nothing to merge.");
            return;
        }

        File commitDir = new File(".minigit/commits/" + commitId);

        if (!commitDir.exists()) {
            System.out.println("Commit not found.");
            return;
        }

        //  Merge (copy files)
        for (File file : commitDir.listFiles()) {

            if (file.getName().equals("meta.txt")) continue;

            File dest = new File(file.getName());

            try {
                Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Merged: " + file.getName());
            } catch (IOException e) {
                System.out.println("Error merging file: " + file.getName());
            }
        }

        System.out.println("\nMerge completed.");
        System.out.println("Now run: add + commit");
    }
}