package net.urbanmc.ezauctions.util;

import net.urbanmc.ezauctions.EzAuctions;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

@SuppressWarnings("ConstantConditions")
public class ReflectionUtil {

    private static final Class<?> bannerClass = getNMSClass("ItemBanner");
    private static double version;

    // Put the version into a double
    static {
        try {
            String version = Bukkit.getVersion(); // "git-Paper-153 MC: 1.13.2"
            String[] spaceSplit = version.split(" "); // { "git-Paper-153", "MC:", "1.13.2" }
            String numberVersion = spaceSplit[2]; // "1.13.2"
            String[] dotSplit = numberVersion.split("\\."); // { "1", "13", "2" }
            ReflectionUtil.version = Double.parseDouble(dotSplit[0] + "." + dotSplit[1]); // 1.13
        } catch (Exception e) {
            // In case anything goes wrong, assume it's a newer version
            EzAuctions.getPluginLogger().severe("Could not determine Bukkit version for reflection. Assuming 1.13+ ...");
            version = 1.13;
        }
    }

    public static String getMinecraftName(ItemStack is) {
        try {
            Object nmsStack = asNMSCopy(is);

            Object item = nmsStack.getClass().getMethod("getItem").invoke(nmsStack);

            // We can do a simple version check like this which should work for future versions
            if (version > 1.12) {
                return (String) item.getClass().getMethod("getName").invoke(item);
            } else {
                if (bannerClass.isAssignableFrom(item.getClass())) {
                    Object enumColor = item.getClass().getMethod("c", nmsStack.getClass()).invoke(item, nmsStack);
                    String color = enumColor.getClass().getMethod("d").invoke(enumColor).toString();

                    return "item.banner." + color + ".name";
                } else {
                    return item.getClass().getMethod("a", nmsStack.getClass()).invoke(item, nmsStack).toString() +
                            ".name";
                }
            }
        } catch (Exception ex) {
            EzAuctions.getPluginLogger()
                    .log(Level.WARNING, "Error getting minecraft name for " + is.getType().toString(),
                            ex);
            return "";
        }
    }

    /**
     * @return 0 if the item has never been repaired or -1 if it is no longer repairable.
     */
    public static int getXPForRepair(ItemStack is) {
        try {
            Object nmsStack = asNMSCopy(is);

            boolean hasTag = (boolean) nmsStack.getClass().getMethod("hasTag").invoke(nmsStack);

            if (!hasTag)
                return 0;

            Object tag = nmsStack.getClass().getMethod("getTag").invoke(nmsStack);

            boolean hasKey = (boolean) tag.getClass().getMethod("hasKey", String.class).invoke(tag, "RepairCost");

            if (!hasKey)
                return 0;

            int cost = (int) tag.getClass().getMethod("getInt", String.class).invoke(tag, "RepairCost");

            boolean repairable = cost <= 40;

            if (repairable)
                return cost;
            else
                return -1;
        } catch (Exception ex) {
            EzAuctions.getPluginLogger().log(Level.WARNING,
                    "Error getting xp needed for repair for " + is.getType().toString(),
                    ex);
            return 0;
        }
    }

    public static String getItemAsJson(ItemStack is) {
        try {
            Object nmsStack = asNMSCopy(is);

            Class<?> nbtTagCompoundClazz = getNMSClass("NBTTagCompound");
            Method saveMethod = nmsStack.getClass().getMethod("save", nbtTagCompoundClazz);

            Object nmsNbtTagCompoundObj = nbtTagCompoundClazz.newInstance();
            Object jsonItem = saveMethod.invoke(nmsStack, nmsNbtTagCompoundObj);

            String itemJson = jsonItem.toString();

            // Prevent sending a packet that could be mishandled by bungeecord
            if (itemJson.getBytes(StandardCharsets.UTF_8).length > Short.MAX_VALUE) {
                EzAuctions.getPluginLogger().severe("An item that exceeded max packet length was attempted to be auctioned!");
                return getItemAsJson(new ItemStack(is.getType(), 1));
            }

            return itemJson;
        } catch (Exception ex) {
            EzAuctions.getPluginLogger().log(Level.WARNING,
                    "Error getting item as json. Item: " + is.getType().toString(),
                    ex);
            return "";
        }
    }

    private static Object asNMSCopy(ItemStack is) {
        try {
            return getCraftItemStackClass().getMethod("asNMSCopy", ItemStack.class).invoke(null, is);
        } catch (Exception ex) {
            EzAuctions.getPluginLogger().log(Level.WARNING,
                    "Error getting item as NMS copy. Item: " + is.getType().toString(),
                    ex);
            return null;
        }
    }

    private static Class<?> getCraftItemStackClass() {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

        try {
            return Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static Class<?> getNMSClass(String name) {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

        try {
            return Class.forName("net.minecraft.server." + version + "." + name);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
