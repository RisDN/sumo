package hu.ris.sumo;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
		
		
		if(!plugin.joinedPlayers.contains(p.getPlayer())) {
			return;
		}
		
		if(!plugin.inArenaPlayers.contains(p.getPlayer())) {
			return;
		}
		
		
		plugin.lose(p.getPlayer());
		
		if(plugin.joinedPlayers.size() < plugin.getConfig().getInt("minimum")) {
			Bukkit.getScheduler().cancelTask(plugin.startSched);
			for(Player pl : plugin.joinedPlayers) {
				pl.sendMessage(plugin.messageFormatter(plugin.getConfig().getString("nincselegendo")));
			}
		}
	}
	
	@EventHandler
	public void onWorldChange(PlayerTeleportEvent e) {
		if(!plugin.ingame) {
			return;
		}
		
		if(!plugin.inArenaPlayers.contains(e.getPlayer())) {
			return;
		}

		if(!plugin.location_fs.getConfig("locations.yml").getLocation("arena1").getWorld().getName().equals(e.getTo().getWorld().getName())) {
			plugin.lose(e.getPlayer());
		}
		
	} 
	
}
