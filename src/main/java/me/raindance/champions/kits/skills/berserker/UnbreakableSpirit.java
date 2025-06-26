package me.raindance.champions.kits.skills.berserker;

import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.util.PacketUtil;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.skilltypes.Instant;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;

@SkillMetadata(id = 109, skillType = SkillType.Berserker, invType = InvType.AXE)
public class UnbreakableSpirit extends Instant implements ICooldown {
    private boolean antikb = false;

    @Override
    public float getCooldown() {
        return 10;
    }

    @Override
    public String getName() {
        return "Unbreakable Spirit";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.AXE;
    }

    @Override
    protected void doSkill(PlayerEvent event, Action action) {
        if (!rightClickCheck(action) || onCooldown()) return;
        setLastUsed(System.currentTimeMillis());
        StatusApplier applier = StatusApplier.getOrNew(getPlayer());
        applier.getEffects().forEach(status -> {
            if (status.isNegative())
                applier.removeStatus(status);
        });
        SoundPlayer.sendSound(getPlayer().getLocation(), "mob.endermen.scream", 0.8F, 10);
        WrapperPlayServerWorldParticles packet = ParticleGenerator.createParticle(EnumWrappers.Particle.VILLAGER_ANGRY, 4);
        getPlayer().sendMessage(getUsedMessage());
        packet.setLocation(getPlayer().getEyeLocation());
        PacketUtil.asyncSend(packet, getPlayers());

        if (getChampionsPlayer().getEnergyBar().getEnergy() >= 4) {
            antikb = true;
        }
        else {
            antikb = false;
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void damage(DamageApplyEvent e){
        if(e.isCancelled()) return;
        if(e.getVictim() == getPlayer() && antikb && System.currentTimeMillis() - getLastUsed() < 2000L) {
            e.setModified(true);
            e.setDoKnockback(false);
        }
    }
}
