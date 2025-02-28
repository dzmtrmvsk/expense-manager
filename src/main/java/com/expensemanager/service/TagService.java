package com.expensemanager.service;

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

	@Autowired
	public TagService(TagRepository tagRepository) {
		this.tagRepository = tagRepository;
	}

	public List<Tag> getAllTags() {
		return tagRepository.findAll();
	}

	public Tag getTagById(Long id) {
		return tagRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Tag with ID " + id + " not found"));
	}

	public Tag createTag(TagDTO tagDTO) {
		log.info("Creating tag with name={}", tagDTO.getName());
		Tag tag = new Tag();
		tag.setName(tagDTO.getName());
		return tagRepository.save(tag);
	}

	public Tag updateTag(Long id, TagDTO tagDTO) {
		Tag existing = getTagById(id);
		log.info("Updating tag id={} with new name={}", id, tagDTO.getName());
		existing.setName(tagDTO.getName());
		return tagRepository.save(existing);
	}

	public void deleteTag(Long id) {
		Tag existing = getTagById(id);
		log.warn("Deleting tag id={}", id);
		tagRepository.delete(existing);
	}
}
