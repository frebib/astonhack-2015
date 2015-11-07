package utils3D;

public class Vertex {
    // Vertex data
    private float[] xyzw = new float[] {0f, 0f, 0f, 1f};
    private float[] rgba = new float[] {1f, 1f, 1f, 1f};
    private float[] uv = new float[] {0f, 0f};
    private float[] nml = new float[] { 0f, 0f, 0f };
     
    // The amount of bytes an element has
    public static final int elementBytes = 4;
     
    // Elements per parameter
    public static final int positionElementCount 	= 4;
    public static final int colorElementCount 		= 4;
    public static final int textureElementCount 	= 2;
    public static final int normalElementCount      = 3;
     
    // Bytes per parameter
    public static final int positionBytesCount 	= positionElementCount * elementBytes;
    public static final int colorByteCount 		= colorElementCount * elementBytes;
    public static final int textureByteCount 	= textureElementCount * elementBytes;
    public static final int normalByteCount     = normalElementCount * elementBytes;
     
    // Byte offsets per parameter
    public static final int positionByteOffset 	= 0;
    public static final int colorByteOffset 	= positionByteOffset + positionBytesCount;
    public static final int textureByteOffset 	= colorByteOffset + colorByteCount;
    public static final int normalByteOffset    = textureByteOffset + textureByteCount;
     
    // The amount of elements that a vertex has
    public static final int elementCount = positionElementCount + colorElementCount + textureElementCount + normalElementCount;    
    
    // The size of a vertex in bytes, like in C/C++: sizeof(Vertex)
    public static final int stride = positionBytesCount + colorByteCount + textureByteCount + normalByteCount;
     
    /**
     * Sets the x, y, z position of the defined vertex
     * @param x
     * @param y
     * @param z
     */
    public void setXYZ(float x, float y, float z) {
        this.setXYZW(x, y, z, 1f);
    }
    
    /**
     * Sets the RGB colour of the vertex with 100% alpha (transparency)
     * @param r
     * @param g
     * @param b
     */
    public void setRGB(float r, float g, float b) {
        this.setRGBA(r, g, b, 1f);
    }
    
    /**
     * Sets the UV texture coordinate of the vertex
     * @param u
     * @param v
     */
    public void setUV(float u, float v) {
        this.uv = new float[] {u, v};
    }
    
    /**
     * Sets the XYZW component of the vertex. Normally the w-component is set to 1.0f to allow matrix4-translations to work inside
     * the shader.
     * @param x
     * @param y
     * @param z
     * @param w
     */
    public void setXYZW(float x, float y, float z, float w) {
        this.xyzw = new float[] {x, y, z, w};
    }
    
    /**
     * Set the RGBA colour of the vertex (r=red, g=green, b=blue, a=alpha(transparency)
     * @param r : 0.0f - 1.0f
     * @param g : 0.0f - 1.0f
     * @param b : 0.0f - 1.0f
     * @param a : 0.0f - 1.0f
     */
    public void setRGBA(float r, float g, float b, float a) {
        this.rgba = new float[] {r, g, b, 1f};
    }
    
    public void setNML( float nx, float ny, float nz ){
        this.nml = new float[]{ nx, ny, nz };
    }
     
    /**
     * @return Returns a float array containing the vertex data.
     */
    public float[] getElements() {
        float[] out = new float[Vertex.elementCount];
        int i = 0;
         
        // Insert XYZW elements
        out[i++] = this.xyzw[0];
        out[i++] = this.xyzw[1];
        out[i++] = this.xyzw[2];
        out[i++] = this.xyzw[3];
        // Insert RGBA elements
        out[i++] = this.rgba[0];
        out[i++] = this.rgba[1];
        out[i++] = this.rgba[2];
        out[i++] = this.rgba[3];
        // Insert ST elements
        out[i++] = this.uv[0];
        out[i++] = this.uv[1];
        // Insert NML elements
        out[i++] = this.nml[0];
        out[i++] = this.nml[1];
        out[i++] = this.nml[2];
         
        return out;
    }
    /**
     * @return Returns a float array containing the x, y, z, w coordinates of the vertex
     */
    public float[] getXYZW() {
        return new float[] {this.xyzw[0], this.xyzw[1], this.xyzw[2], this.xyzw[3]};
    }
	 /**
	  * @return Returns a float array containing the x, y, z coordinates of the vertex
	  */
    public float[] getXYZ() {
        return new float[] {this.xyzw[0], this.xyzw[1], this.xyzw[2]};
    }
    /**
     * @return returns a float array containing the RGBA colour components of the vertex
     */
    public float[] getRGBA() {
        return new float[] {this.rgba[0], this.rgba[1], this.rgba[2], this.rgba[3]};
    }
    /**
     * @return returns a float array containing the RGB colour components of the vertex
     */
    public float[] getRGB() {
        return new float[] {this.rgba[0], this.rgba[1], this.rgba[2]};
    }
    /**
     * @return returns a float array containing the UV texture coordinates of the vertex
     */
    public float[] getUV() {
        return new float[] {this.uv[0], this.uv[1]};
    }
}