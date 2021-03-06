package tw.mics.spigot.plugin.nomoreesp;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public enum Config {
    DEBUG("debug", false, "is plugin show debug message?"),
    HIDE_ENTITY_ENABLE("hide-entity.enable", true, ""),
    HIDE_ENTITY_ENABLE_WORLDS("hide-entity.enable-worlds", Arrays.asList("world","world_nether","world_the_end"), ""),
    HIDE_ENTITY_HIDE_LIST("hide-entity.hide-list", Arrays.asList("PLAYER","VILLAGER"), ""),
    HIDE_ENTITY_HIDE_RANGE("hide-entity.hide-range", 48, ""),
    HIDE_ENTITY_BYPASS_OP("hide-entity.bypass-op", true, ""),
    HIDE_ENTITY_BYPASS_SPECTATOR("hide-entity.bypass-spectator", true, ""),
    HIDE_ENTITY_BYPASS_BY_PERMISSION("hide-entity.bypass-by-permission", true, ""),

    FAKE_HEALTH_ENABLE("fake-health.enable", true, ""),
    FAKE_HEALTH_ENABLE_WORLDS("fake-health.enable-worlds", Arrays.asList("world","world_nether","world_the_end"), ""),
    FAKE_HEALTH_DISABLE_LIST("fake-health.disable-list", Arrays.asList("HORSE", "ZOMBIE_HORSE", 
            "SKELETON_HORSE", "DONKEY", "LLAMA", "MULE", "PIG", "WOLF"), ""),
    FAKE_HEALTH_VALUE("fake-health.value", 20, "");

    private final Object value;
    private final String path;
    private final String description;
    private static YamlConfiguration cfg;
    private static final File f = new File(getPlugin().getDataFolder(), "config.yml");

    private Config(String path, Object val, String description) {
        this.path = path;
        this.value = val;
        this.description = description;
    }

    public String getPath() {
        return path;
    }

    public String getDescription() {
        return description;
    }

    public Object getDefaultValue() {
        return value;
    }

    public boolean getBoolean() {
        return cfg.getBoolean(path);
    }

    public int getInt() {
        return cfg.getInt(path);
    }

    public double getDouble() {
        return cfg.getDouble(path);
    }

    public String getString() {
        return replaceColors(cfg.getString(path));
    }

    public List<String> getStringList() {
        return cfg.getStringList(path);
    }
    
    public void removeStringList(String str) {
        List<String> str_list = getStringList();
        Iterator<String> str_itr = str_list.iterator();
        while(str_itr.hasNext()){
            if(str_itr.next().equals(str)){
                str_itr.remove();
            }
        }
        cfg.set(path, str_list);
        save();
    }

    public static void load() {
        boolean save_flag = false;
        
        NoMoreESP.getInstance().getDataFolder().mkdirs();
        String header = "";
        cfg = YamlConfiguration.loadConfiguration(f);

        for (Config c : values()) {
            if(c.getDescription().toLowerCase().equals("removed")){
                if(cfg.contains(c.getPath())){
                    save_flag = true;
                    cfg.set(c.getPath(), null);
                }
                continue;
            }
            if(!c.getDescription().isEmpty()){
                header += c.getPath() + ": " + c.getDescription() + System.lineSeparator();
            }
            if (!cfg.contains(c.getPath())) {
                save_flag = true;
                c.set(c.getDefaultValue(), false);
            }
        }
        cfg.options().header(header);
        
        if(save_flag){
            save();
            cfg = YamlConfiguration.loadConfiguration(f);
        }
    }

    public static void save() {
        try {
            cfg.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void set(Object value, boolean save) {
        cfg.set(path, value);
        if (save) {
            save();
        }
    }

    private static JavaPlugin getPlugin() {
        return NoMoreESP.getInstance();
    }

    private static String replaceColors(String message) {
        return message.replaceAll("&((?i)[0-9a-fk-or])", "��$1");
    }
}
