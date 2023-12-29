package vn.com.twendie.avis.mobile.api;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import vn.com.twendie.avis.api.core.config.EnableApiCore;
import vn.com.twendie.avis.api.rest.config.EnableRestCommonApi;
import vn.com.twendie.avis.data.config.EnableDataModel;
import vn.com.twendie.avis.locale.config.EnableLocaleModule;
import vn.com.twendie.avis.mobile.api.converter.DriverCommonInfoConverter;
import vn.com.twendie.avis.mobile.api.converter.JourneyDiaryStationFeeConverter;
import vn.com.twendie.avis.notification.config.EnableServiceNotification;
import vn.com.twendie.avis.queue.config.EnableCommonQueue;
import vn.com.twendie.avis.security.config.EnableSecurityModule;
import vn.com.twendie.avis.tracking.config.EnableServiceTrackingGps;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@EnableJpaRepositories("vn.com.twendie.avis.mobile.api.repository")
@EnableLocaleModule
@EnableSecurityModule
@EnableRestCommonApi
@EnableDataModel
@EnableSwagger2
@EnableApiCore
@EnableServiceNotification
@EnableCommonQueue
@EnableServiceTrackingGps
@EnableScheduling
@Slf4j
public class MobileApiApplication {

    @Bean
    public ModelMapper modelMapper(
            DriverCommonInfoConverter driverCommonInfoConverter,
            JourneyDiaryStationFeeConverter journeyDiaryStationFeeConverter
    ) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        modelMapper.addConverter(driverCommonInfoConverter);
        modelMapper.addConverter(journeyDiaryStationFeeConverter);
        return modelMapper;
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public static void main(String[] args) {
        SpringApplication.run(MobileApiApplication.class, args);
        log.info("Start avis mobile api service");
    }
}
