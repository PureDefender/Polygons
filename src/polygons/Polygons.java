package polygons;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import java.util.Scanner;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.lwjgl.input.Keyboard;

/**
 *
 * File: Polygons.java
 *
 * @author Sydney Class: CS 4450-01 - Computer Graphics Assignment: Program 2
 * Date Last Modified: 2/22/2020
 *
 * Purpose: To demonstrate the drawing of polygons using the OpenGL library
 * Specifically GL_POINTS and glVertex2f with transformations and a scan-line
 * algorithm
 */
public class Polygons {

    private float r = 0.0f, g = 0.0f, b = 0.0f, scanLine;
    private File file = new File("coordinates.txt");
    private ArrayList<polygon> polyList = new ArrayList();
    private float[][] allEdges, globalEdges, activeEdges;
    private int numEdges = 0;
    int checkI = 0;
    int checkX = 0;

    /**
     * Calls the appropriate functions to begin the program
     */
    public void start() {
        try {
            createWindow();
            initGL();
            render();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the window to be used in the program
     *
     * @throws Exception
     */
    private void createWindow() throws Exception {
        Display.setFullscreen(false);

        Display.setDisplayMode(new DisplayMode(640, 480));
        Display.setTitle("Sydney Ho - Program 2");
        Display.create();
    }

    /**
     * Initializes the graphics library
     */
    private void initGL() {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        glOrtho(0, 640, 0, 480, 1, -1);
        glMatrixMode(GL_MODELVIEW);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT,
                GL_NICEST);
    }

    /**
     * Render function to start drawing
     */
    private void render() {
        while (!Display.isCloseRequested()) {
            try {
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                glLoadIdentity();
                glPointSize(1);
                checkInput();
                drawShapes();
                Display.update();
                Display.sync(60);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Display.destroy();
    }

    /**
     * Method with all key bindings and their respective functions
     */
    private void checkInput() {
        // Ensures no out of bounds exception
        try {
        checkX = checkI % polyList.size();
        } catch (ArithmeticException e) {
            
        }
        // If ESC key is pressed, exit system
        if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            System.exit(0);
        }
        // If Up arrow is pressed, select the next polygon
        if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
            checkI++;
        }
        // If Down arrow is pressed, select the previous polygon
        if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
            checkI--;
        }
        // If right arrow is pressed, increase RGB values by 0.1 for selected polygon
        if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
            polyList.get(checkX).r += 0.1f;
            polyList.get(checkX).g += 0.1f;
            polyList.get(checkX).b += 0.1f;
        }
        // If left arrow is pressed, decrease RGB values by 0.1 for selected polygon
        if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
            polyList.get(checkX).r -= 0.1f;
            polyList.get(checkX).g -= 0.1f;
            polyList.get(checkX).b -= 0.1f;
        }
        System.out.println("i" + checkI);
        System.out.println(checkX);
    }

    /**
     * Attempts to automatically read and draw from the file Allows the user to
     * select the file from a GUI
     *
     * @throws IOException
     */
    private void drawShapes() throws IOException {
        try {
            // Attempt to read coordinates.txt file in current directory
            readFile(file);
        } catch (FileNotFoundException ex) {
            // If file is not found automatically, have the user select the file
            JFileChooser chooser = new JFileChooser();
            // Only allow for .txt files
            FileNameExtensionFilter filter = new FileNameExtensionFilter("*.txt", "txt", "text");
            chooser.setFileFilter(filter);
            int returnVal = chooser.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = chooser.getSelectedFile();
                // Read in the selected .txt file
                readFile(file);
            } else {
                // If the user decides to not enter a file, the program exits
                System.out.println("No file chosen, exiting program.");
                System.exit(0);
            }
        }

    }

    /**
     *
     * @param path File path of the coordinates.txt file to be read
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void readFile(File file) throws FileNotFoundException {
        Scanner filesc = new Scanner(file);
        while (filesc.hasNextLine()) {
            // Save line read into string to be processed
            String temp = filesc.nextLine();

            // Covering each symbol case (Polygon, Translate, Rotate, Scale
            if (temp.startsWith("P") || temp.startsWith("p")) {

                addPolygon(temp, filesc);

            } else if (temp.startsWith("S") || temp.startsWith("s")) {
                String[] space = temp.split(" ");

                polyList.get(polyList.size() - 1).pivotX = Float.parseFloat(space[3]);
                polyList.get(polyList.size() - 1).pivotY = Float.parseFloat(space[4]);
                polyList.get(polyList.size() - 1).transformations.add(temp);
            } else {
                polyList.get(polyList.size() - 1).transformations.add(temp);

            }
        }
        // Begin iterating through list of polygons (polyList)
        for (polygon polyList1 : polyList) {
            glPushMatrix();
            // Begin iterating through all transformations
            for (int j = 0; j < polyList1.transformations.size(); j++) {
                // Transformation string to be read
                String temp = polyList1.transformations.get(j);
                if (temp.startsWith("t") || temp.startsWith("T")) {
                    translatePolygon(temp);
                } else if (temp.startsWith("r") || temp.startsWith("R")) {
                    rotatePolygon(temp);
                } else if (temp.startsWith("s") || temp.startsWith("S")) {
                    scalePolygon(temp);
                }
            }
            // Moving polygon to 'origin' (pivot points given)
            glTranslatef(-1 * polyList1.pivotX, -1 * polyList1.pivotY, 0f);
            // Setting color
            glColor3f(polyList1.r, polyList1.g, polyList1.b);
            // Begin drawing polygon
            glBegin(GL_POLYGON);
            for (int p = 0; p < polyList1.getX().length; p++) {
                glVertex2f(polyList1.getX()[p], polyList1.getY()[p]);
            }
            glEnd();
            glPopMatrix();
        }

    }

    /**
     * Initializes the All Edges table with all vertices/edges
     */
    private void initializeAllEdgesTable() {
        allEdges = new float[numEdges][4];
        int p = 0;

        // Iterate through all the polygons, add all edges to the allEdges table
        for (int i = 0; i < polyList.size() - 1; i++) {
            for (int k = 0; k < polyList.get(i).getX().length; k++) {
                float x0, x1, y0, y1;
                x0 = polyList.get(i).getX()[k];
                x1 = polyList.get(i).getX()[(k + 1) % polyList.get(i).getX().length];
                y0 = polyList.get(i).getY()[k];
                y1 = polyList.get(i).getY()[(k + 1) % polyList.get(i).getY().length];
                float m;

                // Make sure y0 is the max y value
                if (y1 > y0) {
                    float temp = y1;
                    y1 = y0;
                    y0 = temp;
                    temp = x1;
                    x1 = x0;
                    x0 = temp;
                } else if (y0 == y1) { // If slope of edge is 0
                    m = 0;
                    allEdges[p][0] = y0;
                    allEdges[p][1] = y1;
                    allEdges[p][2] = x0;
                    allEdges[p][3] = m;
                    p++;
                    continue;
                }

                // m here is considered 1/m as the equation is flipped
                m = (x0 - x1) / (y0 - y1);
                allEdges[p][0] = y1;
                allEdges[p][1] = y0;
                allEdges[p][2] = x0;
                allEdges[p][3] = m;
                p++;
            }
        }
    }

    /**
     * sorts the All Edges table by min y column and corresponding x value
     */
    private void sortAllEdges() {
        // Sort allEdges table by minimum y column
        insertSort(allEdges, 0);

        // Sort all edges by x value - using comparator
        Arrays.sort(allEdges, (float[] o1, float[] o2) -> {
            if (o1[0] == o2[0]) {
                return (int) (o2[2] - o1[2]);
            }
            return (int) (o2[0] - o1[0]);
        });
    }

    /**
     * Initializes the global edge table
     */
    private void initializeGlobalEdgesTable() {
        globalEdges = new float[numEdges][3];

        // Copy allEdges into globalEdges, skip m = 0
        for (int i = 0; i < numEdges; i++) {
            float[] tempMatrix = allEdges[i];

            // If 1/m is infinite (slope = 0), skip row
            boolean inf = Float.isInfinite(tempMatrix[3]);
            if (inf) {
                continue;
            }
            int tempLength = tempMatrix.length;
            globalEdges[i] = new float[tempLength];
            System.arraycopy(tempMatrix, 0, globalEdges[i], 0, tempLength);
        }
    }

    /**
     * Initializes the scan line with the min y value of first global edge
     */
    private void initializeScanLine() {
        scanLine = globalEdges[0][0];
    }

    /**
     * Initializes the Active Edge Table with all edges matching the first min y
     * value
     */
    private void initializeActiveEdgeTable() {
        activeEdges = new float[numEdges][4];
        for (int i = 0; i < globalEdges.length; i++) {
            if (globalEdges[i][0] == scanLine) {
                activeEdges[i][0] = globalEdges[i][1];
                activeEdges[i][1] = globalEdges[i][2];
                activeEdges[i][2] = globalEdges[i][3];
            }
        }
    }

    /**
     * Insertion sort column-wise by a specified column for 2D array
     *
     * @param a 2d array to be sorted
     * @param c column to be sorted by
     */
    private void insertSort(float[][] a, int c) {
        float key;
        int j;
        for (int i = 1; i < numEdges; i++) {
            key = a[i][c];
            float[] keyRow = a[i];
            j = i - 1;
            while ((j >= 0) && (a[j][c] > key)) {
                a[j + 1] = a[j];
                j = j - 1;
            }
            a[j + 1] = keyRow;
        }
    }

    /**
     *
     * @param s String that takes in the RGB values, signifies start of polygon
     * vertices
     * @param sc Scanner used to read from the file
     */
    private void addPolygon(String s, Scanner sc) {
        String[] space = s.split(" ");

        // Creating temporary polygon object to be added to list
        polygon tempPoly = new polygon();

        // Saving RGB values of given polygon
        tempPoly.r = Float.parseFloat(space[1]);
        tempPoly.g = Float.parseFloat(space[2]);
        tempPoly.b = Float.parseFloat(space[3]);

        while (sc.hasNextLine()) {
            String temp = sc.nextLine();

            // Encountering a "T" line means transformations begin
            if (temp.equalsIgnoreCase("T")) {
                break;
            }
            String[] coord = temp.split(" ");
            tempPoly.addxy(Float.parseFloat(coord[0]), Float.parseFloat(coord[1]));
            numEdges++;
        }

        // Add the polygon to the list of polygons
        tempPoly.convert();
        polyList.add(tempPoly);
    }

    /**
     *
     * @param s String read in from file (ex. t 100 -75)
     * @param poly polygon to be translated
     */
    private void translatePolygon(String s) {
        String[] temp = s.split(" ");

        float translateX = Float.parseFloat(temp[1]);
        float translateY = Float.parseFloat(temp[2]);

        glTranslatef(translateX, translateY, 0f);

        /*
         // Formula for translating a point
         for (int i = 0; i < poly.getX().length; i++) {
         poly.getX()[i] = poly.getX()[i] + translateX;
         poly.getY()[i] = poly.getY()[i] + translateY;
         }
         */
    }

    /**
     *
     * @param s String read in from file (ex. r 30 0 0)
     * @param poly polygon to be rotated
     */
    private void rotatePolygon(String s) {
        String[] temp = s.split(" ");

        float angle = Float.parseFloat(temp[1]);
        float x = Float.parseFloat(temp[2]);
        float y = Float.parseFloat(temp[3]);

        glRotatef(angle, x, y, 1f);
        /*
         // Formula for rotating a point around a specific coordinate
         for (int i = 0; i < poly.getX().length; i++) {
         poly.getX()[i] = (float) (centerX + (poly.getX()[i] - centerX) * Math.cos(angle) - (poly.getY()[i] - centerY) * Math.sin(angle));
         poly.getY()[i] = (float) (centerX + (poly.getY()[i] - centerX) * Math.sin(angle) + (poly.getY()[i] - centerY) * Math.cos(angle));
         }
         */
    }

    /**
     *
     * @param s String read in from file (ex. s 0.5 0.5 0 0)
     * @param poly polygon to be scaled
     */
    private void scalePolygon(String s) {
        String[] temp = s.split(" ");

        // Scaling values for x, y, and center of polygon
        float scaleX = Float.parseFloat(temp[1]);
        float scaleY = Float.parseFloat(temp[2]);
        float px = Float.parseFloat(temp[3]);
        float py = Float.parseFloat(temp[4]);

        glScalef(scaleX, scaleY, 0f);
        glTranslatef(-1 * px, -1 * py, 0f);

        /*
         for (int i = 0; i < poly.getX().length; i++) {
         poly.getX()[i] = centerX + (poly.getX()[i] - centerX) * scaleX;
         poly.getY()[i] = centerY + (poly.getY()[i] - centerY) * scaleY;
         }
         */
    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {

        Polygons shape = new Polygons();
        shape.start();
    }
}
