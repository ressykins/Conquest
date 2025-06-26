package me.raindance.champions.kits.skills.warden;

import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.skilltypes.Instant;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.time.TimeHandler;
import com.podcrash.api.time.resources.TimeResource;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;

import java.util.Random;

@SkillMetadata(id = 908, skillType = SkillType.Warden, invType = InvType.AXE)
public class HoldPosition extends Instant implements TimeResource, ICooldown {
    private boolean active = false;
    @Override
    public float getCooldown() {
        return 14;
    }

    @Override
    public String getName() {
        return "Hold Position";
    }


    @Override
    public ItemType getItemType() {
        return ItemType.AXE;
    }

    @Override
    protected void doSkill(PlayerEvent event, Action action) {
        if (!rightClickCheck(action) || !isHolding()) return;
        if (onCooldown()) return;

        int duration = 4;
        StatusApplier applier = StatusApplier.getOrNew(getPlayer());
        applier.applyStatus(Status.WEAKNESS, duration, 1);
        applier.applyStatus(Status.RESISTANCE, duration, 2);
        applier.applyStatus(Status.SLOW, duration, 1);
        applier.applyStatus(Status.GROUND, duration, 1);
        TimeHandler.repeatedTime(1, 0, this);
        setLastUsed(System.currentTimeMillis());
        SoundPlayer.sendSound(getPlayer().getLocation(), "mob.endermen.scream", 0.75F, 10, getPlayers());
        active = true;
        TimeHandler.repeatedTime(1,0, this);

        getPlayer().sendMessage(getUsedMessage());
    }

    @Override
    public void task() {
        Random rand = new Random();
        WrapperPlayServerWorldParticles particle = ParticleGenerator.createParticle(getPlayer().getLocation().toVector(),
                EnumWrappers.Particle.SPELL_MOB, new int[]{1, 1, 1, 0}, 5,
                rand.nextFloat() / 2f, 0.25f + (rand.nextFloat() - 0.15f), rand.nextFloat() / 2f);
        getPlayer().getWorld().getPlayers().forEach(player -> ParticleGenerator.generate(player, particle));
    }

    @Override
    public boolean cancel() {
        return (System.currentTimeMillis() - getLastUsed() >= 4000L) || getGame().isRespawning(getPlayer());
    }

    @Override
    public void cleanup() {
        active = false;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void damage(DamageApplyEvent e){
        if(e.isCancelled()) return;
        if(active && e.getVictim() == getPlayer()) {
            e.setModified(true);
            e.setDoKnockback(false);
        }
    }
}
