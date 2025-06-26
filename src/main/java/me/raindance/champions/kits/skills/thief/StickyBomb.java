package me.raindance.champions.kits.skills.thief;

import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.packetwrapper.abstractpackets.AbstractPacket;
import com.packetwrapper.abstractpackets.WrapperPlayServerEntityStatus;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.events.DeathApplyEvent;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Interaction;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.time.TimeHandler;
import com.podcrash.api.time.resources.TimeResource;
import com.podcrash.api.util.PacketUtil;
import com.podcrash.api.world.BlockUtil;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

@SkillMetadata(id = 708, skillType = SkillType.Thief, invType = InvType.SWORD)
public class StickyBomb extends Interaction implements TimeResource {
    private final int damage = 8; // Damage dealt on detonation
    private int i = 0;
    private double chargeTime = 3;          // How long it takes to charge, in seconds.
    private Player targetPlayer;
    private boolean active;

    @Override
    public void doSkill(LivingEntity victim) {
        // if(isAlly(victim)) return;
        targetPlayer = (Player) victim;

        // Notify the player that the bomb is attached
        targetPlayer.sendMessage(String.format("%sSticky Bomb> %s%s%s stuck a sticky bomb on you!",
            ChatColor.BLUE,
            ChatColor.YELLOW,
            getPlayer().getName(),
            ChatColor.GRAY));

        // Play sound effect for attaching the bomb
        targetPlayer.getWorld().playSound(targetPlayer.getLocation(), Sound.SLIME_WALK, 1.0f, 1.0f);

        WrapperPlayServerEntityStatus packet = new WrapperPlayServerEntityStatus();
        packet.setEntityId(targetPlayer.getEntityId());
        packet.setEntityStatus(WrapperPlayServerEntityStatus.Status.ENTITY_HURT);

        AbstractPacket packet2 = ParticleGenerator.createBlockEffect(targetPlayer.getLocation().toVector(), Material.SLIME_BLOCK.getId());
        for (Player player : getPlayers()) {
            packet.sendPacket(player);
            packet2.sendPacket(player);
        }

        active = true;
        TimeHandler.repeatedTime(10, 0, this);
        setLastUsed(System.currentTimeMillis());
        landed();
    }

    @Override
    public float getCooldown() {
        return 8;
    }

    @Override
    public String getName() {
        return "Sticky Bomb";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.SWORD;
    }

    @Override
    public void task() {
        i += 10;
        
        AbstractPacket bleedPacket = ParticleGenerator.createBlockEffect(targetPlayer.getLocation(), Material.SLIME_BLOCK.getId());
        PacketUtil.asyncSend(bleedPacket, getPlayers());
    }

    @Override
    public boolean cancel() {
        return (i >= (chargeTime * 20)) || getGame().isRespawning(getPlayer()) || !active;
    }

    @Override
    public void cleanup() {
        if(i >= (chargeTime * 20) && active) {
            detonateStickyBomb();
            i = 0;
        }
    }

    private void detonateStickyBomb() {
        WrapperPlayServerWorldParticles explosion = ParticleGenerator.createParticle(null, EnumWrappers.Particle.EXPLOSION_HUGE, 1, 0,0,0);
        explosion.setLocation(targetPlayer.getLocation());
        PacketUtil.syncSend(explosion, getPlayers());

        SoundPlayer.sendSound(getPlayer().getLocation(), "random.explode", 0.9F, 70);

        for(Player player : BlockUtil.getPlayersInArea(targetPlayer.getLocation(), 4, getPlayers())) {
            if (player == getPlayer() || isAlly(player)) continue;
            DamageApplier.damage(player, getPlayer(), damage, this, false);
        }

        active = false;
    }

    // Event listener for player death to detonate the bomb if the target player dies
    @EventHandler
    public void kill(DeathApplyEvent event) {
        // If the target player has a sticky bomb attached and is dying, detonate the bomb
        if (event.getPlayer().equals(targetPlayer) && active) {
            detonateStickyBomb();
        }
    }
}
