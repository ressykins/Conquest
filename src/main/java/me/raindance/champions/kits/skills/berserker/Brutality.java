package me.raindance.champions.kits.skills.berserker;

import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.kits.KitPlayer;
import com.podcrash.api.kits.KitPlayerManager;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Passive;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

@SkillMetadata(id = 101, skillType = SkillType.Berserker, invType = InvType.SECONDARY_PASSIVE)
public class Brutality extends Passive {
    @Override
    public String getName() {
        return "Brutality";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @EventHandler
    public void damage(DamageApplyEvent e) {
        if(getPlayer() != e.getAttacker() || !(e.getVictim() instanceof Player)) return;
        
        Player victim = (Player) e.getVictim();
        KitPlayer victimKitPlayer = KitPlayerManager.getInstance().getKitPlayer(victim);
        
        double trueCurrentHP = (victim.getHealth() / victim.getMaxHealth()) * victimKitPlayer.getHP();
        double trueMissingHP = victimKitPlayer.getHP() - trueCurrentHP;
        double bonus = trueMissingHP * 0.1;

        e.setDamage(e.getDamage() + bonus);
        e.setModified(true);
        e.addSource(this);
    }
}
