import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.nio.*;
import java.lang.Math;
import javax.swing.*;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_CCW;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_LEQUAL;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.common.nio.Buffers;
import org.joml.*;
	
public class HW3_FINAL extends JFrame implements GLEventListener, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

	private static final long serialVersionUID = 1L;
	private GLCanvas myCanvas;
	private int renderingProgramBox;
	private int renderingProgramLights;
	private int renderingProgramCube;
	private int vao[] = new int[1];
	private int vbo[] = new int[10];
	
	//Textures
	private int brickTexture, skyboxTexture, iceTexture, marsTexture;
	private float pyrLocX, pyrLocY, pyrLocZ;
	private float pentLocX, pentLocY, pentLocZ;
	private float cubLocX, cubLocY, cubLocZ;

	//Torus
	private Torus myTorus;
	private int numTorusVertices, numTorusIndices;
	
	private Vector3f torusLoc = new Vector3f(0.0f, 0.0f, 0.0f);
	//private Vector3f cameraLoc = new Vector3f(0.0f, 0.0f, 3.0f);
	
	// display function
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private Matrix4f pMat = new Matrix4f();  // perspective matrix
	private Matrix4f vMat = new Matrix4f();  // view matrix
	private Matrix4f mMat = new Matrix4f();  // model matrix
	private Matrix4f mvMat = new Matrix4f(); // model-view matrix
	private int mvLoc, projLoc;
	private float aspect;

	//Camera
	private float targetX, targetY, targetZ;//Where camera is looking at
	private Vector3f camVec, targetVec, f,r,u;//Vectors that define camera
	private float Ddist=1.0f, Dangle=0.01f; //Distance and angle increments
	private float cubeLocX, cubeLocY, cubeLocZ;
	private float cameraX, cameraY, cameraZ;
	private float sphLocX, sphLocY, sphLocZ;
	
	//NUEVO LUZ
	
	private Matrix4f invTrMat = new Matrix4f(); // inverse-transpose
	private int nLoc;
	private int globalAmbLoc, ambLoc, diffLoc, specLoc, posLoc, mambLoc, mdiffLoc, mspecLoc, mshiLoc;
	private Vector3f currentLightPos = new Vector3f();
	private float[] lightPos = new float[3];

	// white light properties
	float[] globalAmbient = new float[] { 1f, 1f, 1f, 1.0f };
	float[] lightAmbient = new float[] { 1f, 1f, 1f, 1.0f };
	float[] lightDiffuse = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
	float[] lightSpecular = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
		
	// gold material
	float[] matAmbGold = Utils.goldAmbient();
	float[] matDifGold = Utils.goldDiffuse();
	float[] matSpeGold = Utils.goldSpecular();
	float matShiGold = Utils.goldShininess();	
	
	//Silver material
	float[] matAmbSil = Utils.silverAmbient();
	float[] matDifSil = Utils.silverDiffuse();
	float[] matSpeSil = Utils.silverSpecular();
	float matShiSil = Utils.silverShininess();	
	
	// bronze material
	float[] matAmbBronce = Utils.bronzeAmbient();
	float[] matDifBronce = Utils.bronzeDiffuse();
	float[] matSpeBronce = Utils.bronzeSpecular();
	float matShiBronce = Utils.bronzeShininess();
	
	//apagar luz
	boolean apagarLuz = false;
	
	private Vector3f initialLightLoc = new Vector3f(5.0f, 2.0f, 2.0f);
	private float amt = 0.0f;

	public HW3_FINAL() {	
		
		setTitle("HW3-Nuevo");
		setSize(800, 800);
		//Making sure we get a GL4 context for the canvas
        GLProfile profile = GLProfile.get(GLProfile.GL4);
        GLCapabilities capabilities = new GLCapabilities(profile);
		myCanvas = new GLCanvas(capabilities);
 		//end GL4 context
		myCanvas.addGLEventListener(this);
		myCanvas.addKeyListener(this);

		this.add(myCanvas);
		this.setVisible(true);
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		Animator animator = new Animator(myCanvas);
		animator.start();
		
	}

	public void display(GLAutoDrawable drawable) {	
		
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		
		vMat.identity().setTranslation(-camVec.x(), -camVec.y(), -camVec.z());

		// draw cube map
		
		gl.glUseProgram(renderingProgramBox);
		
		mMat.identity().setTranslation(camVec.x(), camVec.y(), camVec.z());
		
		mvMat.lookAt(camVec, targetVec, new Vector3f(0.0f,1.0f,0.0f));

		
		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);
		
		mvLoc = gl.glGetUniformLocation(renderingProgramBox, "mv_matrix");
		projLoc = gl.glGetUniformLocation(renderingProgramBox, "proj_matrix");
		
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
				
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, skyboxTexture);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);	     // cube is CW, but we are viewing the inside
		gl.glDisable(GL_DEPTH_TEST);
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
		gl.glEnable(GL_DEPTH_TEST);
		
		// Torus
		
		gl.glUseProgram(renderingProgramLights);
		
		mvLoc = gl.glGetUniformLocation(renderingProgramLights, "mv_matrix");
		projLoc = gl.glGetUniformLocation(renderingProgramLights, "proj_matrix");
		nLoc = gl.glGetUniformLocation(renderingProgramLights, "norm_matrix");
		
		mMat.identity();
		vMat.translation(-camVec.x(), -camVec.y(), -camVec.z());
		
		mMat.translate(torusLoc.x(), torusLoc.y(), torusLoc.z());
		mMat.rotateX((float)Math.toRadians(30.0f));

		if(!apagarLuz) {
		
			currentLightPos.set(initialLightLoc);
			currentLightPos.rotateAxis((float)Math.toRadians(2), 0.0f, 0.0f, 1.0f);
			
		}
		
		installLightsGold(vMat);
		
		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);
		
		mvMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);

		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
		
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));

		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, brickTexture);

		gl.glClear(GL_DEPTH_BUFFER_BIT);
		
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		
		gl.glEnable(GL_DEPTH_TEST);
		
		gl.glDepthFunc(GL_LEQUAL);

		gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[4]);
		gl.glDrawElements(GL_TRIANGLES, numTorusIndices, GL_UNSIGNED_INT, 0);
		
		
		//Dibujar pirámide
		
		mMat.translation(pyrLocX, pyrLocY, pyrLocZ);
		
		if(!apagarLuz) {
			currentLightPos.set(initialLightLoc);
			currentLightPos.rotateAxis((float)Math.toRadians(2), 0.0f, 0.0f, 1.0f);
		}
		
		installLightsBronce(vMat);

		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);
		
		mvMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);

		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
		gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, marsTexture);
		
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		
		gl.glEnable(GL_DEPTH_TEST);
		
		gl.glDepthFunc(GL_LEQUAL);

		gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[4]);
		gl.glDrawElements(GL_TRIANGLES, numTorusIndices, GL_UNSIGNED_INT, 0);
		
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, 18);
		
		//Dibujar pentágono
				
		mMat.translation(pentLocX, pentLocY, pentLocZ);
		
		if(!apagarLuz) {
			currentLightPos.set(initialLightLoc);
			currentLightPos.rotateAxis((float)Math.toRadians(2), 0.0f, 0.0f, 1.0f);
		}
		
		installLightsBronce(vMat);

		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);
		
		mvMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);
				
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]); 
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, marsTexture);
		
		gl.glClear(GL_DEPTH_BUFFER_BIT);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		
		gl.glEnable(GL_DEPTH_TEST);
		
		gl.glDepthFunc(GL_LEQUAL);

		gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[4]);
		gl.glDrawElements(GL_TRIANGLES, numTorusIndices, GL_UNSIGNED_INT, 0);
		
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		
		gl.glDrawArrays(GL_TRIANGLES, 0, 120); 
		
		//cuadrado donde está la luz
		
		gl.glUseProgram(renderingProgramCube);
		
		mvLoc = gl.glGetUniformLocation(renderingProgramCube, "mv_matrix");
		projLoc = gl.glGetUniformLocation(renderingProgramCube, "proj_matrix");
		nLoc = gl.glGetUniformLocation(renderingProgramCube, "norm_matrix");
		
		mMat.translation(cubLocX, cubLocY, cubLocZ);
		mMat.scale(0.05f, 0.05f, 0.05f);

		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);

		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, marsTexture);
		
		gl.glClear(GL_DEPTH_BUFFER_BIT);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		
		gl.glEnable(GL_DEPTH_TEST);
		
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
		
		
	}

	public void init(GLAutoDrawable drawable) {	
		
		GL4 gl = (GL4) GLContext.getCurrentGL();
		renderingProgramBox = Utils.createShaderProgram("HW3/vertShaderBox.glsl", "HW3/fragShaderBox.glsl");
		renderingProgramLights = Utils.createShaderProgram("HW3/vertShaderNew.glsl", "HW3/fragShaderNew.glsl");
		renderingProgramCube = Utils.createShaderProgram("Prog4_1_data/vertShaderPlainRedCube.glsl", "Prog4_1_data/fragShaderPlainRedCube.glsl");

		
		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(90.0f), aspect, 0.1f, 1000.0f);
		
		setupVertices();
		
		initCamera();
		
		skyboxTexture = Utils.loadTexture("HW3/lakeIslandSkyBox.jpg");
		brickTexture = Utils.loadTexture("HW3/brick1.jpg");
		iceTexture = Utils.loadTexture("HW3/ice.jpg");
		marsTexture = Utils.loadTexture("HW3/mars.jpg");

		pentLocX = 2.0f; pentLocY = -3.0f; pentLocZ = -3.0f;
		pyrLocX = -2.0f; pyrLocY = 3f; pyrLocZ = -3.0f;
		cubLocX = currentLightPos.x; cubLocY = currentLightPos.y; cubLocZ = currentLightPos.z;
		
	}
	

	private void initCamera() {
		
		cameraX = 0.0f; cameraY = 0.0f; cameraZ = 5f;
		sphLocX = 0.0f; sphLocY = 0.0f; sphLocZ = -1.0f;
		targetX=0.0f; targetY=0.0f; targetZ=0.0f;
		cubeLocX = 0.0f; cubeLocY = -2.0f; cubeLocZ = 0.0f;
		camVec=new Vector3f(cameraX, cameraY, cameraZ);
		targetVec=new Vector3f(targetX, targetY, targetZ);
		
	}
	
	private void installLightsGold(Matrix4f vMatrix) {	
		
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		ambientLight(vMatrix, gl);
	
		//  set the uniform light and material values in the shader
		gl.glProgramUniform4fv(renderingProgramLights, globalAmbLoc, 1, globalAmbient, 0);
		gl.glProgramUniform4fv(renderingProgramLights, ambLoc, 1, lightAmbient, 0);
		gl.glProgramUniform4fv(renderingProgramLights, diffLoc, 1, lightDiffuse, 0);
		gl.glProgramUniform4fv(renderingProgramLights, specLoc, 1, lightSpecular, 0);
		gl.glProgramUniform3fv(renderingProgramLights, posLoc, 1, lightPos, 0);
		gl.glProgramUniform4fv(renderingProgramLights, mambLoc, 1, matAmbGold, 0);
		gl.glProgramUniform4fv(renderingProgramLights, mdiffLoc, 1, matDifGold, 0);
		gl.glProgramUniform4fv(renderingProgramLights, mspecLoc, 1, matSpeGold, 0);
		gl.glProgramUniform1f(renderingProgramLights, mshiLoc, matShiGold);
	}

	private void installLightsBronce(Matrix4f vMatrix) {	
		
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		ambientLight(vMatrix, gl);
	
		//  set the uniform light and material values in the shader
		gl.glProgramUniform4fv(renderingProgramLights, globalAmbLoc, 1, globalAmbient, 0);
		gl.glProgramUniform4fv(renderingProgramLights, ambLoc, 1, lightAmbient, 0);
		gl.glProgramUniform4fv(renderingProgramLights, diffLoc, 1, lightDiffuse, 0);
		gl.glProgramUniform4fv(renderingProgramLights, specLoc, 1, lightSpecular, 0);
		gl.glProgramUniform3fv(renderingProgramLights, posLoc, 1, lightPos, 0);
		gl.glProgramUniform4fv(renderingProgramLights, mambLoc, 1, matAmbBronce, 0);
		gl.glProgramUniform4fv(renderingProgramLights, mdiffLoc, 1, matDifBronce, 0);
		gl.glProgramUniform4fv(renderingProgramLights, mspecLoc, 1, matSpeBronce, 0);
		gl.glProgramUniform1f(renderingProgramLights, mshiLoc, matShiBronce);
	}
	
	private void ambientLight(Matrix4f vMatrix, GL4 gl) {
		
		currentLightPos.mulPosition(vMatrix);
		lightPos[0]=currentLightPos.x(); lightPos[1]=currentLightPos.y(); lightPos[2]=currentLightPos.z();
				
		// get the locations of the light and material fields in the shader
		
		globalAmbLoc = gl.glGetUniformLocation(renderingProgramLights, "globalAmbient");
		ambLoc = gl.glGetUniformLocation(renderingProgramLights, "light.ambient");
		diffLoc = gl.glGetUniformLocation(renderingProgramLights, "light.diffuse");
		specLoc = gl.glGetUniformLocation(renderingProgramLights, "light.specular");
		posLoc = gl.glGetUniformLocation(renderingProgramLights, "light.position");
		mambLoc = gl.glGetUniformLocation(renderingProgramLights, "material.ambient");
		mdiffLoc = gl.glGetUniformLocation(renderingProgramLights, "material.diffuse");
		mspecLoc = gl.glGetUniformLocation(renderingProgramLights, "material.specular");
		mshiLoc = gl.glGetUniformLocation(renderingProgramLights, "material.shininess");
	}

	
	private void setupVertices() {	
		
		GL4 gl = (GL4) GLContext.getCurrentGL();
	
		// cube
		
		float[] cubeVertexPositions =
		{	-1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, 1.0f,  1.0f, -1.0f, -1.0f,  1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, 1.0f, -1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
			1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
			1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f, -1.0f,  1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f, -1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,
			-1.0f,  1.0f, -1.0f, 1.0f,  1.0f, -1.0f, 1.0f,  1.0f,  1.0f,
			1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f, -1.0f
		};
		
		float[] cubeTextureCoord =
		{	1.00f, 0.6666666f, 1.00f, 0.3333333f, 0.75f, 0.3333333f,	// back face lower right
			0.75f, 0.3333333f, 0.75f, 0.6666666f, 1.00f, 0.6666666f,	// back face upper left
			0.75f, 0.3333333f, 0.50f, 0.3333333f, 0.75f, 0.6666666f,	// right face lower right
			0.50f, 0.3333333f, 0.50f, 0.6666666f, 0.75f, 0.6666666f,	// right face upper left
			0.50f, 0.3333333f, 0.25f, 0.3333333f, 0.50f, 0.6666666f,	// front face lower right
			0.25f, 0.3333333f, 0.25f, 0.6666666f, 0.50f, 0.6666666f,	// front face upper left
			0.25f, 0.3333333f, 0.00f, 0.3333333f, 0.25f, 0.6666666f,	// left face lower right
			0.00f, 0.3333333f, 0.00f, 0.6666666f, 0.25f, 0.6666666f,	// left face upper left
			0.25f, 0.3333333f, 0.50f, 0.3333333f, 0.50f, 0.0000000f,	// bottom face upper right
			0.50f, 0.0000000f, 0.25f, 0.0000000f, 0.25f, 0.3333333f,	// bottom face lower left
			0.25f, 1.0000000f, 0.50f, 1.0000000f, 0.50f, 0.6666666f,	// top face upper right
			0.50f, 0.6666666f, 0.25f, 0.6666666f, 0.25f, 1.0000000f		// top face lower left
		};
		
		//Pirámide
		
		float[] pyramidPositions =
			{	-1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,    //front
				1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,    //right
				1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,  //back
				-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,  //left
				-1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, //LF
				1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f  //RR
			};
		
		float[] pyrTextureCoordinates =
			{
				0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
				-5.0f,-5.0f,5.0f,-5.0f,2.5f,5.0f,
				0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
				0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
				0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
				0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
				1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f
			};
		
		//Pentágono
		Pentagon myPrism = new Pentagon(1, 1f);
		
		float[] prismVectors = myPrism.getVertices();
		float[] prismTextCoords = myPrism.getTextureCoordinates();
				
		// torus
	
		myTorus = new Torus(0.5f, 0.2f, 48);
		numTorusVertices = myTorus.getNumVertices();
		numTorusIndices = myTorus.getNumIndices();
	
		Vector3f[] vertices = myTorus.getVertices();
		Vector2f[] texCoords = myTorus.getTexCoords();
		Vector3f[] normals = myTorus.getNormals();
		int[] indices = myTorus.getIndices();
		
		float[] pvalues = new float[vertices.length*3];
		float[] tvalues = new float[texCoords.length*2];
		float[] nvalues = new float[normals.length*3];


		for (int i=0; i<numTorusVertices; i++)	{	
			pvalues[i*3]   = (float) vertices[i].x();
			pvalues[i*3+1] = (float) vertices[i].y();
			pvalues[i*3+2] = (float) vertices[i].z();
			tvalues[i*2]   = (float) texCoords[i].x();
			tvalues[i*2+1] = (float) texCoords[i].y();
			nvalues[i*3]   = (float) normals[i].x();
			nvalues[i*3+1] = (float) normals[i].y();
			nvalues[i*3+2] = (float) normals[i].z();
		}
		
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(10, vbo, 0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer cvertBuf = Buffers.newDirectFloatBuffer(cubeVertexPositions);
		gl.glBufferData(GL_ARRAY_BUFFER, cvertBuf.limit()*4, cvertBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer ctexBuf = Buffers.newDirectFloatBuffer(cubeTextureCoord);
		gl.glBufferData(GL_ARRAY_BUFFER, ctexBuf.limit()*4, ctexBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		FloatBuffer torBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, torBuf.limit()*4, torBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		FloatBuffer torTexBuf = Buffers.newDirectFloatBuffer(tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, torTexBuf.limit()*4, torTexBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[4]);
		IntBuffer idxBuf = Buffers.newDirectIntBuffer(indices);
		gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, idxBuf.limit()*4, idxBuf, GL_STATIC_DRAW);
	
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		FloatBuffer pyrBuf = Buffers.newDirectFloatBuffer(pyramidPositions);
		gl.glBufferData(GL_ARRAY_BUFFER, pyrBuf.limit()*4, pyrBuf, GL_STATIC_DRAW);
	
		//Pentágono
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		FloatBuffer vertBufPrism = Buffers.newDirectFloatBuffer(prismVectors);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBufPrism.limit()*4, vertBufPrism, GL_STATIC_DRAW);
		
		//bind texture coords
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		FloatBuffer texBufPrism = Buffers.newDirectFloatBuffer(prismTextCoords);
		gl.glBufferData(GL_ARRAY_BUFFER, texBufPrism.limit()*4, texBufPrism, GL_STATIC_DRAW);
		
		//nvalues
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		FloatBuffer norBuf = Buffers.newDirectFloatBuffer(nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit()*4, norBuf, GL_STATIC_DRAW);
		
		//textura pirámide		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
		FloatBuffer textPyr = Buffers.newDirectFloatBuffer(pyrTextureCoordinates);
		gl.glBufferData(GL_ARRAY_BUFFER, textPyr.limit()*4, textPyr, GL_STATIC_DRAW);
	
	}

	public static void main(String[] args) { new HW3_FINAL(); }
	public void dispose(GLAutoDrawable drawable) {}
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{	aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(90.0f), aspect, 0.1f, 1000.0f);
	}
	
	//CAMARA STUFF
	

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		//Calculate camera forward, left and up vector
		Vector3f f=new Vector3f();
		camVec.sub(targetVec,f);
		f.normalize();
		Vector3f r=new Vector3f();
		f.negate(r);
		r.cross(0, 1, 0);
		Vector3f u=new Vector3f();
		f.cross(r,u);
		//System.out.println(e);
		
		if (e.getKeyChar()=='w'){//Move camera forward by Ddist
			camVec.sub(f.mul(Ddist));
			myCanvas.display();
		}		
		if (e.getKeyChar()=='s'){//Move camera backwards by Ddist
			camVec.add(f.mul(Ddist));
			//System.out.println(e.getKeyChar()+""+camVec);
			myCanvas.display();
		}		
		if (e.getKeyChar()=='d'){//Right
			camVec.add(r.mul(Ddist));
			myCanvas.display();
		}		
		if (e.getKeyChar()=='a'){//Left
			camVec.sub(r.mul(Ddist));
			myCanvas.display();
		}		
		if (e.getKeyCode()==38){//Up
			camVec.add(u.mul(Ddist));
			myCanvas.display();
		}		
		if (e.getKeyCode()==40){//Down
			camVec.sub(u.mul(Ddist));
			myCanvas.display();
		}	
		
		if (e.getKeyChar()=='o') { //apagar y encender luz
			
			if(apagarLuz) {
				apagarLuz=false;
			} else if(!apagarLuz) {
				apagarLuz=true;
			}
			
		}	
		
		
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}