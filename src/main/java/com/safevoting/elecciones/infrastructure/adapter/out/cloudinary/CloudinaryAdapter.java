package com.safevoting.elecciones.infrastructure.adapter.out.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.safevoting.elecciones.domain.repository.ImageStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.Map;

@Component
public class CloudinaryAdapter implements ImageStorageService {

    private final Cloudinary cloudinary;
    private final long timeoutSeconds;

    public CloudinaryAdapter(@Value("${cloudinary.cloud-name}") String cloudName,
                             @Value("${cloudinary.api-key}") String apiKey,
                             @Value("${cloudinary.api-secret}") String apiSecret,
                             @Value("${cloudinary.timeout-seconds}") long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    @Override
    public Mono<String> upload(byte[] image) {
        String base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(image);
        return Mono.fromCallable(() -> {
                    Map<?, ?> uploadResult = cloudinary.uploader().upload(base64Image, ObjectUtils.emptyMap());
                    return (String) uploadResult.get("secure_url");
                })
                .timeout(java.time.Duration.ofSeconds(timeoutSeconds))
                .onErrorResume(e -> Mono.empty());
    }
}
