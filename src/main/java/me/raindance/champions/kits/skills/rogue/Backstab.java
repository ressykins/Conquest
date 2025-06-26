package me.raindance.champions.kits.skills.rogue;

import com.podcrash.api.damage.Cause;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Passive;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.util.VectorUtil;
import org.bukkit.Effect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

@SkillMetadata(id = 601, skillType = SkillType.Rogue, invType = InvType.PRIMARY_PASSIVE)
public class Backstab extends Passive {
    private final float bonus = 3;

    @Override
    public String getName() {
        return "Backstab";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    public void onHit(DamageApplyEvent event) {
        //cba with non players
        if (event.isCancelled() || event.getCause() != Cause.MELEE) return;
        if (event.getAttacker() != getPlayer()) return;
        Player damager = (Player) event.getAttacker();
        LivingEntity victim = event.getVictim();
        if (VectorUtil.angleIsAround(damager.getLocation().getDirection(), victim.getLocation().getDirection(), 60)) {
            event.addSource(this);
            event.setDamage(event.getDamage() + bonus);
            event.setModified(true);
            StatusApplier.getOrNew(victim).applyStatus(Status.SLOW, 3, 0);
            SoundPlayer.sendSound(victim.getLocation(), "game.neutral.hurt", 0.5F, 126);
            victim.getWorld().playEffect(event.getVictim().getLocation(), Effect.STEP_SOUND, 55);
        }
    }
}
