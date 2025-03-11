package com.expensemanager.repository;

import com.expensemanager.model.Expense;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

	@Query("SELECT DISTINCT e FROM Expense e "
			+ "LEFT JOIN FETCH e.tags "
			+ "LEFT JOIN FETCH e.category")
	List<Expense> findAllWithAssociations();

	@Query("SELECT e FROM Expense e "
			+ "LEFT JOIN FETCH e.tags "
			+ "LEFT JOIN FETCH e.category "
			+ "WHERE e.id = :id")
	Optional<Expense> findByIdWithAssociations(@Param("id") Long id);

	@Query("SELECT DISTINCT e FROM Expense e "
			+ "LEFT JOIN FETCH e.tags "
			+ "LEFT JOIN FETCH e.category "
			+ "WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :namePart, '%'))")
	List<Expense> searchByNamePart(@Param("namePart") String namePart);

	@Query("SELECT DISTINCT e FROM Expense e "
			+ "LEFT JOIN FETCH e.tags "
			+ "LEFT JOIN FETCH e.category "
			+ "WHERE e.category.name = :categoryName")
	List<Expense> findByCategoryName(@Param("categoryName") String categoryName);

	@Query("SELECT DISTINCT e FROM Expense e "
			+ "LEFT JOIN FETCH e.tags "
			+ "LEFT JOIN FETCH e.category "
			+ "WHERE e.amount BETWEEN :min AND :max")
	List<Expense> findByAmountRange(@Param("min") double min, @Param("max") double max);

	@Query("SELECT DISTINCT e FROM Expense e "
			+ "LEFT JOIN FETCH e.tags "
			+ "LEFT JOIN FETCH e.category "
			+ "JOIN e.tags t "
			+ "WHERE t.name = :tagName")
	List<Expense> findByTagName(@Param("tagName") String tagName);
}
