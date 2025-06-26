package me.raindance.champions.kits.skills.duelist;


import com.podcrash.api.damage.Cause;
import com.podcrash.api.events.DamageApplyEvent;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.skilltypes.Passive;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

//How to make a class from scratch
@SkillMetadata(id = 307, skillType = SkillType.Duelist, invType = InvType.PRIMARY_PASSIVE)
public class Revenge extends Passive implements ICooldown {
    private LivingEntity lastAttacked;
    private double lastDamage;

    @Override
    public String getName() {
        return "Revenge";
    }

    @Override
    public float getCooldown() {
        return 5;
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    protected void hit(DamageApplyEvent e) {
        if (e.isCancelled()) return; 

        //we only care about melee attacks
        if (e.getCause() != Cause.MELEE) return;

        if (e.getAttacker() == getPlayer() && e.getVictim() == lastAttacked && !onCooldown()) {
            e.setModified(true); // modify the damage
            e.addSource(this);
            e.setDamage(e.getDamage() + (lastDamage * 0.5));
            getPlayer().sendMessage(getUsedMessage(e.getVictim()));
            setLastUsed(System.currentTimeMillis());
        } else if (e.getVictim() == getPlayer()) {
            lastAttacked = e.getAttacker();
            lastDamage = e.getDamage();
        }

    }
}
