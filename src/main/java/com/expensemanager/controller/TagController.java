package com.expensemanager.controller;

import com.expensemanager.dto.TagDTO;
import com.expensemanager.model.Tag;
import com.expensemanager.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@io.swagger.v3.oas.annotations.tags.Tag(name = "Tag API", description = "Operations on tags")
@RestController
@RequestMapping("/api/tags")
public class TagController {

	private final TagService tagService;

	@Autowired
	public TagController(TagService tagService) {
		this.tagService = tagService;
	}

	@GetMapping
	@Operation(summary = "Get all tags")
	public List<Tag> getAllTags() {
		return tagService.getAllTags();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get tag by ID")
	public Tag getTagById(@PathVariable("id") Long id) {
		return tagService.getTagById(id);
	}

	@PostMapping
	@Operation(summary = "Create new tag")
	public Tag createTag(@Valid @RequestBody TagDTO tagDTO) {
		return tagService.createTag(tagDTO);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update tag by ID")
	public Tag updateTag(@PathVariable("id") Long id, @Valid @RequestBody TagDTO tagDTO) {
		return tagService.updateTag(id, tagDTO);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete tag by ID")
	public void deleteTag(@PathVariable("id") Long id) {
		tagService.deleteTag(id);
	}
}
