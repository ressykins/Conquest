package me.raindance.champions.kits.skills.duelist;

import com.packetwrapper.abstractpackets.AbstractPacket;
import com.packetwrapper.abstractpackets.WrapperPlayServerEntityStatus;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.sound.SoundPlayer;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;

import com.podcrash.api.kits.KitPlayer;
import com.podcrash.api.kits.KitPlayerManager;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Interaction;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

@SkillMetadata(id = 303, skillType = SkillType.Duelist, invType = InvType.SWORD)
public class ComboAttack extends Interaction {
    private LivingEntity attacked = null;
    private int i;
    @Override
    public void doSkill(LivingEntity clickedEntity) {
        if (onCooldown()) return;
        if (isAlly(clickedEntity)) return;
        this.attacked = clickedEntity;
        this.i = 0;
        setLastUsed(System.currentTimeMillis());
        WrapperPlayServerEntityStatus packet = new WrapperPlayServerEntityStatus();
        packet.setEntityId(attacked.getEntityId());
        packet.setEntityStatus(WrapperPlayServerEntityStatus.Status.ENTITY_HURT);

        AbstractPacket packet2 = ParticleGenerator.createBlockEffect(attacked.getLocation().toVector(), Material.OBSIDIAN.getId());
        for (Player player : getPlayers()) {
            packet.sendPacket(player);
            packet2.sendPacket(player);
        }

        landed();
    }

    @EventHandler
    public void hit(DamageApplyEvent event) {
        if (attacked == null || event.getAttacker() != getPlayer()) return;
        if (event.getVictim() != attacked && onCooldown()) {
            getPlayer().sendMessage(String.format("%sCombo Attack> %sYour Combo Attack has ended.",
            ChatColor.BLUE,
            ChatColor.GRAY));
            attacked = null;
            return;
        }
        i++;

        if (i < 3 || System.currentTimeMillis() - getLastUsed() > 3000L) return;
        StatusApplier.getOrNew(attacked).applyStatus(Status.SLOW, 3, 2);


        Player victim = (Player) event.getVictim();
        KitPlayer victimKitPlayer = KitPlayerManager.getInstance().getKitPlayer(victim);
        double trueCurrentHP = (victim.getHealth() / victim.getMaxHealth()) * victimKitPlayer.getHP();
        double trueMissingHP = victimKitPlayer.getHP() - trueCurrentHP;
        double bonus = trueMissingHP * 0.2;

        event.setDamage(event.getDamage() + bonus);
        event.setModified(true);
        event.addSource(this);
        getPlayer().sendMessage(getUsedMessage(event.getVictim()).replace("used", "unleashed a"));
        event.getVictim().sendMessage(String.format("%sCombo Attack> %s%s%s unleashed a %sCombo Attack %son you!",
                ChatColor.BLUE,
                ChatColor.YELLOW,
                getPlayer().getName(),
                ChatColor.GRAY,
                ChatColor.GREEN,
                ChatColor.GRAY));
        SoundPlayer.sendSound(getPlayer().getLocation(), "mob.irongolem.death", 1, 63);

        attacked = null;
    }


    @Override
    public float getCooldown() {
        return 10;
    }

    @Override
    public String getName() {
        return "Combo Attack";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.SWORD;
    }
}
