import javax.swing.JFrame;
import java.awt.Dimension;

public class Main {
    public static int width = 1600;
    public static int height = 900;

    private static JFrame frame;
    private static Config config;
    private static Input input;
    private static RayTracer rayTracer;

    public static void main(String[] args) {
        // Load the configuration file. Currently the configuration file
        // only has options for keyboard controls.
        try {
            config = new Config("config.txt");
        } catch (ParsingException e) {
            System.out.println("Invalid config! " + e.toString() + "\nLoading defaults...");
            config = new Config();
        }

        frame = new JFrame("Ray Tracer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationByPlatform(true);
        frame.setSize(width, height);

        input = new Input(config);
        input.newSize = frame.getSize();

        // If there are any command-line arguments, use the first argument
        // as the world definition file. Otherwise default to "default_scene.txt".
        Scene scene = null;
        try {
            scene = new Scene(args.length == 0 ? "default_scene.txt" : args[0]);
        } catch (ParsingException e) {
            System.out.println(e.toString());
            System.exit(1);
        }
        rayTracer = new RayTracer(scene, new Dimension(width, height), input);
        // Add the ray tracer to the list of Interruptable objects that are
        // interrupted on input.
        input.addInterruptable(rayTracer);

        frame.add(rayTracer);
        frame.setVisible(true);

        // Set up keyboard, mouse, and window resizing listeners.
        frame.addKeyListener(input);
        frame.addMouseListener(input);
        frame.addMouseMotionListener(input);
        frame.addComponentListener(input);

        // Main loop.
        while (true) {
            rayTracer.update();
            if (rayTracer.renderProgressively()) {
                input.waitForInput();
            }
        }
    }
}
