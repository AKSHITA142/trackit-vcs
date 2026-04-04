package utils;
import java.security.MessageDigest;

public class HashUtil {

    public static String generateHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] bytes = md.digest(input.getBytes());

            StringBuilder hash = new StringBuilder();

            for (byte b : bytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hash.append('0');
                hash.append(hex);
            }

            return hash.toString();

        } catch (Exception e) {
            throw new RuntimeException("Hash failed");
        }
    }
}