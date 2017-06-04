package org.maox.arkablock.states;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.maox.arkablock.ArkaBlock;
import org.maox.arkablock.entities.Ball;
import org.maox.arkablock.entities.Ship;
import org.maox.games.Game;
import org.maox.games.GameState;
import org.maox.games.entities.Entity;
import org.maox.games.entities.EntityManager;
import org.maox.graphics.textures.Texture;
import org.maox.graphics.textures.TextureLoader;

/**
 * Estado encargado de la renderización del mundo de juego y de la mecanica del juego
 * 
 * @author Alex
 */
public class InGameState implements GameState, EntityManager {

	/** Puntero al juego princpial */
	Game game = null;

	/** El nombre indentificativo de este estado */
	public static final String NAME = "inGame";
	
	/** Texturas usadas para el renderizado de objetos  */
	private Texture	texBackground;

	/** Entidades del juego */
	private ArrayList<Entity> entities = new ArrayList<Entity>();
	/** Entidad que representa la nave del jugador */
	private Ship player;
	/** Entidad que representa la bola del jugados */
	private Ball ball;
	
	/** Variables de juego (vidas, puntuación, si se ha terminado) */
	/** Puntuación actual */
	private int score;
	/** El número de vidas */
	private int life = 3;
	/** True si el juego a terminado */
	private boolean gameOver;
	/** Tiempo desde que sale el mensaje de Game Over hasta que se reinica al menu */
	private int gameOverTimeout;
	/** Nivel del juego */
	private int level;
	
	/**
	 * @see org.maox.games.GameState#getName
	 */
	@Override
	public String getName() {
		return NAME;
	}

	/**
	 * @see org.maox.games.GameState#init
	 */
	@Override
	public void init(Game game) throws Exception {
		// Asociación del puntero de juego
		this.game = game;

		// Carga de las texturas utilizadas
		TextureLoader loader = TextureLoader.getInstance();
		texBackground = loader.getTexture("img/bg_mig.jpg");

		// Definición de la luz que ilumine la escena
		initLight();
		
		// TODO Inicialización del sonido
	}
	
	/**
	 * Inicialización de la Luz que ilumina la escena
	 * @throws Exception
	 */
	public void initLight() throws Exception {

		// Los Buffer tenrán el componente RGB de la luz (4 paramatro es 1 para normalizar)
		
		// Luz ambiental general
		FloatBuffer bufferModel = BufferUtils.createFloatBuffer(4);
		bufferModel.put(1f).put(1f).put(1f).put(1f);
		bufferModel.flip();
		
		glLightModel(GL_LIGHT_MODEL_AMBIENT, bufferModel);
		
		// Modo de cálculo del brillo de la luz especular
		glLightModeli(GL_LIGHT_MODEL_LOCAL_VIEWER, GL_TRUE);
		
		// Invertir normales si el poligono se da la vuelta
		glLightModeli(GL_LIGHT_MODEL_TWO_SIDE, GL_TRUE);
		

		// Caracteristicas de la Luz 0 (Se usará como Luz general)
		// Luz ambiental (Procede de todas partes)
		FloatBuffer bufferAmb = BufferUtils.createFloatBuffer(4);
		bufferAmb.put(0.2f).put(0.2f).put(0.2f).put(1); 
		bufferAmb.flip();
		
		glLight(GL_LIGHT0, GL_AMBIENT, bufferAmb);
		
		// Luz Difusa (Procede de la fuente y rebota en todas direcciones)
		FloatBuffer bufferDif = BufferUtils.createFloatBuffer(4);
		bufferDif.put(0.5f).put(0.5f).put(0.5f).put(1); 
		bufferDif.flip();
		
		glLight(GL_LIGHT0, GL_DIFFUSE, bufferDif);
		
		// Luz especular (Procede de la fuente y rebota en una dirección (brillo))
		FloatBuffer bufferEsp = BufferUtils.createFloatBuffer(4);
		bufferEsp.put(0.8f).put(0.8f).put(0.8f).put(1); 
		bufferEsp.flip();
		
		glLight(GL_LIGHT0, GL_SPECULAR, bufferEsp);

		// Posición de la Luz
		FloatBuffer  bufferPos = BufferUtils.createFloatBuffer(4);
		bufferPos.put(0).put(0).put(-5).put(0);
		bufferPos.flip();
		
		glLight(GL_LIGHT0, GL_POSITION, bufferPos);
		
		// Activar la Luz
		glEnable(GL_LIGHTING);
		
		// Activar la luz 0
		glEnable(GL_LIGHT0);
	}

