package me.lortseam.completeconfig.data;

import com.google.common.collect.Lists;
import lombok.NonNull;
import me.lortseam.completeconfig.CompleteConfig;
import me.lortseam.completeconfig.data.extension.DataExtension;
import me.lortseam.completeconfig.data.transform.Transformation;

import java.util.*;

public final class ConfigRegistry {

    private static final Set<Config> configs = new HashSet<>();
    private static final Map<String, Config> mainConfigs = new HashMap<>();
    private static final Set<EntryOrigin> origins = new HashSet<>();

    static void registerConfig(Config config) {
        if (!configs.add(config)) {
            throw new RuntimeException(config + " already exists");
        }
        String modId = config.getMod().getId();
        if (!mainConfigs.containsKey(modId)) {
            mainConfigs.put(modId, config);
        } else {
            mainConfigs.put(modId, null);
        }
    }

    static void registerEntryOrigin(EntryOrigin origin) {
        if (origins.contains(origin)) {
            throw new RuntimeException(origin.getField() + " was already resolved");
        }
        origins.add(origin);
    }

    /**
     * Sets the main config for a mod.
     *
     * <p>If a mod has only one config registered, that config is the main one. Therefore, setting the main config is
     * only required when a mod has two or more configs.
     *
     * @param config the main config
     */
    public static void setMainConfig(@NonNull Config config) {
        mainConfigs.put(config.getMod().getId(), config);
    }

    public static Map<String, Config> getMainConfigs() {
        return Collections.unmodifiableMap(mainConfigs);
    }

    private final List<Transformation> transformations = Lists.newArrayList(Transformation.DEFAULTS);

    ConfigRegistry() {
        for (Collection<Transformation> transformations : CompleteConfig.collectExtensions(DataExtension.class, DataExtension::getTransformations)) {
            registerTransformations(transformations);
        }
    }

    void registerTransformations(Collection<Transformation> transformations) {
        this.transformations.addAll(transformations);
    }

    List<Transformation> getTransformations() {
        return new ArrayList<>(transformations);
    }

}
