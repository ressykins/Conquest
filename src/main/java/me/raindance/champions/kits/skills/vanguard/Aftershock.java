package me.raindance.champions.kits.skills.vanguard;

import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.time.TimeHandler;
import com.podcrash.api.time.resources.TimeResource;
import com.podcrash.api.util.PacketUtil;
import com.podcrash.api.world.BlockUtil;
import com.podcrash.api.kits.enums.ItemType;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.skilltypes.Instant;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.SkillType;
import me.raindance.champions.kits.enums.InvType;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;

import java.util.Random;

@SkillMetadata(id = 810, skillType = SkillType.Vanguard, invType = InvType.AXE)
public class Aftershock extends Instant implements ICooldown, TimeResource {
    private float cooldown = 13;
    private double chargeTime = 3;          // How long it takes to charge, in seconds.
    private int radius = 5;
    // private double launchYValue = 0.65;
    private double damage = 9;
    private float duration = 3f;

    // private final Random random = new Random();
    private int i = 0;

    @Override
    public float getCooldown() {
        return cooldown;
    }

    @Override
    protected void doSkill(PlayerEvent event, Action action) {
        if(!rightClickCheck(action)) return;
        SoundPlayer.sendSound(getPlayer().getLocation(), "creeper.primed", 1.5F, 63);
        setLastUsed(System.currentTimeMillis());
        TimeHandler.repeatedTime(20, 0, this);
        StatusApplier.getOrNew(getPlayer()).applyStatus(Status.ABSORPTION, duration, 0, false, false);
    }

    @Override
    public String getName() {
        return "Aftershock";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.AXE;
    }

    @Override
    public void task() {
        i += 20;
        
        Random rand = new Random();

        // Get the player's current location and move the particle effect upwards
        Location particleLocation = getPlayer().getLocation().add(0, 2.2, 0);
        
        // Create the particle effect
        WrapperPlayServerWorldParticles particle = ParticleGenerator.createParticle(
                particleLocation.toVector(),
                EnumWrappers.Particle.SMOKE_LARGE,
                5,
                rand.nextFloat() / 2f,
                0.25f + (rand.nextFloat() - 0.15f),
                rand.nextFloat() / 2f
        );
        
        // Send the particle effect to all players in the world
        getPlayer().getWorld().getPlayers().forEach(player -> ParticleGenerator.generate(player, particle));
        
    }

    @Override
    public boolean cancel() {
        return (i >= (chargeTime * 20)) || getGame().isRespawning(getPlayer());
    }

    @Override
    public void cleanup() {
        if(i >= (chargeTime * 20)) {
            WrapperPlayServerWorldParticles explosion = ParticleGenerator.createParticle(null, EnumWrappers.Particle.EXPLOSION_HUGE, 1, 0,0,0);
            explosion.setLocation(getPlayer().getLocation());
            PacketUtil.syncSend(explosion, getPlayers());

            SoundPlayer.sendSound(getPlayer().getLocation(), "random.explode", 0.9F, 70);

            for(Player player : BlockUtil.getPlayersInArea(getPlayer().getLocation(), radius, getPlayers())) {
                if (player == getPlayer() || isAlly(player)) continue;
                // player.setVelocity(player.getVelocity().setY(launchYValue));
                DamageApplier.damage(player, getPlayer(), damage, this, true);
                StatusApplier.getOrNew(player).applyStatus(Status.SLOW, duration, 1);
            }
            getPlayer().sendMessage(getUsedMessage());
            i = 0;
        }
    }
}
