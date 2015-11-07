package utils3D;

import static org.lwjgl.opengl.GL20.*;
import Application3D.Application3D;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import com.sun.prism.impl.BufferUtil;

import static org.lwjgl.opengl.GL11.*;

public class Shader {
    /**
     * The shader class is responsible for acting as a wrapper for a GLSL shader program.
     * 
     * Shaders are used for controlling the rendering process used to render graphics to the screen.
     * The class loads the shader as well as preparing uniforms
     */
	// Shader vertex and fragment programs
	private int vertexShaderIndex;
	private int fragmentShaderIndex;
	private int programIndex;
	
	// Shader uniform locations
	private int matrixProjectionLocation = 0;
	private int matrixViewLocation		 = 0;
	private int matrixWorldLocation	     = 0;
	private int uniformColourLocation    = 0;
	private int uniformGrayscaleLocation = 0;
	private int uniformMatrixWorldInverseLocation = 0;
	private int uniformMatrixViewInverseLocation = 0;
	private int uniformLightingLocation = 0;
	private int uniformLightPos = 0;
	private ByteBuffer lightBuffer;
	
	private float[][] lights;
	
	public Shader( String vertexShader, String fragmentShader ) {
		lightBuffer = BufferUtils.createByteBuffer( 8 * 4 * 4 );
		// Load shader vertex and fragment component
		vertexShaderIndex 	= loadShader( vertexShader, GL_VERTEX_SHADER );
		fragmentShaderIndex = loadShader( fragmentShader, GL_FRAGMENT_SHADER );
	
		// Bind vertex and fragment shader to program
		programIndex = glCreateProgram();
		glAttachShader( programIndex, vertexShaderIndex );
		glAttachShader( programIndex, fragmentShaderIndex );
		
		// Setup attribute locations
		glBindAttribLocation( programIndex, 0, "in_Position" );
		glBindAttribLocation( programIndex, 1, "in_Color" );
		glBindAttribLocation( programIndex, 2, "in_TextureCoord" );
		glBindAttribLocation( programIndex, 3, "in_Normal" );
		
		// Link program
		glLinkProgram( programIndex );
		glValidateProgram( programIndex );
		
		
		
		// Determine uniform locations
		matrixProjectionLocation = glGetUniformLocation( programIndex, "matrixProjection" );
		matrixViewLocation 		 = glGetUniformLocation( programIndex, "matrixView" );
		matrixWorldLocation 	 = glGetUniformLocation( programIndex, "matrixModel" );
		uniformColourLocation    = glGetUniformLocation( programIndex, "colour" );
		uniformGrayscaleLocation = glGetUniformLocation( programIndex, "grayscale" );
		uniformLightPos          = glGetUniformLocation( programIndex, "u_light" );
		uniformMatrixWorldInverseLocation = glGetUniformLocation( programIndex, "matrixModelInverse" );
		uniformMatrixViewInverseLocation = glGetUniformLocation( programIndex, "matrixViewInverse" );
		uniformLightingLocation           = glGetUniformLocation( programIndex, "lighting" );
		Application3D.getApp().appCheckGLError( "SHADER END");
		
		lights = new float[8][4];
	}
	
	private int loadShader(String filename, int type) {
        StringBuilder shaderSource = new StringBuilder();
        int shaderID = 0;
         
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                shaderSource.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Could not read file.");
            e.printStackTrace();
            System.exit(-1);
        }
         
        shaderID = glCreateShader(type);
        glShaderSource(shaderID, shaderSource);
        glCompileShader(shaderID);
         
        if ( glGetShaderi(shaderID, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println("Could not compile shader."+filename);
            String log = glGetShaderInfoLog(shaderID, 65536);
            if(log.length() != 0){
                System.out.println("Program link log:\n" + log);
            }
            System.exit(-1);
        }
         
        return shaderID;
    }
	
	public int getUniformLocationMatrixProjection(){
		return matrixProjectionLocation;
	}
	
	public int getUniformLocationMatrixView(){
		return matrixViewLocation;
	}
	
	public int getUniformLocationMatrixWorld(){
		return matrixWorldLocation;
	}
	
	public int getUniformLocationMatrixWorldInverse(){
	    return uniformMatrixWorldInverseLocation;
	}
	public int getUniformLocationMatrixViewInverse(){
	    return uniformMatrixViewInverseLocation;
	}
	
	public int getUniformLocationColour(){
	    return uniformColourLocation;
	}
	
	public int getProgramIndex(){
		return programIndex;
	}
	
	public void setUniformColour( float r, float g, float b, float a ){
	    glUniform4f( uniformColourLocation, r, g, b, a );
	}
	
	public void setUniformColour( GColour gc ) {
	    setUniformColour( gc.getRed(), gc.getGreen(), gc.getBlue(), gc.getAlpha() );
	}
	
    public void setUniformGrayscaleRatio(float ratio) {
        glUniform1f( uniformGrayscaleLocation, ratio );
    }
    
	public void setUniformGrayscale( boolean gs ){
	    setUniformGrayscaleRatio( gs?1.0f:0.0f );
	}
	
	public void setLight( int id, float x, float y, float z, float range ){
	    /*
	    glUniform*/
		lights[id][0] = x;
		lights[id][1] = y;
		lights[id][2] = z;
		lights[id][3] = range;
	}
	
	public void setUniformLighting(){
		FloatBuffer bufferedLights;
		
		bufferedLights = lightBuffer.asFloatBuffer();
		bufferedLights.rewind();
		for ( int i=0; i < 8; i++ ) {
			bufferedLights.put( lights[i] );
		}
		bufferedLights.flip();
		//glUniformMatrix4( uniformLightPos, false, bufferedLights );
		glUniform4(uniformLightPos, bufferedLights);
	}
	
	public void bind(){
		glUseProgram( programIndex );
	}
	
	public void unbind(){
		glUseProgram( 0 );
	}
	
	public void destroy(){
		glUseProgram( 0 );
		glDeleteProgram( programIndex );
	}
}
