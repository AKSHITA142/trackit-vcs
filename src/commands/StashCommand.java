package commands;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StashCommand {

    public static void apply() {

        File stashDir = new File(".minigit/stash");

        File[] stashes = stashDir.listFiles();

        if (stashes == null || stashes.length == 0) {
            System.out.println("No stash found.");
            return;
        }

        // Get latest stash
        File latest = stashes[stashes.length - 1];

        for (File file : latest.listFiles()) {

            File dest = new File(file.getName());

            try {
                Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.out.println("Error applying stash: " + file.getName());
            }
        }

        System.out.println("Stash applied: " + latest.getName());
    }

    public static void save() {

        File workingDir = new File(".");
        File stashDir = new File(".minigit/stash");

        if (!stashDir.exists()) {
            System.out.println("Repository not initialized.");
            return;
        }

        // Create unique stash id
        String stashId = "stash_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        File newStash = new File(stashDir, stashId);
        newStash.mkdir();

        boolean hasChanges = false;

        for (File file : workingDir.listFiles()) {

            if (file.getName().equals(".minigit")) continue;
            if (file.getName().endsWith(".java")) continue;
            if (file.getName().endsWith(".class")) continue;
            if (!file.isFile()) continue;

            try {
                Files.copy(file.toPath(),
                        new File(newStash, file.getName()).toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
                hasChanges = true;
            } catch (IOException e) {
                System.out.println("Error stashing file: " + file.getName());
            }
        }

        if (!hasChanges) {
            newStash.delete();
            System.out.println("Nothing to stash.");
            return;
        }

        // Clear working directory (only user files)
        for (File file : workingDir.listFiles()) {

            if (file.getName().equals(".minigit")) continue;
            if (file.getName().endsWith(".java")) continue;
            if (file.getName().endsWith(".class")) continue;
            if (!file.isFile()) continue;

            file.delete();
        }

        System.out.println("Changes stashed: " + stashId);
    }
}