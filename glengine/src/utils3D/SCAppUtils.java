package utils3D;

public class SCAppUtils {
    /**
     * This class provides a few generic all-purpose methods to be used by the application.
     */
    
    /**
     * Returns true if the point (px, py) lies within the rectangle defined by the minimum and maximum coordinate
     * boubds.
     * @param px
     * @param py
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    public static boolean pointInRectangle( float px, float py, float x1, float y1, float x2, float y2){
        return (px>= Math.min(x1, x2) && px<=Math.max( x1, x2 ) && py >= Math.min( y1, y2 ) && py <= Math.max( y1, y2));
    }
    
    /**
     * Returns true if the point (px, py) lies with the circle of centre (cx, cy) with the given radius.
     * @param px
     * @param py
     * @param cx
     * @param cy
     * @param radius
     * @return
     */
    public static boolean pointInCircle( float px, float py, float cx, float cy, float radius){
        return ( distance( px, py, cx, cy)<= radius );
    }
    
    /**
     * Returns the distance between two points in 2D space.
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    public static float distance( float x1, float y1, float x2, float y2 ){
        return (float) Math.sqrt( (x2-x1)*(x2-x1) + (y2-y1)*(y2-y1) );
    }
    
    /**
     * Returns the distance between two points in 3D space.
     * @param x1
     * @param y1
     * @param z1
     * @param x2
     * @param y2
     * @param z2
     * @return
     */
    public static float distance3D( float x1, float y1, float z1, float x2, float y2, float z2 ){
        return (float) Math.sqrt( (x2-x1)*(x2-x1) + (y2-y1)*(y2-y1) + (z2-z1)*(z2-z1) );
    }
}
