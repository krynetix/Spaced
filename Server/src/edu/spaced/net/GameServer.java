package edu.spaced.net;

import java.io.IOException;

import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

import edu.spaced.controllers.AccessController;
import edu.spaced.simulation.Level;
import edu.spaced.simulation.Simulation;

/**
 * Singleton class that manages the server. Central point of communication for
 * all network communication.
 * 
 * @author Logan Lowell
 *
 */
public class GameServer extends Network {
	private static final class GameServerHolder {
		public static GameServer INSTANCE = new GameServer();
	}
	
	public static GameServer getInstance() {
		return GameServerHolder.INSTANCE;
	}
	
	//////////////
	
	private Server server;
	
	private final long TICKS_PER_SECOND = 20; // 20 updates/second
		
	private GameServer() {
		server = new Server();
		register(server);
		
		server.addListener(this);
	}
	
	/**
	 * Start the server up!
	 */
	public void start() {
		try {
			server.bind(TCP_PORT, UDP_PORT);
		} catch (IOException e) {
			Log.error("Unable to start server!");
			Log.error(e.getMessage());
			System.exit(1);
		}
		
		// TODO Move the simulation outta here
		// Begin a single simulation
		Simulation sim = new Simulation(Level.loadFile("simple.tmx"));
		// Add server-side controllers. They self-register to the network.
		@SuppressWarnings("unused")
		AccessController access = new AccessController(sim);

		// Start our server once we've loaded a level
		try {
			server.start();
		
			// Begin server loop
			long lastTime = System.nanoTime();
			boolean running = true;
			while(running) {
				// Ensure we run at 20 updates/sec
				long currentTime = System.nanoTime();
				running = sim.update((currentTime - lastTime) / 1000000f);
				long millisToNextUpdate = Math.max(0, (1000 / TICKS_PER_SECOND) - ((System.nanoTime() - currentTime) / 1000000));
				lastTime = currentTime;
				try {
					Thread.sleep(millisToNextUpdate);
				} catch (InterruptedException e) {
					break;
				}
			}
		} finally {
			stop();
		}
	}
	
	public void stop() {
		server.stop();
	}
	
	public void sendToAllTCP(Object object) {
		server.sendToAllTCP(object);
	}
	
	public void sendToAllUDP(Object object) {
		server.sendToAllUDP(object);
	}
	
	public void sendToAllExceptTCP(int connectionID, Object object) {
		server.sendToAllExceptTCP(connectionID, object);
	}
	
	public void sendToAllExceptUDP(int connectionID, Object object) {
		server.sendToAllExceptUDP(connectionID, object);
	}
	
	
}
