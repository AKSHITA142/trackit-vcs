import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.security.MessageDigest;
import commands.BranchCommand;
import commands.CheckoutCommand;
import core.HeadManager;
import utils.HashUtil;

public class MiniGit {


  public static void initRepository() {

    File repo = new File(".minigit");

    // ✅ Check if repo already exists FIRST
    if (repo.exists()) {
        System.out.println("Repository already exists.");
        return;
    }

    boolean created = repo.mkdir();

    if (created) {

        // ✅ Create main folders
        new File(".minigit/objects").mkdir();
        new File(".minigit/commits").mkdir();
        new File(".minigit/staging").mkdir();

        // ✅ Create branches folder
        File branchDir = new File(".minigit/branches");
        branchDir.mkdir();

        // ✅ Create main branch file
        File mainBranch = new File(".minigit/branches/main");

        try (FileWriter writer = new FileWriter(mainBranch)) {
            writer.write("");  // initially no commit
        } catch (IOException e) {
            System.out.println("Error creating main branch.");
        }

        // ✅ Create HEAD file pointing to main
        File headFile = new File(".minigit/HEAD");

        try (FileWriter writer = new FileWriter(headFile)) {
            writer.write("main");
        } catch (IOException e) {
            System.out.println("Error setting HEAD.");
        }

        System.out.println("Repository initialized successfully at: " + repo.getAbsolutePath());
      
    } else {
        System.out.println("Failed to initialize repository.");
    }
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

    // 🔥 Build data for hashing
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

    // ✅ Update branch pointer
    String currentBranch = HeadManager.getCurrentBranch();
    File branchFile = new File(".minigit/branches/" + currentBranch);

    try (FileWriter writer = new FileWriter(branchFile)) {
        writer.write(commitId);
    } catch (IOException e) {
        System.out.println("Error updating branch.");
    }

    System.out.println("Commit successful with ID: " + commitId.substring(0, 7));
}


  //showlog()

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
        
      default:
        System.out.println("Unknown command: " + command);
    }
  }
}