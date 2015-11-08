package utils3D;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModelImporter {
	
	public static VertexBuffer importModel( String filename ) {
	    VertexBuffer vbo = new VertexBuffer();
		try( BufferedReader br 	= new BufferedReader(new FileReader(new File( filename )))){
			
			// Dump all data from file
			String line;
			List< List<Float> > fileData = new ArrayList<>();
			
			
			while ( (line = br.readLine()) != null ) {
				
				// Collect into array list of floats
				List<Float> floats = new ArrayList<>();
				for( String s : line.split( " " )) floats.add( Float.parseFloat(s));
				
				// Add to line list
				fileData.add( floats );
			}
			
			// Perform import logic
				// Read header
				float header0 = fileData.get(0).get(0);
				float header1 = fileData.get(1).get(0);
				
				// Read data
				int color, r, g, b;
				
				for ( int i = 2; i < fileData.size(); i++ ) {
				    Vertex v = null;
					// Get call type
					CallType callType = CallType.values()[ (int)(float)fileData.get(i).get(0) ];
					List<Float> rd = fileData.get(i);
					
					switch( callType ){
					    case START_PRIMITIVE:
					        // n / a : UNSUPPORTED BY VBO SETUP ASSUMES TRIANGLE-LIST
					    break;
					    case END_PRIMITIVE:
					        // n / a : UNSUPPORTED BY VBO SETUP, ASSUMES TRIANGLE-LIST
					    break;
                        //case MODEL_VERTEX:
                        case MODEL_VERTEX_NORMAL:
                            v = new Vertex();
                            v.setXYZ( -rd.get(1), rd.get(2), rd.get(3) );
                            v.setRGB(  1.0f, 1.0f, 1.0f );
                            v.setUV( 0.0f, 0.0f );
                            v.setNML( rd.get(4), rd.get(5), rd.get(6));
                        break;
                        //case MODEL_VERTEX_COLOUR:
                        case MODEL_VERTEX_NORMAL_COLOUR:
                            v = new Vertex();
                            v.setXYZ( -rd.get(1), rd.get(2), rd.get(3) );
                            
                            color = (int)(float)rd.get(7);
                            r     = ( color ) & 256;
                            g     = ( color >> 8) & 256;
                            b     = ( color >> 16) & 256;
                         
                            v.setRGB(  r/255.0f, g/255.0f, b/255.0f );
                            v.setUV( 0.0f, 0.0f );
                            v.setNML( rd.get(4), rd.get(5), rd.get(6));
                        break;
                        case MODEL_VERTEX_NORMAL_TEXTURE:
                        //case MODEL_VERTEX_TEXTURE:
                            v = new Vertex();
                            v.setXYZ( -rd.get(1), rd.get(2), rd.get(3) );
                            v.setRGB(  1.0f, 1.0f, 1.0f );
                            v.setUV( rd.get(7), rd.get(8) );
                            v.setNML( rd.get(4), rd.get(5), rd.get(6));
                        break;
                        case MODEL_VERTEX_NORMAL_TEXTURE_COLOUR:
                        //case MODEL_VERTEX_TEXTURE_COLOUR:
                            v = new Vertex();
                            v.setXYZ( -rd.get(1), rd.get(2), rd.get(3) );
                            
                            color = (int)(float)rd.get(9);
                            r     = ( color ) & 256;
                            g     = ( color >> 8) & 256;
                            b     = ( color >> 16) & 256;
                         
                            v.setRGB(  r/255.0f, g/255.0f, b/255.0f );
                            v.setUV( rd.get(7), rd.get(8) );
                            v.setNML( rd.get(4), rd.get(5), rd.get(6));
                        break;
                        default:
                            v=null;
                        break;
					}
                    if ( v!=null ){
                        v.setRGB( 1, 1, 1 );
                        vbo.add( v );
                    }
				}
			
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		vbo.prepareNormals();
		vbo.freeze();
        return vbo;
	}
	
	/**
	 * Call types for the D3D (GMMODO model format.
	 * @author Michael
	 *
	 */
	private static enum CallType {
		START_PRIMITIVE,
		END_PRIMITIVE,
		MODEL_VERTEX,
		MODEL_VERTEX_COLOUR,
		MODEL_VERTEX_TEXTURE,
		MODEL_VERTEX_TEXTURE_COLOUR,
		MODEL_VERTEX_NORMAL,
		MODEL_VERTEX_NORMAL_COLOUR,
		MODEL_VERTEX_NORMAL_TEXTURE,
		MODEL_VERTEX_NORMAL_TEXTURE_COLOUR
	}
}
