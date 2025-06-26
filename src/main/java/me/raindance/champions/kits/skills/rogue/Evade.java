package me.raindance.champions.kits.skills.rogue;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.podcrash.api.damage.Cause;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.events.DeathApplyEvent;

import me.raindance.champions.Main;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.ICharge;
import com.podcrash.api.kits.iskilltypes.action.IPassiveTimer;
import com.podcrash.api.kits.skilltypes.Instant;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.time.TimeHandler;
import com.podcrash.api.time.resources.TimeResource;
import com.podcrash.api.util.MathUtil;
import com.podcrash.api.util.PacketUtil;
import com.podcrash.api.util.TitleSender;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.util.Vector;

@SkillMetadata(id = 604, skillType = SkillType.Rogue, invType = InvType.SWORD)
public class Evade extends Instant implements ICharge, IPassiveTimer {
    private boolean isEvading = false;
    private long time;
    private int charges = 2;
    private int chargeCooldown = 5;

    @Override
    public void task() {
        addCharge();
    }

    @Override
    public boolean cancel() {
        return false;
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void start() {
        runAsync(chargeCooldown * 20, 0);
    }

    @Override
    public void addCharge() {
        if(charges >= getMaxCharges()) return;
        charges++;
        getPlayer().sendMessage(getCurrentChargeMessage());
    }
    private void removeCharge() {
        if(charges == 0) return;
        charges--;
        getPlayer().sendMessage(getCurrentChargeMessage());
    }

    @Override
    public int getCurrentCharges() {
        return charges;
    }

    @Override
    public int getMaxCharges() {
        return 2;
    }

    private boolean hasCharges() {
        return charges > 0;
    }

    @Override
    protected void doSkill(PlayerEvent event, Action action) {
        if (!rightClickCheck(action)) return;
        if (StatusApplier.getOrNew(event.getPlayer()).has(Status.SLOW)) {
            event.getPlayer().sendMessage(getCannotUseWhileMessage("Slowed"));
            return;
        }
        if (StatusApplier.getOrNew(event.getPlayer()).has(Status.GROUND)) {
            event.getPlayer().sendMessage(getCannotUseWhileMessage("Grounded"));
            return;
        }
        if (StatusApplier.getOrNew(event.getPlayer()).has(Status.ROOTED)) {
            event.getPlayer().sendMessage(getCannotUseWhileMessage("Rooted"));
            return;
        }
        //if in roughly quarter of a second you right click again, then don't start it (which shouldn't be humanly possible)
        if(System.currentTimeMillis() - time <= 225) return;
        if (hasCharges()) {
            time = System.currentTimeMillis();
            isEvading = true;
            getPlayer().sendMessage(String.format("%sSkill> %sYou prepared to %s%s%s.",
                    ChatColor.BLUE, ChatColor.GRAY, ChatColor.GREEN, getName(), ChatColor.GRAY));
            TimeHandler.repeatedTimeAsync(1, 0, new ActiveEvade());
        } else this.getPlayer().sendMessage(getNoChargeMessage());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void hit(DamageApplyEvent event) {
        if (event.getCause() != Cause.MELEE || event.isCancelled()) return;
        LivingEntity player = event.getVictim();
        if (player == getPlayer() && isEvading) {
            LivingEntity damager = event.getAttacker();
            player.getWorld().playEffect(player.getLocation(), Effect.SMOKE, 5);
            isEvading = false;
            event.setCancelled(true);

            Location tp = d((Player) player, damager);
            player.sendMessage(getUsedMessage());
            damager.sendMessage(String.format("%sSkill> %s%s %sused %s%s%s.",
                    ChatColor.BLUE, ChatColor.YELLOW, player.getName(), ChatColor.GRAY, ChatColor.GREEN, getName(), ChatColor.GRAY));
            player.teleport(tp);
            SoundPlayer.sendSound(getPlayer(), "random.successful_hit", 0.8F, 20);

            player.setFallDistance(0);
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.instance, () -> this.setLastUsed(0L), 1L);
        }
    }

    private Location d(Player victim, LivingEntity damager) {
        Location victimLocation = victim.getLocation();
        Location damagerLocation = damager.getLocation();
        return victim.isSneaking()
                ? shiftEvade(victimLocation, damagerLocation)
                : evade(victimLocation, damagerLocation);
    }

    private Location evade(Location victimLocation, Location damagerLocation) {
        // Based on the damager's location
        // End Location
        double constantY = damagerLocation.getY() + 0.01d;
        Location tempLocation = damagerLocation.clone();
        Vector vect = damagerLocation.getDirection().normalize().multiply(-2.0);
        Location behind = damagerLocation.add(vect);
        double d = 0.05;
        double inc = 0.05;
        double x2 = behind.getX(), y2 = behind.getY(), z2 = behind.getZ();
        // x1,y2,z1
        double x1 = tempLocation.getX(), y1 = tempLocation.getY(), z1 = tempLocation.getZ();
        // Possible points
        while (d < 1) {
            // x2,y2,z2

            double x = x1 + d * (x2 - x1);
            double y = constantY; // y1 + 0.05d * (y2 - y1); // this stays constant
            double z = z1 + d * (z2 - z1);
            Location testLocation = new Location(victimLocation.getWorld(), x, y, z);

            d += inc;

            if (!testLocation.getBlock().isEmpty()) {
                break;
            }
            tempLocation = testLocation;
            if (testLocation.distanceSquared(behind) < 0.20) {
                break;
            }
        }
        return tempLocation.setDirection(damagerLocation.getDirection());
    }
  /*
  Vector vect = damagerLocation.getDirection().normalize().multiply(-2.4);
  Location behind = damagerLocation.add(vect);
  int i = 0;
  while(!behind.getBlock().isEmpty()){
      if(behind.getBlock().isLiquid()) break;
      behind.add(damagerLocation.getDirection().add(new Vector(0, 0.01, 0)));
      i++;
      if(i > 10) {
          behind = damagerLocation;
      }
  }
  behind.setDirection(damagerLocation.getDirection());
  return behind;
  */

    private Location shiftEvade(Location victimLocation, Location damagerLocation) {
        // Based on the victim's location
        double constantY = victimLocation.getY() + 0.01d;
        Location tempLocation = victimLocation.clone();
        Vector vect = victimLocation.getDirection().normalize().multiply(-2.0);
        Location behind = victimLocation.add(vect);
        double d = 0.05;
        double inc = 0.05;
        double x2 = behind.getX(), y2 = behind.getY(), z2 = behind.getZ();
        // x1,y2,z1
        double x1 = tempLocation.getX(), y1 = tempLocation.getY(), z1 = tempLocation.getZ();
        // Possible points
        while (d < 1) {
            // x2,y2,z2

            double x = x1 + d * (x2 - x1);
            double y = constantY; // y1 + 0.05d * (y2 - y1); // this stays constant
            double z = z1 + d * (z2 - z1);

            Location testLocation = new Location(victimLocation.getWorld(), x, y, z);
            tempLocation = testLocation;
            d += inc;

            if (!testLocation.getBlock().isEmpty()) {
                break;
            }
            tempLocation = testLocation;
            if (testLocation.distanceSquared(behind) < 0.20) {
                break;
            }

        }
        return tempLocation.setDirection(victimLocation.getDirection());
    }

    private class ActiveEvade implements TimeResource {
        private final double godmode = 600D;
        @Override
        public void task() {
            long nextTime = time + (long) godmode;
            double timeLeft = (nextTime - System.currentTimeMillis())/godmode;
            TitleSender.sendTitle(getPlayer(), TitleSender.simpleTime("Evade: ",
                    MathUtil.round(timeLeft, 2) + " s",timeLeft, 1D));
        }

        @Override
        public boolean cancel() {
            if (isEvading) {
                if (System.currentTimeMillis() - time <= 600L) {
                    if (getPlayer().isBlocking()) {
                        return false;
                    } else return true;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        }

        @Override
        public void cleanup() {
            if(isEvading)
            getPlayer().sendMessage(getFailedMessage());


            WrapperPlayServerWorldParticles packet = ParticleGenerator.createParticle(EnumWrappers.Particle.VILLAGER_ANGRY, 4);
            packet.setLocation(getPlayer().getEyeLocation());
            PacketUtil.asyncSend(packet, getPlayers());
            
            SoundPlayer.sendSound(getPlayer(),"note.pling", 0.75F, 2);
            setLastUsed(System.currentTimeMillis());
            TitleSender.sendTitle(getPlayer(), TitleSender.emptyTitle());
            isEvading = false;
            removeCharge();
        }
    }

    @Override
    public String getName() {
        return "Evade";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.SWORD;
    }

    @EventHandler
    public void kill(DeathApplyEvent event) {
        if(event.getAttacker() != getPlayer()) return;
        addCharge();
    }
}
