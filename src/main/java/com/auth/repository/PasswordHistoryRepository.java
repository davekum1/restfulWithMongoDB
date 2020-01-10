package com.auth.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.auth.entity.PasswordHistory;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long>{
	 public Page<PasswordHistory> findAllByUuid(Pageable pageable, UUID uuid);

	    @Override
	    public <T extends PasswordHistory> T save(T passwordHistory);

	    @Override
	    void deleteInBatch(Iterable<PasswordHistory> entities);

	    @Override
	    public <S extends PasswordHistory> S saveAndFlush(S entity);

}
