package com.onarandombox.multiverseinventories.util.data;

import com.onarandombox.multiverseinventories.api.DataStrings;
import com.onarandombox.multiverseinventories.api.PlayerStats;
import com.onarandombox.multiverseinventories.api.profile.ContainerType;
import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.api.profile.ProfileType;
import com.onarandombox.multiverseinventories.api.share.ProfileEntry;
import com.onarandombox.multiverseinventories.api.share.Sharable;
import com.onarandombox.multiverseinventories.util.Logging;
import com.onarandombox.multiverseinventories.util.MinecraftTools;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Default implementation of a player profile, that is, the data per world/group/gamemode.
 */
class DefaultPlayerProfile implements PlayerProfile {

    private Map<Sharable, Object> data = new HashMap<Sharable, Object>();

    private ItemStack[] inventoryContents = new ItemStack[PlayerStats.INVENTORY_SIZE];
    private ItemStack[] armorContents = new ItemStack[PlayerStats.ARMOR_SIZE];

    private OfflinePlayer player;
    private ContainerType containerType;
    private String containerName;
    private ProfileType profileType;

    public DefaultPlayerProfile(ContainerType containerType, String containerName, ProfileType profileType, OfflinePlayer player) {
        this.containerType = containerType;
        this.profileType = profileType;
        this.containerName = containerName;
        this.player = player;
        armorContents = MinecraftTools.fillWithAir(armorContents);
        inventoryContents = MinecraftTools.fillWithAir(inventoryContents);
    }

    public DefaultPlayerProfile(ContainerType containerType, String containerName, ProfileType profileType, String playerName, Map playerData) {
        this(containerType, containerName, profileType, Bukkit.getOfflinePlayer(playerName));
        for (Object keyObj : playerData.keySet()) {
            String key = keyObj.toString();
            if (key.equalsIgnoreCase(DataStrings.PLAYER_STATS)) {
                this.parsePlayerStats(playerData.get(key).toString());
            } else {
                if (playerData.get(key) == null) {
                    Logging.fine("Player data '" + key + "' is null for: " + playerName);
                    continue;
                }
                try {
                    Sharable sharable = ProfileEntry.lookup(false, key);
                    if (sharable == null) {
                        Logging.fine("Player fileTag '" + key + "' is unrecognized!");
                        continue;
                    }
                    this.data.put(sharable, sharable.getSerializer().deserialize(playerData.get(key).toString()));
                } catch (Exception e) {
                    Logging.fine("Could not parse fileTag: '" + key + "' with value '" + playerData.get(key).toString() + "'");
                    Logging.fine(e.getMessage());
                }
            }
        }
        Logging.finer("Created player profile from map for '" + playerName + "'.");
    }

    /**
     * @param stats Parses these values to fill out this Profile.
     */
    protected void parsePlayerStats(String stats) {
        if (stats.isEmpty()) {
            return;
        }
        String[] statsArray = stats.split(DataStrings.GENERAL_DELIMITER);
        for (String stat : statsArray) {
            try {
                String[] statValues = DataStrings.splitEntry(stat);
                Sharable sharable = ProfileEntry.lookup(true, statValues[0]);
                this.data.put(sharable, sharable.getSerializer().deserialize(statValues[1]));
            } catch (Exception e) {
                Logging.warning("Could not parse stat: '" + stat + "' for player '" + getPlayer().getName() + "' for "
                        + getContainerType() + " '" + getContainerName() + "'");
                Logging.warning("Exception: " + e.getClass() + " Message: " + e.getMessage());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> playerData = new LinkedHashMap<String, Object>();
        StringBuilder statBuilder = new StringBuilder();
        for (Map.Entry<Sharable, Object> entry : this.data.entrySet()) {
            if (entry.getValue() != null) {
                if (entry.getKey().getSerializer() == null) {
                    continue;
                }
                Sharable sharable = entry.getKey();
                if (sharable.getProfileEntry().isStat()) {
                    if (!statBuilder.toString().isEmpty()) {
                        statBuilder.append(DataStrings.GENERAL_DELIMITER);
                    }
                    statBuilder.append(DataStrings.createEntry(sharable.getProfileEntry().getFileTag(),
                            sharable.getSerializer().serialize(entry.getValue())));
                } else {
                    playerData.put(sharable.getProfileEntry().getFileTag(),
                            sharable.getSerializer().serialize(entry.getValue()));
                }
            }
        }
        if (!statBuilder.toString().isEmpty()) {
            playerData.put(DataStrings.PLAYER_STATS, statBuilder.toString());
        }
        return playerData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContainerType getContainerType() {
        return this.containerType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContainerName() {
        return this.containerName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OfflinePlayer getPlayer() {
        return this.player;
    }

    @Override
    public <T> T get(Sharable<T> sharable) {
        return sharable.getType().cast(this.data.get(sharable));
    }

    @Override
    public <T> void set(Sharable<T> sharable, T value) {
        this.data.put(sharable, value);
    }

    @Override
    public ProfileType getProfileType() {
        return this.profileType;
    }
}

