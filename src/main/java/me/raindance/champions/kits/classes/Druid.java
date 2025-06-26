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

public class Druid extends ChampionsPlayer {
    public Druid(Player player, List<Skill> skills) {
        super(player, 50);
        this.skills = new HashSet<>(skills);
        setSound(new SoundWrapper("random.break", 0.95F, 90));
        this.armor = new Material[]{Material.LEATHER_BOOTS, Material.LEATHER_LEGGINGS, Material.LEATHER_CHESTPLATE, Material.LEAVES};
    }

    @Override
    public void effects() {
        super.effects();
        this.setUsesEnergy(true, 200);
        getEnergyBar().toggleRegen(true);
        double ratePerSecond = 10D;
        double ratePerTick = ratePerSecond / 20D;
        getEnergyBar().setNaturalRegenRate(ratePerTick);
    }

    @Override
    public SkillType getType() {
        return SkillType.Druid;
    }

    @Override
    public boolean equip() {
        if(!super.equip()) return false;
        EntityEquipment equipment = getPlayer().getEquipment();
        for(ItemStack armor : equipment.getArmorContents()) {
            if(armor.getType() == Material.LEAVES) continue;
            colorGreen(armor);
        }
        return true;
    }

    //TODO: itemstackutil methods for this
    private void colorGreen(ItemStack leatherArmor) {
        LeatherArmorMeta meta = (LeatherArmorMeta) leatherArmor.getItemMeta();
        meta.setColor(Color.GREEN);

        leatherArmor.setItemMeta(meta);
    }
}
