package hu.ris.sumo;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveEventHandler implements Listener {

	static Main plugin;
	public PlayerMoveEventHandler(Main main) {
		plugin = main;
	}
	
	@EventHandler
	public void checkHeight(PlayerMoveEvent e) {
		Player player = e.getPlayer();
		if(!plugin.getInArenaPlayers().contains(player)) {
			return;
		}
		if(e.getTo().getY() == e.getFrom().getY()) {
			return;
		}
		if(player.getLocation().getY() <= plugin.location_fs.getConfig("locations.yml").getLocation("arena1").getY()-2) {
			plugin.lose(player);
		}
	}

}
