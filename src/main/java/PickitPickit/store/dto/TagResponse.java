package PickitPickit.store.dto;

import PickitPickit.store.domain.Tag;

public record TagResponse(
        Long tagId,
        String name
) {
    public static TagResponse from(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName());
    }
}
