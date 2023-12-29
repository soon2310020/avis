package vn.com.twendie.avis.notification.service;

import org.javatuples.Quartet;
import org.springframework.data.domain.Page;
import vn.com.twendie.avis.data.enumtype.DriverContractStatusEnum;
import vn.com.twendie.avis.data.enumtype.NotificationContentEnum;
import vn.com.twendie.avis.data.enumtype.NotificationSettingTypeEnum;
import vn.com.twendie.avis.data.enumtype.NotificationTypeEnum;
import vn.com.twendie.avis.data.model.*;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public interface NotificationService {

    Notification findById(long id);

    Notification save(Notification notification);

    List<Notification> saveAll(Collection<Notification> notifications);

    Notification push(Notification notification, boolean async);

    Notification saveAndPush(Notification notification, boolean async);

    List<Notification> saveAllAndPush(Collection<Notification> notifications, boolean async);

    Notification build(Long userId, Contract contract, NotificationContentEnum content, String... params);

    Notification build(Long userId, Long specId, NotificationContentEnum content,
                             NotificationTypeEnum type, NotificationSettingTypeEnum typeSetting,
                             Timestamp fromDate, Timestamp toDate, String... params);

    default Notification build(Quartet<Contract, User, DriverContractStatusEnum, NotificationContentEnum> quartet) {
        return build(quartet.getValue1().getId(), quartet.getValue0(), quartet.getValue3(), quartet.getValue0().getCode());
    }

    default List<Notification> build(Collection<Quartet<Contract, User, DriverContractStatusEnum, NotificationContentEnum>> quartets) {
        return quartets.stream()
                .filter(quartet -> Objects.nonNull(quartet.getValue3()))
                .map(this::build)
                .collect(Collectors.toList());
    }

    Notification pushNotificationSignature(User user, NotificationContentEnum content, NotificationSettingTypeEnum typeSetting,
                                           Long specId, Timestamp fromDate, Timestamp toDate, boolean pushNoti, String... params);

    Notification buildCustomerNoti(Contract contract);

    Notification buildCustomerNoti(ContractChangeHistory contractChangeHistory);

    Notification buildCustomerNoti(LogContractPriceCostType logContractPriceCostType);

    Notification buildCustomerNoti(LogContractNormList logContractNormList);

    Page<Notification> findAllByDriverId(Long driverId, int page);

    Page<Notification> findByUserId(long userId, int page, int size);

    void updateNotificationStatus(List<Integer> notificationIds, String status);
}
