package GGUI;

public interface GButtonListener {
    /**
     * Gets fired when the button is clicked. Another click cannot be registered until the mouse is released
     * @param button
     */
    public void onGButtonClick( GButton button );
    
    /**
     * Gets fired when the button is clicked with the right mouse. Another click cannot be registered until the mouse is released
     * @param button
     */
    public void onGButtonRClick( GButton button );
    /**
     * Gets fired when the mouse is hovering over the button
     * @param button
     */
    public void onGButtonHover( GButton button );
    /**
     * Gets fired whilst the mouse is being clicked on the button
     * @param button
     */
    public void onGButtonDown( GButton button );
    /**
     * Gets fired when the mouse first enters the button region
     * @param button
     */
    public void onGButtonMouseEnter( GButton button );
}
