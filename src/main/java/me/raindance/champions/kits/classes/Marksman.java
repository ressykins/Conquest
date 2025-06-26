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

public class Marksman extends ChampionsPlayer {

    public Marksman(Player player, List<Skill> skills) {
        super(player, 45);
        this.skills = new HashSet<>(skills);
        setSound(new SoundWrapper("random.bow", 0.95F, 126));
        this.armor = new Material[]{Material.LEATHER_BOOTS, Material.LEATHER_LEGGINGS, Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET};
    }

    public SkillType getType() {
        return SkillType.Marksman;
    }


    @Override
    public boolean equip() {
        if(!super.equip()) return false;
        EntityEquipment equipment = getPlayer().getEquipment();
        colorRed(equipment.getBoots());
        colorRed(equipment.getHelmet());
        colorRed(equipment.getChestplate());
        colorRed(equipment.getLeggings());
        return true;
    }

    private void colorRed(ItemStack leatherArmor) {
        LeatherArmorMeta meta = (LeatherArmorMeta) leatherArmor.getItemMeta();
        meta.setColor(Color.RED);

        leatherArmor.setItemMeta(meta);
    }
}
