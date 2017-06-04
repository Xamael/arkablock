
package org.maox.arkablock;

import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_NICEST;
import static org.lwjgl.opengl.GL11.GL_PERSPECTIVE_CORRECTION_HINT;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_SMOOTH;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glHint;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glShadeModel;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.maox.time.Clock.getTimeMilis;

import java.util.HashMap;
import java.util.Iterator;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.maox.arkablock.states.InGameState;
//import org.lwjgl.util.glu.GLU;
import org.maox.games.Game;
import org.maox.games.GameState;
import org.maox.graphics.Graphics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Juego de rompeladrillos
 * 
 * @author Alex
 */
public class ArkaBlock implements Game {
	/* Log */
	private final Logger logger = LoggerFactory.getLogger(ArkaBlock.class);

	// Constantes de Sistema
	final public static int WIDTH = 800;
	final public static int HEIGHT = 600;
	final private int TARGET_FPS = 60; // Objetivo de FPS
	final private long OPTIMAL_TIME = 1000 / TARGET_FPS; // Tiempo optimo en ms entre Frames   
	
	/** Lista de los estado de juego registrados */
	private HashMap<String, GameState> gameStates = new HashMap<String, GameState>();
	/** Estado de juego actual activo */
	private GameState currentState;

	/**
	 * Crea la ventana de juego
	 */
	public ArkaBlock() {
		try {
			// Obtener cuantos Bits por Pixel hay actualmente en el escritorio
			int currentBpp = Display.getDisplayMode().getBitsPerPixel();
			
			// Encontrar el DisplayMode 800x600
			DisplayMode mode = Graphics.findDisplayMode(WIDTH, HEIGHT, currentBpp);
			
			// Si el modo no está disponible salir con error.
			if (mode == null) {
				logger.fatal("Modo de video "+WIDTH+"x"+HEIGHT+"x"+currentBpp+" no disponible.");
				return;
			}
			
			// Configurar y crear el display LWJGL
			Display.setTitle("Arkanoid");
			Display.setDisplayMode(mode);
			Display.setFullscreen(false);
			
			// Creación de la ventana (Vista)
			Display.create();
			
			// Inicializar los estados de juego
			init();

		} catch (Exception e) {
			e.printStackTrace();
			logger.fatal(e);
		}
	}	
	
	/**
	 * Inicializa la ventana y los recursos del juego
	 * @throws Exception 
	 */
	public void init() throws Exception {
		// Inicialización de Sonido
		initSound();
		
		// Inicialización del modo grafico
		initGraphics();
		
		// Inicialización de los estados de juego
		initStates();
	}
	
	/**
	 * Inicialización de los estados de juego
	 * Existirán 2 estados de juego:
	 *    1. El menu de juego
	 *    2. El bucle de juego
	 * @throws Exception 
	 */
	private void initStates() throws Exception {
		// TODO
		//addState(new MenuState());
		addState(new InGameState());
		
		//Inicializar los estados de juego para que carguen los recursos que necesiten
		Iterator<GameState> states = gameStates.values().iterator();
			
		// Loop con los estados que se han registrado
		while (states.hasNext()) {
			GameState state = states.next();
			state.init(this);
		}
	}
	
	/**
	 * Añade un estado al juego. La clave será el nombre del estado
	 * 
	 * @param state Estado a añadir
	 * @see GameState.getName()
	 */
	public void addState(GameState state) {
		if (currentState == null) {
			currentState = state;
		}
		
		gameStates.put(state.getName(), state);
	}

