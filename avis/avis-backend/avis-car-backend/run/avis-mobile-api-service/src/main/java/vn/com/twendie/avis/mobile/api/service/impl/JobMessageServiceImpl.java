package vn.com.twendie.avis.mobile.api.service.impl;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.Topic;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.mobile.api.model.redis.JobMessage;
import vn.com.twendie.avis.mobile.api.service.JobMessageService;

@Service
public class JobMessageServiceImpl implements JobMessageService {

    private final RedisTemplate<String, Object> redisTemplate;

    private final Topic topic;

    public JobMessageServiceImpl(RedisTemplate<String, Object> redisTemplate,
                                 Topic topic) {
        this.redisTemplate = redisTemplate;
        this.topic = topic;
    }

    @Override
    public void push(JobMessage jobMessage) {
        redisTemplate.convertAndSend(topic.getTopic(), jobMessage);
    }

}
