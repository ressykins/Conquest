package me.raindance.champions.kits.skills.vanguard;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.util.PacketUtil;

import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.skilltypes.Instant;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;

@SkillMetadata(id = 806, skillType = SkillType.Vanguard, invType = InvType.AXE)
public class Rally extends Instant implements ICooldown {
    private final double radiusSquared = 5 * 5;
    @Override
    public float getCooldown() {
        return 15;
    }

    @Override
    public String getName() {
        return "Rally";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.AXE;
    }

    @Override
    protected void doSkill(PlayerEvent event, Action action) {
        if(action != Action.RIGHT_CLICK_BLOCK  && action != Action.RIGHT_CLICK_AIR) return;
        if(onCooldown()) return;

        WrapperPlayServerWorldParticles packet = ParticleGenerator.createParticle(EnumWrappers.Particle.NOTE, 4);
        // getPlayer().sendMessage(getUsedMessage());
        packet.setLocation(getPlayer().getEyeLocation());
        PacketUtil.asyncSend(packet, getPlayers());
        
        getPlayer().sendMessage(getUsedMessage());
        setLastUsed(System.currentTimeMillis());
        Location currentLoc = getPlayer().getLocation();
        SoundPlayer.sendSound(currentLoc, "mob.horse.gallop", 0.75F, 1);
        for(Player other : getPlayers()) {
            if(other != getPlayer() //In lobby, isAlly may not work properly
                    && !isAlly(other)) continue;
            Location otherLoc = other.getLocation();
            double distSquared = currentLoc.distanceSquared(otherLoc);
            if(distSquared > radiusSquared) continue;
            StatusApplier.getOrNew(other).applyStatus(Status.SPEED, 5, 1, true, true);
        }
    }
}
