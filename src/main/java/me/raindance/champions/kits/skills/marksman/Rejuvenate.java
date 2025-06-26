package me.raindance.champions.kits.skills.marksman;

import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.events.game.GameStartEvent;
import com.podcrash.api.kits.enums.ItemType;
import com.podcrash.api.kits.iskilltypes.action.IConstruct;
import com.podcrash.api.kits.skilltypes.Passive;
import com.podcrash.api.time.TimeHandler;
import com.podcrash.api.time.resources.TimeResource;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.SkillType;
import me.raindance.champions.kits.enums.InvType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

@SkillMetadata(id = 513, skillType = SkillType.Marksman, invType = InvType.SECONDARY_PASSIVE)
public class Rejuvenate extends Passive implements TimeResource, IConstruct {

    private Long lastHit = -8000L;
    private int waitTime = 8;

    @Override
    public String getName() {
        return "Rejuvenate";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @EventHandler
    public void onStart (GameStartEvent e) {
        StatusApplier.getOrNew(getPlayer()).applyStatus(Status.REGENERATION, Integer.MAX_VALUE, 0, false, false);
        StatusApplier.getOrNew(getPlayer()).applyStatus(Status.SPEED, Integer.MAX_VALUE, 0, false, false);
    }

    @Override
    public void afterRespawn() {
        TimeHandler.delayTime(3, () -> {
            StatusApplier.getOrNew(getPlayer()).applyStatus(Status.REGENERATION, Integer.MAX_VALUE, 0, false, false);
            StatusApplier.getOrNew(getPlayer()).applyStatus(Status.SPEED, Integer.MAX_VALUE, 0, false, false);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHit(DamageApplyEvent e) {
        if(!e.isCancelled() && e.getVictim().equals(getPlayer())) {
            resetTimer();
            StatusApplier.getOrNew(getPlayer()).removeStatus(Status.REGENERATION);
            StatusApplier.getOrNew(getPlayer()).removeStatus(Status.SPEED);
        }
    }

    /**
     * Updates the notRunning bool to reflect whether the skill is still waiting to active
     * @return Whether the skill activated on this check.
     */
    private boolean shouldApplyBuff() {
        //Bukkit.broadcastMessage("diff is " + (System.currentTimeMillis() - lastHit));

        return System.currentTimeMillis() - lastHit > (waitTime * 1000)
                && !getGame().isRespawning(getPlayer())
                && !DamageApplier.getInvincibleEntities().contains(getPlayer())
                && !getGame().isSpectating(getPlayer());
    }

    private void resetTimer() {
        lastHit = System.currentTimeMillis();
    }

    @Override
    public void task() {
        if(shouldApplyBuff()) {
            StatusApplier.getOrNew(getPlayer()).applyStatus(Status.REGENERATION, Integer.MAX_VALUE, 0, false, false);
            StatusApplier.getOrNew(getPlayer()).applyStatus(Status.SPEED, Integer.MAX_VALUE, 0, false, false);
        }
    }

    @Override
    public boolean cancel() {
        return false;
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void afterConstruction() {
        StatusApplier.getOrNew(getPlayer()).applyStatus(Status.REGENERATION, Integer.MAX_VALUE, 0, false, false);
        StatusApplier.getOrNew(getPlayer()).applyStatus(Status.SPEED, Integer.MAX_VALUE, 0, false, false);

        TimeHandler.repeatedTime(5, 0, this);
        // resetTimer();
    }
}