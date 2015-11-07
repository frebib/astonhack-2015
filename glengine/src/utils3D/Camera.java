package utils3D;

import Application3D.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import static org.lwjgl.opengl.GL20.*;

public class Camera {
	// Camera matrices
	private Matrix4f matrixView;
	private Matrix4f matrixProjection;
	private Matrix4f matrixProjectionOrtho;
	private Matrix4f matrixIdentity;
	private FloatBuffer bufferedMatrixView;
	private FloatBuffer bufferedMatrixProjection;
	private FloatBuffer bufferedMatrixProjectionOrtho;

	// Camera properties
	private float FOV = 80.0f;
	private float aspect;
	private float camNear = 0.1f;
	private float camFar = 1368.0f;

	// Camera location
	private Vector3f position;
	private Vector3f positionSmooth;
	private Vector3f to;
	private Vector3f up;
	private Vector3f direction;
	private Vector3f cameraLookAt;

	// Working variables
	private int clickX, clickY;
	private boolean clicked = false;

	// Camera rotation
	private float yaw = (float) Math.PI;
	private float pitch = -0.5944446f;
	private float roll;

	// Single-tap button control
	private boolean keyQPressed, keyEPressed;

	private Application3D scapp;

	// Constructor
	public Camera(float FOV, float screenWidth, float screenHeight, Application3D scapp) {
		this.FOV = FOV;
		this.aspect = screenWidth / screenHeight;

		matrixView = new Matrix4f();
		matrixProjection = new Matrix4f();
		matrixProjectionOrtho = new Matrix4f();
		matrixIdentity = new Matrix4f();
		matrixIdentity.setIdentity();

		position = new Vector3f(-539.90204f, 851.88873f, 227.5f);
		to = new Vector3f(1, 1, 1);
		up = new Vector3f(0, 0, 1);
		direction = new Vector3f(0, 0, 0);
		positionSmooth = new Vector3f(position);
		cameraLookAt = new Vector3f(0, 0, 0);

		bufferedMatrixView = BufferUtils.createFloatBuffer(16);
		bufferedMatrixProjection = BufferUtils.createFloatBuffer(16);
		bufferedMatrixProjectionOrtho = BufferUtils.createFloatBuffer(16);
		prepareProjectionMatrix();
		prepareProjectionMatrix2D(0.0f, screenWidth, 0.0f, screenHeight, -128.0f, 128.0f);

		this.scapp = scapp;
	}

	/**
	 * Prepares the projection matrix for 3D-perspective rendering.
	 */
	private void prepareProjectionMatrix() {
		// Projection matrix (Frustum)
		float y_scale = this.coTangent(this.degreesToRadians(FOV / 2f));
		float x_scale = y_scale / aspect;
		float frustum_length = camFar - camNear;

		matrixProjection.m00 = x_scale;
		matrixProjection.m11 = y_scale;
		matrixProjection.m22 = -((camFar + camNear) / frustum_length);
		matrixProjection.m23 = -1;
		matrixProjection.m32 = -((2 * camNear * camFar) / frustum_length);
		matrixProjection.m33 = 0;

		matrixProjection.store(bufferedMatrixProjection);
		bufferedMatrixProjection.flip();
	}

	/**
	 * Prepare an orthographic projection matrix based on the bounding region
	 * defined by (right, left, top, bottom) given clipping planes (near, far)
	 * 
	 * @param right
	 * @param left
	 * @param top
	 * @param bottom
	 * @param near
	 * @param far
	 */
	public void prepareProjectionMatrix2D(float left, float right, float top, float bottom, float near, float far) {

		matrixProjectionOrtho.m00 = 2.0f / (right - left);
		matrixProjectionOrtho.m30 = -(right + left) / (right - left);
		matrixProjectionOrtho.m11 = 2.0f / (top - bottom);
		matrixProjectionOrtho.m31 = -(top + bottom) / (top - bottom);
		matrixProjectionOrtho.m22 = -2.0f / (far - near);
		matrixProjectionOrtho.m32 = -(far + near) / (far - near);
		matrixProjectionOrtho.m33 = 1.0f;

		matrixProjectionOrtho.store(bufferedMatrixProjectionOrtho);
		bufferedMatrixProjectionOrtho.flip();
	}

