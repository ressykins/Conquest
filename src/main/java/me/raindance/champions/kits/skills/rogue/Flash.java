package me.raindance.champions.kits.skills.rogue;

import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.events.DeathApplyEvent;

import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.ICharge;
import com.podcrash.api.kits.iskilltypes.action.IPassiveTimer;
import com.podcrash.api.kits.skilltypes.Instant;
import com.podcrash.api.time.TimeHandler;
import com.podcrash.api.util.PacketUtil;
import com.podcrash.api.world.BlockUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.util.Vector;

@SkillMetadata(id = 605, skillType = SkillType.Rogue, invType = InvType.AXE)
public class Flash extends Instant implements ICharge, IPassiveTimer {
    //rate of charges still not implemented
    private final int MAX_CHARGES = 5;
    private int delay = 1;
    private int charges = MAX_CHARGES;
    private long lastTimeHit = 0;


    @Override
    public void start() {
        if (getPlayer() != null) TimeHandler.repeatedTimeAsync(20L, 0L, this);
    }


    protected void doSkill(PlayerEvent e, Action action) {
        if (!(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        if (!rightClickCheck(action)) return;
        if (StatusApplier.getOrNew(getPlayer()).has(Status.SLOW)) {
            getPlayer().sendMessage(getCannotUseWhileMessage("Slowed"));
            return;
        }
        if (StatusApplier.getOrNew(getPlayer()).has(Status.GROUND)) {
            getPlayer().sendMessage(getCannotUseWhileMessage("Grounded"));
            return;
        }
        if (StatusApplier.getOrNew(getPlayer()).has(Status.ROOTED)) {
            getPlayer().sendMessage(getCannotUseWhileMessage("Rooted"));
            return;
        }
        double distance = 34D;
        Player player = getPlayer();

        //if player still has charges, then use the skill
        if (getCurrentCharges() > 0) {
            Location location = player.getLocation().add(new Vector(0, 0.1, 0));
            Vector increment = player.getLocation().getDirection().normalize().multiply(0.2);
            for (int i = 0; i < distance; i++) {
                if (!BlockUtil.isSafe(location)) {
                    location.subtract(increment.multiply(2));
                    break;
                } else {
                    WrapperPlayServerWorldParticles packet = ParticleGenerator.createParticle(location.clone().add(0, 1, 0).toVector(), EnumWrappers.Particle.FIREWORKS_SPARK, 2, 0, 0, 0);
                    PacketUtil.syncSend(packet, getPlayers());
                    location.add(increment);
                }
            }
            player.teleport(location);
            player.setFallDistance(0);
            player.getWorld().playSound(player.getLocation(), Sound.WITHER_SHOOT, 0.4f, 1.2f);
            player.getWorld().playSound(player.getLocation(), Sound.SILVERFISH_KILL, 1f, 1.6f);
            charges--;
            player.sendMessage(getCurrentChargeMessage());
        } else {
        //if the player is out of charges, don't let them use the skill
            player.sendMessage(getNoChargeMessage());
        }
    }

    @EventHandler
    public void hit(DamageApplyEvent event) {
        if(event.isCancelled()) return;
        /*
        if(event.getVictim() == getPlayer()) {
            lastTimeHit = System.currentTimeMillis();
        }

         */
    }

    public void task() {
        addCharge();
    }

    public boolean cancel() {
        return false;
    }

    public void cleanup() {
        charges = 0;
    }

    public void addCharge() {
        if(System.currentTimeMillis() - lastTimeHit <= delay * 1000L) return;
        if (getCurrentCharges() < MAX_CHARGES && System.currentTimeMillis() - getLastUsed() >= 5000L) {
            charges++;
            this.getPlayer().sendMessage(getCurrentChargeMessage());
            setLastUsed(System.currentTimeMillis());
        }
    }

    public int getCurrentCharges() {
        return charges;
    }

    public int getMaxCharges() {
        return MAX_CHARGES;
    }

    @Override
    public String getName() {
        return "Flash";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.AXE;
    }

    @EventHandler
    public void kill(DeathApplyEvent event) {
        if(event.getAttacker() != getPlayer()) return;
        addCharge();
    }
}
