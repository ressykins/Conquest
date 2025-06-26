package me.raindance.champions.kits.skills.vanguard;

import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.sound.SoundPlayer;

import me.raindance.champions.Main;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;

import com.podcrash.api.kits.KitPlayer;
import com.podcrash.api.kits.KitPlayerManager;
import com.podcrash.api.kits.enums.ItemType;

import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Interaction;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.podcrash.api.callback.sources.CollideBeforeHitGround;

@SkillMetadata(id = 803, skillType = SkillType.Vanguard, invType = InvType.SWORD)
public class RagingBull extends Interaction {
    private final float hitbox = 0.45f;
    // private Player launchedPlayer = null;
    private double collisionDamage;

    @Override
    public float getCooldown() {
        return 12;
    }

    @Override
    public String getName() {
        return "Raging Bull";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.SWORD;
    }

    @Override
    public void doSkill(LivingEntity clickedEntity) {
        if(onCooldown()) return;
        if(isAlly(clickedEntity)) {return;}
        setLastUsed(System.currentTimeMillis());
    
        Player victim = (Player) clickedEntity;
        KitPlayer victimKitPlayer = KitPlayerManager.getInstance().getKitPlayer(victim);
    
        collisionDamage = victimKitPlayer.getHP() * 0.2;
    
        DamageApplier.damage(victim, getPlayer(), 8, this, false);
        Vector vector = getPlayer().getLocation().getDirection().normalize().multiply(1.1d).setY(0.5f);
        victim.setVelocity(vector);
        getPlayer().sendMessage(getUsedMessage());
        SoundPlayer.sendSound(getPlayer().getLocation(), "mob.irongolem.hit", 1.1F, 57);
        
        Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
            getRagingBullEffect(victim).run();
        }, 3L);
    }

    CollideBeforeHitGround getRagingBullEffect(Player launchedPlayer) {
        return new CollideBeforeHitGround(launchedPlayer, 1, hitbox, hitbox, hitbox).then(() -> {
                  List<Entity> entities = CollideBeforeHitGround.getValidEntitiesInRange(launchedPlayer, hitbox, hitbox, hitbox);
                  if (entities.size() == 0) return;
                  for (Entity entity : entities) {
                      if(!(entity instanceof LivingEntity)) continue;
                      if(isAlly((LivingEntity) entity)) continue;
      
                      launchedPlayer.setVelocity(new Vector(0, 0, 0));
                      launchedPlayer.getWorld().playSound(launchedPlayer.getLocation(), Sound.ZOMBIE_WOOD, 2f, 0.2f);
                      DamageApplier.damage((LivingEntity) entity, getPlayer(), collisionDamage, this, false);
      
                      StatusApplier.getOrNew((LivingEntity) entity).applyStatus(Status.GROUND, 2, 3);
                      StatusApplier.getOrNew(launchedPlayer).applyStatus(Status.GROUND, 2, 3);
                  }
              });
      }
    
}
