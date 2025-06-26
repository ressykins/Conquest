package me.raindance.champions.kits.skills.warden;

import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.events.game.GameResurrectEvent;
import com.podcrash.api.sound.SoundPlayer;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Passive;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

@SkillMetadata(id = 903, skillType = SkillType.Warden, invType = InvType.SECONDARY_PASSIVE)
public class Conditioning extends Passive {
    private int stacks = 0;
    // @Override
    // public float getCooldown() {
    //     return 9;
    // }

    @Override
    public String getName() {
        return "Conditioning";
    }

    @Override
    public ItemType getItemType() {
        return null;
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void damage(DamageApplyEvent e){
        if(e.isCancelled()) return;
        if(e.getVictim() == getPlayer()) {
            if (stacks == 15) {
                e.setDamage(e.getDamage() - 1);
            }
            else {
                stacks++;
                if (stacks == 15) {
                    SoundPlayer.sendSound(getPlayer().getLocation(), "mob.irongolem.hit", 0.7F, 77);
                    getPlayer().sendMessage(String.format("%sConditioning> %sYou have become more resilient to damage!",
                                            ChatColor.BLUE, ChatColor.GRAY));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRespawn(GameResurrectEvent event) {
        if (event.getWho() != getPlayer()) return;
        stacks = 0;
    }

    // @EventHandler
    // public void damage(DamageApplyEvent e) {
    //     if (onCooldown() || e.getAttacker() != getPlayer()) return;
    //     if (e.getCause() != Cause.MELEE && e.getCause() != Cause.MELEESKILL) return;
    //     if (isAlly(e.getVictim())) return;

    //     setLastUsed(System.currentTimeMillis());
    //     getPlayer().sendMessage(getUsedMessage(e.getVictim()).replace("used", "unleashed"));
    //     StatusApplier.getOrNew((Player) e.getVictim()).applyStatus(Status.GROUND, 2F, 1);
    //     e.addSource(this);

    //     SoundPlayer.sendSound(getPlayer().getLocation(), "mob.irongolem.hit", 0.7F, 77);
    //     ParticleGenerator.createBlockEffect(getPlayer().getLocation(), Material.WOODEN_DOOR.getId());
    // }
}
