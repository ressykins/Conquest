package me.raindance.champions.kits.skills.thief;

import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;

import com.packetwrapper.abstractpackets.AbstractPacket;
import com.packetwrapper.abstractpackets.WrapperPlayServerEntityStatus;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Interaction;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.time.TimeHandler;
import com.podcrash.api.time.resources.TimeResource;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;

@SkillMetadata(id = 711, skillType = SkillType.Thief, invType = InvType.SWORD)
public class Steal extends Interaction {

    private int invSlot;
    private ItemStack itemStack;
    private final int cooldown = 15; 

    // Check if the weapon is a valid one to be stolen
    private boolean isStealableWeapon(ItemStack weapon) {
        if (weapon.hasItemMeta() && weapon.getItemMeta().hasDisplayName()) {
            String displayName = weapon.getItemMeta().getDisplayName().toLowerCase();
            // Check if the name contains "Sword", "Bow", or "Axe"
            return displayName.contains("sword") || displayName.contains("bow") || displayName.contains("axe");
        }
        return false; // Default to false if no valid name is found
    }

    @Override
    public void doSkill(LivingEntity victim) {
        // pls dunt rob temmaets
        if (isAlly(victim)) return;
        if (StatusApplier.getOrNew(getPlayer()).has(Status.SLOW)) {
            getPlayer().sendMessage(getCannotUseWhileMessage("Slowed"));
            return;
        }
        if (StatusApplier.getOrNew(getPlayer()).has(Status.GROUND)) {
            getPlayer().sendMessage(getCannotUseWhileMessage("Grounded"));
            return;
        }
        if (StatusApplier.getOrNew(getPlayer()).has(Status.ROOTED)) {
            getPlayer().sendMessage(getCannotUseWhileMessage("Rooted"));
            return;
        }

        Player targetPlayer = (Player) victim;
        
        invSlot = targetPlayer.getInventory().getHeldItemSlot();
        itemStack = targetPlayer.getInventory().getItemInHand();

    
        if (!isStealableWeapon(itemStack)) {
            getPlayer().sendMessage(String.format("%sThief> %sYou failed to steal anything.",
            ChatColor.BLUE,
            ChatColor.GRAY));
        }

        else {
            targetPlayer.getInventory().setItem(invSlot, null);

            SoundPlayer.sendSound(targetPlayer.getLocation(), "mob.enderdragon.wings", 1, 63);

            WrapperPlayServerEntityStatus packet = new WrapperPlayServerEntityStatus();
            packet.setEntityId(targetPlayer.getEntityId());
            packet.setEntityStatus(WrapperPlayServerEntityStatus.Status.ENTITY_HURT);

            AbstractPacket packet2 = ParticleGenerator.createBlockEffect(targetPlayer.getLocation().toVector(), Material.CHEST.getId());
            for (Player player : getPlayers()) {
                packet.sendPacket(player);
                packet2.sendPacket(player);
            }

            getPlayer().sendMessage(String.format("%s%s> %sYou successfully stole %s%s's%s weapon!",
                ChatColor.BLUE, 
                getChampionsPlayer().getName(),
                ChatColor.GRAY, 
                ChatColor.YELLOW, 
                targetPlayer.getName(), 
                ChatColor.GRAY));
                targetPlayer.sendMessage(String.format("%sSteal> %s%s%s has temporarily stolen your weapon!",
                ChatColor.BLUE,
                ChatColor.YELLOW,
                getPlayer().getName(),
                ChatColor.GRAY));


            // Give the player back their item after 2 seconds.
            TimeHandler.repeatedTime(5, 0, new TimeResource() {
                int i = 0;
                @Override
                public void task() {
                    i += 1;
                }

                @Override
                public boolean cancel() {
                    return i >= 8;
                }

                @Override
                public void cleanup() {
                    if(!targetPlayer.getInventory().contains(itemStack) || i >= 8) {
                        if(getGame().isRespawning(targetPlayer)) return;
                        SoundPlayer.sendSound(targetPlayer, "random.pop", 1, 63);
                        targetPlayer.getInventory().setItem(invSlot, itemStack);
                    }
                }
            });
        }

        setLastUsed(System.currentTimeMillis());
        landed();
    }

    @Override
    public float getCooldown() {
        return cooldown;
    }

    @Override
    public String getName() {
        return "Steal";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.SWORD;
    }
}
