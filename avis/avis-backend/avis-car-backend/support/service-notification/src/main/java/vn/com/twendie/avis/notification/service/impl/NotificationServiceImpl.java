package vn.com.twendie.avis.notification.service.impl;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.core.util.ListUtils;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.data.enumtype.*;
import vn.com.twendie.avis.data.model.*;
import vn.com.twendie.avis.notification.model.onesignal.PushNotiPayload;
import vn.com.twendie.avis.notification.model.onesignal.PushNotiResponse;
import vn.com.twendie.avis.notification.repository.NotificationRepo;
import vn.com.twendie.avis.notification.service.NotificationContentService;
import vn.com.twendie.avis.notification.service.NotificationService;
import vn.com.twendie.avis.notification.service.PushNotificationService;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static vn.com.twendie.avis.api.core.util.DateUtils.SHORT_PATTERN;
import static vn.com.twendie.avis.data.enumtype.MappingFieldCodeFrontendEnum.*;
import static vn.com.twendie.avis.data.enumtype.NotificationContentEnum.*;
import static vn.com.twendie.avis.data.enumtype.NotificationStatusEnum.*;
import static vn.com.twendie.avis.data.enumtype.NotificationTypeEnum.CONTRACT;
import static vn.com.twendie.avis.queue.constant.QueueConstant.RoutingKeys.PUSH_NOTIFICATION;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final List<String> IGNORE_NOTI_FIELDS = Stream.of(BRANCH_ID, NOTE, VEHICLE_WORKING_AREA)
            .map(MappingFieldCodeFrontendEnum::getName)
            .collect(Collectors.toList());

    private final NotificationRepo notificationRepo;
    private final NotificationContentService notificationContentService;
    private final PushNotificationService pushNotificationService;

    private final DateUtils dateUtils;
    private final ListUtils listUtils;
    private final RabbitTemplate rabbitTemplate;

    public NotificationServiceImpl(NotificationRepo notificationRepo,
                                   NotificationContentService notificationContentService,
                                   PushNotificationService pushNotificationService,
                                   DateUtils dateUtils,
                                   ListUtils listUtils,
                                   RabbitTemplate rabbitTemplate) {
        this.notificationRepo = notificationRepo;
        this.notificationContentService = notificationContentService;
        this.pushNotificationService = pushNotificationService;
        this.dateUtils = dateUtils;
        this.listUtils = listUtils;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public Notification findById(long id) {
        return notificationRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found notification with id: " + id));
    }

    @Override
    public Notification save(Notification notification) {
        if (Objects.nonNull(notification)) {
            return notificationRepo.save(notification);
        } else {
            return null;
        }
    }

    @Override
    public List<Notification> saveAll(Collection<Notification> notifications) {
        return notificationRepo.saveAll(listUtils.filter(notifications, Objects::nonNull));
    }

    @Override
    public Notification push(Notification notification, boolean async) {
        if (async) {
            rabbitTemplate.convertAndSend(PUSH_NOTIFICATION, notification.getId());
            return notification;
        } else {
            try {
                PushNotiResponse response = pushNotificationService.buildAndPush(notification);
                if (Objects.nonNull(response.getId())) {
                    notification.setStatus(SUCCESS.value());
                } else {
                    notification.setStatus(FAILED.value());
                }
            } catch (Exception e) {
                notification.setStatus(FAILED.value());
            }
            return save(notification);
        }
    }

    @Override
    public Notification saveAndPush(Notification notification, boolean async) {
        notification = save(notification);
        return push(notification, async);
    }

    @Override
    public List<Notification> saveAllAndPush(Collection<Notification> notifications, boolean async) {
        if (!CollectionUtils.isEmpty(notifications)) {
            List<Notification> notificationList = saveAll(notifications);
            return notificationList.stream()
                    .map(notification -> push(notification, async))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Notification build(Long userId, Contract contract, NotificationContentEnum content, String... params) {
        return Notification.builder()
                .userId(userId)
                .type(CONTRACT.value())
                .specId(contract.getId())
                .notificationContent(notificationContentService.findById(content.getId()))
                .params(String.join(",", params))
                .status(WAITING.value())
                .build();
    }

    @Override
    public Notification build(Long userId, Long specId, NotificationContentEnum content,
                              NotificationTypeEnum type, NotificationSettingTypeEnum typeSetting,
                              Timestamp fromDate, Timestamp toDate, String... params) {
        return Notification.builder()
                .userId(userId)
                .notificationContent(notificationContentService.findById(content.getId()))
                .specId(specId)
                .status(WAITING.value())
                .typeSetting(typeSetting)
                .fromDate(fromDate)
                .toDate(toDate)
                .params(String.join(",", params))
                .type(type.value())
                .build();
    }

    @Override
    public Notification pushNotificationSignature(User user, NotificationContentEnum content, NotificationSettingTypeEnum typeSetting,
                                                  Long specId, Timestamp fromDate, Timestamp toDate, boolean pushNoti, String... params) {
        Notification notification = build(user.getId(), specId, content, NotificationTypeEnum.SIGNATURE, typeSetting, fromDate, toDate, params);
        save(notification);
        if(pushNoti) {
            PushNotiPayload pushNotiPayload = pushNotificationService.build(notification);
            try {
                pushNotificationService.push(pushNotiPayload);
            } catch (Exception ignored) {
            }
        }
        return notification;
    }

    @Override
    public Notification buildCustomerNoti(Contract contract) {
        Customer customer = contract.getCustomer();
        MemberCustomer memberCustomer = contract.getMemberCustomer();
        User user;
        if (Objects.isNull(memberCustomer)) {
            user = customer.getUser();
        } else if (Objects.isNull(memberCustomer.getParent())) {
            user = memberCustomer.getUser();
        } else {
            user = memberCustomer.getParent().getUser();
        }
        if (Objects.nonNull(user)) {
            NotificationContentEnum content;
            if (contract.isDeleted()) {
                content = DELETE_CONTRACT_CUSTOMER;
            } else {
                switch (ContractStatusEnum.valueOf(contract.getStatus())) {
                    case FINISHED:
                        content = FINISH_CONTRACT_CUSTOMER;
                        break;
                    case CANCELED:
                        content = CANCEL_CONTRACT_CUSTOMER;
                        break;
                    default:
                        content = NEW_CONTRACT_CUSTOMER;
                }
            }
            return build(user.getId(), contract, content, contract.getCode());
        } else {
            return null;
        }
    }

    @Override
    public Notification buildCustomerNoti(ContractChangeHistory contractChangeHistory) {
        String fieldName = contractChangeHistory.getMappingFieldCodeFontend().getName();
        if (IGNORE_NOTI_FIELDS.contains(contractChangeHistory.getMappingFieldCodeFontend().getFieldName())) {
            return null;
        }
        Contract contract = contractChangeHistory.getContract();
        Customer customer = contract.getCustomer();
        MemberCustomer memberCustomer = contract.getMemberCustomer();
        String[] params = new String[] {
                fieldName,
                contract.getCode(),
                dateUtils.format(contractChangeHistory.getFromDate(), SHORT_PATTERN)
        };
        User user;
        if (Objects.isNull(memberCustomer)) {
            user = customer.getUser();
        } else if (Objects.isNull(memberCustomer.getParent())) {
            user = memberCustomer.getUser();
        } else {
            user = memberCustomer.getParent().getUser();
        }
        if (Objects.nonNull(user)) {
            return build(user.getId(), contract, CHANGE_CONTRACT_CUSTOMER, params);
        } else {
            return null;
        }
    }

    @Override
    public Notification buildCustomerNoti(LogContractPriceCostType logContractPriceCostType) {
        Contract contract = logContractPriceCostType.getContract();
        Customer customer = contract.getCustomer();
        MemberCustomer memberCustomer = contract.getMemberCustomer();
        String[] params = new String[] {
                logContractPriceCostType.getCostType().getName(),
                contract.getCode(),
                dateUtils.format(logContractPriceCostType.getFromDate(), SHORT_PATTERN)
        };
        User user;
        if (Objects.isNull(memberCustomer)) {
            user = customer.getUser();
        } else if (Objects.isNull(memberCustomer.getParent())) {
            user = memberCustomer.getUser();
        } else {
            user = memberCustomer.getParent().getUser();
        }
        if (Objects.nonNull(user)) {
            return build(user.getId(), contract, CHANGE_CONTRACT_CUSTOMER, params);
        } else {
            return null;
        }
    }

    @Override
    public Notification buildCustomerNoti(LogContractNormList logContractNormList) {
        Contract contract = logContractNormList.getContract();
        Customer customer = contract.getCustomer();
        MemberCustomer memberCustomer = contract.getMemberCustomer();
        String[] params = new String[] {
                logContractNormList.getNormList().getName(),
                contract.getCode(),
                dateUtils.format(logContractNormList.getFromDate(), SHORT_PATTERN)
        };
        User user;
        if (Objects.isNull(memberCustomer)) {
            user = customer.getUser();
        } else if (Objects.isNull(memberCustomer.getParent())) {
            user = memberCustomer.getUser();
        } else {
            user = memberCustomer.getParent().getUser();
        }
        if (Objects.nonNull(user)) {
            return build(user.getId(), contract, CHANGE_CONTRACT_CUSTOMER, params);
        } else {
            return null;
        }
    }

    @Override
    public Page<Notification> findAllByDriverId(Long driverId, int page) {
        int pageOffSet = page > 0 ? page - 1 : 0;
        List<String> status = new ArrayList<String>() {{
            add(SUCCESS.value());
            add(READ.value());
        }};
        PageRequest pageRequest = PageRequest.of(pageOffSet, 10);
        return notificationRepo.findAllByUserIdAndStatusInAndDeletedFalseOrderByCreatedAtDesc(driverId, status, pageRequest);
    }

    @Override
    public Page<Notification> findByUserId(long userId, int page, int size) {
        int pageOffSet = page > 0 ? page - 1 : 0;
        return notificationRepo.findAllByUserIdAndDeletedFalseOrderByIdDesc(
                userId, PageRequest.of(pageOffSet, size));
    }

    @Override
    public void updateNotificationStatus(List<Integer> notificationIds, String status) {
        notificationRepo.updateNotificationStatus(notificationIds, status);
    }
}
