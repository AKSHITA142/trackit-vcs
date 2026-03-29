package commands;

import core.BranchManager;

public class BranchCommand {

    public static void execute(String[] args) {

        if (args.length < 2) {
            System.out.println("Please provide branch name.");
            return;
        }

        BranchManager.createBranch(args[1]);
    }
}