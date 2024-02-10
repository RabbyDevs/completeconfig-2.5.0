package me.lortseam.completeconfig.data;

import com.google.common.collect.Iterables;
import me.lortseam.completeconfig.api.ConfigContainer;
import me.lortseam.completeconfig.test.data.containers.*;
import me.lortseam.completeconfig.test.data.listeners.EmptyEntryListener;
import me.lortseam.completeconfig.test.data.listeners.ContainerListener;
import me.lortseam.completeconfig.test.data.listeners.SetterEntryListener;
import me.lortseam.completeconfig.text.TranslationBase;
import me.lortseam.completeconfig.text.TranslationKey;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ParentTest {

    private Parent parent;

    @BeforeEach
    public void beforeEach() {
        parent = new Parent() {
            @Override
            public TranslationKey getNameTranslation() {
                return mock(TranslationKey.class);
            }

            @Override
            Config getRoot() {
                var config = mock(Config.class);
                var registry = new ConfigRegistry();
                when(config.getRegistry()).thenReturn(registry);
                return config;
            }

            @Override
            public TranslationKey getBaseTranslation(TranslationBase translationBase, @Nullable Class<? extends ConfigContainer> clazz) {
                return mock(TranslationKey.class);
            }
        };
    }

    @Test
    public void resolve_includeFieldIfAnnotated() {
        parent.resolve(new ContainerWithEntry(), new ContainerWithContainerWithEntry(), new ContainerWithGroupWithEntry());
        assertEquals(2, parent.getEntries().size());
        assertEquals(1, parent.getClusters().size());
    }

    @Test
    public void resolve_excludeFieldIfNotAnnotated() {
        parent.resolve(new ContainerWithField());
        assertTrue(parent.isEmpty());
    }

    @Test
    public void resolve_includeFieldInEntries() {
        parent.resolve(new IncludingEntriesContainerWithEntry());
        assertEquals(1, parent.getEntries().size());
    }

    @Test
    public void resolve_excludeFieldInEntriesIfContainer() {
        parent.resolve(new IncludingEntriesContainerWithEmptyContainer());
        assertTrue(parent.isEmpty());
    }

    @Test
    public void resolve_excludeFieldInEntriesIfExcludeAnnotated() {
        parent.resolve(new IncludingEntriesContainerWithExcludedField());
        assertTrue(parent.isEmpty());
    }

    @Test
    public void resolve_excludeFieldInEntriesIfTransient() {
        parent.resolve(new IncludingEntriesContainerWithTransientField());
        assertTrue(parent.isEmpty());
    }

    @Test
    public void resolve_excludeFieldIfNotIncludeAll() {
        parent.resolve(new EntriesContainerWithField());
        assertTrue(parent.isEmpty());
    }

    @Test
    public void resolve_includeSuperclassFieldIfNonStatic() {
        parent.resolve(new SubclassOfContainerWithEntry(), new SubclassOfContainerWithContainerWithEntry());
        assertEquals(2, parent.getEntries().size());
    }

    @Test
    public void resolve_excludeSuperclassFieldIfStatic() {
        parent.resolve(new SubclassOfContainerWithStaticEntry(), new SubclassOfContainerWithStaticContainerWithEntry());
        assertTrue(parent.isEmpty());
    }

    @Test
    public void resolve_includeFromMethod() {
        parent.resolve(new ContainerRegisteringContainerWithEntry(), new ContainerRegisteringGroupWithEntry());
        assertEquals(1, parent.getEntries().size());
        assertEquals(1, parent.getClusters().size());
    }

    @Test
    public void resolve_includeNestedIfStatic() {
        parent.resolve(new ContainerNestingStaticContainerWithEntry());
        assertEquals(1, parent.getEntries().size());
    }

    @Test
    public void resolve_throwIfNestedNonContainer() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> parent.resolve(new ContainerNestingStaticClass()));
        assertEquals("Transitive " + ContainerNestingStaticClass.Class.class + " must implement " + ConfigContainer.class.getSimpleName(), exception.getMessage());
    }

    @Test
    public void resolve_throwIfNestedNonStatic() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> parent.resolve(new ContainerNestingContainerWithEntry()));
        assertEquals("Transitive " + ContainerNestingContainerWithEntry.Container.class + " must be static", exception.getMessage());
    }

    @Test
    public void resolve_listenEntrySetter() {
        SetterEntryListener listener = new SetterEntryListener();
        parent.resolve(listener);
        boolean value = !listener.getValue();
        Iterables.getOnlyElement(parent.getEntries()).setValue(value);
        assertEquals(value, listener.getValue());
    }

    @Test
    public void resolve_doNotUpdateEntryListenerField() {
        EmptyEntryListener listener = new EmptyEntryListener();
        parent.resolve(listener);
        boolean oldValue = listener.getValue();
        Iterables.getOnlyElement(parent.getEntries()).setValue(!oldValue);
        assertEquals(oldValue, listener.getValue());
    }

    @Test
    public void resolve_listenContainer() {
        var listener = new ContainerListener();
        parent.resolve(listener);
        boolean value = listener.getValue();
        Iterables.getOnlyElement(parent.getEntries()).setValue(!value);
        assertTrue(listener.isCalled());
    }

}
