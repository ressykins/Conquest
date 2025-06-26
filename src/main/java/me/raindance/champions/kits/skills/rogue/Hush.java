package me.raindance.champions.kits.skills.rogue;

import com.packetwrapper.abstractpackets.AbstractPacket;
import com.podcrash.api.damage.Cause;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.util.PacketUtil;
import com.podcrash.api.world.BlockUtil;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.skilltypes.Passive;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.ArrayList;
import java.util.List;

/**
 *Night Blade
 * Class: Rogue
 * Type: Secondary Passive
 * If there is only 1 other player within 5 blocks of you, your next melee attack inflicts Blindness and Slowness III for 3 seconds.
 * Cooldown: 5 seconds.
 */
@SkillMetadata(id = 606, skillType = SkillType.Rogue, invType = InvType.SECONDARY_PASSIVE)
public class Hush extends Passive implements ICooldown {
    private float duration = 3;
    private int radius = 5;

    @Override
    public float getCooldown() {
        return 5;
    }

    @Override
    public String getName() {
        return "Hush";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void damage(DamageApplyEvent event) {
        // Cooldown and attacker check
        if(onCooldown() || event.getAttacker() != getPlayer() || isAlly(event.getVictim())) return;
        // Cause check (make sure it only procs off melee hits)
        if(event.getCause() != Cause.MELEE && event.getCause() != Cause.MELEESKILL) return;

        // Make sure we dont include respawning or spectators
        List<Player> toCount = new ArrayList<>();
        for (Player player : getGame().getBukkitPlayers()) {
            if (!getGame().isRespawning(player) && !getGame().isSpectating(player)) toCount.add(player);
        }

        List<Player> nearPlayers = BlockUtil.getPlayersInArea(getPlayer().getLocation(), radius, toCount);
        // proximity check: only activate this skill if it is a 1v1
        if (nearPlayers.size() == 2) {
            getPlayer().sendMessage(getUsedMessage(event.getVictim()));
            setLastUsed(System.currentTimeMillis());
            event.addSource(this);
            StatusApplier.getOrNew(event.getVictim()).applyStatus(Status.SILENCE, duration, 0);
            AbstractPacket shadowBreak = ParticleGenerator.createBlockEffect(event.getVictim().getEyeLocation(), Material.COAL_BLOCK.getId());
            event.getVictim().getWorld().playSound(event.getVictim().getLocation(), Sound.BAT_HURT, 1.0f, 1.5f);
            PacketUtil.asyncSend(shadowBreak, getPlayer().getWorld().getPlayers());
            event.setModified(true);
        }
    }
}
