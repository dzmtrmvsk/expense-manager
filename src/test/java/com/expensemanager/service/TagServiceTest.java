package com.expensemanager.service;

import com.expensemanager.cache.TagCache;
import com.expensemanager.dto.TagDTO;
import com.expensemanager.exception.ResourceNotFoundException;
import com.expensemanager.model.Tag;
import com.expensemanager.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

	@Mock
	private TagRepository tagRepository;

	@Mock
	private TagCache tagCache;

	@InjectMocks
	private TagService tagService;

	// ---------- getAllTags ----------

	@Test
	void testGetAllTags_CacheMissAndHit() {
		Tag tag1 = new Tag();
		tag1.setId(1L);
		Tag tag2 = new Tag();
		tag2.setId(2L);

		when(tagRepository.findAll()).thenReturn(List.of(tag1, tag2));
		when(tagCache.get(1L)).thenReturn(null); // cache miss
		when(tagCache.get(2L)).thenReturn(tag2); // cache hit

		List<Tag> result = tagService.getAllTags();
		assertThat(result).containsExactly(tag1, tag2);
		verify(tagCache).put(1L, tag1);
		verify(tagCache, never()).put(eq(2L), any());
	}

	// ---------- getTagById ----------

	@Test
	void testGetTagById_FromCache() {
		Tag tag = new Tag();
		tag.setId(10L);
		when(tagCache.get(10L)).thenReturn(tag);

		Tag result = tagService.getTagById(10L);
		assertThat(result).isEqualTo(tag);
		verify(tagRepository, never()).findById(anyLong());
	}

	@Test
	void testGetTagById_FromRepositoryAndCached() {
		Tag tag = new Tag();
		tag.setId(20L);
		when(tagCache.get(20L)).thenReturn(null);
		when(tagRepository.findById(20L)).thenReturn(Optional.of(tag));

		Tag result = tagService.getTagById(20L);
		assertThat(result).isEqualTo(tag);
		verify(tagCache).put(20L, tag);
	}

	@Test
	void testGetTagById_NotFound() {
		when(tagCache.get(30L)).thenReturn(null);
		when(tagRepository.findById(30L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> tagService.getTagById(30L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Tag with ID 30 not found");
	}

	// ---------- createTag ----------

	@Test
	void testCreateTag() {
		TagDTO dto = new TagDTO();
		dto.setName("urgent");

		Tag tagToSave = new Tag();
		tagToSave.setName("urgent");

		Tag savedTag = new Tag();
		savedTag.setId(40L);
		savedTag.setName("urgent");

		when(tagRepository.save(any(Tag.class))).thenReturn(savedTag);

		Tag result = tagService.createTag(dto);
		assertThat(result.getId()).isEqualTo(40L);
		assertThat(result.getName()).isEqualTo("urgent");
		verify(tagCache).put(40L, savedTag);
	}

	// ---------- updateTag ----------

	@Test
	void testUpdateTag() {
		Tag existing = new Tag();
		existing.setId(50L);
		existing.setName("old");

		when(tagCache.get(50L)).thenReturn(existing);

		TagDTO dto = new TagDTO();
		dto.setName("new");

		Tag updated = new Tag();
		updated.setId(50L);
		updated.setName("new");

		when(tagRepository.save(existing)).thenReturn(updated);

		Tag result = tagService.updateTag(50L, dto);
		assertThat(result.getName()).isEqualTo("new");
		verify(tagCache).put(50L, updated);
	}

	// ---------- deleteTag ----------

	@Test
	void testDeleteTag() {
		Tag existing = new Tag();
		existing.setId(60L);
		when(tagCache.get(60L)).thenReturn(existing);

		tagService.deleteTag(60L);
		verify(tagRepository).delete(existing);
		verify(tagCache).remove(60L);
	}
}
