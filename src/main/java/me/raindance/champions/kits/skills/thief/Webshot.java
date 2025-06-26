package me.raindance.champions.kits.skills.thief;

import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.world.BlockUtil;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.BowShotSkill;
import org.bukkit.Location;
import org.bukkit.Material;
// import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

@SkillMetadata(id = 701, skillType = SkillType.Thief, invType = InvType.BOW)
public class Webshot extends BowShotSkill {

    public Webshot() {
        super();
    }

    @Override
    public String getName() {
        return "Webshot";
    }

    @Override
    public float getCooldown() {
        return 12;
    }

    @Override
    protected void shotArrow(Arrow arrow, float force) {
        Player player = getPlayer();
        player.sendMessage(getUsedMessage());
        WrapperPlayServerWorldParticles particle = ParticleGenerator.createParticle(arrow.getLocation().toVector(), EnumWrappers.Particle.SPELL_INSTANT,
                new int[]{150, 75, 0}, 1, 0,0,0);
        ParticleGenerator.generateProjectile(arrow, particle);
    }

    @Override
    protected void shotEntity(DamageApplyEvent event, Player shooter, LivingEntity victim, Arrow arrow, float force) {
        Location v_loc = victim.getLocation();
        // World world = v_loc.getWorld();
        List<Block> blocks = BlockUtil.getBlocksWithinRange(v_loc, 1.5, true);

        for(Block block : blocks) {
            int totalDuration = 2;
            BlockUtil.restoreAfterBreak(block.getLocation(), Material.WEB, (byte) 0, totalDuration);
        }

        arrow.remove();
    }

    @Override
    protected void shotGround(Player shooter, Location location, Arrow arrow, float force) {
        Location a_loc = arrow.getLocation();
        // World world = a_loc.getWorld();
        List<Block> blocks = BlockUtil.getBlocksWithinRange(a_loc, 1.5, true);

        for(Block block : blocks) {
            int totalDuration = 2;
            BlockUtil.restoreAfterBreak(block.getLocation(), Material.WEB, (byte) 0, totalDuration);
        }
    }
}
