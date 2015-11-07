package utils3D;

import Application3D.Application3D;

import java.io.IOException;

import org.newdawn.slick.openal.Audio;
import org.newdawn.slick.openal.AudioLoader;
import org.newdawn.slick.util.ResourceLoader;

public class AudioUtils {
    /** This class is responsible for the loading and playing of all audio files
     */
    
    public AudioUtils( ){
        
        // Load some general sounds
       /* this.loadSound( "res/sfx/sfRollover.wav",          "buttonClick" );
        this.loadSound( "res/sfx/sfClick.wav",     "buttonHover" );
        this.loadSound( "res/music/sm_music_main.wav",          "musicMain" );
        this.loadSound( "res/sfx/sf_mrx_turn_start.wav",        "mrxTurnStart" );
        this.loadSound( "res/sfx/sf_detective_turn_start.wav",  "detectiveTurnStart" );
        this.playMusic( "musicMain" );*/
    }
    /**
     * Loads a new sound file and adds it to the global resource system with the given key.
     * @param filename
     * @param name
     */
    public synchronized void loadSound( String filename, String name ){
        try {
            Audio newAudio = AudioLoader.getAudio("WAV", ResourceLoader.getResourceAsStream(filename));
            Application3D.getApp().getResources().addSound( newAudio, name );
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("Sound failed to load!");
            System.exit(1);
        }
    }
    
    /**
     * Given a key 'name', it will play a sound.
     * @param name name The key of the sound to play
     * @param volume volume the volume of the audio
     */
    public synchronized void playSound( String name, float volume ){
        Audio newAudio = Application3D.getApp().getResources().getSound( name );
        newAudio.playAsSoundEffect( 1, 1, false );
    }
    
    /**
     * Given a key 'name', it will loop a sound (as music).
     * @param name The key of the sound to play
     * @param volume the volume of the audio
     */
    public synchronized void playMusic( String name, float volume ){
        Audio newAudio = Application3D.getApp().getResources().getSound( name );
        newAudio.playAsSoundEffect( 1, 0.25f, true );//playAsMusic( 1, 1.0f, true );    
    }
    
    // Overloads
    public synchronized void playMusic( String name ){
        this.playMusic( name, 1 );
    }
    public synchronized void playSound( String name ){
        this.playSound( name, 1);
    }
    
    // Getters
    /**
     * @param name
     * @return true if the sound is playing
     */
    public synchronized boolean getIsPlaying( String name ){
        return Application3D.getApp().getResources().getSound( name ).isPlaying();
    }
    
    // Control
    public synchronized void stopSound( String name ){
        Application3D.getApp().getResources().getSound( name ).stop();
    }
    
    
}
