package me.raindance.champions.inventory;

import com.podcrash.api.util.ChatUtil;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChampionsItem {

    private int slotID;
    private String name;
    private int count;
    private int damage;
    private List<String> desc;
    private Material material;
    private byte data = 0;

    public static List<ChampionsItem> details = new ArrayList<>();

    private ChampionsItem(int slotID, String name, int count, int damage, List<String> desc, Material material, byte data) {
        this.slotID = slotID;
        this.name = name;
        this.count = count;
        this.damage = damage;
        this.desc = desc;
        this.material = material;
        this.data = data;

        details.add(this);
    }

    public static final ChampionsItem MARKSMAN_SWORD = new ChampionsItem(1, ChatColor.YELLOW + "Marksman Sword", 1, 6, Arrays.asList(ChatColor.GOLD + "Deals 6 damage."), Material.IRON_SWORD);
    public static final ChampionsItem VANGUARD_AXE = new ChampionsItem(2, ChatColor.YELLOW + "Vanguard Axe", 1, 6, Arrays.asList(ChatColor.GOLD + "Deals 6 damage."), Material.IRON_AXE);
    public static final ChampionsItem VANGUARD_SWORD = new ChampionsItem(3, ChatColor.YELLOW + "Vanguard Sword", 1, 6, Arrays.asList(ChatColor.GOLD + "Deals 6 damage."), Material.IRON_SWORD);
    public static final ChampionsItem WARDEN_SWORD = new ChampionsItem(4, ChatColor.YELLOW + "Warden Sword", 1, 6, Arrays.asList(ChatColor.GOLD + "Deals 6 damage."), Material.IRON_SWORD);
    public static final ChampionsItem WARDEN_AXE = new ChampionsItem(5, ChatColor.YELLOW + "Warden Axe", 1, 6, Arrays.asList(ChatColor.GOLD + "Deals 6 damage."), Material.IRON_AXE);
    public static final ChampionsItem HUNTER_SWORD = new ChampionsItem(6, ChatColor.YELLOW + "Hunter Sword", 1, 6, Arrays.asList(ChatColor.GOLD + "Deals 6 damage."), Material.IRON_SWORD);
    public static final ChampionsItem HUNTER_AXE = new ChampionsItem(7, ChatColor.YELLOW + "Hunter Axe", 1, 6, Arrays.asList(ChatColor.GOLD + "Deals 6 damage."), Material.IRON_AXE);

    public static final ChampionsItem SPELL_SWORD = new ChampionsItem(8, ChatColor.YELLOW + "Spell Sword", 1, 5, Arrays.asList(ChatColor.GOLD + "Deals 5 damage."), Material.GOLD_SWORD);
    public static final ChampionsItem SPELL_AXE = new ChampionsItem(9, ChatColor.YELLOW + "Spell Axe", 1, 5, Arrays.asList(ChatColor.GOLD + "Deals 5 damage."), Material.GOLD_AXE);
    // public static final ChampionsItem SPELL_SHOVEL = new ChampionsItem(10, ChatColor.YELLOW + "Spell Shovel", 1, 5, Arrays.asList(ChatColor.GOLD + "Deals 5 damage."), Material.GOLD_SPADE);
    public static final ChampionsItem LIFE_SWORD = new ChampionsItem(11, ChatColor.YELLOW + "Life Sword", 1, 5, Arrays.asList(ChatColor.GOLD + "Deals 5 damage."), Material.WOOD_SWORD);
    public static final ChampionsItem LIFE_AXE = new ChampionsItem(12, ChatColor.YELLOW + "Life Axe", 1, 5, Arrays.asList(ChatColor.GOLD + "Deals 5 damage."), Material.WOOD_AXE);
    // public static final ChampionsItem LIFE_SHOVEL = new ChampionsItem(13, ChatColor.YELLOW + "Life Shovel", 1, 5, Arrays.asList(ChatColor.GOLD + "Deals 5 damage."), Material.WOOD_SPADE);
    public static final ChampionsItem THIEF_SWORD = new ChampionsItem(14, ChatColor.YELLOW + "Thief Sword", 1, 6, Arrays.asList(ChatColor.GOLD + "Deals 5 damage."), Material.STONE_SWORD);
    public static final ChampionsItem THIEF_AXE = new ChampionsItem(15, ChatColor.YELLOW + "Thief Axe", 1, 6, Arrays.asList(ChatColor.GOLD + "Deals 5 damage."), Material.STONE_AXE);

    public static final ChampionsItem ROGUE_SWORD = new ChampionsItem(16, ChatColor.YELLOW + "Rogue Sword", 1, 7, Arrays.asList(ChatColor.GOLD + "Deals 7 damage."), Material.DIAMOND_SWORD);
    public static final ChampionsItem ROGUE_AXE = new ChampionsItem(17, ChatColor.YELLOW + "Rogue Axe", 1, 7, Arrays.asList(ChatColor.GOLD + "Deals 7 damage."), Material.DIAMOND_AXE);
    public static final ChampionsItem BERSERKER_AXE = new ChampionsItem(18, ChatColor.YELLOW + "Berserker Axe", 1, 7, Arrays.asList(ChatColor.GOLD + "Deals 7 damage."), Material.DIAMOND_AXE);
    public static final ChampionsItem DUELIST_SWORD = new ChampionsItem(19, ChatColor.YELLOW + "Duelist Sword", 1, 7, Arrays.asList(ChatColor.GOLD + "Deals 7 damage."), Material.DIAMOND_SWORD);

    public static final ChampionsItem MARKSMAN_BOW = new ChampionsItem(20, ChatColor.YELLOW + "Marksman Bow", 1, Arrays.asList(ChatColor.GOLD + "Deals 1 - 8 damage based on", ChatColor.GOLD + "time charged."), Material.BOW);
    public static final ChampionsItem HUNTER_BOW = new ChampionsItem(21, ChatColor.YELLOW + "Hunter Bow", 1, Arrays.asList(ChatColor.GOLD + "Deals 1 - 8 damage based on", ChatColor.GOLD + "time charged."), Material.BOW);
    public static final ChampionsItem THIEF_BOW = new ChampionsItem(22, ChatColor.YELLOW + "Thief Bow", 1, Arrays.asList(ChatColor.GOLD + "Deals 1 - 8 damage based on", ChatColor.GOLD + "time charged."), Material.BOW);

    public static final ChampionsItem MARKSMAN_ARROWS = new ChampionsItem(23, ChatColor.YELLOW + "Marksman Arrows", 64, Arrays.asList(ChatColor.GOLD + "To be fired from the Marksman Bow."), Material.ARROW);
    public static final ChampionsItem HUNTER_ARROWS = new ChampionsItem(24, ChatColor.YELLOW + "Hunter Arrows", 64, Arrays.asList(ChatColor.GOLD + "To be fired from the Hunter Bow."), Material.ARROW);
    public static final ChampionsItem THIEF_ARROWS = new ChampionsItem(25, ChatColor.YELLOW + "Thief Arrows", 24, Arrays.asList(ChatColor.GOLD + "To be fired from the Thief Bow."), Material.ARROW);

    public static final ChampionsItem MUSHROOM_STEW = new ChampionsItem(26, ChatColor.WHITE + "Mushroom Stew", 1, Arrays.asList(ChatColor.GOLD + "Click to consume, granting", ChatColor.GOLD + "you Regeneration II for 4",
            ChatColor.GOLD + "seconds."), Material.MUSHROOM_SOUP);
    public static final ChampionsItem WATER_BOTTLE = new ChampionsItem(27, ChatColor.WHITE + "Water Bottle", 1, Arrays.asList(ChatColor.GOLD + "A Swiggity Swooty", ChatColor.GOLD + "Cure all negative effects!"), Material.POTION);
    public static final ChampionsItem ELIXIR = new ChampionsItem(33, ChatColor.WHITE + "Elixir", 1, 0, Arrays.asList(ChatColor.GOLD + "Right-click to throw. Splashes an aura",
            ChatColor.GOLD + "of healing that restores up to 4 health", ChatColor.GOLD + "based on distance."), Material.POTION, (byte) 16453);
    public static final ChampionsItem GRAPPLING_HOOK = new ChampionsItem(10, ChatColor.YELLOW + "Grappling Hook", 1, 0, Arrays.asList(ChatColor.GOLD + "Cast to launch a hook that locks into",
    ChatColor.GOLD + "place upon landing. Recast to", ChatColor.GOLD + "pull yourself towards it."), Material.FISHING_ROD);

    
    public static final ChampionsItem COBWEB = new ChampionsItem(28, ChatColor.WHITE + "Cobweb", 4, Arrays.asList(ChatColor.GOLD + "Left-click to throw in target direction,", ChatColor.GOLD + "placing a cobweb on impact that lasts",
            ChatColor.GOLD + "for 8 seconds."), Material.WEB);
    public static final ChampionsItem SMOKE_BOMB = new ChampionsItem(29, ChatColor.WHITE + "Smoke Bomb", 1, 0, Arrays.asList(ChatColor.GOLD + "Click to throw in target direction,",
            ChatColor.GOLD + "exploding after impact and inflicting", ChatColor.GOLD + "Blindness and Slowness II to all players", ChatColor.GOLD + "within 4 blocks for 3 seconds."), Material.FIREWORK_CHARGE);
    public static final ChampionsItem STUN_CHARGE = new ChampionsItem(30, ChatColor.WHITE + "Stun Charge", 1, 0, Arrays.asList(ChatColor.GOLD + "Right-click to drop or left-click to throw",
            ChatColor.GOLD + "on the ground. It will take about 2", ChatColor.GOLD + "seconds for it to ready itself. If a player", ChatColor.GOLD + "steps on it, they will be Silenced, Grounded", ChatColor.GOLD + "and Shocked for 4 seconds. Disappears if",
            ChatColor.GOLD + "you die, switch kits, or after 20 seconds."), Material.REDSTONE_LAMP_OFF);
    public static final ChampionsItem MEAD = new ChampionsItem(31, ChatColor.WHITE + "Bread", 1, 0, Arrays.asList(ChatColor.GOLD + "Click to consume, granting", ChatColor.GOLD + "Strength I but making you Vulnerable for 5 seconds."), Material.POTION, (byte) 8227);
    public static final ChampionsItem BEAR_TRAP = new ChampionsItem(32, ChatColor.WHITE + "Bear Trap", 2, 0, Arrays.asList(ChatColor.GOLD + "Right-click to drop or or left-click to throw",
            ChatColor.GOLD + "on the ground. It will take about 2", ChatColor.GOLD + "seconds for it to ready itself. If a player", ChatColor.GOLD + "steps on it, they will take 3 damage and",
            ChatColor.GOLD + "be Rooted for 3 seconds. Disappears if you", ChatColor.GOLD + "die, switch kits, or after 20 seconds."), Material.STONE_PLATE);
    
    public static final ChampionsItem PROXIMITY_MINE = new ChampionsItem(34, ChatColor.WHITE + "Proximity Mine", 1, 0, Arrays.asList(ChatColor.GOLD + "Right-click to drop or left-click to throw",
    ChatColor.GOLD + "on the ground. It will take about 2", ChatColor.GOLD + "seconds for it to ready itself. If an enemy", ChatColor.GOLD + "steps on it, it will explode, launching", ChatColor.GOLD + "them in the air. Lasts for",
    ChatColor.GOLD + "40 seconds."), Material.TNT);

    /* OLD COLORS
    public static final ChampionsItem MARKSMAN_SWORD = new ChampionsItem(1, ChatColor.WHITE + "Marksman Sword", 1, 6, Arrays.asList(ChatColor.GOLD + "Deals 6 damage."), Material.IRON_SWORD);
    public static final ChampionsItem VANGUARD_AXE = new ChampionsItem(2, ChatColor.WHITE + "Vanguard Axe", 1, 6, Arrays.asList(ChatColor.GOLD + "Deals 6 damage."), Material.IRON_AXE);
    public static final ChampionsItem VANGUARD_SHOVEL = new ChampionsItem(3, ChatColor.WHITE + "Vanguard Shovel", 1, 6, Arrays.asList(ChatColor.GOLD + "Deals 6 damage."), Material.IRON_SPADE);

    public static final ChampionsItem WARDEN_SWORD = new ChampionsItem(4, ChatColor.WHITE + "Warden Sword", 1, 6, Arrays.asList(ChatColor.GOLD + "Deals 6 damage."), Material.IRON_SWORD);
    public static final ChampionsItem WARDEN_AXE = new ChampionsItem(5, ChatColor.WHITE + "Warden Axe", 1, 6, Arrays.asList(ChatColor.GOLD + "Deals 6 damage."), Material.IRON_AXE);

    public static final ChampionsItem HUNTER_SWORD = new ChampionsItem(6, ChatColor.WHITE + "Hunter Sword", 1, 6, Arrays.asList(ChatColor.GOLD + "Deals 6 damage."), Material.IRON_SWORD);
    public static final ChampionsItem HUNTER_AXE = new ChampionsItem(7, ChatColor.WHITE + "Hunter Axe", 1, 6, Arrays.asList(ChatColor.GOLD + "Deals 6 damage."), Material.IRON_AXE);

    public static final ChampionsItem SPELL_SWORD = new ChampionsItem(8, ChatColor.GOLD + "Spell Sword", 1, 5, Arrays.asList(ChatColor.GOLD + "Deals 5 damage."), Material.GOLD_SWORD);
    public static final ChampionsItem SPELL_AXE = new ChampionsItem(9, ChatColor.GOLD + "Spell Axe", 1, 5, Arrays.asList(ChatColor.GOLD + "Deals 5 damage."), Material.GOLD_AXE);
    public static final ChampionsItem SPELL_SHOVEL = new ChampionsItem(10, ChatColor.GOLD + "Spell Shovel", 1, 5, Arrays.asList(ChatColor.GOLD + "Deals 5 damage."), Material.GOLD_SPADE);

    public static final ChampionsItem LIFE_SWORD = new ChampionsItem(11, ChatColor.WHITE + "Life Sword", 1, 5, Arrays.asList(ChatColor.GOLD + "Deals 5 damage."), Material.WOOD_SWORD);
    public static final ChampionsItem LIFE_AXE = new ChampionsItem(12, ChatColor.WHITE + "Life Axe", 1, 5, Arrays.asList(ChatColor.GOLD + "Deals 5 damage."), Material.WOOD_AXE);
    public static final ChampionsItem LIFE_SHOVEL = new ChampionsItem(13, ChatColor.WHITE + "Life Shovel", 1, 5, Arrays.asList(ChatColor.GOLD + "Deals 5 damage."), Material.WOOD_SPADE);

    public static final ChampionsItem THIEF_SWORD = new ChampionsItem(14, ChatColor.WHITE + "Thief Sword", 1, 6, Arrays.asList(ChatColor.GOLD + "Deals 5 damage."), Material.STONE_SWORD);
    public static final ChampionsItem THIEF_AXE = new ChampionsItem(15, ChatColor.WHITE + "Thief Axe", 1, 6, Arrays.asList(ChatColor.GOLD + "Deals 5 damage."), Material.STONE_AXE);

    public static final ChampionsItem ROGUE_SWORD = new ChampionsItem(16, ChatColor.AQUA + "Rogue Sword", 1, 7, Arrays.asList(ChatColor.GOLD + "Deals 7 damage."), Material.DIAMOND_SWORD);
    public static final ChampionsItem ROGUE_AXE = new ChampionsItem(17, ChatColor.AQUA + "Rogue Axe", 1, 7, Arrays.asList(ChatColor.GOLD + "Deals 7 damage."), Material.DIAMOND_AXE);

    public static final ChampionsItem BERSERKER_AXE = new ChampionsItem(18, ChatColor.AQUA + "Berserker Axe", 1, 7, Arrays.asList(ChatColor.GOLD + "Deals 7 damage."), Material.DIAMOND_AXE);

    public static final ChampionsItem DUELIST_SWORD = new ChampionsItem(19, ChatColor.AQUA + "Duelist Sword", 1, 7, Arrays.asList(ChatColor.GOLD + "Deals 7 damage."), Material.DIAMOND_SWORD);

    public static final ChampionsItem MARKSMAN_BOW = new ChampionsItem(20, ChatColor.WHITE + "Marksman Bow", 1, Arrays.asList(ChatColor.GOLD + "Deals 1 - 8 damage based on", ChatColor.GOLD + "time charged."), Material.BOW);
    public static final ChampionsItem HUNTER_BOW = new ChampionsItem(21, ChatColor.WHITE + "Hunter Bow", 1, Arrays.asList(ChatColor.GOLD + "Deals 1 - 8 damage based on", ChatColor.GOLD + "time charged."), Material.BOW);
    public static final ChampionsItem THIEF_BOW = new ChampionsItem(22, ChatColor.WHITE + "Thief Bow", 1, Arrays.asList(ChatColor.GOLD + "Deals 1 - 8 damage based on", ChatColor.GOLD + "time charged."), Material.BOW);

    public static final ChampionsItem MARKSMAN_ARROWS = new ChampionsItem(23, ChatColor.WHITE + "Marksman Arrows", 40, Arrays.asList(ChatColor.GOLD + "To be fired from the Marksman Bow"), Material.ARROW);
    public static final ChampionsItem HUNTER_ARROWS = new ChampionsItem(24, ChatColor.WHITE + "Hunter Arrows", 32, Arrays.asList(ChatColor.GOLD + "To be fired from the Hunter Bow"), Material.ARROW);
    public static final ChampionsItem THIEF_ARROWS = new ChampionsItem(25, ChatColor.WHITE + "Thief Arrows", 16, Arrays.asList(ChatColor.GOLD + "To be fired from the Thief Bow"), Material.ARROW);

    public static final ChampionsItem MUSHROOM_STEW = new ChampionsItem(26, ChatColor.WHITE + "Mushroom Stew", 1, Arrays.asList(ChatColor.GOLD + "Click to consume, granting", ChatColor.GOLD + "you Regeneration II for 4",
            ChatColor.GOLD + "seconds."), Material.MUSHROOM_SOUP);
    public static final ChampionsItem WATER_BOTTLE = new ChampionsItem(27, ChatColor.WHITE + "Water Bottle", 1, Arrays.asList(ChatColor.GOLD + "A Swiggity Swooty", ChatColor.GOLD + "Cure all negative effects!"), Material.POTION);
    public static final ChampionsItem COBWEB = new ChampionsItem(28, ChatColor.WHITE + "Cobweb", 4, Arrays.asList(ChatColor.GOLD + "Left-click to throw in target direction,", ChatColor.GOLD + "placing a cobweb on impact that lasts",
            ChatColor.GOLD + "for 8 seconds."), Material.WEB);

    public static final ChampionsItem SMOKE_BOMB = new ChampionsItem(29, ChatColor.WHITE + "Smoke Bomb", 1, 0, Arrays.asList(ChatColor.GOLD + "Click to throw in target direction,",
            ChatColor.GOLD + "exploding after impact and inflicting", ChatColor.GOLD + "Blindness and Slowness II to all players", ChatColor.GOLD + "within 4 blocks for 3 seconds."), Material.FIREWORK_CHARGE);
    public static final ChampionsItem STUN_CHARGE = new ChampionsItem(30, ChatColor.WHITE + "Stun Charge", 2, 0, Arrays.asList(ChatColor.GOLD + "Right-click to drop or left-click to throw",
            ChatColor.GOLD + "on the ground. It will take about 2", ChatColor.GOLD + "seconds for it to ready itself. If a player", ChatColor.GOLD + "steps on it, they will be Silenced, Grounded", ChatColor.GOLD + "and Shocked for 4 seconds. Disappears if",
            ChatColor.GOLD + "you die, switch kits, or after 20 seconds."), Material.REDSTONE_LAMP_OFF);
    public static final ChampionsItem BREAD = new ChampionsItem(31, ChatColor.WHITE + "Bread", 1, 0, Arrays.asList(ChatColor.GOLD + "Click to consume, granting", ChatColor.GOLD + "you Strength I for 3 seconds."), Material.BREAD);
    public static final ChampionsItem BEAR_TRAP = new ChampionsItem(32, ChatColor.WHITE + "Bear Trap", 2, 0, Arrays.asList(ChatColor.GOLD + "Right-click to drop or or left-click to throw",
            ChatColor.GOLD + "on the ground. It will take about 2", ChatColor.GOLD + "seconds for it to ready itself. If a player", ChatColor.GOLD + "steps on it, they will take 3 damage and",
            ChatColor.GOLD + "be Rooted for 3 seconds. Disappears if you", ChatColor.GOLD + "die, switch kits, or after 20 seconds."), Material.STONE_PLATE);
    public static final ChampionsItem ELIXIR = new ChampionsItem(33, ChatColor.WHITE + "Elixir", 2, 0, Arrays.asList(ChatColor.GOLD + "Right-click to throw. Splashes an aura",
            ChatColor.GOLD + "of healing that restores up to 7 health", ChatColor.GOLD + "based on distance."), Material.POTION, (byte) 16421);

     */

    private ChampionsItem(int slotID, String name, int count, int damage, List<String> desc, Material material) {
        this(slotID, name, count, damage, desc, material, (byte) 0);
    }
    private ChampionsItem(int slotID, String name, int count, List<String> desc, Material material) {
        this(slotID, name, count, 0, desc, material);
    }

    public int getSlotID() {
        return slotID;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }
    
    public List<String> getDesc() {
        return desc;
    }

    public Material getMaterial() {
        return material;
    }

    public ItemStack toItemStack(){
        ItemStack itemStack;

        if(this.getName().equals(ChatColor.WHITE + "Elixir")) {
            Potion potion = new Potion(PotionType.INSTANT_HEAL, 1);
            potion.setSplash(true);
            itemStack = potion.toItemStack(getCount());
        }
        else {
            itemStack = new ItemStack(material, count, data);
        }
        /*
        switch (this) {
            case ELIXIR:
                Potion potion = new Potion(PotionType.INSTANT_HEAL, 2);
                potion.setSplash(true);
                itemStack = potion.toItemStack(1);
                break;
            default:
                itemStack = new ItemStack(material, count, data);
                break;
        }
       */
        if(Enchantment.DURABILITY.canEnchantItem(itemStack)) {
            /*Not sure which one is correct

            NbtCompound unbreakableTag = (NbtCompound) NbtFactory.fromItemTag(CraftItemStack.asCraftCopy(itemStack));
            // according to this right? https://minecraft.gamepedia.com/Player.dat_format#Item_structure
            unbreakableTag.put("Unbreakable", 1);
            */
            //will just use this one for now
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setBoolean("Unbreakable", true);

            NBTTagList modifiers = new NBTTagList();
            NBTTagCompound damager = new NBTTagCompound();
            damager.set("AttributeName", new NBTTagString("generic.attackDamage"));
            damager.set("Name", new NBTTagString("generic.attackDamage"));
            damager.set("Amount", new NBTTagInt(damage));
            damager.set("Operation", new NBTTagInt(0));
            damager.set("UUIDLeast", new NBTTagInt(894654));
            damager.set("UUIDMost", new NBTTagInt(2872));

            modifiers.add(damager);
            tag.set("AttributeModifiers", modifiers);
            nmsStack.setTag(tag);
            itemStack = CraftItemStack.asBukkitCopy(nmsStack);

        }
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(ChatColor.RESET + name);
        List<String> arrays = new ArrayList<>(desc);
        meta.setLore(arrays);
        itemStack.setItemMeta(meta);

        return itemStack;
    }

    public static ChampionsItem getBySlotID(int slotID) {
        for(ChampionsItem item : details) {
            if(item.getSlotID() == slotID) return item;
        }
        return null;
    }

    public static ChampionsItem getByName(String name) {
        name = ChatUtil.purge(name);
        for(ChampionsItem item : details) {
            String unfiltered = ChatUtil.purge(item.getName());
            if(unfiltered.equals(name)) return item;
        }
        return null;
    }
}
