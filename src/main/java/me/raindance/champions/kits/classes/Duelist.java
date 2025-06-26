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

public class Duelist extends ChampionsPlayer {

    public Duelist(Player player, List<Skill> skills) {
        super(player, 55);
        this.skills = new HashSet<>(skills);
        setSound(new SoundWrapper("random.break", 0.95F, 115));
        this.armor = new Material[]{Material.LEATHER_BOOTS, Material.LEATHER_LEGGINGS, Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET};
    }

    public SkillType getType() {
        return SkillType.Duelist;
    }

    @Override
    public boolean equip() {
        if(!super.equip()) return false;
        EntityEquipment equipment = getPlayer().getEquipment();
        for(ItemStack armor : equipment.getArmorContents()) {
            colorBlack(armor);
        }
        return true;
    }

    private void colorBlack(ItemStack leatherArmor) {
        LeatherArmorMeta meta = (LeatherArmorMeta) leatherArmor.getItemMeta();
        meta.setColor(Color.NAVY);

        leatherArmor.setItemMeta(meta);
    }
}