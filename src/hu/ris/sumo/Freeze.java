package hu.ris.sumo;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class Freeze implements Listener {

	static Main plugin; 
	public Freeze(Main main) {
		plugin = main;
	}
	
	private ArrayList<Player> freezedPlayers = new ArrayList<>();
	public void freeze(Player p, Boolean state) {
		if(state) {
			freezedPlayers.add(p);
		} else if(freezedPlayers.contains(p)) {
			freezedPlayers.remove(p);
		}
	}

	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		if(freezedPlayers.contains(e.getPlayer())) {
			e.setCancelled(true);
		}
	}
	
}
