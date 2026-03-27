import java.io.File;


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
  public static void main(String[] args) {

    if (args.length == 0) {
      System.out.println("No command Provided");
      return;
    }
    String command = args[0];

    switch (command) {
      case "init":
        System.out.println("Intializing repository....");
        initRepository();
        break;
      case "add":
        if (args.length < 2) {
          System.out.println("Please provide a file name");
        } else {
          System.out.println("Adding file:" + args[1]);
        }
        break;
      case "commit":
        if (args.length < 2) {
          System.out.println("Please provide a commit message");
        } else {
          System.out.println("Commiting with message: " + args[1]);
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