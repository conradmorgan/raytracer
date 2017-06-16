import javax.swing.JPanel;
import java.awt.image.BufferedImage;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.lang.Thread;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Semaphore;
import java.util.Random;
import java.lang.InterruptedException;

public final class RayTracer extends JPanel implements Interruptable {
    public Scene scene;
    public Input input;

    private BufferedImage image;
    private int[][] pixelCache;
    private int[] threadWork;
    private AtomicInteger pixelsLeft;

    private boolean progressiveAbort;

    private Dimension size;

    public RayTracer(Scene scene, Dimension size, Input input) {
        this.scene = scene;
        this.input = input;
        size = input.newSize;
        pixelsLeft = new AtomicInteger();
        threadWork = new int[4];
        setSize(size);
    }

    @Override
    public void interrupt() {
        progressiveAbort = true;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (image != null) {
            g.drawImage(image, 0, 0, this);
        }
    }

    public void update() {
        if (!input.newSize.equals(size)) {
            size = input.newSize;
            updateSize();
        }
        scene.camera.rotate(input.getDeltaMouseX() / 2.0, -input.getDeltaMouseY() / 2.0);
        scene.camera.move(input.getKeyboardVector());
    }

    // Returns true if it rendered completely.
    // Returns false if rendering was aborted.
    public boolean renderProgressively() {
        double fps = 30;
        double timeSpent = 0.0;
        int depth = 7;
        progressiveAbort = false;
        boolean moving;
        do {
            long t1 = System.nanoTime();
            render(depth);
            repaint();
            long t2 = System.nanoTime();
            timeSpent += (double)(t2 - t1) / 1.0e9;
            depth++;
            moving = input.moving();
        } while (pixelsLeft() > 0 && (timeSpent <= 1 / fps && moving || !progressiveAbort && !moving));
        boolean renderComplete = (pixelsLeft() == 0);
        reset();
        return renderComplete;
    }


    private void updateSize() {
        BufferedImage newImage = new BufferedImage((int)size.getWidth(), (int)size.getHeight(), BufferedImage.TYPE_INT_RGB);
        if (image != null) {
            for (int y = 0; y < image.getHeight() && y < newImage.getHeight(); y++) {
                for (int x = 0; x < image.getWidth() && x < newImage.getWidth(); x++) {
                    newImage.setRGB(x, y, image.getRGB(x, y));
                }
            }
        }
        image = newImage;
        pixelCache = new int[image.getWidth()][];
        for (int x = 0; x < image.getWidth(); x++) {
            pixelCache[x] = new int[image.getHeight()];
        }
        reset();
    }

    private int pixelsLeft() {
        return pixelsLeft.get();
    }

    private void render(int depth) {
        int mx = image.getWidth() / 2;
        int my = image.getHeight() / 2;
        final int d = depth - 1;
        Thread[] threads = new Thread[]{
            new Thread() {
                @Override
                public void run() {
                    Graphics2D g = image.createGraphics();
                    renderDepth(d, 0, 0, mx, my, g, 0);
                    g.dispose();
                }
            },
            new Thread() {
                @Override
                public void run() {
                    Graphics2D g = image.createGraphics();
                    renderDepth(d, mx, 0, image.getWidth(), my, g, 1);
                    g.dispose();
                }
            },
            new Thread() {
                @Override
                public void run() {
                    Graphics2D g = image.createGraphics();
                    renderDepth(d, 0, my, mx, image.getHeight(), g, 2);
                    g.dispose();
                }
            },
            new Thread() {
                @Override
                public void run() {
                    Graphics2D g = image.createGraphics();
                    renderDepth(d, mx, my, image.getWidth(), image.getHeight(), g, 3);
                    g.dispose();
                }
            }
        };
        // Sort threads by how much work they were able to do last frame.
        // This way quadrant threads who didn't get as much work done get
        // to start first.
        for (int i = threads.length - 1; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                if (threadWork[j] > threadWork[j + 1]) {
                    int tw = threadWork[j];
                    threadWork[j] = threadWork[j + 1];
                    threadWork[j + 1] = tw;
                    Thread t = threads[j];
                    threads[j] = threads[j + 1];
                    threads[j + 1] = t;
                }
            }
        }
        for (int i = 0; i < threadWork.length; i++) {
            threadWork[i] = 0;
        }
        for (Thread thread : threads) {
            thread.start();
        }
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch(Exception e) {
        }
    }

    private void reset() {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                pixelCache[x][y] = -1;
            }
        }
        pixelsLeft.set(image.getWidth() * image.getHeight());
    }

    private void renderDepth(int depth, int x1, int y1, int x2, int y2, Graphics2D g, int thread) {
        // I can't remember why I did this, it's to prevent stuttering and tearing or something.
        if (depth > 5 && progressiveAbort) {
            return;
        }
        if (depth == 0) {
            renderRect(x1, y1, x2, y2, g, thread);
            return;
        }
        int mx = (x2 - x1)/2 + x1;
        int my = (y2 - y1)/2 + y1;
        depth--;
        renderDepth(depth, x1, y1, mx, my, g, thread);
        renderDepth(depth, mx, y1, x2, my, g, thread);
        renderDepth(depth, x1, my, mx, y2, g, thread);
        renderDepth(depth, mx, my, x2, y2, g, thread);
        if (progressiveAbort) {
            return;
        }
    }

    private void renderRect(int x1, int y1, int x2, int y2, Graphics2D g, int thread) {
        if (x2 <= x1 || y2 <= y1) {
            return;
        }
        int mx = (x2 - x1)/2 + x1;
        int my = (y2 - y1)/2 + y1;
        int color = pixelCache[mx][my];
        if (color == -1) {
            color = getRenderedColor(mx, my);
            pixelCache[mx][my] = color;
            pixelsLeft.decrementAndGet();
            threadWork[thread]++;
        }
        g.setColor(new Color(color));
        g.fillRect(x1, y1, x2 - x1, y2 - y1);
    }

    // Cast a ray into the world from a given pixel location and calculate its resultant color.
    private int getRenderedColor(int x, int y) {
        Ray3 ray = scene.camera.getRay(
            ((double)x + 0.5) / image.getWidth(),
            1 - ((double)y + 0.5)/image.getHeight(),
            (double)size.getWidth() / size.getHeight()
        );
        return scene.getRayColor(ray);
    }
}
