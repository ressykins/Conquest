package me.raindance.champions.kits.itemskill;

import com.google.common.reflect.ClassPath;
import me.raindance.champions.Main;
import com.podcrash.api.listeners.ListenerBase;
import com.podcrash.api.kits.annotation.ItemMetaData;
import me.raindance.champions.kits.itemskill.item.Soup;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/*
    This dude manages items
 */
public class ItemHelper extends ListenerBase {
    private final List<ItemActionData> map;

    public ItemHelper(JavaPlugin plugin) {
        super(plugin);
        this.map = new ArrayList<>();
        try {
            setupItems();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setupItems() throws ClassNotFoundException {
        String path = "me.raindance.champions.kits.itemskill.item";

        ClassPath cp = getClassPath(Soup.class.getClassLoader());
        Set<ClassPath.ClassInfo> classInfoSet = cp.getTopLevelClasses(path);
        for(ClassPath.ClassInfo info : classInfoSet) {
            Class<?> itemClass = Class.forName(info.getName());

            IItem item = (IItem) emptyConstructor(itemClass);

            if(item instanceof Listener)
                Bukkit.getPluginManager().registerEvents((Listener) item, Main.getInstance());

            if(item == null) throw new RuntimeException("item cannot be null! current at: " + info.getName());
            ItemMetaData annot = itemClass.getAnnotation(ItemMetaData.class);
            Material mat = annot.mat();
            Action[] actions = annot.actions();
            int id = annot.data();
            ItemActionData data = new ItemActionData(mat, actions, item, id);
            map.add(data);
        }
    }

    //TODO: move this stuff to reflection package in the engine
    private ClassPath getClassPath(ClassLoader classLoader) {
        try {
            return ClassPath.from(classLoader);
        } catch (IOException e) {
            e.printStackTrace();
            Main.getInstance().getLogger().log(Level.SEVERE, e.getMessage());
            throw new IllegalStateException("getting classpath erred out");
        }
    }
    private static <T> T emptyConstructor(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getConstructor();
            return constructor.newInstance();
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    private ItemActionData getItemAction(ItemStack itemStack) {
        Material mat = itemStack.getType();
        MaterialData matData = itemStack.getData();
        for(ItemActionData data : map) {
            if(data.materialEquals(mat, matData))
                return data;
        }
        return null;
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void click(PlayerInteractEvent e) {
        // if (e.getItem().getType() != Material.FISHING_ROD) return;
        
        if(e.getItem() == null) return;
        ItemActionData itemHandler = getItemAction(e.getItem());
        if (itemHandler == null) return;
        Player p = e.getPlayer();
        Action action = e.getAction();
        if(itemHandler.actionContains(action)) {
            itemHandler.doItemAction(p, action);
            removeItemFromHand(p);

        }else e.setCancelled(true);
        p.updateInventory();
    }

    private void removeItemFromHand(Player player) {
        ItemStack item = player.getItemInHand();
        int slot = player.getInventory().getHeldItemSlot();
        int amnt = item.getAmount();
        if(amnt > 1) {
            item.setAmount(amnt - 1);
        }else {
            PlayerInventory inventory = player.getInventory();
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.instance, () -> inventory.clear(slot), 1);
        }
        player.updateInventory();
    }
}
