package com.expensemanager.service;

import com.expensemanager.cache.TagCache;
import com.expensemanager.dto.TagDTO;
import com.expensemanager.exception.ResourceNotFoundException;
import com.expensemanager.model.Tag;
import com.expensemanager.repository.TagRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TagService {

	private final TagRepository tagRepository;
	private final TagCache tagCache;

	@Autowired
	public TagService(TagRepository tagRepository, TagCache tagCache) {
		this.tagRepository = tagRepository;
		this.tagCache = tagCache;
	}

	public List<Tag> getAllTags() {
		List<Tag> tags = tagRepository.findAll();
		log.info("Retrieved {} tags from repository", tags.size());
		for (Tag tag : tags) {
			if (tagCache.get(tag.getId()) == null) {
				tagCache.put(tag.getId(), tag);
				log.info("Tag with id {} added to cache", tag.getId());
			} else {
				log.info("Tag with id {} already exists in cache", tag.getId());
			}
		}
		return tags;
	}

	public Tag getTagById(Long id) {
		Tag cachedTag = tagCache.get(id);
		if (cachedTag != null) {
			log.info("Tag with id {} retrieved from cache", id);
			return cachedTag;
		}
		Tag tag = tagRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Tag with ID " + id + " not found"));
		tagCache.put(id, tag);
		log.info("Tag with id {} retrieved from repository and added to cache in getTagById", id);
		return tag;
	}

	public Tag createTag(TagDTO tagDTO) {
		log.info("Creating tag with name={}", tagDTO.getName());
		Tag tag = new Tag();
		tag.setName(tagDTO.getName());
		Tag savedTag = tagRepository.save(tag);
		tagCache.put(savedTag.getId(), savedTag);
		log.info("Tag with id {} created and added to cache in createTag", savedTag.getId());
		return savedTag;
	}

	public Tag updateTag(Long id, TagDTO tagDTO) {
		Tag existing = getTagById(id);
		log.info("Updating tag id={} with new name={}", id, tagDTO.getName());
		existing.setName(tagDTO.getName());
		Tag updatedTag = tagRepository.save(existing);
		tagCache.put(id, updatedTag);
		log.info("Tag with id {} updated and cache refreshed", id);
		return updatedTag;
	}

	public void deleteTag(Long id) {
		Tag existing = getTagById(id);
		log.warn("Deleting tag id={}", id);
		tagRepository.delete(existing);
		tagCache.remove(id);
		log.info("Tag with id {} removed from cache", id);
	}
}
