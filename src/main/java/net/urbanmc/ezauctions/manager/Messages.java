package net.urbanmc.ezauctions.manager;

import net.urbanmc.ezauctions.EzAuctions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.*;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class Messages {

    private static Messages instance = new Messages();

    private final File FILE = new File(EzAuctions.getDataDirectory(), "messages.properties");

    private ResourceBundle bundle;

    private Messages() {
        createFile();
        loadBundle();
    }

    public static Messages getInstance() {
        return instance;
    }

    public static String getString(String key, Object... args) {
        return instance.getStringFromBundle(key, args);
    }

    private void createFile() {
        if (!FILE.getParentFile().isDirectory()) {
            FILE.getParentFile().mkdir();
        }

        if (!FILE.exists()) {
            try {
                FILE.createNewFile();

                InputStream input = getClass().getClassLoader().getResourceAsStream("messages.properties");
                OutputStream output = new FileOutputStream(FILE);

                EzAuctions.copy(input, output);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadBundle() {
        try {
            InputStream input = new FileInputStream(FILE);
            Reader reader = new InputStreamReader(input, "UTF-8");

            bundle = new PropertyResourceBundle(reader);

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getStringFromBundle(String key, Object... args) {
        try {
            return format(bundle.getString(key), true, args);
        } catch (Exception e) {
            if (e instanceof MissingResourceException) {
                EzAuctions.getPluginLogger().severe("Missing message in message.properties! Message key: " + key);
            }
            else {
                EzAuctions.getPluginLogger().log(Level.SEVERE, "Error fetching key '" + key + "' in message.properties!", e);
            }
            return key;
        }
    }

    public String getStringWithoutColoring(String key, Object... args) {
        try {
            return format(bundle.getString(key), false, args);
        } catch (Exception e) {
            if (e instanceof MissingResourceException) {
                EzAuctions.getPluginLogger().severe("Missing message in message.properties! Message key: " + key);
            }
            else {
                EzAuctions.getPluginLogger().log(Level.SEVERE, "Error fetching key '" + key + "' in message.properties!", e);
            }
            return key;
        }
    }

    private String format(String message, boolean color, Object... args) {
        message = message.replace("{prefix}", bundle.getString("prefix"));

        if (args != null) {
            message = MessageFormat.format(message, args);
        }

        if (color) {
            message = ChatColor.translateAlternateColorCodes('&', message);
        }

        return message;
    }

    public void reload() {
        createFile();
        loadBundle();
    }
}
