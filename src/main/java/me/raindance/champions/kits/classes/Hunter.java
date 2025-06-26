package me.raindance.champions.kits.classes;

import com.podcrash.api.sound.SoundWrapper;
import com.podcrash.api.kits.Skill;
import me.raindance.champions.kits.SkillType;
import me.raindance.champions.kits.ChampionsPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;

public class Hunter extends ChampionsPlayer {

    public Hunter(Player player, List<Skill> skills) {
        super(player, 50);
        this.skills = new HashSet<>(skills);
        setSound(new SoundWrapper("random.break", 0.95F, 115));
        this.armor = new Material[]{Material.CHAINMAIL_BOOTS, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_HELMET};
    }

    public SkillType getType() {
        return SkillType.Hunter;
    }
}
