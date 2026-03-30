import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.security.MessageDigest;
import commands.BranchCommand;
import commands.CheckoutCommand;
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

    if (repo.mkdir()) {

        // existing folders
        new File(".minigit/objects").mkdir();
        new File(".minigit/commits").mkdir();
        new File(".minigit/staging").mkdir();

        //  NEW: branches folder
        new File(".minigit/branches").mkdir();

        //  NEW: create main branch
        File mainBranch = new File(".minigit/branches/main");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(mainBranch))) {
            writer.write(""); // no commit yet
        } catch (IOException e) {
            System.out.println("Error creating main branch.");
        }

        //  NEW: set HEAD to main
        HeadManager.setCurrentBranch("main");

        System.out.println("Repository initialized successfully at: " + repo.getAbsolutePath());

    } else {
        System.out.println("Failed to initialize repository.");
    }
}
   
  //add
  public static void add(String filename){
    File file=new File(filename);
    System.out.println(file.getAbsolutePath());
    if(!file.exists()){
      System.out.println("File does not exist: " + filename);
      return;
    }
    File stagingDir=new File(".minigit/staging");

    if(!stagingDir.exists()){
      System.out.println("Staging area does not exist. Please initialize the repository first.");
      return;
    }
    File destFile=new File(stagingDir, file.getName());

    try{
      Files.copy(file.toPath(), destFile.toPath(),StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      System.out.println("Error occurred while adding file: " + filename);
    }
  }


  //commit
 public static void commit(String message){

    File stagingDir = new File(".minigit/staging");

    if(!stagingDir.exists() || stagingDir.listFiles().length == 0){
        System.out.println("Nothing to commit.");
        return;
    }

    File commitsDir = new File(".minigit/commits");

    // 🔥 STEP 1: Build data for hashing
    StringBuilder data = new StringBuilder();
    data.append(message);

    for(File file : stagingDir.listFiles()){
        data.append(file.getName());
        try{
            data.append(new String(Files.readAllBytes(file.toPath())));
        } catch (IOException e) {
            System.out.println("Error reading file: " + file.getName());
        }
    }

    //  STEP 2: Generate hash (instead of timestamp)
    String commitId = HashUtil.generateHash(data.toString());

    //  STEP 3: Create commit folder using hash
    File newCommit = new File(commitsDir, commitId);
    newCommit.mkdir();

    //copy files from staging to commit 
    for(File file : stagingDir.listFiles()){
        File dest = new File(newCommit, file.getName());
        try{
            Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("Error occurred while committing file: " + file.getName());
        }
    }

    //save commit message + hash (better)
    try{
        FileWriter writer = new FileWriter(new File(newCommit, "meta.txt"));
        writer.write("Commit ID: " + commitId + "\n");
        writer.write("Message: " + message + "\n");
        writer.close();
    } catch (IOException e) {
        System.out.println("Error occurred while saving commit message.");
    }

    //clear staging area
    for(File file : stagingDir.listFiles()){
        file.delete();
    }

    System.out.println("Commit successful with ID: " + commitId.substring(0,7));
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

    // Sort commits (latest first)
    Arrays.sort(commits, (a, b) -> b.getName().compareTo(a.getName()));

    for (File commit : commits) {

        File messageFile = new File(commit, "message.txt");

        String message = "No message";

        try {
            BufferedReader reader = new BufferedReader(new FileReader(messageFile));
            message = reader.readLine();
            reader.close();
        } catch (IOException e) {
            System.out.println("Error reading commit message.");
        }

        String commitId = commit.getName();
        String shortId = commitId.length() > 7 ? commitId.substring(0, 7) : commitId;

      System.out.println("commit " + shortId);
      System.out.println("--------------------------------");
      System.out.println("Message: " + message);
      System.out.println();
    }
}


//status

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

    File stagingDir = new File(".minigit/staging");
    File workingDir = new File(".");

    //  Staged Files
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

    //  Untracked Files
    System.out.println("\nUntracked Files:");
    File[] workingFiles = workingDir.listFiles();

    boolean foundUntracked = false;

    if (workingFiles != null) {
        for (File file : workingFiles) {

            if (file.getName().equals(".minigit")) continue;
            if (file.getName().endsWith(".java")) continue;
            if (file.getName().endsWith(".class")) continue;
            if (file.isDirectory()) continue;

            if (!stagedNames.contains(file.getName())) {
                System.out.println("- " + file.getName());
                foundUntracked = true;
            }
        }
    }

    if (!foundUntracked) {
        System.out.println("No untracked files.");
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
        
      default:
        System.out.println("Unknown command: " + command);
    }
  }
}