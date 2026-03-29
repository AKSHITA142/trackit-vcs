package core;

import java.io.*;

public class HeadManager {

    private static final String HEAD_FILE = ".minigit/HEAD";

    public static void setCurrentBranch(String branchName) {
        try (FileWriter writer = new FileWriter(HEAD_FILE)) {
            writer.write(branchName);
        } catch (IOException e) {
            System.out.println("Error writing HEAD.");
        }
    }

    public static String getCurrentBranch() {

        File head = new File(HEAD_FILE);

        if (!head.exists()) {
            return "main"; // default
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(head))) {
            return reader.readLine();
        } catch (IOException e) {
            return "main";
        }
    }
}