package utils3D;

import Application3D.Application3D;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.util.vector.Vector2f;

public class GLFont {
	private Font font;
	private int fontSize;
	private CharData[] charData;
	private Texture fontTexture;

	public static final boolean FONT_DEBUG = false;
	public static final int CHAR_PADDING = 1;
	public static final int FA_LEFT   = 0;
	public static final int FA_CENTER = 1;
	public static final int FA_RIGHT  = 2;
	
	public int DRAW_STYLE = FA_LEFT;
	
	public GLFont(Font font) {
		this.charData    = new CharData[256];
		this.font        = font;
		this.fontSize    = font.getSize();
		
		// Create the sprite-sheet
		long time = System.currentTimeMillis();
		BufferedImage fontImage = generateFontBitmap();
	    // Export to see how pretty it is
		// Load the font into openGL
		this.fontTexture = Application3D.getApp().getResources().loadTexture( fontImage, "font_"+this.font.getFontName()+"_"+this.fontSize );
	}

	/**
	 * Exports a font as a png image
	 * @param fontImage
	 * @param fileName
	 */
	public void exportFont(BufferedImage fontImage, String fileName) {
		try {
		    // retrieve image
		    File outputfile = new File(fileName);
		    ImageIO.write(fontImage, "png", outputfile);
		} catch (IOException e) {
		    //
		}
	}
	/**
	 * Renders a string
	 * @param shader
	 * @param text
	 * @param x
	 * @param y
	 */
	public void drawString(Shader shader, String text, float x, float y, float alpha) {
		if (text == null || text.length() == 0)
			return;
		
		Color color = Color.white;
		float offsetX = 0;
		if (this.DRAW_STYLE == FA_CENTER) {
			offsetX = -getStringWidth(text)/2f - 1;
		}else if (this.DRAW_STYLE == FA_RIGHT) {
			offsetX = -getStringWidth(text);
		}
		fontTexture.bind();
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (ch == ChatColor.COLOR_CODE.charAt(0) && i < text.length() - 1) {
				ChatColor chatColor = ChatColor.getChatColor( text.substring(i, i + 2) );
				color = new Color(chatColor.getRed(), chatColor.getGreen(), chatColor.getBlue());
				i++;
			} else {
				CharData data = charData[(int)ch];

				
				Application3D.getApp().getWorldMatrix().setIdentity();
				Application3D.getApp().getWorldMatrix().translate( new Vector2f( x + offsetX - data.getXOffset(), y - data.getYOffset() - 1 ) );
				Application3D.getApp().getActiveShader().setUniformColour( color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, (color.getAlpha()/255f)*alpha );
				Application3D.getApp().matrixWorldBind();
				data.getModel().render();/*draw(fontTexture, null, null, null, shader, color);*/
				Application3D.getApp().getWorldMatrix().setIdentity();
				offsetX += data.getCharBounds().getWidth();
			}
		}
	}
	
	/**
	 * Gets the width of the string when rendered with the font
	 * @param text
	 * @return
	 */
	public float getStringWidth(String text) {
		text = ChatColor.stripColor(text);
		int stringWid = 0;
		for (int i = 0; i < text.length(); i++) {
			stringWid += charData[(int)text.charAt(i)].getCharBounds().getWidth();
		}
		return stringWid;
	}
	
	/**
	 * Draws a string with a shadow in the given direction
	 * @param shader
	 * @param text
	 * @param x
	 * @param y
	 * @param direction
	 */
	public void drawStringShadow(Shader shader, String text, float x, float y, Vector2f direction, float alpha, float shadowAlpha) {
		drawString(shader, ChatColor.BLACK + ChatColor.stripColor(text), x + direction.x, y + direction.y, shadowAlpha);
		drawString(shader, text, x, y, alpha);
	}
	
	/**
	 * Draws a string with a black outline.
	 * @param shader
	 * @param text
	 * @param x
	 * @param y
	 */
	public void drawStringOutlined(Shader shader, String text, float x, float y, float alpha, float outlineAlpha) {
		String text2 = ChatColor.BLACK + ChatColor.stripColor(text);
		drawString(shader, text2, x + 1, y, outlineAlpha );
		drawString(shader, text2, x - 1, y, outlineAlpha );
		drawString(shader, text2, x, y + 1, outlineAlpha );
		drawString(shader, text2, x, y - 1, outlineAlpha );
		drawString(shader, text, x, y, alpha);
	}
	
	public void drawStringShadowOutlined(Shader shader, String text, float x, float y, Vector2f direction, float alpha, float shadowAlpha) {
	    this.drawStringShadow( shader, text, x, y, direction, alpha, shadowAlpha );
	    this.drawStringOutlined(  shader, text, x, y, alpha, shadowAlpha );
	}
	
	/**
	 * Rounds up to the next nearest power of two.
	 * @param dimension
	 * @return
	 */
	private static int getNextPowerOf2(int dimension) {
		int val = dimension;
		int powof2 = 1;
		while( powof2 < val ) powof2 <<= 1;
		return powof2;
	}

	private BufferedImage generateFontBitmap() {
		int width  = getNextPowerOf2((16 + CHAR_PADDING) * fontSize);
		int height = getNextPowerOf2((16 + CHAR_PADDING) * fontSize);
		int cellSize = width/16;
		
		// Create the base font bitmap
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) image.getGraphics();
		
		// Clear the background color
		g.setColor(new Color(0,0,0,1));
		g.fillRect( 0, 0, width, height );

		// Start letter drawing
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.white);
		g.setFont(font);
		
		// Get font data from all characters and render to bitmap
		FontMetrics fontMetrics = g.getFontMetrics();
		for (int i = 0; i < charData.length; i++) {
			int cellX = (i % 16) * cellSize;
			int cellY = (i / 16) * cellSize;
			float centerX = cellX + (cellSize / 2f);
			float centerY = cellY + (cellSize / 2f);
			
			charData[i] = new CharData((char)i, fontMetrics, g,
													cellSize,
													cellX/(float)width,
													cellY/(float)height,
													cellSize/(float)width,
													cellSize/(float)width);
			
			Rectangle2D rect = charData[i].getCharBounds();
			float boundsWid = (float) rect.getWidth();
			float boundsHei = (float) rect.getHeight();
			
			if (FONT_DEBUG) {				
				// Draw the char bounds
				g.setColor(Color.blue);
				g.drawRect((int)(centerX - boundsWid/2d), (int)(centerY - boundsHei/2d), (int)boundsWid, (int)boundsHei);
				
				// Draw horizontal line
				g.setColor(Color.yellow);
				int drawY = (int) ((centerY - boundsHei/2d) + charData[i].getAscent());
				g.drawLine(cellX, drawY, cellX + cellSize, drawY);
				
				// Draw the cell
				g.setColor(Color.red);
				g.drawRect(cellX, cellY, cellSize - 1, cellSize - 1);
			}
			
			// Draw the character
			g.setColor(Color.white);
			g.drawString(charData[i].getCharacter(), centerX - (boundsWid/2f), (int)(centerY - boundsHei/2d) + fontMetrics.getAscent());
		}
		
		return image;
	}

	private class CharData {
		private String c;
		private int ascent;
		private Rectangle2D charBound;
		private VertexBuffer letter;
		private int xoff;
		private int yoff;
		private float cellSize;
		
		public CharData(char c, FontMetrics font, Graphics g, float cellSize, float tx, float ty, float tw, float th) {
			this.c = String.valueOf(c);
			this.ascent = font.getAscent();
			this.charBound = font.getStringBounds(this.c, g);
			this.cellSize = cellSize;
			
			this.letter = new VertexBuffer();
			this.letter.addQuadExt( 0, 0, cellSize, cellSize, tx, ty, tx + tw, ty+th );
			this.letter.freeze();
			
			float centerX = cellSize / 2f;
			float centerY = cellSize / 2f;
			float boundsWid = (float) charBound.getWidth();
			float boundsHei = (float) charBound.getHeight();
			float left = centerX - (boundsWid/2f);
			float top  = centerY - (boundsHei/2f);
			this.xoff = (int) left;
			this.yoff = (int) top;
		}
		
		public float getCellSize() {
			return this.cellSize;
		}

		public float getXOffset() {
			return this.xoff;
		}
		
		public float getYOffset() {
			return this.yoff;
		}

		public VertexBuffer getModel() {
			return this.letter;
		}
		
		public String getCharacter() {
			return this.c;
		}
		
		public Rectangle2D getCharBounds() {
			return charBound;
		}
		
		public int getAscent() {
			return this.ascent;
		}
	}

	public int getSize() {
		return this.fontSize;
	}

}
