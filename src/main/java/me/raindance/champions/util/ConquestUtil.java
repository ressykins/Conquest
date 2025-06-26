package me.raindance.champions.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.podcrash.api.kits.KitPlayer;
import com.podcrash.api.kits.Skill;
import me.raindance.champions.inventory.SkillData;
import me.raindance.champions.kits.ChampionsPlayer;
import me.raindance.champions.kits.SkillInfo;
import me.raindance.champions.kits.SkillType;
import me.raindance.champions.inventory.ChampionsInventory;
import me.raindance.champions.inventory.ChampionsItem;
import me.raindance.champions.kits.classes.Duelist;
import me.raindance.champions.kits.skills.duelist.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public final class ConquestUtil {
    private static final Map<SkillType, Constructor<? extends ChampionsPlayer>> constructors = new HashMap<>(); //reflection is expensive


    public static ChampionsPlayer defaultBuild(Player player) {
        List<Skill> skills = new ArrayList<>();
        skills.add(new LifeRip());
        skills.add(new Lunge());
        skills.add(new Dragonslayer());
        skills.add(new Lifeline());
        skills.add(new Challenger());

        Duelist knight = new Duelist(player, skills);
        return knight;
    }

    public static ItemStack[] getDefaultHotbar() {
        return new ItemStack[] {
            new ItemStack(ChampionsItem.DUELIST_SWORD.toItemStack()),
            new ItemStack(ChampionsItem.MUSHROOM_STEW.toItemStack()),
            new ItemStack(ChampionsItem.MUSHROOM_STEW.toItemStack()),
            new ItemStack(ChampionsItem.MUSHROOM_STEW.toItemStack()),
            new ItemStack(ChampionsItem.MUSHROOM_STEW.toItemStack()),
        };
    }

    public static String getDefaultSerialized(SkillType skillType) {
        StringBuilder data = new StringBuilder();
        data.append("{\"skilltype\":\"")
                .append(skillType.getName())
                .append("\",\"skills\":")
                .append(Arrays.toString(skillType.getDefaultSkills()))
                .append(",\"items\":{");
        int[] hotbarIDs = ChampionsInventory.getDefaultHotbarIDs(skillType);
        for (int i = 0; i < hotbarIDs.length; i++) {
            data.append("\"").append(i).append("\"").append(":").append(hotbarIDs[i]).append(",");
        }
        data.deleteCharAt(data.length()-1);
        data.append("}}");
        return data.toString();
    }

    private static Constructor<? extends ChampionsPlayer> getConstructor(SkillType skillType) {
        if(skillType == SkillType.Global) throw new IllegalArgumentException("Global is not allowed");
        if(!constructors.containsKey(skillType)) {
            try {
                Class<? extends ChampionsPlayer> clazz = (Class<? extends ChampionsPlayer>) Class.forName("me.raindance.champions.kits.classes." + skillType.getName());
                Constructor cons = clazz.getDeclaredConstructor(Player.class, List.class);
                constructors.put(skillType, cons);
            }catch (ClassNotFoundException|NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return constructors.get(skillType);
    }

    public static  <T extends KitPlayer> T newObj(Player owner, List<Skill> skills, SkillType skillType) {
        try {
            return (T) getConstructor(skillType).newInstance(owner, skills);
        }catch (InstantiationException|IllegalAccessException| InvocationTargetException e){
            e.printStackTrace();
        }
        throw new IllegalArgumentException("something went wrong ya");
    }

    public static List<String> readSkills(String jsonStr) {
        List<String> skillWord = new ArrayList<>();
        JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();

        JsonArray skillsJson = json.getAsJsonArray("skills");
        for (int i = 0, size = skillsJson.size(); i < size; i++) {
            int id = skillsJson.get(i).getAsInt();
            SkillData data = SkillInfo.getSkill(id);
            if(data == null) {
                System.out.println(id);
                SkillInfo.getSkills(SkillType.Druid).forEach(System.out::println);
            }else skillWord.add(String.format("%s%s%s", ChatColor.RESET, ChatColor.LIGHT_PURPLE, data.getName()));
        }
        return skillWord;
    }
}
