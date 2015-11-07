package utils3D;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex3f;
import Application3D.Application3D;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.lwjgl.opengl.GL13;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class RenderUtils {
    /**
     * This class is used for rendering generic objects. It contains methods for rendering common 2D and 3D shapes.
     * It efficiently handles rendering by ensuring that all shapes are simply pre-frozen vbo's.
     * 
     * Provides a collection of generic utilities for rendering of simple 3D shapes and sprites on the 2D
     * projection.
     */
    private VertexBuffer vboQuad;
    private VertexBuffer vboCube;
    private VertexBuffer vboFloor;
    
    private GLFont fontBSansVeryLarge;
    private GLFont fontBSansLarge;
    private GLFont fontBSansMedium;
    private GLFont fontBSansSmall;
    
    private Texture texPlain;
    
    // Static METHODS
    /**
     * Renders a 3D floor using openGLs immediate rendering mode. [ONLY USE FOR DEBUGGING]
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param z1
     */
    public static void drawFloorImmediate( float x1, float y1, float x2, float y2, float z1 ){
        glBegin( GL_TRIANGLES );
            glVertex3f(x1, y2, z1);
            glVertex3f(x1, y1, z1);
            glVertex3f(x2, y1, z1);
            
            glVertex3f(x2, y2, z1);
            glVertex3f(x1, y2, z1);
            glVertex3f(x2, y1, z1);
        glEnd();
    }
    
    // Non-static methods
    public RenderUtils(){
        this.prepareQuad();
        //this.prepareCube();
        this.prepareFloor();
        this.prepareFonts();
        this.prepareTextures();
    }
    
    private void prepareQuad(){
        vboQuad = new VertexBuffer();
        {
            vboQuad.addQuad( 0, 0, 1, 1);
        }
        vboQuad.freeze();
    }
    
    private void prepareFloor(){
        vboFloor = new VertexBuffer();
        {
            vboFloor.addFloor( 0, 0, 1, 1, 0 );
        }
        vboFloor.freeze();
    }
    
    private void prepareFonts(){
        Font ft;
        try {
            ft  = Font.createFont(Font.TRUETYPE_FONT, new File( "res/fonts/BRLNSB.TTF" ));
            
            // Build Very Large font
            fontBSansVeryLarge = new GLFont( ft.deriveFont(Font.PLAIN, 48));
            
            // Build Large font
            fontBSansLarge     = new GLFont( ft.deriveFont( Font.PLAIN, 28 ));
            
            // Build Medium font
            fontBSansMedium    = new GLFont( ft.deriveFont( Font.PLAIN, 18 ));
            
            // Build small font
            fontBSansSmall     = new GLFont( ft.deriveFont( Font.PLAIN, 14 ));
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private void prepareTextures(){
        texPlain = Application3D.getApp().getResources().loadTexture( "res/textures/scGuiWhite.png", "tex_plain" );
    }
    //////////////////////////////////////////////////////////////////
    // User Methods
    /**
     * Renders a 2D quad to the screen
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void drawQuad( float x, float y, float width, float height ){
       Matrix4f wm = Application3D.getApp().getWorldMatrix();
       wm.setIdentity();
       wm.translate( new Vector2f( x, y) );
       wm.scale( new Vector3f( width, height, 1 ) );
       Application3D.getApp().matrixWorldBind();
       this.vboQuad.render();
       wm.setIdentity();
    }
    
    public void drawQuadPlain( float x, float y, float width, float height, GColour colour ){
        texPlain.bind();
        Application3D.getApp().getActiveShader().setUniformColour( colour );
        drawQuad( x, y, width, height );
    }
    
    public void drawQuadPlainOutline( float x, float y, float width, float height, int outlineWidth, GColour colour, GColour outlineCol ){
        texPlain.bind();
        Application3D.getApp().getActiveShader().setUniformColour( outlineCol );
        drawQuad( x-outlineWidth, y-outlineWidth, width+outlineWidth*2, height+outlineWidth*2 );
        Application3D.getApp().getActiveShader().setUniformColour( colour );
        drawQuad( x, y, width, height );
    }
    
    /**
     * This function is used to render a 2D sprite to the screen given a few parameters.
     * @param x
     * @param y
     * @param width
     * @param height
     * @param tex
     * @param red
     * @param green
     * @param blue
     * @param alpha
     */
    public void drawSprite( float x, float y, Texture tex, float r, float g, float b, float alpha ){
        tex.bind();
        Application3D.getApp().getActiveShader().setUniformColour( r, g, b, alpha );
        Application3D.getApp().getWorldMatrix().setIdentity();
        drawQuad( x, y, tex.getWidth(), tex.getHeight() );
    }
    
    /**
     * Renders a 2D sprite to the screen given a few parameters
     * @param x
     * @param y
     * @param tex
     * @param originX
     * @param originY
     * @param xscale
     * @param yscale
     * @param colour
     */
    public void drawSpriteExt( float x, float y, Texture tex, float originX, float originY, float xscale, float yscale, GColour colour ){
        tex.bind();
        Application3D.getApp().getActiveShader().setUniformColour( colour );
        drawQuad( x-originX*xscale, y-originY*yscale, tex.getWidth()*xscale, tex.getHeight()*yscale );
    }
    
    /**
     * Returns the rendering font instance
     * @return
     */
    public GLFont getFont( FontSize fs ){
        switch ( fs ) {
            case VERY_LARGE: return fontBSansVeryLarge;
            case LARGE:      return fontBSansLarge;
            case MEDIUM:     return fontBSansMedium;
            case SMALL:      return fontBSansSmall;
        }
        return fontBSansSmall;
    }
    
    /**
     * Renders a string with the given font size at position x, y
     * @param text
     * @param x
     * @param y
     * @param fontSize
     */
    public void drawString( String text, int x, int y, FontSize fontSize, float alpha ){
        GLFont ft = this.getFont( fontSize );
        ft.drawString( Application3D.getApp().getActiveShader(), text, x, y, alpha );
    }
    
    /**
     * Renders a string with the given font size at position x, y which has a drop-shadow with the given relative direction vector
     * @param text
     * @param x
     * @param y
     * @param fontSize
     * @param direction
     */
    public void drawStringShadow( String text, int x, int y, Vector2f direction, FontSize fontSize, float alpha, float shadowAlpha ){
        GLFont ft = this.getFont( fontSize );
        ft.drawStringShadow( Application3D.getApp().getActiveShader(), text, x, y, direction, alpha, shadowAlpha );
    }
    
    /**
     * Draws an outlined string with the given font size at position x, y.
     * @param text
     * @param x
     * @param y
     * @param fontSize
     */
    public void drawStringOutlined( String text, int x, int y, FontSize fontSize, float alpha, float outlineAlpha ){
        GLFont ft = this.getFont( fontSize );
        ft.drawStringOutlined( Application3D.getApp().getActiveShader(), text, x, y, alpha, outlineAlpha );
    }
    
    /**
     * Draws an outlined string with the given font size at position x, y with a drop-shadow with the given relative direction vector.
     * @param text
     * @param x
     * @param y
     * @param fontSize
     * @param direction
     */
    public void drawStringOutlinedShadow( String text, int x, int y, Vector2f direction, FontSize fontSize, float alpha, float shadowAlpha ){
        GLFont ft = this.getFont( fontSize );
        ft.drawStringShadowOutlined( Application3D.getApp().getActiveShader(), text, x, y, direction, alpha, shadowAlpha );
    }
    
    /**
     * Allows the text align for rendered Strings to be modified.
     * @param textAlign
     */
    public void setTextAlign( int textAlign ){
        fontBSansVeryLarge.DRAW_STYLE = textAlign;
        fontBSansLarge.DRAW_STYLE = textAlign;
        fontBSansMedium.DRAW_STYLE = textAlign;
        fontBSansSmall.DRAW_STYLE = textAlign;
    }
}

