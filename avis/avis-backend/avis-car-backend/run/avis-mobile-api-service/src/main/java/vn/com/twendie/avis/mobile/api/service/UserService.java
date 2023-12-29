package vn.com.twendie.avis.mobile.api.service;

import vn.com.twendie.avis.data.model.JourneyDiary;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.mobile.api.model.response.DriverCommonInfo;

import java.util.List;

public interface UserService {

    User findById(Long id);

    User getUserByUsername(String username);

    User save(User user);

    DriverCommonInfo getDriverCommonInfo(User user);

    void assignToJourneyDiary(User user, JourneyDiary journeyDiary);

    void unAssignFromJourneyDiary(User user);

    List<User> findByNotificationWeekAndRoleId(Long roleId);

    List<User> findByNotificationMonthAndRoleId(Long roleId);

    List<User> findByRoleId(Long roleId);
}

