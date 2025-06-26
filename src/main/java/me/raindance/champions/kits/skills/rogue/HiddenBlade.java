package me.raindance.champions.kits.skills.rogue;

import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DeathApplyEvent;
import com.podcrash.api.events.StatusApplyEvent;
import com.podcrash.api.sound.SoundPlayer;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Interaction;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

@SkillMetadata(id = 609, skillType = SkillType.Rogue, invType = InvType.SWORD)
public class HiddenBlade extends Interaction {
    private LivingEntity affectedPlayer = null;

    @Override
    public void doSkill(LivingEntity clickedEntity) {
        if(onCooldown()) return;
        if(isAlly(clickedEntity)) {return;}
        setLastUsed(System.currentTimeMillis());
        affectedPlayer = clickedEntity;

        StatusApplier applier = StatusApplier.getOrNew(affectedPlayer);
        applier.getEffects().forEach(status -> {
            if (status == Status.RESISTANCE || status == Status.ABSORPTION)
                applier.removeStatus(status);
        });

        DamageApplier.damage(clickedEntity, getPlayer(), 7, this, false);
        SoundPlayer.sendSound(clickedEntity.getLocation(), "random.anvil_land", 0.9F, 110);

        landed();
    }

    @Override
    public float getCooldown() {
        return 5;
    }

    @Override
    public String getName() {
        return "Hidden Blade";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.SWORD;
    }

    @EventHandler
    public void kill(DeathApplyEvent event) {
        if(event.getAttacker() != getPlayer()) return;
        HiddenBlade casted = (HiddenBlade) event.getSources().get(0);
        if(casted.getPlayer() == this.getPlayer()) {
            getChampionsPlayer().heal(5);
        }
    }

    @EventHandler
    public void onStatus(StatusApplyEvent e) {
        if (System.currentTimeMillis() - getLastUsed() < 3000L && e.getEntity() == affectedPlayer) {
            if(e.getStatus() == Status.RESISTANCE || e.getStatus() == Status.ABSORPTION) e.setCancelled(true);
        }
    }
}
