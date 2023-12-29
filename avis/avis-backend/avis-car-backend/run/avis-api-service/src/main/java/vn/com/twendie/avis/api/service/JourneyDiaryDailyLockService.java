package vn.com.twendie.avis.api.service;

import vn.com.twendie.avis.api.model.payload.CreateOrUpdateJDDLockPayload;
import vn.com.twendie.avis.data.model.JourneyDiaryDailyLock;
import vn.com.twendie.avis.data.model.User;

public interface JourneyDiaryDailyLockService {

    JourneyDiaryDailyLock find();

    JourneyDiaryDailyLock createOrUpdate(CreateOrUpdateJDDLockPayload payload, User user);
}
