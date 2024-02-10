package me.lortseam.completeconfig.gui.yacl;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import me.lortseam.completeconfig.CompleteConfig;
import me.lortseam.completeconfig.data.*;
import me.lortseam.completeconfig.gui.ConfigScreenBuilder;
import me.lortseam.completeconfig.gui.GuiProvider;
import me.lortseam.completeconfig.gui.yacl.controller.ListController;
import net.minecraft.client.gui.screen.Screen;

import java.awt.*;
import java.util.Collection;
import java.util.List;

/**
 * A screen builder based on the YetAnotherConfigLib library.
 */
public final class YaclScreenBuilder extends ConfigScreenBuilder<ControllerFunction<?>> {

    private static final List<GuiProvider<ControllerFunction<?>>> globalProviders = Lists.newArrayList(
            GuiProvider.create(BooleanEntry.class, entry -> (Option<Boolean> option) -> {
                         var builder = BooleanControllerBuilder.create(option);
                         entry.getValueFormatter().ifPresent(formatter -> {
                             builder.valueFormatter(formatter)
                                     // Don't use default colors if custom formatter is present
                                     .coloured(false);
                         });
                         return builder;
                    },
                    (BooleanEntry entry) -> !entry.isCheckbox(), boolean.class, Boolean.class),
            GuiProvider.create(BooleanEntry.class, entry -> (Option<Boolean> option) -> TickBoxControllerBuilder.create(option),
                    BooleanEntry::isCheckbox, boolean.class, Boolean.class),
            GuiProvider.create((Entry<Integer> entry) -> (Option<Integer> option) -> {
                        var builder = IntegerFieldControllerBuilder.create(option);
                        entry.getValueFormatter().ifPresent(builder::valueFormatter);
                        return builder;
                    },
                    int.class, Integer.class),
            GuiProvider.create((Entry<Long> entry) -> (Option<Long> option) -> {
                        var builder = LongFieldControllerBuilder.create(option);
                        entry.getValueFormatter().ifPresent(builder::valueFormatter);
                        return builder;
                    },
                    long.class, Long.class),
            GuiProvider.create((Entry<Float> entry) -> (Option<Float> option) -> {
                        var builder = FloatFieldControllerBuilder.create(option);
                        entry.getValueFormatter().ifPresent(builder::valueFormatter);
                        return builder;
                    },
                    float.class, Float.class),
            GuiProvider.create((Entry<Double> entry) -> (Option<Double> option) -> {
                        var builder = DoubleFieldControllerBuilder.create(option);
                        entry.getValueFormatter().ifPresent(builder::valueFormatter);
                        return builder;
                    },
                    double.class, Double.class),
            GuiProvider.create(BoundedEntry.class, (BoundedEntry<Integer> entry) -> (Option<Integer> option) -> {
                        var builder = IntegerFieldControllerBuilder.create(option)
                                .min(entry.getMin())
                                .max(entry.getMax());
                        entry.getValueFormatter().ifPresent(builder::valueFormatter);
                        return builder;
                    },
                    int.class, Integer.class),
            GuiProvider.create(BoundedEntry.class, (BoundedEntry<Long> entry) -> (Option<Long> option) -> {
                        var builder = LongFieldControllerBuilder.create(option)
                                .min(entry.getMin())
                                .max(entry.getMax());
                        entry.getValueFormatter().ifPresent(builder::valueFormatter);
                        return builder;
                    },
                    long.class, Long.class),
            GuiProvider.create(BoundedEntry.class, (BoundedEntry<Float> entry) -> (Option<Float> option) -> {
                        var builder = FloatFieldControllerBuilder.create(option)
                                .min(entry.getMin())
                                .max(entry.getMax());
                        entry.getValueFormatter().ifPresent(builder::valueFormatter);
                        return builder;
                    },
                    float.class, Float.class),
            GuiProvider.create(BoundedEntry.class, (BoundedEntry<Double> entry) -> (Option<Double> option) -> {
                        var builder = DoubleFieldControllerBuilder.create(option)
                                .min(entry.getMin())
                                .max(entry.getMax());
                        entry.getValueFormatter().ifPresent(builder::valueFormatter);
                        return builder;
                    },
                    double.class, Double.class),
            GuiProvider.create(SliderEntry.class, (SliderEntry<Integer> entry) -> (Option<Integer> option) -> {
                        var builder = IntegerSliderControllerBuilder.create(option)
                                .range(entry.getMin(), entry.getMax())
                                .step(entry.getInterval().orElse(1));
                        entry.getValueFormatter().ifPresent(builder::valueFormatter);
                        return builder;
                    },
                    int.class, Integer.class),
            GuiProvider.create(SliderEntry.class, (SliderEntry<Long> entry) -> (Option<Long> option) -> {
                        var builder = LongSliderControllerBuilder.create(option)
                                .range(entry.getMin(), entry.getMax())
                                .step(entry.getInterval().orElse(1L));
                        entry.getValueFormatter().ifPresent(builder::valueFormatter);
                        return builder;
                    },
                    long.class, Long.class),
            GuiProvider.create(SliderEntry.class, (SliderEntry<Float> entry) -> (Option<Float> option) -> {
                        var builder = FloatSliderControllerBuilder.create(option)
                                .range(entry.getMin(), entry.getMax())
                                .step(entry.getInterval().orElse(0.1f));
                        entry.getValueFormatter().ifPresent(builder::valueFormatter);
                        return builder;
                    },
                    float.class, Float.class),
            GuiProvider.create(SliderEntry.class, (SliderEntry<Double> entry) -> (Option<Double> option) -> {
                        var builder = DoubleSliderControllerBuilder.create(option)
                                .range(entry.getMin(), entry.getMax())
                                .step(entry.getInterval().orElse(0.01));
                        entry.getValueFormatter().ifPresent(builder::valueFormatter);
                        return builder;
                    },
                    double.class, Double.class),
            GuiProvider.create(entry -> (Option<String> option) -> StringControllerBuilder.create(option),
                    String.class),
            GuiProvider.create(EnumEntry.class, (EnumEntry<?> entry) -> (Option<Enum<?>> option) -> {
                        var builder = EnumControllerBuilder.create((Option) option)
                                .enumClass(entry.getTypeClass());
                        entry.getValueFormatter().ifPresent(builder::valueFormatter);
                        return builder;
                    }),
            GuiProvider.create(ColorEntry.class, (ColorEntry<Color> entry) -> (Option<Color> option) -> ColorControllerBuilder.create(option)
                            .allowAlpha(entry.isAlphaMode()),
                    Color.class),
            GuiProvider.create((Entry<List<Integer>> entry) -> (Option<List<Integer>> listOption) -> ListController.createBuilder(
                    IntegerFieldControllerBuilder::create
            ), new TypeToken<List<Integer>>() {}.getType()),
            GuiProvider.create((Entry<List<Long>> entry) -> (Option<List<Long>> listOption) -> ListController.createBuilder(
                    LongFieldControllerBuilder::create
            ), new TypeToken<List<Long>>() {}.getType()),
            GuiProvider.create((Entry<List<Float>> entry) -> (Option<List<Float>> listOption) -> ListController.createBuilder(
                    FloatFieldControllerBuilder::create
            ), new TypeToken<List<Float>>() {}.getType()),
            GuiProvider.create((Entry<List<Double>> entry) -> (Option<List<Double>> listOption) -> ListController.createBuilder(
                    DoubleFieldControllerBuilder::create
            ), new TypeToken<List<Double>>() {}.getType()),
            GuiProvider.create((Entry<List<String>> entry) -> (Option<List<String>> listOption) -> ListController.createBuilder(
                    StringControllerBuilder::create
            ), new TypeToken<List<String>>() {}.getType())
    );

