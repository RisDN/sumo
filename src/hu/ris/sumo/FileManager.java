package hu.ris.sumo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class FileManager {

    private Main plugin;
    private FileConfiguration file = null;
    private File configFile = null;

    public FileManager(Main plugin, String filename) {
        this.plugin = plugin;
        saveDefaultConfig(filename);
    }

    public void reloadConfig(String filename) {
        if (this.configFile == null)
            this.configFile = new File(this.plugin.getDataFolder(), filename);
        this.file = YamlConfiguration.loadConfiguration(this.configFile);

        InputStream defaultStream = this.plugin.getResource(filename);
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            this.file.setDefaults(defaultConfig);
        }
    }

    public FileConfiguration getConfig(String filename) {
        if (this.file == null)
            reloadConfig(filename);
        return this.file;
    }

    public void saveConfig(String filename) {
    	plugin.getLogger().info("cica");
        if (this.file == null || this.configFile == null)
            return;
        try {
            this.getConfig(filename).save(this.configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Nem mentheto config ide: " + this.configFile, e);
        }
    }

    public void saveDefaultConfig(String filename) {
        if (this.configFile == null)
            this.configFile = new File(this.plugin.getDataFolder(), filename);

        if (!this.configFile.exists()) {
            this.plugin.saveResource(filename, false);
        }
    }
}