	/**
	 * This method prepares the view matrix for use in rendering. The view
	 * matrix represents the position of the virtual camera in 3D space. - Reset
	 * view matrix to identity matrix - Convert from spherical coordinates to
	 * Cartesian view-direction unit vector - Pass Vectors into lookAt method to
	 * build matrix. - Buffer the matrix so it can be streamed to the GPU.
	 */
	private void prepareViewMatrix() {
		matrixView.setIdentity();

		matrixView.rotate(pitch, new Vector3f(1, 0, 0));
		matrixView.rotate(-yaw, new Vector3f(0, 0, 1));
		matrixView.translate(new Vector3f(-positionSmooth.x, -positionSmooth.y, -positionSmooth.z));

		matrixView.store(bufferedMatrixView);

		bufferedMatrixView.flip();

	}

	/**
	 * @return Returns a reference to the camera's projection matrix
	 */
	public Matrix4f getProjectionMatrix() {
		return matrixProjection;
	}

	/**
	 * @return Returns a reference to the camera's view matrix
	 */
	public Matrix4f getViewMatrix() {
		return matrixView;
	}

	/**
	 * Prepares the specified shader for 3D rendering.
	 * 
	 * @param s
	 */
	public void setProjection3D(Shader s) {
		prepareViewMatrix();

		glUniformMatrix4(s.getUniformLocationMatrixProjection(), false, bufferedMatrixProjection);
		glUniformMatrix4(s.getUniformLocationMatrixView(), false, bufferedMatrixView);

		/*
		 * matrixView.invert(); matrixView.store( bufferedMatrixView );
		 * bufferedMatrixView.flip(); glUniformMatrix4(
		 * s.getUniformLocationMatrixViewInverse(), false, bufferedMatrixView );
		 */
	}

	/**
	 * Configures the specified shader 'shader' for 2D rendering by passing the
	 * buffered, orthographic projection matrix to the shader.
	 * 
	 * @param s
	 */
	public void setProjection2D(Shader s) {
		glUniformMatrix4(s.getUniformLocationMatrixProjection(), false, bufferedMatrixProjectionOrtho);
	}

	/**
	 * @param angle
	 * @return Returns the cotangent of the given angle
	 */
	private float coTangent(float angle) {
		return (float) (1f / Math.tan(angle));
	}

	/**
	 * Converts an angle from degrees to radians
	 * 
	 * @param degrees
	 * @return angle in radians
	 */
	private float degreesToRadians(float degrees) {
		return degrees * (float) (Math.PI / 180d);
	}

	// Update the camera. This allows the camera to be moved, rotated (or
	// whatever else is necessary)
	public void update() {
		// Temporary mouse look [HACKY, but Java doens't want to cooperate]
		/*if (!Mouse.isButtonDown(2) && Display.isActive()) {

			int mx = Mouse.getX();
			int my = Mouse.getY();

			int cx = this.scapp.getWindowWidth() / 2;
			int cy = this.scapp.getWindowHeight() / 2;

			int dx = mx - cx;
			int dy = my - cy;

			pitch -= dy / 180.0;
			yaw -= dx / 180.0;

			// Reset Mouse
			pitch = (float) Math.min(Math.max(-Math.PI, pitch), 0);
			Mouse.setCursorPosition(cx, cy);
		}*/
		// Update direction
		cameraLookAt.set((float) Math.cos(yaw + Math.PI / 2.0), (float) Math.sin(yaw + Math.PI / 2.0),
				(float) Math.sin(pitch - Math.PI / 2.0));
/*
		// Movement controls
		if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			Vector3f.sub(position, direction, position);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			Vector3f.add(position, direction, position);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			direction.set((float) Math.cos(yaw) * speed, (float) Math.sin(yaw) * speed, 0);
			Vector3f.sub(position, direction, position);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			direction.set((float) Math.cos(yaw) * speed, (float) Math.sin(yaw) * speed, 0);
			Vector3f.add(position, direction, position);
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_P)) {
			System.out.println(position.x + " " + position.y + " " + position.z);
		}

		direction.set((float) Math.cos(yaw + Math.PI / 2.0), (float) Math.sin(yaw + Math.PI / 2.0),
				(float) Math.sin(pitch + Math.PI / 2.0));*/

		// Smooth
		positionSmooth.x += (position.x - positionSmooth.x) * 0.65;
		positionSmooth.y += (position.y - positionSmooth.y) * 0.65;
		positionSmooth.z += (position.z - positionSmooth.z) * 0.65;

		prepareViewMatrix();
	}

