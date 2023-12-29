package com.stg.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.stg.constant.ComboCode;
import com.stg.entity.combo.ComboSuggestion;

public interface ComboSuggestionRepository extends JpaRepository<ComboSuggestion, Long> {

	@Query(value = "SELECT * FROM combo_suggestion where combo_code = :comboCode and fee_rank = :feeRank", nativeQuery = true)
	List<ComboSuggestion> findComboSuggestionByComboCodeAndFeeRank(String comboCode, Integer feeRank);

	@Query(value = "SELECT cs FROM ComboSuggestion cs where comboCode in ?1 and feeRank = ?2")
    List<ComboSuggestion> findComboSuggestionByComboCodeAndFeeRank(List<ComboCode> comboCodes, Integer feeRank);

}
