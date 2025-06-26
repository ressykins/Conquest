package me.raindance.champions.kits.skills.vanguard;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DeathApplyEvent;
import com.podcrash.api.util.EntityUtil;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Passive;
import com.podcrash.api.sound.SoundPlayer;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;


@SkillMetadata(id = 805, skillType = SkillType.Vanguard, invType = InvType.PRIMARY_PASSIVE)
public class Oppression extends Passive {
    public Oppression() {
        super();
    }
    @Override
    public String getName() {
        return "Oppression";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @EventHandler
    public void kill(DeathApplyEvent event) {
        if(event.getAttacker() != getPlayer()) return;

        SoundPlayer.sendSound(getPlayer().getLocation(), "mob.wither.spawn", 0.9F, 63);
        Random rand = new Random();
        WrapperPlayServerWorldParticles particle = ParticleGenerator.createParticle(getPlayer().getLocation().toVector(),
                EnumWrappers.Particle.VILLAGER_ANGRY, 5,
                rand.nextFloat() / 2f, 0.25f + (rand.nextFloat() - 0.15f), rand.nextFloat() / 2f);
        getPlayer().getWorld().getPlayers().forEach(player -> ParticleGenerator.generate(player, particle));

        Location location = getPlayer().getLocation();
        for (Player victim : getPlayers()) {
            if(victim == getPlayer() || isAlly(victim)) continue;
            if(victim.getLocation().distanceSquared(location) <= 36) {
                if (EntityUtil.isBelow(victim, 0.5)) {
                      StatusApplier.getOrNew(victim).applyStatus(Status.GROUND, 3, 0);
                }
                StatusApplier.getOrNew(victim).applyStatus(Status.WEAKNESS, 3, 0);
            }
        }
    }
}
