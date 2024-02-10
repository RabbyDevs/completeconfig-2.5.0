package me.lortseam.completeconfig.test.extension;

import com.google.common.jimfs.Jimfs;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.junit.jupiter.api.extension.Extension;

import java.util.Optional;

import static org.mockito.Mockito.*;

public class FabricLoaderExtension implements Extension {

    static {
        EnvType env = switch (System.getProperty("fabric.dli.env")) {
            case "client" -> EnvType.CLIENT;
            case "server" -> EnvType.SERVER;
            default -> throw new IllegalArgumentException("Unknown environment property");
        };
        FabricLoader loader = mock(FabricLoader.class);
        when(loader.getEnvironmentType()).thenReturn(env);
        when(loader.isModLoaded(anyString())).thenReturn(true);
        when(loader.getModContainer(anyString())).thenAnswer(invocation -> {
            ModMetadata metadata = mock(ModMetadata.class);
            when(metadata.getId()).thenReturn(invocation.getArgument(0, String.class));
            ModContainer mod = mock(ModContainer.class);
            when(mod.getMetadata()).thenReturn(metadata);
            return Optional.of(mod);
        });
        when(loader.getConfigDir()).thenReturn(Jimfs.newFileSystem().getPath(""));
        mockStatic(FabricLoader.class).when(FabricLoader::getInstance).thenReturn(loader);
    }

}
