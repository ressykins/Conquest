package me.raindance.champions.kits.skills.duelist;


import com.comphenix.protocol.wrappers.EnumWrappers;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DeathApplyEvent;
import com.podcrash.api.events.game.GameResurrectEvent;
import com.podcrash.api.events.game.GameStartEvent;
import com.podcrash.api.kits.iskilltypes.action.IPassiveTimer;
import com.podcrash.api.kits.skilltypes.Passive;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.SkillType;
import me.raindance.champions.kits.enums.InvType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Random;

@SkillMetadata(id = 311, skillType = SkillType.Duelist, invType = InvType.SECONDARY_PASSIVE)
public class Vitality extends Passive implements IPassiveTimer {
    // private long timeOfLastCancel;
    private Random rand;
    // private boolean tierTwoEnabled = false;
    // private final int tierTwoTime = 45; //SECONDS till tier 2 health boost
    // private boolean shouldEnableTier2 = false;

    private int extraHeart = 0;

    @Override
    public String getName() {
        return "Vitality";
    }

    @Override
    public void init() {
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRespawn(GameResurrectEvent event) {
        if (event.getWho() != getPlayer()) return;
        start();
    }

    @Override
    public void start() {
        getPlayer().setMaxHealth(20);
        getPlayer().setHealth(20);
        extraHeart = 0;

        // getPlayer().setMaxHealth(20);
        // timeOfLastCancel = System.currentTimeMillis();
        // double health = getPlayer().getHealth();
        // getPlayer().setMaxHealth(22);
        // getPlayer().setHealth(health + 2);
        // shouldEnableTier2 = true;

        // TimeHandler.delayTime(20 * tierTwoTime, () -> {
        //     if (System.currentTimeMillis() - timeOfLastCancel < 20 * tierTwoTime) return;
        //     if (!shouldEnableTier2) return;
        //     double health1 = getPlayer().getHealth();
        //     getPlayer().setMaxHealth(24);
        //     getPlayer().setHealth(health1 + 2);
        // });
    }

    
    @EventHandler
    public void kill(DeathApplyEvent event) {
        if(event.getAttacker() != getPlayer()) return;
        double health = getPlayer().getHealth();
        double maxHealth = getPlayer().getMaxHealth();
        if (extraHeart < 3) {
            getPlayer().setMaxHealth(maxHealth + 2);
            getPlayer().setHealth(health + 2);
            extraHeart++;
            WrapperPlayServerWorldParticles packet = ParticleGenerator.createParticle(getPlayer().getLocation().toVector(), EnumWrappers.Particle.HEART,
                    3, rand.nextFloat(), 0.9f, rand.nextFloat());
            getPlayer().getWorld().getPlayers().forEach(p -> ParticleGenerator.generate(p, packet));
        }
        else {
            StatusApplier.getOrNew(getPlayer()).applyStatus(Status.REGENERATION, 10, 0);
        }
    }

    @Override
    public void stop() {
        // shouldEnableTier2 = false;
        getPlayer().setMaxHealth(20);
    }

    @EventHandler
    public void gameStart(GameStartEvent event) {
        start();
    }

    @EventHandler
    public void onDeath(DeathApplyEvent event) {
        // if (event.getPlayer() == getPlayer()) timeOfLastCancel = System.currentTimeMillis();
    }

}