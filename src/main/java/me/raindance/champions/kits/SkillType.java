package me.raindance.champions.kits;

import me.raindance.champions.inventory.ChampionsInventory;

import java.util.Arrays;

public enum SkillType {
    Vanguard("Vanguard", new int[]{809,802,804,813,812}),
    Berserker("Berserker", new int[]{106,102,114,101,105}),
    Duelist("Duelist", new int[]{313,305,304,302,309}),
    Warden("Warden", new int[]{904,902,907,911,906}),
    Marksman("Marksman", new int[]{502,505,501,504,506}),
    Hunter("Hunter", new int[]{408,406,404,403,405}),
    Sorcerer("Sorcerer", new int[]{1011,1001,1007,1006,1002}),
    Druid("Druid", new int[]{206,207,201,205,202}),
    Rogue("Rogue", new int[]{604,605,601,611,603}),
    Thief("Thief", new int[]{708,705,702,710,706}),
    Global("All", new int[]{});

    /**
     * Knight >> Duelist & Warden
     * Brute >> Vanguard & Berserker
     * Ranger >> Marksman & Hunter
     * Mage >> Sorcerer & Druid
     * Assassin >> Rogue & Thief
     */
    private String name;
    private int[] defaultSkills;

    SkillType(String name, int[] defaultSkills) {
        this.name = name;
        this.defaultSkills = defaultSkills;
    }

    public String getName() {
        return name;
    }

    public int[] getDefaultSkills() {
        return defaultSkills;
    }


    public static SkillType getByName(String name){
        name = name.toLowerCase();
        for(SkillType skillType : SkillType.values()) {
            if(name.contains(skillType.getName().toLowerCase())) return skillType;
        }
        return null;
    }
    @Override
    public String toString() {
        return getName();
    }

    private final static SkillType[] details = SkillType.values();
    public static SkillType[] details() {
        return details;
    }
}
