package org.maox.arkablock.entities;

import java.io.IOException;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.glu.Sphere;

import static org.lwjgl.opengl.GL11.*;

import org.maox.games.entities.AbstractEntity2D;
import org.maox.games.entities.Entity;
import org.maox.games.entities.EntityManager;
import org.maox.graphics.textures.Texture;
import org.maox.graphics.textures.TextureLoader;

/**
 * Entidad que representa la bola manejada por del jugador.
 * Esta entidad es responsable de mostrar el modelo
 * 
 * @author Alex
 */
public class Ball extends AbstractEntity2D {
	/** Textura a aplicar al modelo */
	private Texture texture = null;
	/** Identificador del Display List que indentifica este modelo */
	private int listID;
	/** Tamaño de la bola */
	private int iRadius = 7;
	/** Velocidad máxima de la bola*/
	private float fVelMax = 1500;
	/** Velocidad inicial de la bola*/
	private float fVelIni = 200;
	
	
	/**
	 * Crear una nueva entidad de jugador
	 */
	public Ball() throws IOException {
		TextureLoader loaderTex = TextureLoader.getInstance();

		// Se carga la textura de la nave y el modelo de la nave
		texture = loaderTex.getTexture("img/fire.jpg");
		
		initModel();
		initPos();
	
	}
	
	/**
	 * Inicialización de la posición inicial
	 */
	public void initPos() {
		// Posición y velocidad inicial
		positionX = 400;
		positionY = 540;
		velocityX = 0;
		velocityY = 0;		
	}

	/**
	 * Creación del modelo de la bola
	 */
	private void initModel() {
		
		Sphere sphere = new Sphere();

		//Se genera el identificador del Display List
		listID = glGenLists(1);
		
		// Comienzo de las instrucciones del modelo
		glNewList(listID, GL_COMPILE);
		
		sphere.draw(iRadius, 10, 10);
		
		glEndList();
	}

	/**
	 * @see org.maox.graphics.Entity#update()
	 */
	public void update(EntityManager manager, int delta) {

		// Si la bola está parada su posición será la de la nave
		if (velocityY == 0) {
			velocityX = manager.getEntity("vaus").getVelocityX();
		}
		// Arranque de la partida dando al espacio
		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE) && velocityX == 0) {
			// Velocidad inicial
			velocityX += fVelIni;
			velocityY -= fVelIni;
		}
		
		// Velocidad máxima que se permitira
		if (velocityX > fVelMax) velocityX = fVelMax;
		if (velocityX < -fVelMax) velocityX = -fVelMax;
		
		float fVelocityXAct = velocityX;
		float fVelocityYAct = velocityY;
		
		// Si ha sobrepasado el limite del area de dibujo ya no tendrá velocidad
		super.update(manager, delta);
		
		// Cambio de dirección
		if (velocityX==0)
			velocityX = - fVelocityXAct;
		
		if (velocityY==0)
			velocityY = - fVelocityYAct;
	}
	
	/**
	 * @see org.maox.graphics.Entity#render()
	 */
	public void render() {
		// TODO
		// Establecer iluminación para el modelo
		glEnable(GL_LIGHTING);
		
		// Almacenar la matrix actual para poder modificar sin afectar nada
		// de otro sitio
		glPushMatrix();

		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();

		// Posicionar el modelo a partir de la posición acutal
		glTranslatef(positionX, positionY, positionZ);
				
		// Definir el material del que está hecho el modelo para
		// saber como refleja la Luz
		FloatBuffer material = BufferUtils.createFloatBuffer(4);
		material.put(1).put(1).put(1).put(1); 
		material.flip();
		
		// Material de las caras frontales y posteriores
		glMaterial(GL_FRONT, GL_DIFFUSE, material);
		glMaterial(GL_BACK, GL_DIFFUSE, material);
		// El brillo especular pequeño (de 0 a 255).
		glMaterialf(GL_FRONT, GL_SHININESS, 20);
		
		// Enlazar la textura al modelo y renderizar el modelo
		glDisable(GL_TEXTURE_2D);

		if (texture != null)
		{
			// Le indico a OpenGL que voy a usar texturas para pintar los
			// objetos y que van a ser de 2 dimensiones (un dibujo normal).
			glEnable(GL_TEXTURE_2D);
			// Modo de mezcla de la textura con el color del material (MODULATE: Mezcla)
			glTexEnvf(GL_TEXTURE_2D, GL_TEXTURE_ENV_MODE, GL_MODULATE);
			texture.bind();
		}

		glCallList(listID);
		
		// Restaurar la matriz a como estaba al entrar en el metodo
		glPopMatrix();
	}

	/**
	 * @see org.maox.graphics.Entity#getSize()
	 */
	public float getSize() {
		// the size of the player
		return iRadius*2;
	}

	/**
	 * @see org.maox.graphics.Entity#getSizeX()
	 */
	@Override
	public float getSizeX() {
		return getSize()*2;
	}

	/**
	 * @see org.maox.graphics.Entity#getSizeY()
	 */
	@Override
	public float getSizeY() {
		return getSize()*2;
	}	
	/**
	 * @see org.maox.graphics.Entity#collide()
	 */
	public void collide(EntityManager manager, Entity other) {
		// TODO
		// if we've collide with a rock then the rock must split apart,
		// and our velocity needs to be changed to push us away from the
		// rock
		/*
		if (other instanceof Rock) {
			velocityX = (getX() - other.getX());
			velocityY = (getY() - other.getY());
			
			((Rock) other).split(manager, this);
			
			// notify the class manging the entities that the player
			// has been hit, just in case anything needs doing
			manager.playerHit();
		}
		*/
	}
	
	/**
	 * Modifica la dirección o velocidad horizontal de la bola multiplicando por el factor
	 * @param factorX 
	 */
	public void changeX(float factorX) {
		velocityX *= factorX;	
	}
	
	/**
	 * Modifica la dirección o velocidad vertical de la bola multiplicando por el factor
	 * @param factorY 
	 */
	public void changeY(float factorY) {
		velocityY *= factorY;	
	}
}