	/**
	 * @see org.maox.games.GameState#render
	 */
	@Override
	public void render(int delta) {
		// TODO Auto-generated method stub
		
		// Limpieza del buffer de pantalla y del bit de profundidad
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		// Inicializar la matriz de transformación
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		
		// Se dibuja el fondo
		drawBackground();

		// Bucle por todas las entidades para renderizarlas
		for (int i=0;i<entities.size();i++) {
			Entity entity = (Entity) entities.get(i);
			entity.render();
		}

		glFlush();
	}

	private void drawBackground() {
		// Establecer iluminación
		glDisable(GL_LIGHTING);
		
		glEnable(GL_TEXTURE_2D);
		glTexEnvf(GL_TEXTURE_2D, GL_TEXTURE_ENV_MODE, GL_DECAL);

		texBackground.bind();
		
		glBegin(GL11.GL_QUADS);
			glTexCoord2f(0,1);
			glVertex2i(0,0);
			glTexCoord2f(0,0);
			glVertex2i(0,ArkaBlock.HEIGHT);
			glTexCoord2f(1,0);
			glVertex2i(ArkaBlock.WIDTH, ArkaBlock.HEIGHT);
			glTexCoord2f(1,1);
			glVertex2i(ArkaBlock.WIDTH, 0);
		glEnd();		
	}

	/**
	 * @see org.maox.games.GameState#update
	 */
	@Override
	public void update(int delta) throws Exception {
		// TODO Auto-generated method stub

		// Si se ha finalizado el juego, se espera unos instantes (gemOverTimeout)
		// antes de volver al menu
		if (gameOver) {
			gameOverTimeout -= delta;
			if (gameOverTimeout < 0) {
				game.changeToState(MenuState.NAME);
			}
		}
		
		// Actualizar todas las entidades actuales del Estado
		for (int i=0;i<entities.size();i++) {
			Entity entity = (Entity) entities.get(i);

			// Actualización de la logica
			entity.update(this, delta);
			
			// Comprobación de colisiones
			for (int j=i+1;j<entities.size();j++) {
				Entity other = (Entity) entities.get(j);
					
				if (entity.collides(other)) {
					entity.collide(this, other);
					other.collide(this, entity);
				}
			}
			
			// Si la posición vertical de la bola es inferior a la de la nave
			// Se pierde una vida
			if (ball.getY() + ball.getSizeY() < player.getY() + player.getSizeY()) {
				ballMissed();
			}
			
			// COmprobación si quedan bloques por romper
			/*
			if (entity instanceof Block) {
				blockCount++;
			}
			*/
		}

	}

	/**
	 * Método llamado cuando se pierda una bola
	 */
	private void ballMissed() {
		// TODO Auto-generated method stub
		//life --;
		//ball.initPos();
		//player.initPos();
	}

	/**
	 * @see org.maox.games.GameState#enter
	 */
	@Override
	public void enter() throws Exception {
		// TODO Auto-generated method stub
		
		// Carga de las entidades iniciales
		entities.clear();
		
		// Nave del jugador
		player = new Ship();
		entities.add(player);
		
		// Bola
		ball = new Ball();
		entities.add(ball);
		
		// Variables de juego
		life = 3;
		score = 0;
		level = 1;
		gameOver = false;
	}

	/**
	 * @see org.maox.games.GameState#leave
	 */
	@Override
	public void leave() {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeEntity(Entity entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addEntity(Entity entity) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see org.maox.graphics.Entity#render()
	 */
	@Override
	public Entity getEntity(String key) {
		// TODO Auto-generated method stub
		if (key.equals("vaus"))
			return player;
		
		return null;
	}

}
