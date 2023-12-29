package vn.com.twendie.avis.mobile.api.service;

import vn.com.twendie.avis.mobile.api.model.redis.JobMessage;

public interface JobMessageService {
    void push(JobMessage jobMessage);
}