    static {
        for (Collection<GuiProvider<ControllerFunction<?>>> providers : CompleteConfig.collectExtensions(YaclGuiExtension.class, YaclGuiExtension::getProviders)) {
            globalProviders.addAll(providers);
        }
    }

    public YaclScreenBuilder() {
        super(globalProviders);
    }

    @Override
    public Screen build(Screen parentScreen, Config config) {
        var configBuilder = YetAnotherConfigLib.createBuilder()
                .title(getTitle(config))
                .save(config::save);
        if (!config.getEntries().isEmpty()) {
            // If there is only one cluster, use the config title for the cluster name
            var name = config.getClusters().isEmpty() ? getTitle(config) : config.getName();
            var categoryBuilder = ConfigCategory.createBuilder()
                    .name(name);
            for (Entry<?> entry : config.getEntries()) {
                categoryBuilder.option(buildOption(entry));
            }
            configBuilder.category(categoryBuilder.build());
        }
        for (var cluster : config.getClusters()) {
            var categoryBuilder = ConfigCategory.createBuilder()
                    .name(cluster.getName());
            cluster.getDescription().ifPresent(categoryBuilder::tooltip);
            for (Entry<?> entry : cluster.getEntries()) {
                categoryBuilder.option(buildOption(entry));
            }
            for (var subCluster : cluster.getClusters()) {
                var groupBuilder = OptionGroup.createBuilder()
                        .name(subCluster.getName());
                subCluster.getDescription().ifPresent(description -> groupBuilder.description(OptionDescription.of(description)));
                for (Entry<?> entry : subCluster.getEntries()) {
                    groupBuilder.option(buildOption(entry));
                }
                if (!subCluster.getClusters().isEmpty()) {
                    throw new UnsupportedOperationException("YACL screen builder doesn't support more than 2 levels of groups");
                }
                categoryBuilder.group(groupBuilder.build());
            }
            configBuilder.category(categoryBuilder.build());
        }
        return configBuilder.build().generateScreen(parentScreen);
    }

