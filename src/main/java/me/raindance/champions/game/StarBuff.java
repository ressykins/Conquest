package me.raindance.champions.game;

import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.game.GTeam;
import com.podcrash.api.game.Game;
import com.podcrash.api.game.GameState;
import com.podcrash.api.game.TeamEnum;
import com.podcrash.api.game.scoreboard.GameScoreboard;
import com.podcrash.api.plugin.PodcrashSpigot;
import com.podcrash.api.time.resources.TimeResource;
import com.podcrash.api.util.PacketUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Helper module to handle buffed players
 */
public class  StarBuff implements TimeResource {
    public static final String PREFIX = ChatColor.WHITE + "" + ChatColor.BOLD + "STAR:" + ChatColor.RESET + " ";
    private Game game;
    private GameScoreboard scoreboard;
    private String holder;
    private boolean dead;
    private long endTime;

    public StarBuff(Game game) {
        this.game = game;
        this.scoreboard = game.getGameScoreboard();
        this.dead = false;
    }

    public void replaceLine(String prefix, String line) {
        List<String> lines = scoreboard.getLines();
        for(int i = 0; i < lines.size(); i++) {
            String curr = lines.get(i);
            if(!curr.toLowerCase().contains("star:")) continue;
            int thing = i + 1;
            scoreboard.setPrefix(thing, prefix);
            scoreboard.setSuffix(thing, line);
            break;
        }
    }

    public void setCollector(Player collector) {
        this.holder = collector.getName();
        this.endTime = System.currentTimeMillis() + 1000L * 30;
        replaceLine(PREFIX + ChatColor.YELLOW + "" + ChatColor.BOLD, holder);
        runAsync(1, 0);
    }

    public LivingEntity getCollector() {
        if(holder == null) return null;
        return Bukkit.getPlayer(holder);
    }
    public void collectorDiedNotify(Player died) {
        if(!died.getName().equalsIgnoreCase(holder)) return;
        this.dead = true;
    }

    @Override
    public void task() {
        //give the buffed player some particle effects
        WrapperPlayServerWorldParticles packet = ParticleGenerator.createParticle(EnumWrappers.Particle.VILLAGER_HAPPY, 3);
        Player owner = Bukkit.getPlayer(holder);

        List<Status> effects = StatusApplier.getOrNew(owner).getEffects();
        long timeLeft = endTime - System.currentTimeMillis();
        timeLeft /= 1000L; //Convert to seconds

        //Make sure the effects are there (incase other stuff gives stronger regen but ends)
        if (!effects.contains(Status.STRENGTH) && timeLeft > 0.5) {
            StatusApplier.getOrNew(owner).applyStatus(Status.STRENGTH, timeLeft, 0, false, true);
        }
        if (!effects.contains(Status.RESISTANCE) && timeLeft > 0.5) {
            StatusApplier.getOrNew(owner).applyStatus(Status.RESISTANCE, timeLeft, 0, false, true);
        }
        if (!effects.contains(Status.REGENERATION) && timeLeft > 0.5) {
            StatusApplier.getOrNew(owner).applyStatus(Status.REGENERATION, timeLeft, 0, false, true);
        }


        Location location = owner.getLocation().add(0, 1.10, 0);
        packet.setLocation(location);
        PacketUtil.asyncSend(packet, location.getWorld().getPlayers());
    }

    @Override
    public boolean cancel() {
        return dead || System.currentTimeMillis() > endTime || game.getGameState() == GameState.LOBBY;
    }

    @Override
    public void cleanup() {
        TeamEnum team = game.getTeamEnum(Bukkit.getPlayer(holder));
        TeamEnum oppoTeam = getOppositeTeam(team);
        if(dead) {
            game.increment(oppoTeam, 300);
            //game.broadcast(team.getChatColor() + holder + " lost the buff!");
            game.broadcast(String.format("%s%s%s has lost the star!", ChatColor.WHITE, ChatColor.BOLD, holder));
            //alert the players that the collector lost the buff and gave the opposite team the points back
            this.dead = false;
        } else if (game.getGameState() == GameState.STARTED) {
            //game.broadcast(team.getChatColor() + holder + " lost the star peacefully.");
            game.broadcast(String.format("%s%s%s lost the star peacefully.", ChatColor.WHITE, ChatColor.BOLD, holder));
            //alert the players that the collector lost the buff peacefully
        }
        replaceLine(PREFIX + ChatColor.GRAY + "" + ChatColor.BOLD, "Inactive");

        this.holder = null;
    }

    /**
     * move to Game
     * @param team
     * @return
     */
    public TeamEnum getOppositeTeam(TeamEnum team) {
        for(GTeam teamL : game.getTeams()) {
            if(teamL.getTeamEnum() == team) continue;
            return teamL.getTeamEnum();
        }
        return null;
    }
}
