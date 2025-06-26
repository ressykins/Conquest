package me.raindance.champions.inventory;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.podcrash.api.db.tables.ChampionsKitTable;
import com.podcrash.api.db.tables.DataTableType;
import com.podcrash.api.db.TableOrganizer;
import com.podcrash.api.plugin.Configurator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.game.GameManager;
import com.podcrash.api.game.GameState;
import com.podcrash.api.plugin.PodcrashSpigot;
import com.podcrash.api.util.ItemStackUtil;
import com.podcrash.api.kits.KitPlayer;
import com.podcrash.api.kits.KitPlayerManager;
import com.podcrash.api.kits.Skill;

import me.raindance.champions.kits.ChampionsPlayer;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.sound.SoundPlayer;
import me.raindance.champions.util.ConquestUtil;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class InvFactory {
    private static ChampionsKitTable table;
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static Map<String, Integer> buildMap = new HashMap<>();
    private static Map<String, Integer> buildIDHistory = new HashMap<>();
    private static Map<String, SkillType> skillTypeHistory = new HashMap<>();
    private static final Configurator kitConfigurator = PodcrashSpigot.getInstance().getConfigurator("kits");
    private InvFactory() {

    }

    private static ChampionsKitTable getKitTable() {
        if(table == null) table = TableOrganizer.getTable(DataTableType.KITS);
        return table;
    }
    /**
     * Take a inventory menu and turn it into a list of skills.
     * It will iterate through the inventory looking for enchanted books.
     * Then it will find the corresponding bookformatter, which will give its constructor.
     * @param player the user
     * @param menu the menu
     * @return the list of skills
     */
    private static List<Skill> convertInventoryToSkills(Player player, Inventory menu) {
        List<Skill> skills = new ArrayList<>();
        for (ItemStack book : menu.getContents()) {
            //only pass if it has the enchantent
            if(book == null || !book.containsEnchantment(Enchantment.DAMAGE_ALL)) continue;
            SkillData data = InventoryData.itemStackToSkillData(book);
            if(data == null) continue;
            Skill skill = data.newInstance();
            skill.setPlayer(player);
            skills.add(skill);
        }
        return skills;
    }

    /**
     * Shorthand for a constructor of a ChampionsPlayer
     * @param skillType
     * @param player
     * @param skills
     * @return
     */
    private static ChampionsPlayer findViaSkillType(SkillType skillType, Player player, List<Skill> skills) {
        return ConquestUtil.newObj(player, skills, skillType);
    }

    /**
     * Turn an inventory using the above methods {@link #convertInventoryToSkills(Player, Inventory)}
     * This will turn the inventory into a list of skills.
     * Then, remove the duplicated types.
     * Then, register the player.
     * @param player
     * @param menu
     * @param skillType
     * @return
     */
    public static ChampionsPlayer inventoryToChampion(Player player, Inventory menu, SkillType skillType) {
        List<Skill> skills = convertInventoryToSkills(player, menu);
        ChampionsPlayer newPlayer = findViaSkillType(skillType, player, skills);
        newPlayer.setDefaultHotbar();

        return newPlayer;
    }

    /**
     * Handles either
     * Applying - {@link #apply(Player, SkillType, int)}
     * Editing - {@link  #edit(Player, SkillType, int)}
     * Or
     * Deleting - {@link #delete(Player, SkillType, int)}
     * @param player
     * @param skillType
     * @param item
     */
    public static void clickAtBuildMenu(Player player, SkillType skillType, ItemStack item, int buildID) {
        if(item == null || item.getType() == Material.AIR) return;
        switch (item.getType()) {
            case EMERALD:
                apply(player, skillType, buildID);
                break;
            case INK_SACK:
                if(item.getData() instanceof Dye && !((Dye) item.getData()).getColor().equals(DyeColor.GRAY))
                    apply(player, skillType, buildID);
                break;
            case ANVIL:
                edit(player, skillType, buildID);
                break;
            case SLIME_BALL:
                setAsDefault(player, skillType, buildID);
                break;
            case FIREBALL:
                if(delete(player, skillType, buildID)) {
                    player.closeInventory();
                    player.openInventory(MenuCreator.createKitTemplate(player, skillType));
                }
                break;
        }
    }

    /**
     * Used for when a player rejoins, it will put in the exact same kit that the player
     * @see {@link #apply(Player, SkillType, int)}
     * @param player
     */
    public static void applyLastBuild(Player player) {
        String path = player.getUniqueId() + ".current";
        if(!kitConfigurator.hasPath(path)){
            kitConfigurator.set(path, "duelist#1");
        }
        kitConfigurator.readString(path, currentBuild -> {
            String[] split = currentBuild.split("#");
            SkillType skillType = SkillType.getByName(split[0]);
            int buildID = Integer.parseInt(split[1]);
            apply(player, skillType, buildID);

            if(GameManager.getGame().getGameState() == GameState.LOBBY) {
                GameManager.getGame().updateLobbyInventory(player);
            }
        });
    }

    /**
     * Read the build id of specific skilltype of a specific player UUID and return the
     * serialized string for skills.
     * @see {@link KitPlayer#serialize()}
     *
     * @param player
     * @param skillType
     * @param buildID
     */
    private static void apply(Player player, SkillType skillType, int buildID) {
        getKitTable().getJSONDataAsync(player.getUniqueId(), skillType.getName(), buildID).thenAccept(deserializedPlayer -> {
            String serializedInfo;
            if(deserializedPlayer == null) {
                player.sendMessage(ChatColor.BLUE + "Conquest>" + ChatColor.GRAY + " There is no build loaded here! Click the anvil to make a kit!");
                return;
            } else serializedInfo = deserializedPlayer;

            player.getInventory().clear();
            player.closeInventory();

            ChampionsPlayer cPlayer = ChampionsPlayer.deserialize(player, serializedInfo);
            KitPlayerManager.getInstance().addKitPlayer(cPlayer);
            cPlayer.restockInventory();
            if(GameManager.getGame().getGameState() == GameState.LOBBY) {
                GameManager.getGame().updateLobbyInventory(player);
            }
            SoundPlayer.sendSound(cPlayer.getPlayer(), "random.levelup", 0.75F, 63);
            setCurrent(player, skillType, buildID);
        });
    }

    private static void setAsDefault(Player player, SkillType skillType, int buildID) {
        String deserializedPlayer = getKitTable().getJSONData(player.getUniqueId(), skillType.getName(), buildID);
        String serializedInfo;
        if(deserializedPlayer == null) {
            player.sendMessage(ChatColor.BLUE + "Conquest>" + ChatColor.GRAY + " There is no build loaded here! Click the anvil to make a kit!");
            return;
        } else serializedInfo = deserializedPlayer;

        getKitTable().alter(player.getUniqueId(), skillType.getName(), 0, serializedInfo);
        SoundPlayer.sendSound(player, "random.levelup", 0.75F, 63);

        Inventory inv = MenuCreator.createKitTemplate(player, skillType);
        player.openInventory(inv);


    }

    /**
     * Gets the basic skill selection for the skilltype.
     * Gets the basic hotbar selection for the skilltype.
     * Then adds both of the  edited skills and items via the stored strings.
     * @param player
     * @param skillType
     * @param buildID
     */
    public static void edit(Player player, SkillType skillType, int buildID) {
        StatusApplier.getOrNew(player).removeStatus(Status.values());
        buildMap.put(player.getName(), buildID);
        buildIDHistory.put(player.getName(), buildID);
        skillTypeHistory.put(player.getName(), skillType);
        UUID uuid = player.getUniqueId();
        player.getInventory().clear();
        player.updateInventory();

        //The editing tip: You can save your rearranged inventory in this menu only!
        ItemStackUtil.createItem(player.getInventory(),395, 1, 23, "&e&lTip:", "Rearrange your hotbar in this menu to save it.");

        String json = getKitTable().getJSONData(uuid, skillType.getName(), buildID);


        if(json == null) {
            Inventory inv = MenuCreator.createKitMenu(skillType);
            MenuCreator.giveHotbarInventory(player, skillType);
            player.openInventory(inv);
            return;
        }

        JsonObject kitJson = new JsonParser().parse(json).getAsJsonObject();

        Integer[] skillIDs = wrapInteger(kitJson.getAsJsonArray("skills"));
        Inventory inv = MenuCreator.createKitMenu(skillType, skillIDs);

        JsonObject itemsJson = kitJson.getAsJsonObject("items");
        MenuCreator.giveHotBarInventory(player, itemsJson);

        player.openInventory(inv);
    }

    private static Integer[] wrapInteger(JsonArray array) {
        int size = array.size();
        Integer[] ids = new Integer[size];
        for(int i = 0; i < size; i++) {
            ids[i] = array.get(i).getAsInt();
        }
        return ids;
    }
    /**
     * When a player closes the inventory,
     * have it be applied as well as save it as the current build.
     * @param player
     * @param kitPlayer
     */
    public static void editClose(Player player, ChampionsPlayer kitPlayer) {
        UUID uuid = player.getUniqueId();
        String clasz = kitPlayer.getType().getName();
        int id = buildMap.get(player.getName());
        String data = kitPlayer.serialize().toString();
        String current = getKitTable().getJSONData(uuid, clasz, id);
        PodcrashSpigot.debugLog("save1: " + data);
        PodcrashSpigot.debugLog("current: " + current);
        if(current == null) getKitTable().set(uuid, clasz, id, data);
        else getKitTable().alter(uuid, clasz, id, data);

        setCurrent(player, kitPlayer.getType(), buildMap.get(player.getName()));
        buildMap.remove(player.getName());
    }

    public static boolean currentlyEditing(Player player) {
        return buildMap.containsKey(player.getName());
    }

    public static SkillType getLastestSkillType(Player player) {
        if (!skillTypeHistory.containsKey(player.getName())) {return SkillType.Duelist;}
        return skillTypeHistory.get(player.getName());
    }

    public static int getLatestBuildID(Player player) {
        if (!buildIDHistory.containsKey(player.getName())) {return 0;}
        return buildIDHistory.get(player.getName());
    }
    /**
     * duh
     * @param player
     * @param skillType
     * @param buildID
     * @return
     */
    private static boolean delete(Player player, SkillType skillType, int buildID) {
        getKitTable().delete(player.getUniqueId(), skillType.getName(), buildID);

        return true;
    }

    /**
     * Set the current point to the deserialized string as the current build.
     * Ex: assassin#4
     * Where to find the current build would be path uuid.assassin.4
     * @param player
     * @param skillType
     * @param buildID
     */
    private static void setCurrent(Player player, SkillType skillType, int buildID) {
        kitConfigurator.set(player.getUniqueId() + ".current", skillType.getName().toLowerCase() + "#" + buildID);
        kitConfigurator.saveConfig();
    }
}
