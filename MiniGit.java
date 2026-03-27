public class MiniGit {
  public static void main(String[] args) {
    System.out.println("Welcome to MiniGit!");
    if (args.length == 0) {
      System.out.println("No command Provided");
      return;
    }
    String command = args[0];

    switch (command) {
      case "init":
        System.out.println("Intializing repository....");
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