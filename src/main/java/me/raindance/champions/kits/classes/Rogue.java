package me.raindance.champions.kits.classes;

import com.podcrash.api.sound.SoundWrapper;
import com.podcrash.api.kits.KitPlayer;
import com.podcrash.api.kits.Skill;
import me.raindance.champions.kits.SkillType;
import me.raindance.champions.kits.ChampionsPlayer;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.HashSet;
import java.util.List;

public class Rogue extends ChampionsPlayer {
    public Rogue(Player player, List<Skill> skills) {
        super(player, 40);
        this.skills = new HashSet<>(skills);
        setSound(new SoundWrapper("random.bow", 0.95F, 126));
        this.armor = new Material[]{Material.LEATHER_BOOTS, Material.LEATHER_LEGGINGS, Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET};
    }

    @Override
    public SkillType getType() {
        return SkillType.Rogue;
    }

    @Override
    public boolean equip() {
        if(!super.equip()) return false;
        EntityEquipment equipment = getPlayer().getEquipment();
        for(ItemStack armor : equipment.getArmorContents()) {
            colorWhite(armor);
        }
        return true;
    }

    //TODO: itemstackutil methods for this
    private void colorWhite(ItemStack leatherArmor) {
        LeatherArmorMeta meta = (LeatherArmorMeta) leatherArmor.getItemMeta();
        meta.setColor(Color.WHITE);

        leatherArmor.setItemMeta(meta);
    }
}
