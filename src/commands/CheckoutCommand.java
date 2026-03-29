package commands;

import core.HeadManager;

import java.io.*;
import java.nio.file.*;

public class CheckoutCommand {

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

        String commitId = "";

        // Read commit ID
        try (BufferedReader reader = new BufferedReader(new FileReader(branchFile))) {
            commitId = reader.readLine();
        } catch (IOException e) {
            System.out.println("Error reading branch.");
            return;
        }

        File commitDir = new File(".minigit/commits/" + commitId);

        if (!commitDir.exists()) {
            System.out.println("Commit not found.");
            return;
        }

        // 🔥 Clear working directory (except .minigit)
        File workingDir = new File(".");
        for (File file : workingDir.listFiles()) {
            if (file.getName().equals(".minigit")) continue;
            if (file.isFile()) file.delete();
        }

        // 🔥 Copy commit files to working dir
        for (File file : commitDir.listFiles()) {

            if (file.getName().equals("message.txt")) continue;

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