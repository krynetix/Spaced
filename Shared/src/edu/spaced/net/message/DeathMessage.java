package edu.spaced.net.message;

import java.util.List;

import com.esotericsoftware.kryonet.Connection;

import edu.spaced.net.listener.DeathListener;

public class DeathMessage extends NetMessage {
	public long timestamp;
	public int playerId;

	@Override
	public void publish(Connection connection, List<Object> listeners) {
		// Publish to all listeners
		for (Object listener : listeners) {
			((DeathListener)listener).playerDied(playerId);
		}
	}

}
