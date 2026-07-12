package at.flauschigesalex.animated_holo.core._loader;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;

@SuppressWarnings({"UnstableApiUsage", "unused"})
class AnimatedHoloPluginLoader implements PluginLoader {

    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        final var policy = new RepositoryPolicy(true, RepositoryPolicy.UPDATE_POLICY_ALWAYS, RepositoryPolicy.CHECKSUM_POLICY_FAIL);

        final var paperRepo = new RemoteRepository.Builder("paper", "default", "https://repo.papermc.io/repository/maven-public/")
                .setReleasePolicy(policy)
                .setSnapshotPolicy(policy)
                .build();
        final var flxRepo = new RemoteRepository.Builder("flx-library", "default", "https://repo.flauschigesalex.at/repository/maven-public/")
                .setReleasePolicy(policy)
                .setSnapshotPolicy(policy)
                .build();
        
        final var resolver = new MavenLibraryResolver();
        resolver.addDependency(new Dependency(new DefaultArtifact("org.jetbrains.kotlin:kotlin-stdlib:2.3.21"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0"), null));
        
        resolver.addDependency(new Dependency(new DefaultArtifact("at.flauschigesalex.lib.base:default-general:2.4.1"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("at.flauschigesalex.lib.base:default-file:3.1.0"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("at.flauschigesalex.lib.minecraft.paper:minecraft-paper-base:3.1.0"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("at.flauschigesalex.rinth:modrinth-version-api:1.0.0"), null));
        
        resolver.addRepository(paperRepo);
        resolver.addRepository(flxRepo);
        
        classpathBuilder.addLibrary(resolver);
    }
}
