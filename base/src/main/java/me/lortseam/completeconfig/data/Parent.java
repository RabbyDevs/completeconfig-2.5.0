package me.lortseam.completeconfig.data;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.lortseam.completeconfig.api.ConfigContainer;
import me.lortseam.completeconfig.api.ConfigGroup;
import me.lortseam.completeconfig.data.structure.Identifiable;
import me.lortseam.completeconfig.data.structure.StructurePart;
import me.lortseam.completeconfig.data.structure.client.Translatable;
import me.lortseam.completeconfig.text.TranslationBase;
import me.lortseam.completeconfig.text.TranslationKey;
import me.lortseam.completeconfig.util.ReflectionUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.ArrayList; 

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public abstract class Parent implements StructurePart, Translatable {

    private static <C extends StructurePart & Identifiable> void propagateToChildren(Collection<C> children, CommentedConfigurationNode node, Predicate<CommentedConfigurationNode> childNodeCondition, BiConsumer<C, CommentedConfigurationNode> function) {
        for (C child : children) {
            CommentedConfigurationNode childNode = node.node(child.getId());
            if (!childNodeCondition.test(childNode)) {
                continue;
            }
            function.accept(child, childNode);
        }
    }

    private static <C extends StructurePart & Identifiable> void propagateToChildren(Collection<C> children, CommentedConfigurationNode node, BiConsumer<C, CommentedConfigurationNode> function) {
        propagateToChildren(children, node, childNode -> true, function);
    }

    private final EntrySet entries = new EntrySet(this);
    private final ClusterSet clusters = new ClusterSet(this);

    abstract Config getRoot();

    public final Collection<Entry> getEntries() {
        return Collections.unmodifiableCollection(entries);
    }

    public final Collection<Cluster> getClusters() {
        return Collections.unmodifiableCollection(clusters);
    }

    final void resolveContainer(ConfigContainer container) {
        entries.resolve(container);
        for (Class<? extends ConfigContainer> clazz : container.getConfigClasses()) {
            Arrays.stream(clazz.getDeclaredFields()).filter(field -> {
                if (field.isAnnotationPresent(ConfigContainer.Transitive.class)) {
                    if (!ConfigContainer.class.isAssignableFrom(field.getType())) {
                        throw new RuntimeException("Transitive field " + field + " must implement " + ConfigContainer.class.getSimpleName());
                    }
                    return !Modifier.isStatic(field.getModifiers()) || clazz == container.getClass();
                }
                return false;
            }).map(field -> {
                if (!field.canAccess(container)) {
                    field.setAccessible(true);
                }
                try {
                    return (ConfigContainer) field.get(container);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }).forEach(this::resolve);
            Class<?>[] nestedClasses = clazz.getDeclaredClasses();
            nestedClasses =
            Collections.reverse(nestedClasses);
            Arrays.stream(nestedClasses).filter(nestedClass -> {
                if (nestedClass.isAnnotationPresent(ConfigContainer.Transitive.class)) {
                    if (!ConfigContainer.class.isAssignableFrom(nestedClass)) {
                        throw new RuntimeException("Transitive " + nestedClass + " must implement " + ConfigContainer.class.getSimpleName());
                    }
                    if (!Modifier.isStatic(nestedClass.getModifiers())) {
                        throw new RuntimeException("Transitive " + nestedClass + " must be static");
                    }
                    return true;
                }
                return false;
            }).map(nestedClass -> {
                try {
                    return (ConfigContainer) ReflectionUtils.instantiateClass(nestedClass);
                } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                    throw new RuntimeException("Failed to instantiate nested " + nestedClass, e);
                }
            }).forEach(this::resolve);
        }
        var transitives = container.getTransitives();
        if (transitives != null) {
            transitives.forEach(this::resolve);
        }
    }

    final void resolve(ConfigContainer... containers) {
        for (ConfigContainer container : containers) {
            if (container instanceof ConfigGroup) {
                clusters.resolve((ConfigGroup) container);
            } else {
                resolveContainer(container);
            }
        }
    }

    @Override
    public void apply(CommentedConfigurationNode node) {
        propagateToChildren(entries, node, childNode -> !childNode.isNull(), StructurePart::apply);
        propagateToChildren(clusters, node, childNode -> !childNode.isNull(), StructurePart::apply);
    }

    @Override
    public void fetch(CommentedConfigurationNode node) {
        propagateToChildren(entries, node, StructurePart::fetch);
        propagateToChildren(clusters, node, StructurePart::fetch);
    }

    final boolean isEmpty() {
        return entries.isEmpty() && clusters.isEmpty();
    }

    @Environment(EnvType.CLIENT)
    public abstract TranslationKey getBaseTranslation(TranslationBase translationBase, @Nullable Class<? extends ConfigContainer> clazz);

    @Environment(EnvType.CLIENT)
    public final TranslationKey getBaseTranslation() {
        return getBaseTranslation(TranslationBase.INSTANCE, null);
    }

}