	public void setDimensions(int screenWidth, int screenHeight) {
		this.aspect = screenWidth / screenHeight;
		prepareProjectionMatrix2D(0.0f, screenWidth, 0.0f, screenHeight, -128.0f, 128.0f);
	}

	public float getPitch() {
		return this.pitch;
	}
	
	public void setPitch( float pitch ) {
		this.pitch = pitch;
	}

	public float getYaw() {
		return this.yaw;
	}
	
	public void setYaw( float yaw ) {
		this.yaw = yaw;
	}

	public float getX() {
		return this.positionSmooth.x;
	}
	
	public Vector3f getPosition(){
		return this.position;
	}
	
	public void setPosition( Vector3f position ){
		this.position = position;
	}
	
	public Vector3f getDirection(){
		return this.direction;
	}
	
	public void setDirection( Vector3f direction ) {
		this.direction = direction;
	}

	public float getY() {
		return this.positionSmooth.y;
	}

	public float getZ() {
		return this.positionSmooth.z;
	}

	/**
	 * Converts a point in 3D space to a point in 2D space. The X,Y components
	 * of the
	 * 
	 * @param position3D
	 * @return returns a vec3( x-coordinate on screen, y-coordinate on screen,
	 *         depth-bias from camera)
	 */
	public Vector3f projectTo2D(Vector3f position3D) {
		// project to view space
		Vector4f vs = VecByMatrix(this.getViewMatrix(), new Vector4f(-position3D.x, position3D.y, position3D.z, 1.0f));

		// Project to screen space
		Vector4f ss = VecByMatrix(this.getProjectionMatrix(), vs);

		// Perspective divide
		ss.x /= ss.w;
		ss.y /= ss.w;
		ss.z /= ss.w;

		float depth = (1.0f - Math.min(Math.max(0.0f, (-vs.z / this.camFar) * 0.5f + 0.5f), 1.0f)) / 0.5f;
		// Scale and bias
		if (depth >= 0.0f && depth <= 1.0f) {
			return new Vector3f(((float) ss.x * 0.5f + 0.5f) * (float) Display.getWidth(),
					(1.0f - ((float) ss.y * 0.5f + 0.5f)) * (float) Display.getHeight(), depth);
		} else {
			// In the event that the depth is incorrect, i.e the position has
			// been back-projected, this will prevent the coordinate from being
			// re-scaled into the normal range.
			return new Vector3f(-10000.0f, -10000.0f, 0.0f);
		}
	}

	public Vector4f VecByMatrix(Matrix4f m, Vector4f v) {
		return new Vector4f(v.x * m.m00 + v.y * m.m10 + v.z * m.m20 + v.w * m.m30,
				v.x * m.m01 + v.y * m.m11 + v.z * m.m21 + v.w * m.m31,
				v.x * m.m02 + v.y * m.m12 + v.z * m.m22 + v.w * m.m32,
				v.x * m.m03 + v.y * m.m13 + v.z * m.m23 + v.w * m.m33);
	}

	public int[] getScreenCoords(double x, double y, double z) {
		FloatBuffer screenCoords = BufferUtils.createFloatBuffer(3);
		IntBuffer viewport = BufferUtils.createIntBuffer(16);
		GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);

		boolean result = GLU.gluProject((float) x, (float) y, (float) z, this.bufferedMatrixView,
				this.bufferedMatrixProjection, viewport, screenCoords);
		if (result) {
			return new int[] { (int) screenCoords.get(0), (int) screenCoords.get(1) };
		}
		return null;
	}

	public void focusOn(float x, float y, float z, float distance) {

		position.x = x - cameraLookAt.x * distance;
		position.y = y - cameraLookAt.y * distance;
		position.z = z - cameraLookAt.z * distance;
	}

	public void zoomIn(float distance) {
		if (position.z > 110) {
			position.x += cameraLookAt.x * distance;
			position.y += cameraLookAt.y * distance;
			position.z += cameraLookAt.z * distance;
		}
	}

	public void zoomOut(float distance) {
		if (position.z < 500) {
			position.x -= cameraLookAt.x * distance;
			position.y -= cameraLookAt.y * distance;
			position.z -= cameraLookAt.z * distance;
		}
	}

}