	/**
	 * Inicialización de modo grafico de OpenGL
	 */
	private void initGraphics() {
    	// Solo renderizar las caras frontales de los poligonos (muy importante en solidos)
		glEnable(GL_CULL_FACE);
		
		// Funcion de comparacion del buffer de profundidad.
		glDepthFunc(GL_LEQUAL);
		
		// Renderizado suave.
		glShadeModel(GL_SMOOTH); 
		
		// Establecer el color de borrado cuando se use glClear
		glClearColor(0, 0, 0, 1.0f);
		
		// Calidad buena en la correccion de perpectiva
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
		
		// Inicialización de las matrices de proyeccion y transformación e
		// establecer la perspectiva de proyeccion
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();

		// Perspectiva de visualización
		// Establecer la parte de la ventana que se usará para en renderizado
		// En este caso toda la pantalla
		glViewport(0, 0, WIDTH, HEIGHT);
		
		// Perpectiva
		//GLU.gluPerspective(45.0f, ((float)WIDTH) / ((float)HEIGHT), 0.1f, 100.0f);
		//glEnable(GL_DEPTH_TEST); // Test de profundidad
		//glClearDepth(1);

		// Ortografica
		glOrtho(0, WIDTH, HEIGHT, 0, -100, 100);
		glDisable(GL_DEPTH_TEST);
		
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
	}

	/**
	 * Inicialización del Sonido
	 */
	private void initSound() {
		// TODO Inicialización sonido
		//SoundLoader.get().init();
	}
	
	/**
	 * Arranque del juego
	 * @throws Exception 
     */
	public void startGame() throws Exception {
		// Entrar en el bucle de juego
		gameLoop();
	}
	
	/**
	 * Bucle de juego principal. Encargado de la actualización y renderizado
	 * de los estados
	 * @throws Exception 
	 */
	public void gameLoop() throws Exception {

		int fps = 0;
		long lastFpsTime = 0;
		boolean gameRunning = true;
		long lastLoop = getTimeMilis();
		
		currentState.enter();
		
		// Mientras el juego este corriendo se acutalizan y renderizan los estados
		while (gameRunning) {
			
			// Se calcula cuanto ha pasado desde el último bucle para poder actualizar
			// el renderizado y la lógica esa cantidad de tiempo determinada.
			int delta = (int) (getTimeMilis() - lastLoop);
			lastLoop = getTimeMilis();
			
			// Contador de los FPS
			lastFpsTime += delta;
			fps++;
			
			// Actualizar el FPS si ha transcurrido un segundo
			if (lastFpsTime >= 1000) {
				Display.setTitle("Arkanoid (FPS: "+fps+")");
				lastFpsTime = 0;
				fps = 0;
			}
			
			// Se actualiza la lógica dependiendo del tiempo transcurrido
			// Se va a realizar en ciclos de 10 ms
			int step = delta / 10;

			for (int i=0;i<step;i++) {
				currentState.update(10);
			}

			int remainder = delta % 10;

			if (remainder != 0) {
				currentState.update(remainder);
			}
			
			// Se renderiza el estado activo
			currentState.render(delta);
			
			// Se le indica al LWJGL que actualize la vista
			// El resfresco se realiza por Doble Buffering
			// Como efecto secundario se comprobará el teclado / ratón y controladores
			Display.update();
			
			// Si el usuario ha solicitado el cierre de la ventana
			if (Display.isCloseRequested()) {
				gameRunning = false;
				System.exit(0);
			}
			
			// Dormir el Thread para optimizar la CPU
			// Tiempo que se ha tardado en actualizar la logica y renderizar todo
			long timeBetweenUpdate = getTimeMilis() - lastLoop;
			Thread.yield();
			if (timeBetweenUpdate < OPTIMAL_TIME)
			{
				try
				{
					Thread.sleep(OPTIMAL_TIME - timeBetweenUpdate);
				}
				catch (InterruptedException e){};
			}
		} // bucle juego
	}
	
	/**
	 * Cambia el estado actual y lo actualiza. Si el nombre no coincide
	 * con ningún estado registrado no hace nada.
	 * 
	 * @param name Nombre del estado a cambiar
	 * @throws Exception 
	 */
	public void changeToState(String name) throws Exception {
		// Se obtiene el estado desde el nombre dado
		GameState newState = gameStates.get(name);

		if (newState == null) {
			return;
		}
		
		// Se cambia el estado
		currentState.leave();
		currentState = newState;
		currentState.enter();
	}

	/**
	 * Punto de entrada al programa. No necesita parametros de entrada
	 * 
	 * @param argv Parametros de la linea de comandos (no necesarios)
	 */
	public static void main(String argv[]) {
		ArkaBlock game = new ArkaBlock();
		try {
			game.startGame();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
