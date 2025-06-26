package me.raindance.champions.kits.skills.vanguard;

import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.events.game.GameStartEvent;
import com.podcrash.api.kits.enums.ItemType;
import com.podcrash.api.kits.iskilltypes.action.IConstruct;
import com.podcrash.api.kits.skilltypes.Passive;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.time.TimeHandler;
import com.podcrash.api.time.resources.TimeResource;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.SkillType;
import me.raindance.champions.kits.enums.InvType;
import net.minecraft.server.v1_8_R3.EntityLiving;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

@SkillMetadata(id = 812, skillType = SkillType.Vanguard, invType = InvType.INNATE)
public class Plating extends Passive implements TimeResource, IConstruct {

    private Long lastHit = -8000L;
    private int waitTime = 8;

    @Override
    public String getName() {
        return "Plating";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @EventHandler
    public void onStart (GameStartEvent e) {
        StatusApplier.getOrNew(getPlayer()).applyStatus(Status.ABSORPTION, Integer.MAX_VALUE, 0, false, true);
    }

    @Override
    public void afterRespawn() {
        TimeHandler.delayTime(3, () -> {
            StatusApplier.getOrNew(getPlayer()).applyStatus(Status.ABSORPTION, Integer.MAX_VALUE, 0, false, true);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHit(DamageApplyEvent e) {

        CraftEntity craftEntity = (CraftEntity) getPlayer();
        EntityLiving player = (EntityLiving) craftEntity.getHandle();
        float extraHearts = player.getAbsorptionHearts();

        if(!e.isCancelled() && e.getVictim().equals(getPlayer())) {
            resetTimer();
            if (extraHearts == 0) {
                StatusApplier.getOrNew(getPlayer()).removeStatus(Status.ABSORPTION);
            }
        }
    }

    /**
     * Updates the notRunning bool to reflect whether the skill is still waiting to active
     * @return Whether the skill activated on this check.
     */
    private boolean shouldApplyBuff() {
        //Bukkit.broadcastMessage("diff is " + (System.currentTimeMillis() - lastHit));
        CraftEntity craftEntity = (CraftEntity) getPlayer();
        EntityLiving player = (EntityLiving) craftEntity.getHandle();
        float extraHearts = player.getAbsorptionHearts();

        return System.currentTimeMillis() - lastHit > (waitTime * 1000)
                && !getGame().isRespawning(getPlayer())
                && !DamageApplier.getInvincibleEntities().contains(getPlayer())
                && !getGame().isSpectating(getPlayer())
                && extraHearts < 2;
    }

    private void resetTimer() {
        lastHit = System.currentTimeMillis();
    }

    @Override
    public void task() {
        if(shouldApplyBuff()) {
            StatusApplier.getOrNew(getPlayer()).applyStatus(Status.ABSORPTION, Integer.MAX_VALUE, 0, false, true);
            SoundPlayer.sendSound(getPlayer(), "mob.zombie.metal", 0.4f, 126);
            // getPlayer().getWorld().playEffect(getPlayer().getLocation(), Effect.HEART, 1);
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
        StatusApplier.getOrNew(getPlayer()).applyStatus(Status.ABSORPTION, Integer.MAX_VALUE, 0, false, true);

        TimeHandler.repeatedTime(1, 0, this);
        // resetTimer();
    }
}