package me.raindance.champions.kits.skills.rogue;

import com.podcrash.api.damage.Cause;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.events.DeathApplyEvent;
import com.podcrash.api.events.game.GameResurrectEvent;

import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Passive;
import com.podcrash.api.sound.SoundPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.*;
import org.bukkit.event.EventPriority;

@SkillMetadata(id = 602, skillType = SkillType.Rogue, invType = InvType.PRIMARY_PASSIVE)
public class HeadHunter extends Passive {
    private int bonus = 0;
    // private long lastHit = 0;
    // private String affectedPlayer;


    @EventHandler(priority = EventPriority.LOW)
    public void onHit(DamageApplyEvent event) {
        if (event.isCancelled()) return;
        if(event.getCause() != Cause.MELEE) return;
        if(event.getAttacker() != getPlayer()) return;
        LivingEntity victim = event.getVictim();
        event.setDamage(event.getDamage() + bonus);
        event.setModified(true);
        event.addSource(this);
        SoundPlayer.sendSound(victim.getLocation(), "note.hat", 0.9F, 110);


        // if (event.isCancelled()) return;
        // // cba with non players
        // if(event.getCause() != Cause.MELEE) return;
        // if(event.getAttacker() != getPlayer()) return;
        // if (System.currentTimeMillis() - lastHit > 2 * 1000) reset();
        // event.addSource(this);
        // lastHit = System.currentTimeMillis();
        // LivingEntity victim = event.getVictim();
        // event.setDamage(event.getDamage() + bonus);
        // event.setModified(true);
        // SoundPlayer.sendSound(victim.getLocation(), "note.hat", 0.9F, 110);
        // if (bonus < 2) {
        //     if (bonus == 0 || affectedPlayer == null || affectedPlayer.equals(victim.getName())) {
        //         affectedPlayer = victim.getName();
        //         bonus += 0.5;
        //     } else reset();
        // }
    }

    public void reset() {
        bonus = 0;
    }

    @EventHandler
    public void kill(DeathApplyEvent event) {
        if(event.getAttacker() != getPlayer()) return;
        StatusApplier.getOrNew(getPlayer()).applyStatus(Status.ABSORPTION, 30, 0);

        if(bonus < 3) {
            bonus++;
            event.getAttacker().sendMessage(String.format("%sRogue> %sYou have collected a Head. Heads: %s", ChatColor.BLUE, ChatColor.GRAY, bonus));
        }
    }

    @EventHandler
    public void onDeath(DeathApplyEvent event) {
        if (event.getPlayer() == getPlayer()) event.getPlayer().sendMessage(String.format("%sRogue> %sYou lost all your Heads...", ChatColor.BLUE, ChatColor.GRAY));
    }

    @Override
    public String getName() {
        return "Head Hunter";
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRespawn(GameResurrectEvent event) {
        if (event.getWho() != getPlayer()) return;
        reset();
    }
}
