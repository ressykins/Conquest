package me.raindance.champions.kits.skills.hunter;

import com.podcrash.api.damage.Cause;
import com.podcrash.api.events.DamageApplyEvent;
import me.raindance.champions.Main;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.ICharge;
import com.podcrash.api.kits.skilltypes.Passive;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.time.TimeHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.HashMap;

@SkillMetadata(id = 405, skillType = SkillType.Hunter, invType = InvType.INNATE)
public class Sharpshooter extends Passive implements ICharge {
    private final HashMap<Integer, Float> forceMap = new HashMap<>();
    private final int MAX_CHARGES = 4;
    private final int damageBonus = 2;

    private int charges = 0;
    private int miss = 0;
    private long time;
    private boolean justMissed;

    public Sharpshooter() {
        super();
    }

    @Override
    public String getName() {
        return "Sharpshooter";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void shoot(EntityShootBowEvent event){
        if(event.getEntity() == getPlayer() && event.getProjectile() instanceof Arrow) {
            forceMap.put(event.getProjectile().getEntityId(), event.getForce());
        }
    }

    private boolean checkIfValidShooter(DamageApplyEvent e){
        return !e.isCancelled() && e.getVictim() != getPlayer() && e.getAttacker() == getPlayer()
                && e.getArrow() != null && e.getCause() == Cause.PROJECTILE;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void shoot(DamageApplyEvent e) {
        if (!checkIfValidShooter(e)) return;
        justMissed = false;
        time = System.currentTimeMillis();
        e.setModified(true);
        e.setDamage(e.getDamage() + getCurrentCharges() * damageBonus);
        int id = e.getArrow().getEntityId();
        //if(forceMap.get(id) >= 0.9F)
            addCharge();
        forceMap.remove(id);
        getPlayer().sendMessage(getCurrentChargeMessage());
        e.addSource(this);
        playSound();
        miss = 0;
        start();
        Bukkit.getScheduler().runTaskLater(Main.instance, () -> justMissed = true, 3L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void hit(ProjectileHitEvent e) {
        if (e.getEntity().getShooter() == getPlayer()) {
            Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
                if (justMissed) {
                    miss++;
                    if (miss >= 2) {
                        miss = 0;
                        if (charges != 0) {
                            resetCharge();
                            playSound();
                            getPlayer().sendMessage(getCurrentChargeMessage());
                        }
                    }
                }
            }, 1L);
        }
    }

    // @EventHandler(priority = EventPriority.LOW)
    // public void fall(EntityDamageEvent e) {
    //     if(getPlayer() == e.getEntity() && e.getCause() == EntityDamageEvent.DamageCause.FALL) {
    //         double totalDamage = e.getDamage() - 2;
    //         if(totalDamage <= 0) {
    //             e.setDamage(0);
    //             e.setCancelled(true);
    //         }
    //         e.setDamage(totalDamage);
    //     }
    // }

    @Override
    public void addCharge() {
        if (charges < MAX_CHARGES) charges++;
    }

    @Override
    public int getCurrentCharges() {
        return charges;
    }

    @Override
    public int getMaxCharges() {
        return MAX_CHARGES;
    }

    public void resetCharge() {
        charges = 0;
    }

    private void start() {
        stop();
        TimeHandler.repeatedTime(1, 0, this);
    }

    private void stop() {
        TimeHandler.unregister(this);
    }

    @Override
    public void task() {

    }

    @Override
    public boolean isMaxAtStart() {
        return false;
    }

    @Override
    public boolean cancel() {
        return System.currentTimeMillis() - time >= 5000L;
    }

    @Override
    public void cleanup() {
        resetCharge();
        getPlayer().sendMessage(getCurrentChargeMessage());
        playSound();
    }

    private void playSound() {
        float i = (((float) getCurrentCharges()) / ((float) getMaxCharges()));
        SoundPlayer.sendSound(this.getPlayer(), "note.harp", 0.75f, (int) (130 * i));
    }
}
