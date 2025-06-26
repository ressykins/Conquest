package me.raindance.champions.kits;

import com.google.common.reflect.ClassPath;
import com.podcrash.api.db.TableOrganizer;
import com.podcrash.api.db.tables.DataTableType;
import com.podcrash.api.db.tables.EconomyTable;
import com.podcrash.api.kits.Skill;
import com.podcrash.api.plugin.PodcrashSpigot;
import com.podcrash.api.util.ChatUtil;
import com.podcrash.api.util.ReflectionUtil;
import me.raindance.champions.inventory.SkillData;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import me.raindance.champions.kits.skills.warden.Fortitude;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * Class that is going to be used to store and organize all skills.
 */
public final class SkillInfo {
    private final static List<SkillData> skillData = new ArrayList<>();

    //wondering if I should keep this.
    private final static Map<Integer, SkillData> idDataMap = new HashMap<>();
    private final static Map<SkillType, int[]> skillTypeCache = new EnumMap<>(SkillType.class);
    private final static Map<InvType, int[]> invTypeCache = new EnumMap<>(InvType.class);

    public static void setUp() {
        System.out.println("Loading classes");
        try {
            final List<String> list = Arrays.asList("warden", "duelist", "vanguard", "berserker", "marksman", "hunter", "thief", "rogue", "druid", "sorcerer");
            for(String e : list)
                SkillInfo.addSkills(e);
            skillData.sort((s1, s2) -> Integer.compare(s2.getId(), s1.getId()));
            System.out.println("Sorted classes");
        }catch (IOException|ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private static void addSkills(String skillTypeName) throws IOException, ClassNotFoundException {
        String path = "me.raindance.champions.kits.skills." + skillTypeName;
        ClassPath cp = ClassPath.from(Fortitude.class.getClassLoader());
        Set<ClassPath.ClassInfo> classInfoSet = cp.getTopLevelClasses(path);
        StringBuilder skillsLoaded = new StringBuilder(skillTypeName + ": ");

        Map<String, Double> costs = new HashMap<>();
        List<CompletableFuture<Void>> voids = new ArrayList<>();
        for(ClassPath.ClassInfo info : classInfoSet) {
            Class<?> skillClass = Class.forName(info.getName());

            if(!skillClass.isAnnotationPresent(SkillMetadata.class)) {
                PodcrashSpigot.debugLog("Skipping " + info.getName());
                continue;
            }
            Skill skill = (Skill) ReflectionUtil.constructor(skillClass);
            if(skill == null) throw new RuntimeException("skill cannot be null! current at: " + info.getName());
            SkillMetadata annot = skillClass.getAnnotation(SkillMetadata.class);
            SkillType skillType = annot.skillType();
            InvType invType = annot.invType();
            int skillID = annot.id();
            if(idDataMap.keySet().contains(skillID)) {
                String errMessage = "two skills cannot have the same two ids!\n Skills in conflict: ";
                errMessage += skill + "\n" + SkillInfo.getSkill(skillID);

                throw new IllegalStateException(errMessage);
            }
            SkillData data = addSkill(skillID, skillType, invType, skill);
            voids.add(data.requestDescription());
            skillsLoaded.append(skill.getName()).append(" ");

            costs.put(skill.getName(), annot.cost());
        }
        System.out.println(skillsLoaded.toString());

        EconomyTable eco = TableOrganizer.getTable(DataTableType.ECONOMY);
        eco.putItem(costs);
        try {
            CompletableFuture.allOf(voids.toArray(new CompletableFuture[0]))
                .get(5000, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }
    private static SkillData addSkill(int skillID, SkillType skillType, InvType invType, Skill skill, double price) {
        //TODO: put more of this information in the annotations.
        SkillData data = new SkillData(skill, skillID, skill.getName(), invType, skillType, price);
        skillData.add(data);
        idDataMap.put(skillID, data);

        return data;
    }

    private static SkillData addSkill(int skillID, SkillType skillType, InvType invType, Skill skill) {
        //TODO: put more of this information in the annotations.
        SkillData data = new SkillData(skill, skillID, skill.getName(), invType, skillType, 1500);
        skillData.add(data);
        idDataMap.put(skillID, data);

        return data;
    }

    public static int getSkillID(Skill skill) {
        for(SkillData data : skillData) {
            if(data.getName().equalsIgnoreCase(skill.getName()))
                return data.getId();
        }
        return -1;
    }

    public static SkillData getSkill(int id) {
        SkillData data;
        if((data = idDataMap.get(id)) == null) {
            for (SkillData data4 : skillData) {
                if(id == data4.getId()) {
                    data = data4;
                    break;
                }
            }
        }
        return data;
    }

    public static SkillData getSkillFromStrippedName(String strippedName) {
        for (SkillData data : skillData) {
            if (ChatUtil.strip(data.getName()).equals(strippedName)) {
                return data;
            }
        }
        return null;
    }
    /**
     * Used for lazy iteration
     * @param skillType
     * @param skillConsumer
     */
    public static void skillsConsumer(SkillType skillType, Consumer<SkillData> skillConsumer) {
        for(SkillData data : skillData)
            if(data.getSkillType() == skillType)
                skillConsumer.accept(data);
    }
    public static void invsConsumer(InvType invType, Consumer<SkillData> skillConsumer) {
        for(SkillData data : skillData)
            if(data.getInvType() == invType)
                skillConsumer.accept(data);
    }
    public static void skillInvConsumer(SkillType skillType, InvType invType, Consumer<SkillData> skillConsumer) {
        for(SkillData data : skillData)
            if(data.getInvType() == invType && data.getSkillType() == skillType)
                skillConsumer.accept(data);
    }

    public static List<SkillData> getSkills(SkillType skillType) {
        if(skillTypeCache.get(skillType) == null) cacheSkillType(skillType);
        List<SkillData> datas = new ArrayList<>();
        for(int index : skillTypeCache.get(skillType))
            datas.add(skillData.get(index));
        return datas;
    }
    public static List<SkillData> getSkills(InvType invType) {
        if(invTypeCache.get(invType) == null) cacheInvType(invType);
        List<SkillData> datas = new ArrayList<>();
        for(int index : invTypeCache.get(invType))
            datas.add(skillData.get(index));
        return datas;
    }

    private static void cacheSkillType(SkillType skillType) {
        List<Integer> intList = new ArrayList<>();
        for(int i = 0, size = skillData.size(); i < size; i++) {
            SkillData data = skillData.get(i);
            if(data.getSkillType() == skillType)
                intList.add(i);
        }
        int[] ids = new int[intList.size()];
        for(int i = 0; i < ids.length; i++) ids[i] = intList.get(i);
        skillTypeCache.put(skillType, ids);
    }
    private static void cacheInvType(InvType invType) {
        List<Integer> intList = new ArrayList<>();
        for(int i = 0, size = skillData.size(); i < size; i++) {
            SkillData data = skillData.get(i);
            if(data.getInvType() == invType)
                intList.add(i);
        }
        int[] ids = new int[intList.size()];
        for(int i = 0; i < ids.length; i++) ids[i] = intList.get(i);
        invTypeCache.put(invType, ids);
    }

    public static List<SkillData> getSkillData() {
        return skillData;
    }

    public static SkillData getSkillData(Skill skill) {
        for(SkillData data : skillData) {
            if(data.getName().equalsIgnoreCase(skill.getName()))
                return data;
        }
        return null;
    }
}
