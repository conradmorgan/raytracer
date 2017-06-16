import java.util.Scanner;

public class Utils {
    public static String nextLineOrEmpty(Scanner scanner) {
        return scanner.hasNextLine() ? scanner.nextLine() : "";
    }

    public static boolean isIndented(String line) {
        return line.length() > 0 && (line.charAt(0) == '\t' || line.charAt(0) == ' ');
    }

    public static Surface readSurface(Scanner scanner) throws ParsingException {
        switch (scanner.next()) {
        case "diffuse":
            return Surface.Diffuse;
        case "specular":
            return Surface.Specular;
        case "transparent":
            return Surface.Transparent;
        }
        throw new ParsingException("Non-existent surface!");
    }

    public static Vector3 readVector3(Scanner scanner) throws ParsingException {
        String str = scanner.nextLine().trim();
        if (str.charAt(0) != '(' || str.charAt(str.length() - 1) != ')') {
            throw new ParsingException("Coordinates must be parenthesized!");
        }
        str = str.substring(1, str.length() - 1);
        String[] coords = str.split(",");
        if (coords.length != 3) {
            throw new ParsingException("A coordinates must have exactly 3 components!");
        }
        for (int i = 0; i < coords.length; i++) {
            coords[i] = coords[i].trim();
        }
        double[] parsedCoords = new double[coords.length];
        for (int i = 0; i < parsedCoords.length; i++) {
            try {
                parsedCoords[i] = Double.parseDouble(coords[i]);
            } catch (Exception e) {
                throw new ParsingException("Components of coordinate must be numbers!");
            }
        }
        return new Vector3(parsedCoords[0], parsedCoords[1], parsedCoords[2]);
    }
}
