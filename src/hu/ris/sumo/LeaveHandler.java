package hu.ris.sumo;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class LeaveHandler implements Listener {

	static Main plugin;

	public LeaveHandler(Main main) {
		plugin = main;
	}
	
	@EventHandler
	public void onDisconnect(PlayerQuitEvent p) {
		if(!plugin.inArenaPlayers.contains(p.getPlayer())) {
			return;
		}
		plugin.lose(p.getPlayer());
	}
	
	@EventHandler
	public void onWorldChange(PlayerTeleportEvent e) {
		if(!plugin.ingame) {
			return;
		}
		
		if(!plugin.inArenaPlayers.contains(e.getPlayer())) {
			return;
		}

		if(!plugin.location_fs.getConfig("locations.yml").getLocation("arena1").getWorld().equals(e.getTo().getWorld())) {
			plugin.lose(e.getPlayer());
		}
		
	} 
	
}
