package me.raindance.champions.kits;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.podcrash.api.kits.KitPlayer;
import com.podcrash.api.kits.Skill;
import me.raindance.champions.inventory.SkillData;
import me.raindance.champions.kits.SkillType;
import me.raindance.champions.inventory.ChampionsInventory;
import me.raindance.champions.inventory.ChampionsItem;
import me.raindance.champions.kits.classes.Druid;
import me.raindance.champions.kits.classes.Sorcerer;
import me.raindance.champions.util.ConquestUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class ChampionsPlayer extends KitPlayer {
    private final int hp;
    public ChampionsPlayer(Player player, int hp) {
        super(player);
        this.hp = hp;
    }
    
    public abstract SkillType getType();

    @Override
    public String getName() {
        return getType().getName();
    }

    @Override
    public int getHP() {
        return hp;
    }

    @Override
    public void respawn() {
        super.respawn();

        if(this instanceof Druid || this instanceof Sorcerer) {
            ebar.setEnergy(ebar.getMaxEnergy());
        }
    }

    public boolean hotBarContains(Material material) {
        for(ItemStack item : ConquestUtil.getDefaultHotbar()) {
            if(item != null && item.getType() == material)
                return true;
        }
        return false;
    }

    private String niceLookingDescription(Skill skill) {
        StringBuilder result = new StringBuilder();
        List<String> description = SkillInfo.getSkillData(skill).getDescription();
        if(description == null) {
            return "";
        }
        for(int i = 0; i < description.size(); i++) {
            String line = description.get(i);
            if(line != null) {
                result.append(line);
            }
            if(i != description.size() - 1) {
                result.append("\n");
            }
        }
        return result.toString();
    }

    public BaseComponent[] skillsRead() {

        ComponentBuilder builder = new ComponentBuilder(this.getName()).color(net.md_5.bungee.api.ChatColor.YELLOW);
        for (Skill skill : skills) {
            String invTypeName = SkillInfo.getSkill(SkillInfo.getSkillID(skill)).getInvType().getName();
            String skillDesc =  niceLookingDescription(skill);

            HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(skillDesc).create());
            builder.append("\n")
                .append(invTypeName + " ").color(net.md_5.bungee.api.ChatColor.GREEN)
                .append(skill.getName()).color(net.md_5.bungee.api.ChatColor.WHITE).event(hover);
        }
        return builder.create();
    }

    @Override
    public JsonObject serialize() {
        if(jsonObject != null) return jsonObject;
        JsonObject championsObject = new JsonObject();

        championsObject.addProperty("skilltype", this.getType().getName().toLowerCase());

        JsonArray skillArray = new JsonArray();

        for (Skill skill : skills)
            skillArray.add(SkillInfo.getSkillID(skill));

        JsonObject itemsSerial = new JsonObject();
        for (int i = 0; i < defaultHotbar.length; i++) {
            ItemStack item = defaultHotbar[i];
            if(item == null || item.getType() == Material.AIR) continue;
            ChampionsItem championsItem = ChampionsItem.getByName(item.getItemMeta().getDisplayName());
            int slotID = (championsItem == null) ? -1 : championsItem.getSlotID();
            itemsSerial.addProperty(Integer.toString(i), slotID);
        }

        championsObject.add("skills", skillArray);
        championsObject.add("items", itemsSerial);
        this.jsonObject = championsObject;
        return championsObject;
    }

    public static ChampionsPlayer deserialize(Player owner, String jsonStr) {
        JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        SkillType skillType = SkillType.getByName(json.get("skilltype").getAsString());

        JsonObject itemsJson = json.getAsJsonObject("items");
        ItemStack[] items = new ItemStack[9];
        for (Map.Entry<String, JsonElement> entry : itemsJson.entrySet()) {
            String slotKey = entry.getKey();
            int itemID = entry.getValue().getAsInt();
            if(itemID == -1) continue;
            ChampionsItem championsItem = ChampionsItem.getBySlotID(itemID);
            items[Integer.parseInt(slotKey)] = championsItem.toItemStack();
        }

        JsonArray skillsJson = json.getAsJsonArray("skills");
        List<Skill> skills = new ArrayList<>();

        for (int i = 0, size = skillsJson.size(); i < size; i++) {
            int id = skillsJson.get(i).getAsInt();
            SkillData data = SkillInfo.getSkill(id);

            Skill skill = data.newInstance();
            skill.setPlayer(owner);
            skills.add(skill);
        }



        ChampionsPlayer kitPlayer = ConquestUtil.newObj(owner, skills, skillType);
        kitPlayer.setDefaultHotbar(items);
        return kitPlayer; //oh god
    }
}
