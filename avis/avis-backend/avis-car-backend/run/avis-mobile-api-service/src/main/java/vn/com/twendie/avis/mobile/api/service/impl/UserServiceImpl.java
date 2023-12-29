package vn.com.twendie.avis.mobile.api.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.data.model.JourneyDiary;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.mobile.api.model.response.DriverCommonInfo;
import vn.com.twendie.avis.mobile.api.repository.UserRepo;
import vn.com.twendie.avis.mobile.api.service.UserService;

import java.util.List;
import java.util.Objects;

import static vn.com.twendie.avis.data.enumtype.DriverStatusEnum.*;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;

    private final ModelMapper modelMapper;

    public UserServiceImpl(UserRepo userRepo,
                           ModelMapper modelMapper) {
        this.userRepo = userRepo;
        this.modelMapper = modelMapper;
    }

    @Override
    public User findById(Long id) {
        if (Objects.isNull(id)) {
            return null;
        }
        return userRepo.findById(id).orElseThrow(() -> new NotFoundException("Not found user with id: " + id));
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    @Override
    public User save(User user) {
        return userRepo.save(user);
    }

    @Override
    public DriverCommonInfo getDriverCommonInfo(User user) {
        return modelMapper.map(user, DriverCommonInfo.class);
    }

    @Override
    public void assignToJourneyDiary(User user, JourneyDiary journeyDiary) {
        if (Objects.nonNull(user)) {
            user.setCurrentJourneyDiaryId(journeyDiary.getId());
            user.setStatus(UNAVAILABLE.getValue());
        }
    }

    @Override
    public void unAssignFromJourneyDiary(User user) {
        if (Objects.nonNull(user)) {
            user.setCurrentJourneyDiaryId(null);
            if (Objects.isNull(user.getCurrentContractId()) && Objects.isNull(user.getLendingContractId())) {
                user.setStatus(WAITING.getValue());
            } else {
                user.setStatus(APPOINTED.getValue());
            }
        }
    }

    @Override
    public List<User> findByNotificationWeekAndRoleId(Long roleId) {
        return userRepo.findByNotificationWeekAndRole(roleId);
    }

    @Override
    public List<User> findByNotificationMonthAndRoleId(Long roleId) {
        return userRepo.findByNotificationMonthAndRole(roleId);
    }

    @Override
    public List<User> findByRoleId(Long roleId) {
        return userRepo.findByRoleId(roleId);
    }

}
