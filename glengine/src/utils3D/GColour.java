package utils3D;

public class GColour {
    private float r, g, b, a;
    
    public GColour( float r, float g, float b, float a ) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }
    
    public float getRed(){ 
        return r;
    }
    
    public float getGreen(){ 
        return g;
    }
    
    public float getBlue(){ 
        return b;
    }
    
    public float getAlpha(){ 
        return a;
    }
}
