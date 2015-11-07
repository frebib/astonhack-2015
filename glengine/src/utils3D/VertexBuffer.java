package utils3D;

import Application3D.Application3D;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class VertexBuffer {
	private ArrayList<Vertex> vertices;
	private FloatBuffer		  bufferedVertices;
	private ByteBuffer		  indexBuffer;
	private boolean			  isBound = false;
	
	private int vertexArrayIndex;
	private int vertexBufferIndex;
	private int staticVertexCount;
	
	public VertexBuffer(){
		vertices = new ArrayList<>();
		
		// Create vertex buffer
		vertexBufferIndex = glGenBuffers();
		vertexArrayIndex = glGenVertexArrays();
	}
	
	/* The freeze method will upload the static version of the geometry to 
	 * the gpu.
	 * Any vertices added will need to be re-submitted using the freeze function
	 * 
	 */
	public void freeze(){
		// Create Floatbuffer
		ByteBuffer tempBuffer = BufferUtils.createByteBuffer( vertices.size() * Vertex.stride );
		bufferedVertices	  = tempBuffer.asFloatBuffer();
		staticVertexCount 	  = vertices.size();
		for ( Vertex v : vertices ) {
			bufferedVertices.put( v.getElements() );
		}
		bufferedVertices.flip();
		
		// Upload data to buffer
		this.bindBuffers();
		glBufferData( GL_ARRAY_BUFFER, bufferedVertices, GL_STATIC_DRAW );
		
		// Bind vertex attributes
		glVertexAttribPointer( 0, Vertex.positionElementCount, GL_FLOAT, 
								false, Vertex.stride, Vertex.positionByteOffset );
		glVertexAttribPointer( 1, Vertex.colorElementCount, GL_FLOAT,
								false, Vertex.stride, Vertex.colorByteOffset );
		glVertexAttribPointer( 2, Vertex.textureElementCount, GL_FLOAT,
								false, Vertex.stride, Vertex.textureByteOffset );
	    glVertexAttribPointer( 3, Vertex.normalElementCount, GL_FLOAT,
                                false, Vertex.stride, Vertex.normalByteOffset );
		// Set loaded to true (This allows rendering to occur in the render script
		isBound = true;
		
		// Bind to 0
		this.unbindBuffers();
		
		Application3D.getApp().appCheckGLError( "VERTEX BUFFER END");
	}
	
	// Building
	public void add( Vertex v ){
		vertices.add( v );
	}
	public void add( float x, float y, float z, float r, float g, float b, float a, float u, float v, float nx, float ny, float nz){
		Vertex vert = new Vertex();
		vert.setXYZ(x, y, z);
		vert.setRGBA(r, g, b, a);
		vert.setUV(u, v);
		vert.setNML( nx, ny, nz );
		vertices.add( vert );
		
	}
	
	
	
	// Render
	public void render(){
		if( !isBound ){ return; }
		// Bind buffer
		this.bindBuffers();
		this.enableAttributes();
		{
			
			glDrawArrays( GL_TRIANGLES, 0, staticVertexCount );
		}
		this.disableAttributes();
		this.unbindBuffers();
	}
	
	// [IMPORTANT]: This is a debug function and should NOT be used for rendering.
	public void renderImmediate(){
		glBegin( GL_TRIANGLES );
			for ( Vertex v : vertices ) {
				float[] a = v.getXYZ();
				glVertex3f(a[0], a[1], a[2]);
				
			}
		glEnd();
	}
	
	// Sub-scripts used in the render process for binding and unbinding data handles.
	private void bindBuffers(){
		glBindVertexArray( vertexArrayIndex );
		glBindBuffer( GL_ARRAY_BUFFER, vertexBufferIndex );
	}
	
	private void unbindBuffers(){
		glBindBuffer( GL_ARRAY_BUFFER, 0 );
		glBindVertexArray( 0 );
	}
	
	private void enableAttributes(){
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);
		glEnableVertexAttribArray(3);
	}
	
	private void disableAttributes(){
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glDisableVertexAttribArray(2);
		glDisableVertexAttribArray(3);
	}
	
	// Destroy - destroy all GL data
	public void destroy(){
		bindBuffers();
		enableAttributes();
		glDeleteBuffers( vertexBufferIndex );
		glDeleteVertexArrays( vertexArrayIndex );
		unbindBuffers();
		disableAttributes();
	}
	
	
	/// GENERATORS
	public void addFloor( float x1, float y1, float x2, float y2, float z){
        this.add( x1, y1, z, 1, 1, 1, 1, 0, 1, 0, 0, 1);
        this.add( x1, y2, z, 1, 1, 1, 1, 0, 0, 0, 0, 1);
        this.add( x2, y2, z, 1, 1, 1, 1, 1, 0, 0, 0, 1);
        
        this.add( x2, y1, z, 1, 1, 1, 1, 1, 1, 0, 0, 1);
        this.add( x1, y1, z, 1, 1, 1, 1, 0, 1, 0, 0, 1);
        this.add( x2, y2, z, 1, 1, 1, 1, 1, 0, 0, 0, 1);
	}
	
	public void addWall( float x1, float y1, float z1, float x2, float y2, float z2){
        /*this.add( x1, y2, z, 1, 1, 1, 1, 0, 1, 0, 0, 1);
        this.add( x1, y1, z, 1, 1, 1, 1, 0, 0, 0, 0, 1);
        this.add( x2, y1, z, 1, 1, 1, 1, 1, 0, 0, 0, 1);
        
        this.add( x2, y2, z, 1, 1, 1, 1, 1, 1, 0, 0, 1);
        this.add( x1, y2, z, 1, 1, 1, 1, 0, 1, 0, 0, 1);
        this.add( x2, y1, z, 1, 1, 1, 1, 1, 0, 0, 0, 1);*/
		this.add( x2, y2, z1, 1, 1, 1, 1, 1, 1, 0, 0, 1);
		this.add( x1, y1, z1, 1, 1, 1, 1, 0, 1, 0, 0, 1);
		this.add( x1, y1, z2, 1, 1, 1, 1, 0, 0, 0, 0, 1);
		
		this.add( x2, y2, z1, 1, 1, 1, 1, 1, 1, 0, 0, 1);
		this.add( x1, y1, z2, 1, 1, 1, 1, 0, 0, 0, 0, 1);
		this.add( x2, y2, z2, 1, 1, 1, 1, 1, 0, 0, 0, 1);
	}
	
	/**
	 * Adds a model to the model
	 * @param vertexBuffer
	 */
	public void addModel( VertexBuffer vertexBuffer, Matrix4f transformationMatrix ) {
		for( Vertex v : vertexBuffer.vertices ) {
			this.add( v.transform( transformationMatrix ));
		}
	}
	
	public void addQuad( float x1, float y1, float x2, float y2 ){
	    this.add( x1, y2, 0, 1, 1, 1, 1, 0, 1, 0, 0, 1);
        this.add( x1, y1, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1);
        this.add( x2, y1, 0, 1, 1, 1, 1, 1, 0, 0, 0, 1);
        
        this.add( x2, y2, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1);
        this.add( x1, y2, 0, 1, 1, 1, 1, 0, 1, 0, 0, 1);
        this.add( x2, y1, 0, 1, 1, 1, 1, 1, 0, 0, 0, 1);
	}
	
	public void addQuadExt( float x1, float y1, float x2, float y2, float tx1, float ty1, float tx2, float ty2 ){
        this.add( x1, y2, 0, 1, 1, 1, 1, tx1, ty2, 0, 0, 1);
        this.add( x1, y1, 0, 1, 1, 1, 1, tx1, ty1, 0, 0, 1);
        this.add( x2, y1, 0, 1, 1, 1, 1, tx2, ty1, 0, 0, 1);
        
        this.add( x2, y2, 0, 1, 1, 1, 1, tx2, ty2, 0, 0, 1);
        this.add( x1, y2, 0, 1, 1, 1, 1, tx1, ty2, 0, 0, 1);
        this.add( x2, y1, 0, 1, 1, 1, 1, tx2, ty1, 0, 0, 1);
    }
	
	public void prepareNormals(){
		for( int g = 0; g < vertices.size(); g += 3 ){
			Vertex A, B, C;
			A = vertices.get(g);
			B = vertices.get(g+1);
			C = vertices.get(g+2);
			
			float [] Ac, Bc, Cc;
			Ac = A.getXYZ();
			Bc = B.getXYZ();
			Cc = C.getXYZ();
			
			Vector3f AB, AC;
			AB = new Vector3f( Bc[0]-Ac[0], Bc[1]-Ac[1], Bc[2]-Ac[2]);
			AC = new Vector3f( Cc[0]-Ac[0], Cc[1]-Ac[1], Cc[2]-Ac[2]);
			
			Vector3f normal = new Vector3f();
			Vector3f.cross(AB, AC, normal);
			normal.normalise();
			
			A.setNML(normal.x, normal.y, normal.z);
			B.setNML(normal.x, normal.y, normal.z);
			C.setNML(normal.x, normal.y, normal.z);
		}
	}
}
