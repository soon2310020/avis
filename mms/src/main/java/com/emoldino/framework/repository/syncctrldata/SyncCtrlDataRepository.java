package com.emoldino.framework.repository.syncctrldata;

import java.util.Optional;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface SyncCtrlDataRepository extends JpaRepository<SyncCtrlData, Long> {

	Optional<SyncCtrlData> findByName(String name);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<SyncCtrlData> findWithPessimisticLockByName(String name);

}
