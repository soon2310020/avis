package vn.com.twendie.avis.mobile.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.com.twendie.avis.data.model.SystemLog;
import vn.com.twendie.avis.mobile.api.aop.Logging;

public interface SystemLogRepo  extends JpaRepository<SystemLog, Long> {
    @Logging.NoLogging
    @Query(value = " SELECT PS_CURRENT_THREAD_ID()",nativeQuery = true)
    Long findThread();
    @Logging.NoLogging
    @Query(value = " SELECT * FROM performance_schema.data_locks WHERE THREAD_ID = PS_CURRENT_THREAD_ID()",nativeQuery = true)
    Object[][] getDataLocksCurrentThread();

}
