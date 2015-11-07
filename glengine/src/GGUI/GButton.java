package GGUI;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import utils3D.SCAppUtils;

public class GButton {
    private List<GButtonListener> listeners;
    private int x, y, width, height;
    private boolean clicked = false;
    private boolean clickedR = false;
    private boolean active  = false; // If not active, the button CANNOT be clicked.
    private boolean entered = false;
    private boolean hover   = false;
    private boolean mouseUPHover = false;
    
    public GButton( int x, int y, int width, int height){
        listeners = new ArrayList<>();
        this.x      = x;
        this.y      = y;
        this.width  = width;
        this.height = height;
    }
    
    public void addListener( GButtonListener o ){
        listeners.add( o );
    }
    
    public void setPosition( int x, int y){
        this.x = x;
        this.y = y;
    }
    
    public void setSize( int width, int height ){
        this.width  = Math.abs(width);
        this.height = Math.abs(height);
    }
    
    private void onClick(){
        for( GButtonListener gbl : listeners ) gbl.onGButtonClick( this );
    }
    
    private void onRClick(){
        for( GButtonListener gbl : listeners ) gbl.onGButtonRClick( this );
    }
    
    private void isDown(){
        for( GButtonListener gbl : listeners ) gbl.onGButtonDown( this );
    }
    private void onHover(){
        if(!entered){
            this.onMouseEnter();
            entered = true;
        }
        for( GButtonListener gbl : listeners ) gbl.onGButtonHover( this );
    }
    private void onMouseEnter(){
        for( GButtonListener gbl : listeners ) gbl.onGButtonMouseEnter( this );
    }
    
    public void update(){
        hover = false;
        if ( ((this.x + this.width) > 0) && ((this.y + this.height) > 0) && ((this.x ) < Display.getWidth()) && ((this.y) < Display.getHeight())){
            active = true;
            if ( SCAppUtils.pointInRectangle( Mouse.getX(), Display.getHeight()-Mouse.getY(), this.x, this.y, this.x+this.width, this.y+this.height )){
                onHover();
                hover = true;
                if ( Mouse.isButtonDown( 0 )){
                    isDown();
                    if ( mouseUPHover && !clicked ){
                        onClick();
                        clicked = true;
                    }
                    mouseUPHover = false;
                } else if( Mouse.isButtonDown(  1 )){
                    if( mouseUPHover && !clickedR ){
                        onRClick();
                        clickedR = true;
                        
                    }
                    mouseUPHover = false;
                } else {
                    clicked = false;
                    clickedR = false;
                    mouseUPHover = true;
                }
            }else{
                entered = false;
            }
        } else {
            active = false;
            entered = false;
        }
    }
    
    public int getButtonX(){
        return this.x;
    }
    public int getButtonY(){
        return this.y;
    }
    public int getButtonWidth(){
        return this.width;
    }
    public int getButtonHeight(){
        return this.height;
    }
    public boolean getActive(){
        return this.active;
    }
    public boolean getHover(){
        return this.hover;
    }
    
}