    private <T> Option<T> buildOption(Entry<T> entry) {
        Controller<T> parentController = null;
        try {
            parentController = (Controller<T>) createEntry(entry).apply(null).build();
        } catch (NullPointerException ignore) {}
        if (parentController instanceof ListController<?>) {
            return buildListOption((Entry) entry, (ListController) parentController);
        }
        var builder = Option.<T>createBuilder()
                .name(entry.getName())
                .binding(entry.getDefaultValue(), entry::getValue, entry::setValue)
                .controller(option -> ((ControllerFunction<T>) createEntry(entry)).apply(option));
        entry.getDescription().ifPresent(description -> builder.description(OptionDescription.of(description)));
        if (entry.requiresRestart()) {
            builder.flag(OptionFlag.GAME_RESTART);
        }
        return builder.build();
    }

    private <E, T extends List<E>> Option<T> buildListOption(Entry<T> entry, ListController<E> controller) {
        Class<E> elementClass = (Class<E>) entry.getGenericTypes()[0];
        E initialValue = controller.getInitialValue();
        if (initialValue == null) {
            if (elementClass == Boolean.class) {
                initialValue = (E) Boolean.FALSE;
            } else if (elementClass == Integer.class) {
                initialValue = (E) Integer.valueOf(0);
            } else if (elementClass == Long.class) {
                initialValue = (E) Long.valueOf(0);
            } else if (elementClass == Float.class) {
                initialValue = (E) Float.valueOf(0);
            } else if (elementClass == Double.class) {
                initialValue = (E) Double.valueOf(0);
            } else if (elementClass == String.class) {
                initialValue = (E) "";
            }
        }
        var builder = ListOption.createBuilder(elementClass)
                .name(entry.getName())
                .binding(entry.getDefaultValue(), entry::getValue, (value) -> entry.setValue((T) value))
                .controller(controller.getElementControllerBuilder())
                .initial(initialValue);
        entry.getDescription().ifPresent(description -> builder.description(OptionDescription.of(description)));
        if (entry.requiresRestart()) {
            builder.flag(OptionFlag.GAME_RESTART);
        }
        return (Option<T>) builder.build();
    }

}
