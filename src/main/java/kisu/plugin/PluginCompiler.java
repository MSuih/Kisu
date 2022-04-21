package kisu.plugin;

import kisu.exceptions.CompilationFailedException;
import mouse.KisuPlugin;
import mouse.PluginInformation;
import org.joor.CompileOptions;
import org.joor.Reflect;
import org.joor.ReflectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pinej.PineEnums;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class PluginCompiler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public CompiledPlugin compile(String classname, String code) throws CompilationFailedException {
        var processor = new AbstractProcessor() {
            public CompiledPlugin.Information information = null;

            @Override
            public SourceVersion getSupportedSourceVersion() {
                return SourceVersion.latestSupported();
            }

            @Override
            public Set<String> getSupportedAnnotationTypes() {
                return Set.of("mouse.PluginInformation");
            }

            @Override
            public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
                Optional<PluginInformation> pluginInformation = roundEnv.getElementsAnnotatedWith(PluginInformation.class).stream().findAny().map(e -> e.getAnnotation(PluginInformation.class));
                if (pluginInformation.isPresent()) {
                    if (information != null) {
                        throw new IllegalStateException("Found multiple PluginInformation annotations within " + classname);
                    }
                    information = convert(pluginInformation.get());
                    return true;
                } else {
                    return false;
                }
            }
        };
        CompileOptions options = new CompileOptions().processors(processor);
        try {
            logger.info("Compiling {}", classname);
            KisuPlugin plugin = Reflect.compile(classname, code, options).create().get();
            return new CompiledPlugin(plugin, processor.information);
        } catch (ReflectException e) {
            throw new CompilationFailedException("Compilation of " + classname + " failed, check log for details", e);
        }
    }

    public CompiledPlugin.Information convert(PluginInformation information) {
        PineEnums.TargetPlatform platform;
        if (Objects.equals(information.platform(), "PS2")) {
            platform = PineEnums.TargetPlatform.PS2;
        } else if (Objects.equals(information.platform(), "PS3")) {
            platform = PineEnums.TargetPlatform.PS3;
        } else {
            throw new IllegalArgumentException("Unknown platform " + information.platform());
        }
        return new CompiledPlugin.Information(
                platform,
                information.name(),
                information.author(),
                information.version(),
                information.description(),
                information.game().serial(),
                information.game().version(),
                information.game().hash());
    }
}
