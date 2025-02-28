package com.expensemanager.repository;

import com.expensemanager.model.Expense;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

	@Query("SELECT DISTINCT e FROM Expense e LEFT JOIN FETCH e.category LEFT JOIN FETCH e.tags")
	List<Expense> findAllWithAssociations();

	@Query("SELECT e FROM Expense e JOIN e.category c WHERE LOWER(c.name) = LOWER(:categoryName)")
	List<Expense> findByCategoryName(@Param("categoryName") String categoryName);

	@Query("SELECT e FROM Expense e WHERE e.amount BETWEEN :minAmount AND :maxAmount")
	List<Expense> findByAmountRange(@Param("minAmount") double minAmount,
	                                @Param("maxAmount") double maxAmount);

	@Query("SELECT e FROM Expense e WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :namePart, '%'))")
	List<Expense> searchByNamePart(@Param("namePart") String namePart);

	@Query("SELECT e FROM Expense e JOIN e.tags t WHERE LOWER(t.name) = LOWER(:tagName)")
	List<Expense> findByTagName(@Param("tagName") String tagName);
}
