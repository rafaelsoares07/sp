package dev.rafael.cadastro;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        exposeDirectory("photos", registry);
        exposeDirectory("sounds", registry);
    }

    private void exposeDirectory(String folderName, ResourceHandlerRegistry registry) {
        Path path = Paths.get(folderName).toAbsolutePath().normalize();
        String location = "file:" + path.toString() + "/";
        registry.addResourceHandler("/" + folderName + "/**")
                .addResourceLocations(location);
    }
}

