package one.lindegaard.MobHunting.compatibility;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.mobs.MobPlugin;
import one.lindegaard.MobHunting.rewards.MobRewardData;
import me.eccentric_nz.tardisweepingangels.TARDISWeepingAngelSpawnEvent;
import me.eccentric_nz.tardisweepingangels.TARDISWeepingAngels;
import me.eccentric_nz.tardisweepingangels.utils.Monster;

public class TARDISWeepingAngelsCompat implements Listener {

	private static TARDISWeepingAngels mPlugin;
	private static boolean supported = false;
	private static HashMap<String, MobRewardData> mMobRewardData = new HashMap<String, MobRewardData>();
	private File file = new File(MobHunting.getInstance().getDataFolder(), "TARDISWeepingAngels-rewards.yml");
	private YamlConfiguration config = new YamlConfiguration();
	public static final String MH_TARDISWEEPINGANGELS = "MH:TARDISWeepingAngels";

	// http://dev.bukkit.org/bukkit-plugins/tardisweepingangels/

	public TARDISWeepingAngelsCompat() {
		if (isDisabledInConfig()) {
			Bukkit.getLogger().info("[MobHunting] Compatibility with TARDISWeepingAngels is disabled in config.yml");
		} else {
			mPlugin = (TARDISWeepingAngels) Bukkit.getPluginManager().getPlugin("TARDISWeepingAngels");

			Bukkit.getLogger().info("[MobHunting] Enabling compatibility with TARDISWeepingAngelsAPI ("
					+ mPlugin.getDescription().getVersion() + ")");

			supported = true;

			Bukkit.getPluginManager().registerEvents(this, MobHunting.getInstance());

			loadTARDISWeepingAngelsMobsData();
			saveTARDISWeepingAngelsMobsData();
		}
	}

	// **************************************************************************
	// OTHER
	// **************************************************************************

	public static TARDISWeepingAngels getTARDISWeepingAngels() {
		return mPlugin;
	}

	public static boolean isSupported() {
		return supported;
	}

	public static boolean isDisabledInConfig() {
		return MobHunting.getConfigManager().disableIntegrationTARDISWeepingAngels;
	}

	public static boolean isEnabledInConfig() {
		return !MobHunting.getConfigManager().disableIntegrationTARDISWeepingAngels;
	}

	/**
	 * Returns whether an entity is a TARDISWeepingAngels entity.
	 *
	 * @param entity
	 *            the entity to check
	 * @return true if the entity is a TARDISWeepingAngels entity
	 */
	public static boolean isWeepingAngelMonster(Entity entity) {
		if (isSupported())
			return entity.hasMetadata(TARDISWeepingAngelsCompat.MH_TARDISWEEPINGANGELS);
		return false;
		// return ((TARDISWeepingAngels)
		// mPlugin).getWeepingAngelsAPI().isWeepingAngelMonster(entity);
	}

	/**
	 * Returns the Monster type for a TARDISWeepingAngels entity.
	 *
	 * @param entity
	 *            the entity to get the Monster type for
	 * @return the Monster type or null if it is not TARDISWeepingAngels entity
	 */
	public static Monster getWeepingAngelMonsterType(Entity entity) {
		return ((TARDISWeepingAngels) mPlugin).getWeepingAngelsAPI().getWeepingAngelMonsterType(entity);
	}

	public static HashMap<String, MobRewardData> getMobRewardData() {
		return mMobRewardData;
	}

	// **************************************************************************
	// LOAD & SAVE
	// **************************************************************************
	public void loadTARDISWeepingAngelsMobsData() {
		try {
			if (!file.exists()) {
				for (Monster monster : Monster.getValues()) {
					mMobRewardData.put(monster.name(),
							new MobRewardData(MobPlugin.TARDISWeepingAngels, monster.name(), monster.getName(), "40:60",
									"minecraft:give {player} iron_sword 1", "You got an Iron sword.", 1));
					saveTARDISWeepingAngelsMobsData(mMobRewardData.get(monster.name()).getMobType());
				}
				return;
			}

			config.load(file);
			for (String key : config.getKeys(false)) {
				ConfigurationSection section = config.getConfigurationSection(key);
				MobRewardData mob = new MobRewardData();
				mob.read(section);
				mob.setMobType(key);
				mMobRewardData.put(key, mob);
				MobHunting.getStoreManager().insertTARDISWeepingAngelsMobs(key);
			}
			Messages.debug("Loaded %s TARDISWeepingAngels-Mobs", mMobRewardData.size());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}

	}

	public void loadTARDISWeepingAngelsMobsData(String key) {
		try {
			if (!file.exists()) {
				return;
			}

			config.load(file);
			ConfigurationSection section = config.getConfigurationSection(key);
			MobRewardData mob = new MobRewardData();
			mob.read(section);
			mob.setMobType(key);
			mMobRewardData.put(key, mob);
			MobHunting.getStoreManager().insertTARDISWeepingAngelsMobs(key);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	public void saveTARDISWeepingAngelsMobsData() {
		try {
			config.options().header("This a extra MobHunting config data for the TARDISWeepingAngels on your server.");

			if (mMobRewardData.size() > 0) {

				int n = 0;
				for (String str : mMobRewardData.keySet()) {
					ConfigurationSection section = config.createSection(str);
					mMobRewardData.get(str).save(section);
					n++;
				}

				if (n != 0) {
					Messages.debug("Saving Mobhunting extra TARDISWeepingAngels data.");
					config.save(file);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveTARDISWeepingAngelsMobsData(String key) {
		try {
			if (mMobRewardData.containsKey(key)) {
				ConfigurationSection section = config.createSection(key);
				mMobRewardData.get(key).save(section);
				Messages.debug("Saving extra TARDISWeepingAngels data for mob=%s (%s)", key,
						mMobRewardData.get(key).getMobName());
				config.save(file);
			} else {
				Messages.debug("ERROR! TARDISWeepingAngels ID (%s) is not found in mMobRewardData", key);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// **************************************************************************
	// EVENTS
	// **************************************************************************

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	private void onTARDISWeepingAngelSpawnEvent(TARDISWeepingAngelSpawnEvent event) {

		Entity entity = event.getEntity();
		Monster monster = getWeepingAngelMonsterType(entity);

		if (mMobRewardData != null && !mMobRewardData.containsKey(monster.name())) {
			Messages.debug("New TARDIS mob found=%s (%s)", monster.name(), monster.getName());
			mMobRewardData.put(monster.name(), new MobRewardData(MobPlugin.TARDISWeepingAngels, monster.name(),
					monster.getName(), "40:60", "minecraft:give {player} iron_sword 1", "You got an Iron sword.", 1));
			saveTARDISWeepingAngelsMobsData(monster.name());
			MobHunting.getStoreManager().insertTARDISWeepingAngelsMobs(monster.name);
			// Update mob loaded into memory
			MobHunting.getExtendedMobManager().updateExtendedMobs();
		}

		event.getEntity().setMetadata(MH_TARDISWEEPINGANGELS,
				new FixedMetadataValue(mPlugin, mMobRewardData.get(monster.name())));
	}

}
