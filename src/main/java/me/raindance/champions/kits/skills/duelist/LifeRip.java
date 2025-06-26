package me.raindance.champions.kits.skills.duelist;

import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.sound.SoundPlayer;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;

import com.podcrash.api.kits.KitPlayer;
import com.podcrash.api.kits.KitPlayerManager;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Interaction;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;



@SkillMetadata(id = 313, skillType = SkillType.Duelist, invType = InvType.SWORD)
public class LifeRip extends Interaction {
    @Override
    public void doSkill(LivingEntity clickedEntity) {
        if(onCooldown()) return;
        if(isAlly(clickedEntity)) {return;}
        setLastUsed(System.currentTimeMillis());

        
        Player victim = (Player) clickedEntity;
        KitPlayer victimKitPlayer = KitPlayerManager.getInstance().getKitPlayer(victim);
        double targetMaxHP = victimKitPlayer.getHP() * 0.15;

        DamageApplier.damage(clickedEntity, getPlayer(), targetMaxHP, this, false);
        if(getPlayer().getMaxHealth() < getPlayer().getHealth() + targetMaxHP) {
            getPlayer().setHealth(getPlayer().getMaxHealth());
        }
        else {
            getPlayer().setHealth(getPlayer().getHealth() + targetMaxHP);
        }

        SoundPlayer.sendSound(clickedEntity.getLocation(), "mob.wither.hurt", 0.9F, 63);

        landed();
    }

    @Override
    public float getCooldown() {
        return 10;
    }

    @Override
    public String getName() {
        return "Life Rip";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.SWORD;
    }
}
