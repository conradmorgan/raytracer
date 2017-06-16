import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.awt.event.KeyEvent;

public final class Config {
    public int left;
    public int right;
    public int forward;
    public int backward;

    public Config() {
        // Defaults to WASD for movement.
        left = KeyEvent.VK_A;
        right = KeyEvent.VK_D;
        forward = KeyEvent.VK_W;
        backward = KeyEvent.VK_S;
    }

    public Config(String file) throws ParsingException {
        this(); // Set defaults.
        Scanner scanner;
        try {
            scanner = new Scanner(new File(file));
        } catch (FileNotFoundException e) {
            return;
        }
        String line;
        while ((line = Utils.nextLineOrEmpty(scanner)) != "") {
            String[] split = line.split(":");
            if (split.length != 2) {
                throw new ParsingException("Invalid config entry: " + line);
            }
            String behavior = split[0].trim();
            String keyStr = split[1].trim();
            if (keyStr.length() == 0) {
                throw new ParsingException("Behavior must be followed by a specified key: " + behavior);
            }
            int key = KeyEvent.getExtendedKeyCodeForChar(keyStr.charAt(0));
            switch (behavior) {
            case "left":
                left = key;
                break;
            case "right":
                right = key;
                break;
            case "forward":
                forward = key;
                break;
            case "backward":
                backward = key;
                break;
            default:
                throw new ParsingException("No such behavior exists: " + behavior);
            }
        }
    }
}
