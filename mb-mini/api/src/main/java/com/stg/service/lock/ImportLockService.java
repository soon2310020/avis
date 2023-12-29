package com.stg.service.lock;

public interface ImportLockService {
    void lockImportDataMbEmployee(String userID, Runnable runnable);
}
