package com.stg.service.lock.base;

@FunctionalInterface
public interface ExecHandler<T> {
    T exec();
}
