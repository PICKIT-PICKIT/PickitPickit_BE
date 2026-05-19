package PickitPickit.onboarding.service;

import PickitPickit.onboarding.dto.response.DefaultProfileImageResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultProfileImageCatalog {

    private static final List<DefaultProfileImageResponse> DEFAULT_IMAGES = List.of(
            new DefaultProfileImageResponse("DEFAULT_1", "/images/profile-defaults/default-1.png"),
            new DefaultProfileImageResponse("DEFAULT_2", "/images/profile-defaults/default-2.png"),
            new DefaultProfileImageResponse("DEFAULT_3", "/images/profile-defaults/default-3.png"),
            new DefaultProfileImageResponse("DEFAULT_4", "/images/profile-defaults/default-4.png"),
            new DefaultProfileImageResponse("DEFAULT_5", "/images/profile-defaults/default-5.png"),
            new DefaultProfileImageResponse("DEFAULT_6", "/images/profile-defaults/default-6.png")
    );

    public List<DefaultProfileImageResponse> getDefaultImages() {
        return DEFAULT_IMAGES;
    }

    public boolean containsImageUrl(String imageUrl) {
        return DEFAULT_IMAGES.stream()
                .anyMatch(defaultImage -> defaultImage.imageUrl().equals(imageUrl));
    }
}
