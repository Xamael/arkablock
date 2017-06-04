package org.maox.arkablock.entities;

import java.io.IOException;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;

import static org.lwjgl.opengl.GL11.*;

import org.maox.games.entities.AbstractEntity2D;
import org.maox.games.entities.Entity;
import org.maox.games.entities.EntityManager;
import org.maox.graphics.models.ObjLoader;
import org.maox.graphics.models.ObjModel;
import org.maox.graphics.textures.Texture;
import org.maox.graphics.textures.TextureLoader;

/**
 * Entidad que representa la nave del jugador.
 * Esta entidad es responsable de mostrar el modelo
 * 
 * @author Alex
 */
public class Ship extends AbstractEntity2D {
	/** Textura a aplicar al modelo */
	private Texture texture;
	/** El modelo 3d de la nave */
	private ObjModel model;
	/** Factor por el que se va a escalar el modelo (por si es muy grande o pequeño) */
	private float fScale = 15f;
	
	/**
	 * Crear una nueva entidad de jugador
	 */
	public Ship() throws IOException {
		TextureLoader loaderTex = TextureLoader.getInstance();
		ObjLoader loaderObj = ObjLoader.getInstance();

		// Se carga la textura de la nave y el modelo de la nave
		texture = loaderTex.getTexture("img/ship.jpg");
		
		// Carga del modelo 3D
		model = loaderObj.getModel("obj/vaus.obj");
		
		// Posición y velocidad inicial
		initPos();
	}
	
	/**
	 * Inicialización de la posición inicial
	 */
	public void initPos() {
		positionX = 400;
		positionY = 550;
		velocityX = 0;
		velocityY = 0;
	}

	/**
	 * @see org.maox.graphics.Entity#update()
	 */
	public void update(EntityManager manager, int delta) {
		// Si el jugador está pulsado la derecha o izquierda
		// hay que mover la nave. La cantidad de movmiento
		// está escalado por el delta 
		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
			velocityX -= (delta / 0.2f);
		}
		else if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
			velocityX += (delta / 0.2f);
		}
		// Si no hay pulsado nada se para la nave
		else {
			if (Math.abs(velocityX) > 1)
				velocityX = 0;
			else
				velocityX /= 2;
		}
		
		// Velocidad máxima que se permitira
		if (velocityX > 900) velocityX = 900;
		if (velocityX < -900) velocityX = -900;
		
		super.update(manager, delta);
	}
	
	/**
	 * @see org.maox.graphics.Entity#render()
	 */
	public void render() {
		// Establecer iluminación para el modelo
		glEnable(GL_LIGHTING);
		
		// Almacenar la matrix actual para poder modificar sin afectar nada
		// de otro sitio
		glPushMatrix();

		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();

		// Posicionar el modelo a partir de la posición acutal
		glTranslatef(positionX, positionY, positionZ);
				
		// Escalar el modelo porque es demasiado grande
		glScalef(fScale, fScale, fScale);
		
		// Definir el material del que está hecho el modelo para
		// saber como refleja la Luz
		FloatBuffer material = BufferUtils.createFloatBuffer(4);
		material.put(1).put(1).put(1).put(1); 
		material.flip();
		
		// Material de las caras frontales y posteriores
		glMaterial(GL_FRONT, GL_DIFFUSE, material);
		glMaterial(GL_BACK, GL_DIFFUSE, material);
		
		// Enlazar la textura al modelo y renderizar el modelo
		glDisable(GL_TEXTURE_2D);

		if (model.hasTexture())
		{
			// Le indico a OpenGL que voy a usar texturas para pintar los
			// objetos y que van a ser de 2 dimensiones (un dibujo normal).
			glEnable(GL_TEXTURE_2D);
			// Modo de mezcla de la textura con el color del material (MODULATE: Mezcla)
			glTexEnvf(GL_TEXTURE_2D, GL_TEXTURE_ENV_MODE, GL_MODULATE);

			texture.bind();
		}

		model.render();
		
		// Restaurar la matriz a como estaba al entrar en el metodo
		glPopMatrix();
	}

	/**
	 * @see org.maox.graphics.Entity#getSize()
	 */
	public float getSize() {
		// Radio de colisión
		return (model.getSizeX()>model.getSizeY()?model.getSizeX():model.getSizeY())*fScale;
	}

	/**
	 * @see org.maox.graphics.Entity#collide()
	 */
	public void collide(EntityManager manager, Entity other) {
		// En caso de colisión con la bola se cambia de dirección Y la bola
		
		// Aquí se puede jugar con la velocidad de la nave y con que lado le de
		// para cambiar de dirección la bola
		if (other instanceof Ball) {
			// Cambio de sentido por el rebote
			((Ball)other).changeY(-1f);

			// Si es necesario cambio de dirección por el lado que ha golpeado
			if (getX() < other.getX() && other.getVelocityX() < 0)
				((Ball)other).changeX(-1f);

			else if (getX() > other.getX() && other.getVelocityX() > 0)
				((Ball)other).changeX(-1f);
			}
	}

	/**
	 * @see org.maox.graphics.Entity#getSizeX()
	 */
	@Override
	public float getSizeX() {
		return model.getSizeX()*fScale;
	}

	/**
	 * @see org.maox.graphics.Entity#getSizeY()
	 */
	@Override
	public float getSizeY() {
		return model.getSizeY()*fScale;
	}
	
	/**
	 * @see org.maox.graphics.Entity#collides()
	 */
	@Override
	public boolean collides(Entity other) {
		// La colisión de la nave con la bola no será por medio del raido de colisión
		// Ya que al ser un objeto muy alargado no tiene sentido
		// Además sólo puede colisionar con la bola
		
		if (!(other instanceof Ball))
			return false;

		// Distancia entre coordenadas
		float dx = Math.abs(getX() - other.getX());
		float dy = Math.abs(getY() - other.getY());
		
		// Alcances
		float rangeX = Math.abs(other.getSizeX()/2 + getSizeX()/2);
		float rangeY = Math.abs(other.getSizeY()/2 + getSizeY()/2);
		
		// Colisión
		if (dx < rangeX && dy < rangeY)
			return true;

		return false;
	}
}
