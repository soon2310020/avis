package vn.com.twendie.avis.mobile.api.listener;

import lombok.SneakyThrows;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;
import vn.com.twendie.avis.api.core.ApplicationContextProvider;
import vn.com.twendie.avis.mobile.api.model.redis.JobMessage;

@Component
public class JobMessageListener implements MessageListener {

    private final RedisSerializer<?> redisSerializer;

    public JobMessageListener(RedisSerializer<?> redisSerializer) {
        this.redisSerializer = redisSerializer;
    }

    @SneakyThrows
    @Override
    public void onMessage(Message message, byte[] pattern) {

        JobMessage jobMessage = (JobMessage) redisSerializer.deserialize(message.getBody());
        assert jobMessage != null;
        Object bean = ApplicationContextProvider.getApplicationContext().getBean(jobMessage.getClazz());
        bean.getClass()
                .getMethod(jobMessage.getMethodName(), jobMessage.getParameterTypes())
                .invoke(bean, jobMessage.getArgs());

    }

}
