package com.notegraph.api.service;

import com.notegraph.api.domain.Tag;
import com.notegraph.api.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<Tag> getTagsForWorkspace(UUID workspaceId) {
        return tagRepository.findByWorkspaceId(workspaceId);
    }

    @Transactional
    public Tag createTag(Tag tag) {
        if (tagRepository.existsByWorkspaceIdAndNameIgnoreCase(tag.getWorkspace().getId(), tag.getName())) {
            throw new IllegalArgumentException("Tag with this name already exists in the workspace");
        }
        return tagRepository.save(tag);
    }

    @Transactional
    public void deleteTag(UUID tagId) {
        tagRepository.deleteById(tagId);
    }
}
