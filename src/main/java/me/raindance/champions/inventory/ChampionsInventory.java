package me.raindance.champions.inventory;

import com.podcrash.api.util.ItemStackUtil;
import me.raindance.champions.Main;
import me.raindance.champions.kits.SkillType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ChampionsInventory {
    private static final ItemStack[] classItemList;
    private static int cursor = 0;
    static {
        classItemList = new ItemStack[SkillType.details().length];

        addClass(SkillType.Warden, Material.IRON_HELMET,
                "Wardens hold down the line with their",
                "impenetrable bulk, ensuring the safety of their",
                "allies and warding off any adversary that",
                "dares to get within range.");

        addClass(SkillType.Duelist, Material.DIAMOND_SWORD,
                "Duelists excel in one-on-one duels,",
                "utilizing their melee prowess and sustainability",
                "to slay their opponents with ease.");

        addClass(SkillType.Vanguard, Material.DIAMOND_CHESTPLATE,
                "Vanguards lead the charge into battle,",
                        "forcing their adversaries out of position and taking",
                        "the brunt of enemy retaliation to pave the way",
                        "for their allies to claim victory.");
        
        addClass(SkillType.Berserker, Material.DIAMOND_AXE, 
                "Berserkers have an unquenchable thirst for",
                        "battle, allowing them to stay alive in brawls for",
                        "extended periods of time so long as they are shedding",
                        "the blood of their enemies.");

        addClass(SkillType.Marksman, Material.BOW, 
                "Marksmen dispatch targets from afar with",
                        "deadly precision, providing heavy sustained damage",
                        "and support with their trusty bow and arrow.");

        addClass(SkillType.Hunter, Material.BONE,
                "Hunters are agile fighters that make use of their",
                        "environment, patiently wearing their prey down at a safe",
                        "distance before going in to secure the kill.");

        addClass(SkillType.Sorcerer, Material.BLAZE_ROD,
                "Sorcerers primarily fight using their spells,",
                        "whittling and locking down their foes with a",
                        "wide arsenal of destructive and disruptive magic.");

        addClass(SkillType.Druid, Material.SAPLING,
                "Druids bolster their team with their supportive",
                        "abilities, supplying powerful enhancements to their",
                        "allies while keeping enemies at bay with debilitating",
                        "magic.");

        addClass(SkillType.Rogue, Material.EYE_OF_ENDER,
                "Rogues are opportunists who lurk in the shadows,",
                        "waiting for the perfect time to ambush their victims",
                        "and dispatch them with their blinding speed",
                        "and powerful burst damage.");

        addClass(SkillType.Thief, Material.COAL, 
                "Thieves are quick, cunning, and adept at creating",
                        "chaos in the midst of battle, utilizing a wide array of",
                        "tools and and techniques to disorient and deceive their",
                        "enemies.");
        /*

        ItemStack assassin = ItemStackUtil.createItem(Material.LEATHER_HELMET, "Assassin", Arrays.asList("Use stealth hacks", "and insane mobility to kill every1"));
        ItemStack brute = ItemStackUtil.createItem(Material.DIAMOND_HELMET, "Brute", Arrays.asList("Use crowd control", "and filth to kill every1"));
        ItemStack mage = ItemStackUtil.createItem(Material.GOLD_HELMET, "Mage", Arrays.asList("Use insane IQ", "and insane skills to kill every1"));
        ItemStack knight = ItemStackUtil.createItem(Material.IRON_HELMET, "Knight", Arrays.asList("Use defense", "and brain to kill every1"));
        ItemStack ranger = ItemStackUtil.createItem(Material.CHAINMAIL_HELMET, "Ranger", Arrays.asList("Use mobility", "and range to kill every1"));
        */
        cursor = 0;
    }

    private static void addClass(SkillType skillType, Material material, String... description) {
        List<String> desc = new ArrayList<>();
        for(String d : description) {
            desc.add(ChatColor.GRAY + d);
        }
        ItemStack item = ItemStackUtil.createItem(material, String.format("%s%s%s%s", ChatColor.RESET, ChatColor.AQUA, ChatColor.BOLD, skillType.getName()), desc);
        classItemList[cursor] = item;
        cursor++;
    }

    public static ItemStack[] getClassItemList() {
        return classItemList;
    }

    public static ChampionsItem[] getDefaultHotbar(SkillType skillType) {
        switch (skillType) {
            //they won't be merged just in case for easy access
            case Warden:
                return new ChampionsItem[]{ChampionsItem.WARDEN_SWORD, ChampionsItem.WARDEN_AXE, ChampionsItem.MUSHROOM_STEW, ChampionsItem.MUSHROOM_STEW, ChampionsItem.MUSHROOM_STEW, ChampionsItem.MUSHROOM_STEW};
            case Vanguard:
                return new ChampionsItem[] {ChampionsItem.VANGUARD_SWORD, ChampionsItem.VANGUARD_AXE, ChampionsItem.MUSHROOM_STEW, ChampionsItem.MUSHROOM_STEW, ChampionsItem.MUSHROOM_STEW, ChampionsItem.MUSHROOM_STEW};
            case Berserker:
                return new ChampionsItem[] {ChampionsItem.BERSERKER_AXE, ChampionsItem.MUSHROOM_STEW, ChampionsItem.MUSHROOM_STEW, ChampionsItem.MUSHROOM_STEW, ChampionsItem.MUSHROOM_STEW};
            case Duelist:
                return new ChampionsItem[] {ChampionsItem.DUELIST_SWORD, ChampionsItem.MUSHROOM_STEW, ChampionsItem.MUSHROOM_STEW, ChampionsItem.MUSHROOM_STEW, ChampionsItem.MUSHROOM_STEW};
            case Marksman:
                return new ChampionsItem[] {ChampionsItem.MARKSMAN_SWORD, ChampionsItem.MARKSMAN_BOW, ChampionsItem.MUSHROOM_STEW, ChampionsItem.MUSHROOM_STEW, ChampionsItem.MARKSMAN_ARROWS, ChampionsItem.MARKSMAN_ARROWS, ChampionsItem.GRAPPLING_HOOK};
            case Hunter:
                return new ChampionsItem[] {ChampionsItem.HUNTER_SWORD, ChampionsItem.HUNTER_AXE, ChampionsItem.HUNTER_BOW, ChampionsItem.MUSHROOM_STEW, ChampionsItem.MUSHROOM_STEW, ChampionsItem.HUNTER_ARROWS};
            case Thief:
                return new ChampionsItem[] {ChampionsItem.THIEF_SWORD, ChampionsItem.THIEF_AXE, ChampionsItem.THIEF_BOW, ChampionsItem.THIEF_ARROWS, ChampionsItem.ELIXIR, ChampionsItem.ELIXIR};
            case Rogue:
                return new ChampionsItem[] {ChampionsItem.ROGUE_SWORD, ChampionsItem.ROGUE_AXE, ChampionsItem.ELIXIR, ChampionsItem.ELIXIR, ChampionsItem.WATER_BOTTLE};
            case Druid:
                return new ChampionsItem[] {ChampionsItem.LIFE_SWORD, ChampionsItem.LIFE_AXE, ChampionsItem.MUSHROOM_STEW, ChampionsItem.MUSHROOM_STEW, ChampionsItem.MUSHROOM_STEW};
            case Sorcerer:
                return new ChampionsItem[] {ChampionsItem.SPELL_SWORD, ChampionsItem.SPELL_AXE, ChampionsItem.MUSHROOM_STEW, ChampionsItem.MUSHROOM_STEW, ChampionsItem.MUSHROOM_STEW};
            default:
                throw new IllegalStateException("Unexpected value: " + skillType);
        }
    }

    public static int[] getDefaultHotbarIDs(SkillType skillType) {
        ChampionsItem[] items = getDefaultHotbar(skillType);
        int[] ids = new int[items.length];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = items[i].getSlotID();
        }
        return ids;
    }

    static void setHotBar(Inventory inventory, SkillType skillType) {
        ChampionsItem[] itemArray = getDefaultHotbar(skillType);
        for(int i = 0; i < itemArray.length; i++) {
            ChampionsItem id = itemArray[i];
            inventory.setItem(i, id.toItemStack());
        }
    }
}
