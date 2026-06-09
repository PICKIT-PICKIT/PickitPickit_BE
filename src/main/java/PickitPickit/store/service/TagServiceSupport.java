package PickitPickit.store.service;

import PickitPickit.store.domain.Tag;
import PickitPickit.store.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TagServiceSupport {

    private final TagRepository tagRepository;

    public Tag getOrCreate(String name) {
        String normalizedName = Tag.normalizeForLookup(name);
        return tagRepository.findByNormalizedName(normalizedName)
                .orElseGet(() -> tagRepository.save(Tag.create(name)));
    }

    public List<Tag> getOrCreateAll(List<String> names) {
        if (names == null || names.isEmpty()) {
            return List.of();
        }

        Map<String, String> normalizedToDisplay = new LinkedHashMap<>();
        for (String name : names) {
            String normalized = Tag.normalizeForLookup(name);
            normalizedToDisplay.putIfAbsent(normalized, name.trim());
        }

        return normalizedToDisplay.values()
                .stream()
                .map(this::getOrCreate)
                .toList();
    }
}
