package me.raindance.champions.kits.skills.warden;

import com.podcrash.api.damage.Cause;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.kits.Skill;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.util.VectorUtil;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;

@SkillMetadata(id = 904, skillType = SkillType.Warden, invType = InvType.SWORD)
public class DefensiveStance extends Skill {
    private double damageReduction = (1 - 0.8);
    @Override
    public String getName() {
        return "Defensive Stance";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.SWORD;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void hit(DamageApplyEvent event) {
        if ((event.getCause() == Cause.MELEE || event.getCause() == Cause.PROJECTILE)){
            if(event.getAttacker() == getPlayer()) {
                if(System.currentTimeMillis() - getLastUsed() < 1000L) {
                    event.setCancelled(true);
                }
            }
            if ((event.getVictim() == getPlayer() && getPlayer().isBlocking())) {
                Vector damagerYaw = event.getAttacker().getLocation().getDirection();
                Vector victimYaw = event.getVictim().getLocation().getDirection();
                /*
                Bukkit.broadcastMessage(String.format("damagerYaw: %f victimYaw: %f", damagerYaw, victimYaw));
                Bukkit.broadcastMessage(String.format("R: damagerYaw: %f victimYaw: %f", Math.toDegrees(damagerYaw), Math.toRadians(victimYaw)));
                */
                if (!VectorUtil.angleIsAround(damagerYaw, victimYaw, 135)) {
                    SoundPlayer.sendSound(event.getVictim().getLocation(), "mob.zombie.metal", 0.75F, 126);
                    event.setModified(true);
                    event.setDamage(event.getDamage() * damageReduction);
                    event.setDoKnockback(false);
                    this.setLastUsed(System.currentTimeMillis());
                }
            }
        }


    }
}
