package polygons;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * File: polygon.java
 *
 * @author Sydney Class: CS 4450-01 - Computer Graphics Assignment: Program 2
 * Date Last Modified: 2/22/2020
 *
 * Purpose: To hold the vertices of a polygon
 */
public class polygon {

    private List<Float> listX, listY;
    private Float[] x, y;
    private float area = 0;
    public float pivotX = 0, pivotY = 0;
    public List<String> transformations = new ArrayList<String>();
    public float r, g, b;

    public polygon() {
        listX = new ArrayList<Float>();
        listY = new ArrayList<Float>();
    }

    public void addxy(float nx, float ny) {
        listX.add(nx);
        listY.add(ny);
    }

    public void convert() {
        x = new Float[listX.size()];
        y = new Float[listY.size()];
        listX.toArray(x);
        listY.toArray(y);
        // calcCentroid();
    }

    public Float[] getX() {
        return x;
    }

    public Float[] getY() {
        return y;
    }

    
    /**
     * Calculates the centroid of a non-self intersecting polygon using
     * shoelace formula to calculate area
     */
    private void calcCentroid() {
        for (int i = 0; i < x.length; i++) {
            float x0 = x[i];
            float y0 = y[i];
            float x1 = x[(i + 1) % x.length];
            float y1 = y[(i + 1) % y.length];

            float a = (x0 * y1) - (x1 * y0);
            area = area + a;

            pivotX = pivotX + ((x0 + x1) * a);
            pivotY = pivotY + ((y0 + y1) * a);
        }
        area = area / 2;
        pivotX = pivotX / (6 * area);
        pivotY = pivotY / (6 * area);
    }
}
