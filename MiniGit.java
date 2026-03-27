import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.Date;
public class MiniGit {


  public static void initRepository(){
    File repo=new File(".minigit");
    if(repo.exists()){
      System.out.println("Repository already exists.");
      return;
    }

    boolean created=repo.mkdir();
    if(created){
      new File(".minigit/objects").mkdir();
      new File(".minigit/commits").mkdir();
      new File(".minigit/staging").mkdir();

      System.out.println("Repository initialized successfully at: " + repo.getAbsolutePath());
    }
    else{
      System.out.println("Failed to initialize repository.");
    }
  }
   
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

  public static void commit(String message){

    File stagingDir=new File(".minigit/staging");

    if(!stagingDir.exists() || stagingDir.listFiles().length == 0){
      System.out.println("Nothing to commit.");
      return;
    }
    File commitsDir=new File(".minigit/commits");

    //new Commit Folder(time stmap based)

    String commitId=new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    File newCommit = new File(commitsDir, commitId);
    newCommit.mkdir();

    //copy files from staging to commit 

    for(File file : stagingDir.listFiles()){
      File dest=new File(newCommit, file.getName());
      try{
        Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        System.out.println("Error occurred while committing file: " + file.getName());
      }
    }

      //save commit message
      try{
        FileWriter writer=new FileWriter(new File(newCommit, "message.txt"));
        writer.write(message);
        writer.close();
      } catch (IOException e) {
        System.out.println("Error occurred while saving commit message.");
      }

      //clear staging area
      for(File file : stagingDir.listFiles()){
        file.delete();
      }
      System.out.println("Commit successful with ID: " + commitId);
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
        System.out.println("Displaying commit history...");
        break;
      default:
        System.out.println("Unknown command: " + command);
    }
  }
}