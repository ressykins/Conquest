package me.raindance.champions.kits.classes;

import com.podcrash.api.sound.SoundWrapper;
import com.podcrash.api.kits.KitPlayer;
import com.podcrash.api.kits.Skill;
import me.raindance.champions.kits.SkillType;
import me.raindance.champions.kits.ChampionsPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;

public class Warden extends ChampionsPlayer {

    public Warden(Player player, List<Skill> skills) {
        super(player, 60);
        this.skills = new HashSet<>(skills);
        setSound(new SoundWrapper("mob.blaze.hit", 0.95F, 57));
        this.armor = new Material[]{Material.IRON_BOOTS, Material.IRON_LEGGINGS, Material.IRON_CHESTPLATE, Material.IRON_HELMET};
    }

    public SkillType getType() {
        return SkillType.Warden;
    }
}