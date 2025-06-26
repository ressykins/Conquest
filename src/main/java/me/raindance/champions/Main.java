package me.raindance.champions;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.podcrash.api.annotations.GamePlugin;
import com.podcrash.api.damage.HitDetectionInjector;
import com.podcrash.api.disguise.Disguiser;
import com.podcrash.api.game.GameManager;
import com.podcrash.api.plugin.IGamePlugin;
import com.podcrash.api.time.resources.TipScheduler;
import com.podcrash.api.util.ChatUtil;
import com.podcrash.api.util.ConfigUtil;
import com.podcrash.api.plugin.PodcrashSpigot;
import com.podcrash.api.db.redis.Communicator;
import me.raindance.champions.commands.*;
import me.raindance.champions.game.DomGame;
import me.raindance.champions.inventory.InvFactory;
import me.raindance.champions.kits.SkillInfo;
import me.raindance.champions.kits.itemskill.ItemHelper;
import me.raindance.champions.listeners.*;
import me.raindance.champions.listeners.maintainers.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

@GamePlugin
public class Main extends JavaPlugin implements IGamePlugin {
    public static volatile Main instance;
    private static Set<String> defaultAllowedSkills;
    private Properties properties;

    public static final String CHANNEL_NAME = "Champions";
    private TipScheduler tips;
    private ProtocolManager protocolManager;
    private BukkitTask tickTask;
    public Logger log = this.getLogger();
    //Mapping configuration files
    private File mapConfig;
    private FileConfiguration mapConfiguration;

    public void registerListeners() {
        new DomRewardsListener(this);
        new DomGameListener(this);
        new SoundDamage(this);
        new InventoryListener(this);
        new MapListener(this);
        new ObjectiveListener(this);
        new PlayerJoinEventTest(this);
        new ItemHelper(this);
        new TickEventListener(this);
        new Disguiser().disguiserIntercepter();
        new EconomyListener(this);
        new DomKitApplyListener(this);
        new RestrictPickup(this);
    }
    private void setUpClasses() {
        SkillInfo.setUp();

    }
    private void registerInjectors() {

    }
    private void setUp() {

    }

    public static Set<String> getDefaultAllowedSkills() {
        return defaultAllowedSkills;
    }

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        /*
        getConfig().options().copyDefaults(true);
        saveConfig();
        */
        instance = this;

        this.properties = ConfigUtil.readPropertiesFile(getClass().getClassLoader(), "allowed.properties");
        //set allowed skills
        String raw = (String) this.properties.get("allowed");
        List<String> t = new ArrayList<>();
        if(raw != null) {
            for (String r : raw.split(",")) {
                String p = ChatUtil.strip(r);
                t.add(p);
            }
            defaultAllowedSkills = new HashSet<>(t);
        }
        //set config stuff
        PodcrashSpigot spigot = PodcrashSpigot.getInstance();
        String url = "https://docs.google.com/document/d/1QVL8m7C5IH-Hk36IXE8j2bKR6loJr7eb-yhtB8Vv2OI/export?format=txt";
        try {
            InputStreamReader stream = new InputStreamReader(new URL(url).openConnection().getInputStream());
            try (BufferedReader reader = new BufferedReader(stream)) {
                spigot.registerConfigurator("skilldescriptions", reader);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        spigot.registerConfigurator("kits");

        registerCommands();
        registerInjectors();
        setUp();
        setUpClasses();
        CompletableFuture tips = registerTips();

        spigot.getWorldSetter().loadFromEnvVariable("conquest_spawn");

        protocolManager = ProtocolLibrary.getProtocolManager();
        mapConfig = new File(getDataFolder(), "maps.yml");
        mapConfiguration = YamlConfiguration.loadConfiguration(mapConfig);
        saveMapConfig();

        this.log.info(Bukkit.getWorlds().toString());


        try {
            tips.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }


        //This part is really only used for reloading
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        if(players.size() > 0) {
            for(Player p : players) {
                HitDetectionInjector detection = new HitDetectionInjector(p);
                detection.injectHitDetection();
                InvFactory.applyLastBuild(p);
            }
        }

        //Communicator.putLobbyMap("maxsize", GameManager.getGame().getMaxPlayers());
        //executor.shutdown();


        PodcrashSpigot.debugLog("ENDTIME: " + (System.currentTimeMillis() - startTime));
    }

    @Override
    public void onDisable() {
        ProtocolLibrary.getProtocolManager().removePacketListeners(this);
        Bukkit.getScheduler().cancelAllTasks();
        //CustomEntityType.unregisterEntities();
        GameManager.destroyCurrentGame();

    }

    private CompletableFuture<Void> registerTips() {
        String url = "https://docs.google.com/document/d/10LcHuVMY-qiNGcbFWvK7pNWSHEiohzV4T-XBx93YqZ4/export?format=txt";
        return CompletableFuture.runAsync(() -> {
            try {
                tips = TipScheduler.fromURL(url);
            }catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public FileConfiguration getMapConfiguration() {
        return mapConfiguration;
    }
    public void saveMapConfig() {
        try {
            mapConfiguration.save(mapConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerCommands() {
        getCommand("wteleport").setExecutor(new WorldTeleportCommand());
        getCommand("invis").setExecutor(new InvisCommand());
        getCommand("damage").setExecutor(new DamageCommand());
        getCommand("velo").setExecutor(new VelocityCommand());
        getCommand("disguise").setExecutor(new DisguiseCommand());
        getCommand("currentlocation").setExecutor(new CurrentLocationCommand());
        getCommand("copyworld").setExecutor(new CopyWorldCommand());
        getCommand("deleteworld").setExecutor(new DeleteWorldCommand());
        getCommand("rc").setExecutor(new ReloadChampionsCommand());
        getCommand("skill").setExecutor(new SkillCommand());
        getCommand("lock").setExecutor(new LockCommand());
    }

    public static Main getInstance() {
        return instance;
    }
    public ProtocolManager getProtocolManager() {
        return (protocolManager != null) ? protocolManager : ProtocolLibrary.getProtocolManager();
    }

    public TipScheduler getTips() {
        return tips;
    }
}
