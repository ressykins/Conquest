package me.raindance.champions.kits.skills.thief;

import com.packetwrapper.abstractpackets.AbstractPacket;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.skilltypes.Instant;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.util.EntityUtil;
import com.podcrash.api.util.PacketUtil;
import com.podcrash.api.world.BlockUtil;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.SkillType;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.util.Vector;

@SkillMetadata(id = 705, skillType = SkillType.Thief, invType = InvType.AXE)
public class Leap extends Instant implements ICooldown {
    // private boolean isLeap;
    public Leap() {
        setCanUseWhileCooldown(true);
    }

    @Override
    public float getCooldown() {
        return 3F;
    }

    @Override
    public String getName() {
        return "Leap";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.AXE;
    }

    @Override
    protected void doSkill(PlayerEvent event, Action action) {
        if (!rightClickCheck(action)) return;
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
        //idk the proper value
        //double velocity = 1.3D + 0.2D * getLevel();
        Vector facing = getPlayer().getLocation().getDirection().setY(0).normalize();

        Location headLoc = getPlayer().getEyeLocation().subtract(facing);
        Location footLoc = headLoc.clone().subtract(0, 1, 0);

        Location standingLocOne = footLoc.clone().subtract(0, 1, 0);
        Location standingLocTwo = standingLocOne.clone().add(facing);

        if (!BlockUtil.isPassable(standingLocOne.getBlock()) && standingLocTwo.getBlock().getType().equals(Material.AIR)) {
            wallKick();
            return;
        }

        if (!BlockUtil.isPassable(headLoc.getBlock()) || !BlockUtil.isPassable(footLoc.getBlock())) {
            wallKick();
        } else leap();
    }

    private void wallKick() {
        Player player = getPlayer();
        SoundPlayer.sendSound(player.getLocation(), "mob.bat.takeoff", 1, 90);
        Vector v = getPlayer().getLocation().getDirection().multiply(1.2);
        v.setY(0);
        v.multiply(0.7);
        v.setY(v.getY() + 0.7);
        if(v.getY() > 2) v.setY(2);
        if(EntityUtil.onGround(player)) v.setY(v.getY() + 0.2);
        player.setVelocity(v);
        player.setFallDistance(-3);
        // this.isLeap = false;
        getPlayer().sendMessage(String.format("%s%s> %sYou used %sWall Kick%s.",
                ChatColor.BLUE, getChampionsPlayer().getName(), ChatColor.GRAY, ChatColor.GREEN, ChatColor.GRAY));
    }

    private void leap() {
        if(onCooldown()) return;
        if(!getPlayer().isOnGround()) {
            AbstractPacket leapEffect = ParticleGenerator.createBlockEffect(getPlayer().getLocation(), Material.WEB.getId());
            PacketUtil.asyncSend(leapEffect, getPlayer().getWorld().getPlayers());
        }
        Player player = getPlayer();
        SoundPlayer.sendSound(player.getLocation(), "mob.bat.takeoff", 1, 70);
        Vector v = getPlayer().getLocation().getDirection().multiply(1.2);
        if(EntityUtil.onGround(getPlayer())) v.setY(v.getY() + 0.2);
        player.setVelocity(v);
        player.setFallDistance(-3);
        this.setLastUsed(System.currentTimeMillis());
        // this.isLeap = true;

        getPlayer().sendMessage(getUsedMessage());
    }
}
