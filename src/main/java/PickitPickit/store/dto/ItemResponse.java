package PickitPickit.store.dto;

import PickitPickit.store.domain.Item;

import java.util.List;

public record ItemResponse(
        Long itemId,
        String name,
        String category,
        String defaultImageUrl,
        List<TagResponse> tags
) {
    public static ItemResponse from(Item item, List<TagResponse> tags) {
        return new ItemResponse(
                item.getId(),
                item.getName(),
                item.getCategory().name(),
                item.getDefaultImageUrl(),
                tags
        );
    }
}
