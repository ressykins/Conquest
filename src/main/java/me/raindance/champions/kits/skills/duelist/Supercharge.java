package me.raindance.champions.kits.skills.duelist;

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
import com.podcrash.api.kits.skilltypes.Drop;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.time.resources.EntityParticleResource;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerDropItemEvent;

@SkillMetadata(id = 301, skillType = SkillType.Duelist, invType = InvType.DROP)
public class Supercharge extends Drop implements ICooldown {
    private int slowLevel = 1;
    private int slowDuration = 3;
    private long selfTime;
    private boolean use;

    @Override
    public float getCooldown() {
        return 8;
    }

    @Override
    public String getName() {
        return "Supercharge";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    private class BullsChargeParticle extends EntityParticleResource {
        public BullsChargeParticle() {
            super(getPlayer(), ParticleGenerator.createParticle(null, EnumWrappers.Particle.CRIT, 2,0.2F,1F,0.2F), null);
        }

        @Override
        public boolean cancel() {
            return !use || System.currentTimeMillis() - getLastUsed() >= 3 * 1000L || getPlayer().isDead();
        }
    }

    @Override
    public boolean drop(PlayerDropItemEvent e) {
        if (onCooldown()) return false;
        getPlayer().sendMessage(getUsedMessage());
        StatusApplier.getOrNew(getPlayer()).applyStatus(Status.SPEED, 3, 1);
        selfTime = System.currentTimeMillis();
        this.setLastUsed(System.currentTimeMillis());
        SoundPlayer.sendSound(getPlayer().getLocation(), "mob.endermen.scream", 0.75F, 10);
        use = true;
        new BullsChargeParticle().run(1, 1);
        return true;
    }

    @EventHandler(
            priority = EventPriority.MONITOR
    )
    public void hit(DamageApplyEvent e) {
        if (e.isCancelled()) return;
        if (use && e.getAttacker() == getPlayer()) {
            if(e.getVictim() instanceof  Player) {
                if (1000 * 3 >= (System.currentTimeMillis() - selfTime)) {
                    Player victim = (Player) e.getVictim();
                    e.addSource(this);
                    StatusApplier.getOrNew(victim).applyStatus(Status.SLOW, slowDuration, slowLevel);
                    StatusApplier.getOrNew(getPlayer()).removeVanilla(Status.SPEED);
                    SoundPlayer.sendSound(getPlayer().getLocation(), "random.break", 0.75F, 250);
                    SoundPlayer.sendSound(getPlayer().getLocation(), "mob.endermen.scream", .75F, 20);

                    getPlayer().sendMessage(getUsedMessage(e.getVictim()));
                    e.setDoKnockback(false);
                    use = false;
                }
            }
        }
    }
}
