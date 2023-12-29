package vn.com.twendie.avis.queue.config;


import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.com.twendie.avis.queue.constant.QueueConstant;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

@Configuration
public class ModuleConfiguration {

    @Bean
    public Declarables declarables() {
        Declarables declarables = new Declarables();
        declarables.getDeclarables().addAll(
                Arrays.stream(QueueConstant.RoutingKeys.class.getDeclaredFields())
                        .map(Field::getName)
                        .map(Queue::new)
                        .collect(Collectors.toList())
        );
        return declarables;
    }

}
