package saleson.api.notification;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import com.emoldino.api.common.resource.base.alert.util.AlertUtils;
import com.emoldino.framework.util.DateUtils2;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.emoldino.api.common.resource.base.noti.dto.NotiPostIn;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiCode;
import com.emoldino.api.common.resource.base.noti.util.NotiUtils;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.LogicUtils;
import com.querydsl.core.types.Predicate;

import lombok.extern.slf4j.Slf4j;
import saleson.api.broadcastNotification.BroadcastNotificationService;
import saleson.api.notification.payload.AlrEmlNotData;
import saleson.api.notification.payload.AlrEmlNotItem;
import saleson.api.notification.payload.NotificationPayload;
import saleson.api.systemNote.SystemNoteRepository;
import saleson.api.user.UserAlertRepository;
import saleson.api.user.UserRepository;
import saleson.common.config.Const;
import saleson.common.enumeration.*;
import saleson.common.util.DataUtils;
import saleson.common.util.SecurityUtils;
import saleson.dto.SystemNoteParam;
import saleson.model.*;

@Slf4j
@Service
public class NotificationService {
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    UserRepository userRepository;

    @Autowired
    private BroadcastNotificationService broadcastNotificationService;


    public Page<Notification> findAll(Predicate predicate, Pageable pageable) {
        return notificationRepository.findAll(predicate, pageable);
    }

    public Page<Notification> getNotificationOfUser(Integer pageIndex, Integer pageSize) {
        NotificationPayload payload = new NotificationPayload();
        payload.setIsRead(false);
        List<Sort.Order> sorts = Arrays.asList(
                Sort.Order.asc("isRead"),
                Sort.Order.desc("notificationCreatedAt")
        );
        Pageable pageable = PageRequest.of(0, 1, Sort.by(sorts));

        Page<Notification> pageContent = findAll(payload.getPredicate(), DataUtils.getPageable(pageIndex, pageSize, pageable));

        return pageContent;
    }

    public void clearAll() {
        Page<Notification> pageAll = getNotificationOfUser(null, null);
        List<Notification> notificationList = pageAll.getContent();
        for (Notification n : notificationList) {
            n.setRead(true);
        }
        notificationRepository.saveAll(notificationList);
    }

    public void createNoteNotification(SystemNote systemNote, List<SystemNoteParam> systemNoteParamOldList) {
        if (!PageType.TOOLING_SETTING.equals(systemNote.getSystemNoteFunction())
                && !PageType.PART_SETTING.equals(systemNote.getSystemNoteFunction()))
            return;
        User sender = getSender(SecurityUtils.getUserId());
        Set<Long> objectIdsNotification= new HashSet<>();
        for (SystemNoteParam noteParam : systemNote.getSystemNoteParamList()) {
            if (noteParam.getObjectType() == ObjectType.USER) {
                objectIdsNotification.add(noteParam.getObjectId());
                if (systemNoteParamOldList != null) {
                    SystemNoteParam exist = systemNoteParamOldList.stream().filter(sn -> {
                        if (sn.getObjectType() == ObjectType.USER && noteParam.getObjectId() != null && noteParam.getObjectId().equals(sn.getObjectId())) {
                            return true;
                        }
                        return false;
                    }).findFirst().orElse(null);
                    if (exist != null) continue;
                }
                List<Long> receiverIds = Collections.singletonList(noteParam.getObjectId());
                List<User> receivers = userRepository.findAllById(receiverIds);
                NotiUtils.post(NotiCode.NOTE_MENTIONED, toNotiPostIn(sender, receiverIds, systemNote));
                saveNotification(receiverIds, sender, systemNote.getMessage(), systemNote.getObjectFunctionId(), NotificationType.SYSTEM_NOTE);
            }
        }
        // for reply when create
        if(systemNote.getParentId()!=null && systemNoteParamOldList == null){
            SystemNote parentNote = BeanUtils.get(SystemNoteRepository.class).findById(systemNote.getParentId()).orElse(null);
            if(parentNote!=null && parentNote.getCreator()!=null && !parentNote.getCreator().getId().equals(SecurityUtils.getUserId())){
                List<User> receivers = Collections.singletonList(parentNote.getCreator());
                List<Long> receiverIds = Collections.singletonList(parentNote.getCreator().getId());
                NotiUtils.post(NotiCode.NOTE_REPLIED, toNotiPostIn(sender, receiverIds, systemNote));
                saveNotification(receiverIds, sender, systemNote.getMessage(), systemNote.getObjectFunctionId(), NotificationType.SYSTEM_NOTE);
            }
        }

        if (systemNoteParamOldList != null) {
            List<Long> listToDel = systemNoteParamOldList.stream().filter(sn -> ObjectType.USER.equals(sn.getObjectType()) && !objectIdsNotification.contains(sn.getObjectId()) && sn.getObjectId() != null)
                    .map(sn -> sn.getObjectId()).collect(Collectors.toList());
            if (!listToDel.isEmpty()) {
                List<Notification> notificationsDel =notificationRepository.findAllBySystemNoteIdAndUserTargetIdIn(systemNote.getId(), listToDel);
                notificationRepository.deleteAll(notificationsDel);
            }
        }
    }

    @Deprecated
    public List<Notification> createOrUpdateListNotification(SystemNote systemNote, List<SystemNoteParam> systemNoteParamOldList) {
        List<Notification> notificationList = new ArrayList<>();
        Set<Long> objectIdsNotification= new HashSet<>();
        for (SystemNoteParam noteParam : systemNote.getSystemNoteParamList()) {
            if (noteParam.getObjectType() == ObjectType.USER) {
                objectIdsNotification.add(noteParam.getObjectId());
                if (systemNoteParamOldList != null) {
                    SystemNoteParam exist = systemNoteParamOldList.stream().filter(sn -> {
                        if (sn.getObjectType() == ObjectType.USER && noteParam.getObjectId() != null && noteParam.getObjectId().equals(sn.getObjectId())) {
                            return true;
                        }
                        return false;
                    }).findFirst().orElse(null);
                    if (exist != null) continue;
                }
                User userTarget = userRepository.findById(noteParam.getObjectId()).orElse(null);
                Notification n = new Notification(systemNote.getCreator(), userTarget, Instant.now(), systemNote.getSystemNoteFunction(), systemNote.getObjectFunctionId(), systemNote.getId());
                notificationList.add(n);
            }
        }
        // for reply when create
        if(systemNote.getParentId()!=null && systemNoteParamOldList == null){
            SystemNote parentNote = BeanUtils.get(SystemNoteRepository.class).findById(systemNote.getParentId()).orElse(null);
            if(parentNote!=null && parentNote.getCreator()!=null && !parentNote.getCreator().getId().equals(SecurityUtils.getUserId())){
            Notification n = new Notification(systemNote.getCreator(), parentNote.getCreator()
                , Instant.now(), systemNote.getSystemNoteFunction(), systemNote.getObjectFunctionId(), systemNote.getId());
            n.setReply(true);
            notificationList.add(n);
            }
        }
        notificationRepository.saveAll(notificationList);
        notificationList.stream().forEach(n->broadcastNotificationService.createNotificationApp(n));
        //list to remove
        if (systemNoteParamOldList != null) {
            List<Notification> notificationDelList = new ArrayList<>();
            List<Long> listToDel = systemNoteParamOldList.stream().filter(sn -> ObjectType.USER.equals(sn.getObjectType()) && !objectIdsNotification.contains(sn.getObjectId()) && sn.getObjectId() != null)
                    .map(sn -> sn.getObjectId()).collect(Collectors.toList());
            if (!listToDel.isEmpty()) {
               List<Notification> notificationsDel =notificationRepository.findAllBySystemNoteIdAndUserTargetIdIn(systemNote.getId(), listToDel);
                notificationRepository.deleteAll(notificationsDel);
            }
        }

        return notificationList;
    }

    public List<Notification> createListNotification(Topic topic) {

        List<Notification> notificationList = new ArrayList<>();
        Notification n = new Notification(topic.getCreator(), topic.getRecipient(), Instant.now(), PageType.SUPPORT_CENTER, topic.getId(), null);
        n.setTopicId(topic.getTopicId());
        notificationList.add(n);
//        notificationRepository.save(n);
        if (topic.getObjectType() == ObjectType.USER && topic.getObjectId() != null) {
            User userTag = userRepository.findById(topic.getObjectId()).orElse(null);
            if (userTag != null) {
                Notification n2 = new Notification(topic.getCreator(), userTag, Instant.now(), PageType.SUPPORT_CENTER, topic.getId(), null);
                n2.setTopicId(topic.getTopicId());
                notificationList.add(n2);
//                notificationRepository.save(n2);
            }
        }
        notificationRepository.saveAll(notificationList);
        return notificationList;
    }

    public Notification createInviteUserNotification(UserInvite invitation){
        if(invitation == null) return null;
        User newUser = userRepository.findByEmailAndDeletedIsFalse(invitation.getEmail()).orElse(null);
        if(newUser == null) return null;
        Notification n = new Notification(newUser, invitation.getSender(), Instant.now(), PageType.USER,
                newUser.getId(), null, Const.NOTIFICATION_MESSAGE.USER_HAS_REQUESTED_PLATFORM, NotificationType.INVITE_USER);
        return notificationRepository.save(n);
    }

    public void createUserRequestAccessNotification(List<User> receivers, User user){
        User sender = getSender(null);
        List<Long> receiverIds = receivers.stream().map(User::getId).collect(Collectors.toList());
        NotiUtils.post(NotiCode.USER_ACCESS_REQUESTED, toNotiPostIn(sender, receiverIds, user));
        saveNotification(receiverIds, sender, user.getDisplayName(), user.getId(), NotificationType.USER_ACCESS_REQUESTED);
    }

    public void createDataCompletionNotification(DataCompletionOrder order){
        List<Notification> notifications = new ArrayList<>();
        User creator = userRepository.getOne(order.getCreatedBy());
        order.getAssignedUsers().forEach(receiver -> {
            notifications.add(new Notification(creator, receiver, Instant.now(), PageType.USER,
                    creator.getId(), null, creator.getCompany().getName(), NotificationType.DATA_COMPLETION));
        });
        notificationRepository.saveAll(notifications);
    }

    public void createWorkOrderNotification(WorkOrder workOrder, List<Long> receiverIds) {
		User sender = getSender(workOrder.getCreatedBy());

		NotiUtils.post(NotiCode.WO_CREATED, toNotiPostIn(sender, receiverIds, workOrder));
        saveNotification(receiverIds, sender, workOrder.getOrderType().getCode(), workOrder.getId(), NotificationType.WORK_ORDER);
    }

    public void createWorkOrderAcceptNotification(WorkOrder workOrder) {
		User sender = getSender(SecurityUtils.getUserId());
        List<Long> receiverIds = workOrder.getCreatorIds();
		NotiUtils.post(NotiCode.WO_ACCEPTED, toNotiPostIn(sender, receiverIds, workOrder));
        saveNotification(receiverIds, sender, workOrder.getOrderType().getCode(), workOrder.getId(), NotificationType.WORK_ORDER);
    }

    public void createWorkOrderCreationApprovalRequestNotification(WorkOrder workOrder) {
        User sender = getSender(workOrder.getCreatedBy());
        List<User> receivers = userRepository.findByCompanyTypeAndEnabledIsTrue(CompanyType.IN_HOUSE);
        List<Long> receiverIds = receivers.stream().map(User::getId).collect(Collectors.toList());
		NotiUtils.post(NotiCode.WO_CRT_APPR_REQUESTED, toNotiPostIn(sender, receiverIds, workOrder));
		saveNotification(receiverIds, sender, workOrder.getOrderType().getCode(), workOrder.getId(), NotificationType.WORK_ORDER);
    }

    public void createWorkOrderCreationApprovalRequestApprovedNotification(WorkOrder workOrder) {
        User sender = getSender(SecurityUtils.getUserId());
        List<Long> receiverIds = workOrder.getCreatorIds();
		if (workOrder.getApproved())
            NotiUtils.post(NotiCode.WO_CRT_APPROVED, toNotiPostIn(sender, receiverIds, workOrder));
        else NotiUtils.post(NotiCode.WO_CRT_DECLINED, toNotiPostIn(sender, receiverIds, workOrder));
		saveNotification(receiverIds, sender, workOrder.getOrderType().getCode(), workOrder.getId(), NotificationType.WORK_ORDER);
    }

    public void createWorkOrderCompletionApprovalRequestNotification(WorkOrder workOrder) {
        User sender = getSender(SecurityUtils.getUserId());
        List<Long> receiverIds = workOrder.getCreatorIds();
		NotiUtils.post(NotiCode.WO_CPT_APPR_REQUESTED, toNotiPostIn(sender, receiverIds, workOrder));
		saveNotification(receiverIds, sender, workOrder.getOrderType().getCode(), workOrder.getId(), NotificationType.WORK_ORDER);
    }

    public void createWorkOrderCompletionApprovalRequestReviewNotification(WorkOrder workOrder, Long reportedBy, NotiCode notiCode) {
        LogicUtils.assertNotNull(reportedBy, "reportedBy");

        User sender = getSender(SecurityUtils.getUserId());
        List<User> receivers = userRepository.findByIdInOrderByIdDesc(Collections.singletonList(reportedBy));
        List<Long> receiverIds = receivers.stream().map(User::getId).collect(Collectors.toList());
		NotiUtils.post(notiCode, toNotiPostIn(sender, receiverIds, workOrder));
		saveNotification(receiverIds, sender, workOrder.getOrderType().getCode(), workOrder.getId(), NotificationType.WORK_ORDER);
    }

    public void createWorkOrderCompletedNotification(WorkOrder workOrder) {
        User sender = getSender(SecurityUtils.getUserId());
        List<Long> receiverIds = workOrder.getCreatorIds();
        NotiUtils.post(NotiCode.WO_COMPLETED, toNotiPostIn(sender, receiverIds, workOrder));
        saveNotification(receiverIds, sender, workOrder.getOrderType().getCode(), workOrder.getId(), NotificationType.WORK_ORDER);
    }

    public void createWorkOrderCancelledNotification(WorkOrder workOrder) {
        User sender = getSender(workOrder.getCreatedBy());
        List<Long> receiverIds = workOrder.getAssigneeIds();
        NotiUtils.post(NotiCode.WO_CANCELLED, toNotiPostIn(sender, receiverIds, workOrder));
		saveNotification(receiverIds, sender, workOrder.getOrderType().getCode(), workOrder.getId(), NotificationType.WORK_ORDER);
    }

    public void createWorkOrderDeclinedNotification(WorkOrder workOrder) {
        User sender = getSender(SecurityUtils.getUserId());
        List<Long> receiverIds = workOrder.getCreatorIds();
        NotiUtils.post(NotiCode.WO_DECLINED, toNotiPostIn(sender, receiverIds, workOrder));
		saveNotification(receiverIds, sender, workOrder.getOrderType().getCode(), workOrder.getId(), NotificationType.WORK_ORDER);
    }

    public void createWorkOrderModificationRequestNotification(WorkOrder workOrder, List<Long> creatorIds) {
        User sender = getSender(SecurityUtils.getUserId());
        List<Long> receiverIds = creatorIds;
        NotiUtils.post(NotiCode.WO_MOD_APPR_REQUESTED, toNotiPostIn(sender, receiverIds, workOrder));
		saveNotification(receiverIds, sender, workOrder.getOrderType().getCode(), workOrder.getId(), NotificationType.WORK_ORDER);
    }

    public void createWorkOrderModificationRequestApproveNotification(WorkOrder workOrder, Long receiverId, boolean approved) {
        User sender = getSender(SecurityUtils.getUserId());
        List<Long> receiverIds = Collections.singletonList(receiverId);
        if (approved)
            NotiUtils.post(NotiCode.WO_MOD_APPROVED, toNotiPostIn(sender, receiverIds, workOrder));
        else NotiUtils.post(NotiCode.WO_MOD_REJECTED, toNotiPostIn(sender, receiverIds, workOrder));
		saveNotification(receiverIds, sender, workOrder.getOrderType().getCode(), workOrder.getId(), NotificationType.WORK_ORDER);
    }

    private void saveNotification(List<Long> receiverIds, User sender, String topicId, Long objectFunctionId, NotificationType notificationType) {
        List<User> receivers = userRepository.findAllById(receiverIds);
        List<Notification> notifications = new ArrayList<>();
        receivers.forEach(receiver -> {
            Notification notification = new Notification();
            notification.setCreator(sender);
            notification.setUserTarget(receiver);
            notification.setMessage(sender.getCompany().getName());
            notification.setNotificationType(notificationType);
            notification.setNotificationCreatedAt(Instant.now());
            notification.setTopicId(topicId);
            notification.setObjectFunctionId(objectFunctionId);

            notifications.add(notification);
        });

        notificationRepository.saveAll(notifications);
    }

    @Deprecated
    public void createWorkOrderCancelNotification(WorkOrder workOrder) {
        List<Notification> notifications = new ArrayList<>();

        User creator = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
        List<User> receivers = userRepository.findAllById(workOrder.getWorkOrderUsers().stream().map(WorkOrderUser::getUserId).collect(Collectors.toList()));
        receivers.forEach(receiver -> {
            Notification notification = new Notification();
            notification.setCreator(creator);
            notification.setUserTarget(receiver);
            notification.setMessage(creator.getCompany().getName());
            notification.setNotificationType(NotificationType.WORK_ORDER_CANCELLED);
            notification.setNotificationCreatedAt(Instant.now());
            notification.setTopicId(workOrder.getOrderType().getCode());
            notification.setObjectFunctionId(workOrder.getId());

            notifications.add(notification);
        });

        notificationRepository.saveAll(notifications);
    }

    @Deprecated
    public void createWorkOrderCompleteRequestApprovalNotification(WorkOrder workOrder) {
        List<Notification> notifications = new ArrayList<>();
        User creator = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
        List<User> receivers = workOrder.getWorkOrderUsers().stream().map(WorkOrderUser::getUser).collect(Collectors.toList());
        receivers.forEach(receiver -> {
            Notification notification = new Notification();
            notification.setCreator(creator);
            notification.setUserTarget(receiver);
            notification.setMessage(creator.getCompany().getName());
            notification.setNotificationType(NotificationType.WORK_ORDER_COMPLETE_REQUEST_APPROVAL);
            notification.setNotificationCreatedAt(Instant.now());
            notification.setTopicId(workOrder.getOrderType().getCode());
            notification.setObjectFunctionId(workOrder.getId());

            notifications.add(notification);
        });

        notificationRepository.saveAll(notifications);
    }

    @Deprecated
    public void createWorkOrderRejectApprovalNotification(WorkOrder workOrder, Long reportedBy) {
        User creator = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
        User receiver = userRepository.getOne(reportedBy);
        Notification notification = new Notification();
        notification.setCreator(creator);
        notification.setUserTarget(receiver);
        notification.setMessage(creator.getCompany().getName());
        notification.setNotificationType(NotificationType.WORK_ORDER_APPROVAL_REJECTED);
        notification.setNotificationCreatedAt(Instant.now());
        notification.setTopicId(workOrder.getOrderType().getCode());
        notification.setObjectFunctionId(workOrder.getId());

        notificationRepository.save(notification);
    }

    @Deprecated
    public void createWorkOrderCancelApprovalNotification(WorkOrder workOrder, Long reportedBy) {
        User creator = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
        User receiver = userRepository.getOne(reportedBy);
        Notification notification = new Notification();
        notification.setCreator(creator);
        notification.setUserTarget(receiver);
        notification.setMessage(creator.getCompany().getName());
        notification.setNotificationType(NotificationType.WORK_ORDER_APPROVAL_CANCELLED);
        notification.setNotificationCreatedAt(Instant.now());
        notification.setTopicId(workOrder.getOrderType().getCode());
        notification.setObjectFunctionId(workOrder.getId());

        notificationRepository.save(notification);
    }

    @Deprecated
    public void createWorkOrderApproveApprovalNotification(WorkOrder workOrder, Long reportedBy) {
        User creator = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
        User receiver = userRepository.getOne(reportedBy);
        Notification notification = new Notification();
        notification.setCreator(creator);
        notification.setUserTarget(receiver);
        notification.setMessage(creator.getCompany().getName());
        notification.setNotificationType(NotificationType.WORK_ORDER_APPROVAL_APPROVED);
        notification.setNotificationCreatedAt(Instant.now());
        notification.setTopicId(workOrder.getOrderType().getCode());
        notification.setObjectFunctionId(workOrder.getId());

        notificationRepository.save(notification);
    }

    @Deprecated
    public void createWorkOrderCMApprovalRequestApprovalNotification(WorkOrder workOrder) {
        List<Notification> notifications = new ArrayList<>();
        User creator = userRepository.getOne(workOrder.getCreatedBy());
        List<User> receivers = userRepository.findByCompanyTypeAndOrderByName(CompanyType.IN_HOUSE.name());
        receivers.forEach(receiver -> {
            Notification notification = new Notification();
            notification.setCreator(creator);
            notification.setUserTarget(receiver);
            notification.setMessage(creator.getCompany().getName());
            notification.setNotificationType(NotificationType.WORK_ORDER_CM_REQUEST_APPROVAL);
            notification.setNotificationCreatedAt(Instant.now());
            notification.setTopicId(workOrder.getOrderType().getCode());
            notification.setObjectFunctionId(workOrder.getId());

            notifications.add(notification);
        });

        notificationRepository.saveAll(notifications);
    }

    @Deprecated
    public void createWorkOrderCMApprovedNotification(WorkOrder workOrder) {
        User creator = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
        User receiver = userRepository.getOne(workOrder.getCreatedBy());
        Notification notification = new Notification();
        notification.setCreator(creator);
        notification.setUserTarget(receiver);
        notification.setMessage(creator.getCompany().getName());
        notification.setNotificationType(workOrder.getApproved() ? NotificationType.WORK_ORDER_CM_REQUEST_APPROVAL_APPROVED : NotificationType.WORK_ORDER_CM_REQUEST_APPROVAL_DECLINED);
        notification.setNotificationCreatedAt(Instant.now());
        notification.setTopicId(workOrder.getOrderType().getCode());
        notification.setObjectFunctionId(workOrder.getId());

        notificationRepository.save(notification);
    }

    @Deprecated
    public void createWorkOrderRequestChangeNotification(WorkOrder workOrder, Long createdById) {
        User creator = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
        User receiver = userRepository.getOne(createdById);
        Notification notification = new Notification();
        notification.setCreator(creator);
        notification.setUserTarget(receiver);
        notification.setMessage(creator.getCompany().getName());
        notification.setNotificationType(NotificationType.WORK_ORDER_REQUEST_CHANGE);
        notification.setNotificationCreatedAt(Instant.now());
        notification.setTopicId(workOrder.getOrderType().getCode());
        notification.setObjectFunctionId(workOrder.getId());

        notificationRepository.save(notification);
    }

    @Deprecated
    public void createWorkOrderRejectApproveRequestChangeNotification(WorkOrder workOrder, Long createdById, boolean isApproved) {
        User creator = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
        User receiver = userRepository.getOne(createdById);
        Notification notification = new Notification();
        notification.setCreator(creator);
        notification.setUserTarget(receiver);
        notification.setMessage(creator.getCompany().getName());
        notification.setNotificationType(isApproved ? NotificationType.WORK_ORDER_APPROVED_CHANGE : NotificationType.WORK_ORDER_REJECTED_CHANGE);
        notification.setNotificationCreatedAt(Instant.now());
        notification.setTopicId(workOrder.getOrderType().getCode());
        notification.setObjectFunctionId(workOrder.getId());
        notificationRepository.save(notification);
    }

    public void updateStatusWorkOrderNotification(WorkOrder workOrder, Long createdById, NotificationType notificationType) {
        User creator = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
        User receiver = userRepository.getOne(createdById);
        Notification notification = new Notification();
        notification.setCreator(creator);
        notification.setUserTarget(receiver);
        notification.setMessage(creator.getCompany().getName());
        notification.setNotificationType(notificationType);
        notification.setNotificationCreatedAt(Instant.now());
        notification.setTopicId(workOrder.getOrderType().getCode());
        notification.setObjectFunctionId(workOrder.getId());
        notificationRepository.save(notification);
    }

    public void createDataRequestNotification(DataRequest dataRequest, List<Long> receiverIds) {
        User sender = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
        List<User> receivers = userRepository.findAllById(receiverIds);
        if (dataRequest.getDataRequestStatus() == DataCompletionRequestStatus.REQUESTED
                || dataRequest.getDataRequestStatus() == DataCompletionRequestStatus.IN_PROGRESS) {
            NotiUtils.post(NotiCode.DR_REQUESTED, toNotiPostIn(sender, receiverIds, dataRequest));
            saveNotification(receiverIds, sender, dataRequest.getRequestId(), dataRequest.getId(), NotificationType.DATA_REQUEST);
        } else if (dataRequest.getDataRequestStatus() == DataCompletionRequestStatus.COMPLETED) {
            NotiUtils.post(NotiCode.DR_COMPLETED, toNotiPostIn(sender, receiverIds, dataRequest));
            saveNotification(receiverIds, sender, dataRequest.getRequestId(), dataRequest.getId(), NotificationType.DATA_REQUEST);
        } else if (dataRequest.getDataRequestStatus() == DataCompletionRequestStatus.CANCELLED) {
            NotiUtils.post(NotiCode.DR_CANCELED, toNotiPostIn(sender, receiverIds, dataRequest));
            saveNotification(receiverIds, sender, dataRequest.getRequestId(), dataRequest.getId(), NotificationType.DATA_REQUEST);
        } else if (dataRequest.getDataRequestStatus() == DataCompletionRequestStatus.DECLINED) {
            NotiUtils.post(NotiCode.DR_DECLINED, toNotiPostIn(sender, receiverIds, dataRequest));
            saveNotification(receiverIds, sender, dataRequest.getRequestId(), dataRequest.getId(), NotificationType.DATA_REQUEST);
        }
    }

    public void createNotificationForOverdueDataRequest(DataRequest dataRequest, List<DataRequestUser> dataRequestUserList) {
        List<Notification> notificationList = Lists.newArrayList();
        dataRequestUserList.forEach(dataRequestUser -> {
            Notification notification = new Notification();
            notification.setCreator(dataRequest.getCreatedByUser());
            notification.setUserTarget(dataRequestUser.getUser());
            notification.setNotificationType(NotificationType.DATA_REQUEST_OVERDUE_REQUESTEE);
            notification.setObjectFunctionId(dataRequest.getId());
            notification.setMessage(dataRequest.getRequestId());
            notification.setNotificationCreatedAt(Instant.now());
            notificationList.add(notification);
        });
        Notification notification = new Notification();
        notification.setCreator(dataRequest.getCreatedByUser());
        notification.setUserTarget(dataRequest.getCreatedByUser());
        notification.setNotificationType(NotificationType.DATA_REQUEST_OVERDUE_REQUESTER);
        notification.setObjectFunctionId(dataRequest.getId());
        notification.setMessage(dataRequest.getRequestId());
        notification.setNotificationCreatedAt(Instant.now());
        notificationList.add(notification);

        notificationRepository.saveAll(notificationList);
    }

    public void createNotificationDataRequestForReopen(DataRequest dataRequest, List<Long> receiverIds) {
        User sender = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
        List<User> receivers = userRepository.findAllById(receiverIds);
        NotiUtils.post(NotiCode.DR_REOPENED, toNotiPostIn(sender, receiverIds, dataRequest));
        saveNotification(receiverIds, sender, dataRequest.getRequestId(), dataRequest.getId(), NotificationType.DATA_REQUEST);
    }

//    public void createAlertNotification(Object alert, AlertType alertType, boolean test, String email) {
//        User sender = getSender(null);
//        List<Long> receiverIds;
//        List<User> receivers;
//        switch (alertType) {
//            case EFFICIENCY://Uptime
//                MoldEfficiency efficiency = (MoldEfficiency) alert;
//                String limitRangeEfficiency = EfficiencyStatus.OUTSIDE_L1.equals(efficiency.getEfficiencyStatus()) ? "outside limit 1 (L1)" : "outside limit 2 (L2)";
//                receivers = getAlertNotificationReceivers(AlertType.EFFICIENCY, test, email);
//                receiverIds = receivers.stream().map(User::getId).collect(Collectors.toList());
//                NotiUtils.post(NotiCode.TOOLING_UPTIME, toNotiPostIn(sender, receiverIds, efficiency.getId(), efficiency.getMoldId(), limitRangeEfficiency, toAlrEmlNotData(efficiency, alertType)));
//                saveNotification(receiverIds, sender, null, efficiency.getId(), NotificationType.ALERT);
//                break;
//            case CYCLE_TIME://Cycle time
//                MoldCycleTime cycleTime = (MoldCycleTime) alert;
//                String limitRangeCycleTime = CycleTimeStatus.OUTSIDE_L1.equals(cycleTime.getCycleTimeStatus()) ? "outside limit 1 (L1)" : "outside limit 2 (L2)";
//                receivers = getAlertNotificationReceivers(AlertType.CYCLE_TIME, test, email);
//                receiverIds = receivers.stream().map(User::getId).collect(Collectors.toList());
//                NotiUtils.post(NotiCode.TOOLING_CYCLE_TIME, toNotiPostIn(sender, receiverIds, cycleTime.getId(), cycleTime.getMoldId(), limitRangeCycleTime, toAlrEmlNotData(cycleTime, alertType)));
//                saveNotification(receiverIds, sender, null, cycleTime.getId(), NotificationType.ALERT);
//                break;
//            case DISCONNECTED://Tooling Disconnection
//                MoldDisconnect disconnect = (MoldDisconnect) alert;
//                receivers = getAlertNotificationReceivers(AlertType.DISCONNECTED, test, email);
//                receiverIds = receivers.stream().map(User::getId).collect(Collectors.toList());
//                NotiUtils.post(NotiCode.TOOLING_DISCONNECTED, toNotiPostIn(sender, receiverIds, disconnect.getId(), disconnect.getMoldId(), "", toAlrEmlNotData(disconnect, alertType)));
//                saveNotification(receiverIds, sender, null, disconnect.getId(), NotificationType.ALERT);
//                break;
//            case TERMINAL_DISCONNECTED://Terminal Disconnection
//                TerminalDisconnect tDisconnect = (TerminalDisconnect) alert;
//                receivers = getAlertNotificationReceivers(AlertType.TERMINAL_DISCONNECTED, test, email);
//                receiverIds = receivers.stream().map(User::getId).collect(Collectors.toList());
//                NotiUtils.post(NotiCode.TERMINAL_DISCONNECTED, toNotiPostIn(sender, receiverIds, tDisconnect.getId(), tDisconnect.getTerminalId(), "", toAlrEmlNotData(tDisconnect, alertType)));
//                saveNotification(receiverIds, sender, null, tDisconnect.getId(), NotificationType.ALERT);
//                break;
//            case REFURBISHMENT://Refurbishment
//                MoldRefurbishment refurbishment = (MoldRefurbishment) alert;
//                receivers = getAlertNotificationReceivers(AlertType.REFURBISHMENT, test, email);
//                receiverIds = receivers.stream().map(User::getId).collect(Collectors.toList());
//                NotiUtils.post(NotiCode.TOOLING_REFURBISHMENT, toNotiPostIn(sender, receiverIds, refurbishment.getId(), refurbishment.getMold().getEquipmentCode(), ""));
//                saveNotification(receiverIds, sender, null, refurbishment.getId(), NotificationType.ALERT);
//                break;
//            case DETACHMENT://Detachment
//                MoldDetachment detachment = (MoldDetachment) alert;
//                receivers = getAlertNotificationReceivers(AlertType.DETACHMENT, test, email);
//                receiverIds = receivers.stream().map(User::getId).collect(Collectors.toList());
//                NotiUtils.post(NotiCode.SENSOR_DETACHED, toNotiPostIn(sender, receiverIds, detachment.getId(), detachment.getMold().getEquipmentCode(), ""));
//                saveNotification(receiverIds, sender, null, detachment.getId(), NotificationType.ALERT);
//                break;
//            case RELOCATION://Relocation
//                MoldLocation moldLocation = (MoldLocation) alert;
//                String status = moldLocation.getMoldLocationStatus().getTitle();
//                receivers = getAlertNotificationReceivers(AlertType.RELOCATION, test, email);
//                receiverIds = receivers.stream().map(User::getId).collect(Collectors.toList());
//                User relocationSender = getSender(SecurityUtils.getUserId());
//                NotiUtils.post(NotiCode.TOOLING_RELOCATED, toNotiPostIn(relocationSender, receiverIds, moldLocation.getId(), moldLocation.getMoldId(), status, toAlrEmlNotData(moldLocation, alertType)));
//                saveNotification(receiverIds, relocationSender, null, moldLocation.getId(), NotificationType.ALERT);
//                break;
//        }
//    }

//    public void createAlertBatchNotification(List<?> alerts, AlertType alertType, PeriodType periodType) {
//        alerts.forEach(a -> createAlertNotification(a, alertType, periodType));
//    }

//    public void createAlertNotification(Object alert, AlertType alertType) {
//        createAlertNotification(alert, alertType, null);
//    }

//    public void createAlertNotification(List<?> alerts, AlertType alertType, PeriodType periodType, Instant time) {
//        User sender = getSender(null);
//        List<Long> receiverIds;
//        List<User> receivers;
//        switch (alertType) {
//            case EFFICIENCY://Uptime
//                MoldEfficiency efficiency = (MoldEfficiency) alerts.get(0);
//                String limitRangeEfficiency = EfficiencyStatus.OUTSIDE_L1.equals(efficiency.getEfficiencyStatus()) ? "outside limit 1 (L1)" : "outside limit 2 (L2)";
//                receiverIds = periodType == null ? AlertUtils.getUserIdsByMold(AlertType.EFFICIENCY, efficiency.getMold().getId()) : AlertUtils.getUserIdsByMoldAndPeriodType(AlertType.EFFICIENCY, efficiency.getMold().getId(), periodType);
//                NotiUtils.post(NotiCode.TOOLING_UPTIME, toNotiPostIn(sender, receiverIds, efficiency.getId(), efficiency.getMold().getId(), limitRangeEfficiency, toAlrEmlNotData(alerts, alertType, time)));
//                saveNotification(receiverIds, sender, null, efficiency.getId(), NotificationType.ALERT);
//                break;
//            case CYCLE_TIME://Cycle time
//                MoldCycleTime cycleTime = (MoldCycleTime) alerts.get(0);
//                String limitRangeCycleTime = CycleTimeStatus.OUTSIDE_L1.equals(cycleTime.getCycleTimeStatus()) ? "outside limit 1 (L1)" : "outside limit 2 (L2)";
//                receiverIds = periodType == null ? AlertUtils.getUserIdsByMold(AlertType.CYCLE_TIME, cycleTime.getMold().getId()) : AlertUtils.getUserIdsByMoldAndPeriodType(AlertType.CYCLE_TIME, cycleTime.getMold().getId(), periodType);
//                NotiUtils.post(NotiCode.TOOLING_CYCLE_TIME, toNotiPostIn(sender, receiverIds, cycleTime.getId(), cycleTime.getMold().getId(), limitRangeCycleTime, toAlrEmlNotData(cycleTime, alertType)));
//                saveNotification(receiverIds, sender, null, cycleTime.getId(), NotificationType.ALERT);
//                break;
//            case DISCONNECTED://Tooling Disconnection
//                MoldDisconnect disconnect = (MoldDisconnect) alerts.get(0);
//                receiverIds = periodType == null ? AlertUtils.getUserIdsByMold(AlertType.DISCONNECTED, disconnect.getMold().getId()) : AlertUtils.getUserIdsByMoldAndPeriodType(AlertType.DISCONNECTED, disconnect.getMold().getId(), periodType);
//                NotiUtils.post(NotiCode.TOOLING_DISCONNECTED, toNotiPostIn(sender, receiverIds, disconnect.getId(), disconnect.getMold().getId(), "", toAlrEmlNotData(disconnect, alertType)));
//                saveNotification(receiverIds, sender, null, disconnect.getId(), NotificationType.ALERT);
//                break;
//            case TERMINAL_DISCONNECTED://Terminal Disconnection
//                TerminalDisconnect tDisconnect = (TerminalDisconnect) alerts.get(0);
//                receiverIds = periodType == null ? AlertUtils.getUserIdsByTerminal(AlertType.TERMINAL_DISCONNECTED, tDisconnect.getTerminal().getId()) : AlertUtils.getUserIdsByTerminalAndPeriodType(AlertType.TERMINAL_DISCONNECTED, tDisconnect.getTerminal().getId(), periodType);
//                NotiUtils.post(NotiCode.TERMINAL_DISCONNECTED, toNotiPostIn(sender, receiverIds, tDisconnect.getId(), tDisconnect.getTerminal().getId(), "", toAlrEmlNotData(tDisconnect, alertType)));
//                saveNotification(receiverIds, sender, null, tDisconnect.getId(), NotificationType.ALERT);
//                break;
//            case REFURBISHMENT://Refurbishment
//                MoldRefurbishment refurbishment = (MoldRefurbishment) alerts.get(0);
//                receiverIds = periodType == null ? AlertUtils.getUserIdsByMold(AlertType.REFURBISHMENT, refurbishment.getMold().getId()) : AlertUtils.getUserIdsByMoldAndPeriodType(AlertType.REFURBISHMENT, refurbishment.getMold().getId(), periodType);
//                NotiUtils.post(NotiCode.TOOLING_REFURBISHMENT, toNotiPostIn(sender, receiverIds, refurbishment.getId(), refurbishment.getMold().getEquipmentCode(), ""));
//                saveNotification(receiverIds, sender, null, refurbishment.getId(), NotificationType.ALERT);
//                break;
//            case DETACHMENT://Detachment
//                MoldDetachment detachment = (MoldDetachment) alerts.get(0);
//                receiverIds = periodType == null ? AlertUtils.getUserIdsByMold(AlertType.DETACHMENT, detachment.getMold().getId()) : AlertUtils.getUserIdsByMoldAndPeriodType(AlertType.DETACHMENT, detachment.getMold().getId(), periodType);
//                NotiUtils.post(NotiCode.SENSOR_DETACHED, toNotiPostIn(sender, receiverIds, detachment.getId(), detachment.getMold().getEquipmentCode(), ""));
//                saveNotification(receiverIds, sender, null, detachment.getId(), NotificationType.ALERT);
//                break;
//            case RELOCATION://Relocation
//                MoldLocation moldLocation = (MoldLocation) alerts.get(0);
//                String status = MoldLocationStatus.UNAPPROVED.equals(moldLocation.getMoldLocationStatus()) ? MoldLocationStatus.DISAPPROVED.getTitle() :  moldLocation.getMoldLocationStatus().getTitle();
//                receiverIds = periodType == null ? AlertUtils.getUserIdsByMold(AlertType.RELOCATION, moldLocation.getMold().getId()) : AlertUtils.getUserIdsByMoldAndPeriodType(AlertType.RELOCATION, moldLocation.getMold().getId(), periodType);
//                User relocationSender = getSender(SecurityUtils.getUserId());
//                NotiUtils.post(NotiCode.TOOLING_RELOCATED, toNotiPostIn(relocationSender, receiverIds, moldLocation.getId(), moldLocation.getMold().getId(), status, toAlrEmlNotData(moldLocation, alertType)));
//                saveNotification(receiverIds, sender, null, moldLocation.getId(), NotificationType.ALERT);
//                break;
//        }
//    }

    public void createAlertNotification(List<?> alerts, AlertType alertType, List<Long> receiverIds, Instant time, PeriodType periodType) {
        User sender = getSender(null);
        switch (alertType) {
            case EFFICIENCY://Uptime
                MoldEfficiency efficiency = (MoldEfficiency) alerts.get(0);
//                String limitRangeEfficiency = EfficiencyStatus.OUTSIDE_L1.equals(efficiency.getEfficiencyStatus()) ? "outside limit 1 (L1)" : "outside limit 2 (L2)";
                NotiUtils.post(NotiCode.TOOLING_UPTIME, toNotiPostIn(sender, receiverIds, efficiency.getId(), efficiency.getMold().getId(), "", toAlrEmlNotData(alerts, alertType, time, periodType)));
                saveNotification(receiverIds, sender, null, efficiency.getId(), NotificationType.ALERT);
                break;
            case CYCLE_TIME://Cycle time\
                MoldCycleTime cycleTime = (MoldCycleTime) alerts.get(0);
//                String limitRangeCycleTime = CycleTimeStatus.OUTSIDE_L1.equals(cycleTime.getCycleTimeStatus()) ? "outside limit 1 (L1)" : "outside limit 2 (L2)";
                NotiUtils.post(NotiCode.TOOLING_CYCLE_TIME, toNotiPostIn(sender, receiverIds, cycleTime.getId(), cycleTime.getMold().getId(), "", toAlrEmlNotData(alerts, alertType, time, periodType)));
                saveNotification(receiverIds, sender, null, cycleTime.getId(), NotificationType.ALERT);
                break;
            case DISCONNECTED://Tooling Disconnection
                MoldDisconnect disconnect = (MoldDisconnect) alerts.get(0);
                String numberOfToolings = alerts.size() > 1
                        ? alerts.size() + "toolings are" : "Tooling " + disconnect.getMold().getEquipmentCode() + " is";
                NotiUtils.post(NotiCode.TOOLING_DISCONNECTED, toNotiPostIn(sender, receiverIds, disconnect.getId(), disconnect.getMold().getId(), numberOfToolings, toAlrEmlNotData(alerts, alertType, time, periodType)));
                saveNotification(receiverIds, sender, null, disconnect.getId(), NotificationType.ALERT);
                break;
            case TERMINAL_DISCONNECTED://Terminal Disconnection
                TerminalDisconnect tDisconnect = (TerminalDisconnect) alerts.get(0);
                String numberOfTerminals = alerts.size() > 1
                        ? alerts.size() + "terminals are" : "Terminal " + tDisconnect.getTerminal().getEquipmentCode() + " is";
                NotiUtils.post(NotiCode.TERMINAL_DISCONNECTED, toNotiPostIn(sender, receiverIds, tDisconnect.getId(), tDisconnect.getTerminal().getId(), numberOfTerminals, toAlrEmlNotData(alerts, alertType, time, periodType)));
                saveNotification(receiverIds, sender, null, tDisconnect.getId(), NotificationType.ALERT);
                break;
//            case REFURBISHMENT://Refurbishment
//                MoldRefurbishment refurbishment = (MoldRefurbishment) alerts.get(0);
//                NotiUtils.post(NotiCode.TOOLING_REFURBISHMENT, toNotiPostIn(sender, receiverIds, refurbishment.getId(), refurbishment.getMold().getEquipmentCode(), ""));
//                saveNotification(receiverIds, sender, null, refurbishment.getId(), NotificationType.ALERT);
//                break;
            case DETACHMENT://Detachment
                MoldDetachment detachment = (MoldDetachment) alerts.get(0);
                String code = detachment.getMold() != null ? detachment.getMold().getCounterCode() : "";
                NotiUtils.post(NotiCode.SENSOR_DETACHED, toNotiPostIn(sender, receiverIds, detachment.getId(), code, ""));
                saveNotification(receiverIds, sender, null, detachment.getId(), NotificationType.ALERT);
                break;
            case RELOCATION://Relocation
                MoldLocation moldLocation = (MoldLocation) alerts.get(0);
//                String status = MoldLocationStatus.UNAPPROVED.equals(moldLocation.getMoldLocationStatus()) ? MoldLocationStatus.DISAPPROVED.getTitle() :  moldLocation.getMoldLocationStatus().getTitle();
                User relocationSender = getSender(SecurityUtils.getUserId());
                NotiUtils.post(NotiCode.TOOLING_RELOCATED, toNotiPostIn(relocationSender, receiverIds, moldLocation.getId(), moldLocation.getMold().getId(), "", toAlrEmlNotData(alerts, alertType, time, periodType)));
                saveNotification(receiverIds, sender, null, moldLocation.getId(), NotificationType.ALERT);
                break;
        }
    }

    private List<User> getAlertNotificationReceivers(AlertType alertType, boolean test, String email){
        if (test) {
            List<User> receivers = userRepository.findByEmailInAndDeletedIsFalse(Collections.singletonList(email));
            return receivers;
        } else {
            List<UserAlert> userAlerts = BeanUtils.get(UserAlertRepository.class).findByAlertTypeAndAlertOnIsTrue(alertType);
            List<User> receivers = userAlerts.stream().map(UserAlert::getUser).collect(Collectors.toList());
            return receivers;
        }
    }

    private AlrEmlNotData toAlrEmlNotData(Object alert, AlertType alertType, PeriodType periodType) {
        switch (alertType) {
            case EFFICIENCY://Uptime
                MoldEfficiency efficiency = (MoldEfficiency) alert;
                return AlrEmlNotData.builder()
                        .dateString(getTitleDate(efficiency.getNotificationAt(), periodType))
                        .logo("alert")
                        .columnTitle1("Tooling ID")
                        .columnTitle2("Company")
                        .columnTitle3("Plant")
                        .columnTitle4("Uptime Status")
                        .columnValue1(efficiency.getMold().getEquipmentCode())
                        .columnValue2(efficiency.getMold().getCompanyName())
                        .columnValue3(efficiency.getMold().getLocationName())
                        .columnValue4(efficiency.getEfficiencyStatus().getTitle())
                        .ctaTitle("Log In and View Details")
                        .build();
            case CYCLE_TIME://Cycle time
                MoldCycleTime cycleTime = (MoldCycleTime) alert;
                return AlrEmlNotData.builder()
                        .dateString(getTitleDate(cycleTime.getNotificationAt(), periodType))
                        .logo("alert")
                        .columnTitle1("Tooling ID")
                        .columnTitle2("Company")
                        .columnTitle3("Plant")
                        .columnTitle4("CT Status")
                        .columnValue1(cycleTime.getMold().getEquipmentCode())
                        .columnValue2(cycleTime.getMold().getCompanyName())
                        .columnValue3(cycleTime.getMold().getLocationName())
                        .columnValue4(cycleTime.getCycleTimeStatus().getTitle())
                        .ctaTitle("Log In and View Details")
                        .build();
            case DISCONNECTED://Disconnection
                MoldDisconnect disconnect = (MoldDisconnect) alert;
                return AlrEmlNotData.builder()
                        .dateString(getTitleDate(disconnect.getNotificationAt(), periodType))
                        .logo("disconnection")
                        .columnTitle1("Tooling ID")
                        .columnTitle2("Company")
                        .columnTitle3("Plant")
                        .columnTitle4("Last Connection")
                        .columnValue1(disconnect.getMold().getEquipmentCode())
                        .columnValue2(disconnect.getMold().getCompanyName())
                        .columnValue3(disconnect.getMold().getLocationName())
                        .columnValue4(DateUtils2.format(disconnect.getMold().getLastShotAt(), DateUtils2.DatePattern.yyyy_MM_dd_HH_mm_ss, DateUtils2.Zone.GMT))
                        .ctaTitle("Log In and View Details")
                        .build();
            case TERMINAL_DISCONNECTED://Terminal Disconnection
                TerminalDisconnect tDisconnect = (TerminalDisconnect) alert;
                return AlrEmlNotData.builder()
                        .dateString(getTitleDate(tDisconnect.getNotificationAt(), periodType))
                        .logo("disconnection")
                        .columnTitle1("Terminal ID")
                        .columnTitle2("Company")
                        .columnTitle3("Plant")
                        .columnTitle4("Last Connection")
                        .columnValue1(tDisconnect.getTerminal().getEquipmentCode())
                        .columnValue2(tDisconnect.getTerminal().getCompanyName())
                        .columnValue3(tDisconnect.getTerminal().getLocationName())
                        .columnValue4(DateUtils2.format(tDisconnect.getNotificationAt(), DateUtils2.DatePattern.yyyy_MM_dd_HH_mm_ss, DateUtils2.Zone.GMT))
                        .ctaTitle("Log In and View Details")
                        .build();
            case RELOCATION://Relocation
                MoldLocation moldLocation = (MoldLocation) alert;
                return AlrEmlNotData.builder()
                        .dateString(getTitleDate(moldLocation.getNotificationAt(), periodType))
                        .logo("alert")
                        .columnTitle1("Terminal ID")
                        .columnTitle2("Company")
                        .columnTitle3("Plant")
                        .columnTitle4("Status")
                        .columnValue1(moldLocation.getMold().getEquipmentCode())
                        .columnValue2(moldLocation.getMold().getCompanyName())
                        .columnValue3(moldLocation.getMold().getLocationName())
                        .columnValue4(MoldLocationStatus.UNAPPROVED.equals(moldLocation.getMoldLocationStatus()) ? MoldLocationStatus.DISAPPROVED.getTitle() :  moldLocation.getMoldLocationStatus().getTitle())
                        .ctaTitle("Log In and View Details")
                        .build();
            default: return new AlrEmlNotData();
        }
    }

    private AlrEmlNotData toAlrEmlNotData(List<?> alerts, AlertType alertType, Instant time, PeriodType periodType) {
        switch (alertType) {
            case EFFICIENCY://Uptime
                return AlrEmlNotData.builder()
                        .dateString(getTitleDate(time, periodType))
                        .logo("alert")
                        .columnTitle1("Tooling ID")
                        .columnTitle2("Company")
                        .columnTitle3("Plant")
                        .columnTitle4("Uptime Status")
                        .alerts(alerts.stream().map(a -> toAlrEmlNotItem(a, alertType)).collect(Collectors.toList()))
                        .ctaTitle("Log In and View Details")
                        .build();
            case CYCLE_TIME://Cycle time
                return AlrEmlNotData.builder()
                        .dateString(getTitleDate(time, periodType))
                        .logo("alert")
                        .columnTitle1("Tooling ID")
                        .columnTitle2("Company")
                        .columnTitle3("Plant")
                        .columnTitle4("CT Status")
                        .alerts(alerts.stream().map(a -> toAlrEmlNotItem(a, alertType)).collect(Collectors.toList()))
                        .ctaTitle("Log In and View Details")
                        .build();
            case DISCONNECTED://Disconnection
                return AlrEmlNotData.builder()
                        .dateString(getTitleDate(time, periodType))
                        .logo("disconnection")
                        .columnTitle1("Tooling ID")
                        .columnTitle2("Company")
                        .columnTitle3("Plant")
                        .columnTitle4("Last Connection")
                        .alerts(alerts.stream().map(a -> toAlrEmlNotItem(a, alertType)).collect(Collectors.toList()))
                        .ctaTitle("Log In and View Details")
                        .build();
            case TERMINAL_DISCONNECTED://Terminal Disconnection
                return AlrEmlNotData.builder()
                        .dateString(getTitleDate(time, periodType))
                        .logo("disconnection")
                        .columnTitle1("Terminal ID")
                        .columnTitle2("Company")
                        .columnTitle3("Plant")
                        .columnTitle4("Last Connection")
                        .alerts(alerts.stream().map(a -> toAlrEmlNotItem(a, alertType)).collect(Collectors.toList()))
                        .ctaTitle("Log In and View Details")
                        .build();
            case RELOCATION://Relocation
                return AlrEmlNotData.builder()
                        .dateString(getTitleDate(time, periodType))
                        .logo("alert")
                        .columnTitle1("Terminal ID")
                        .columnTitle2("Company")
                        .columnTitle3("Plant")
                        .columnTitle4("Status")
                        .alerts(alerts.stream().map(a -> toAlrEmlNotItem(a, alertType)).collect(Collectors.toList()))
                        .ctaTitle("Log In and View Details")
                        .build();
            default: return new AlrEmlNotData();
        }
    }

    private AlrEmlNotItem toAlrEmlNotItem(Object alert, AlertType alertType) {
        switch (alertType) {
            case EFFICIENCY://Uptime
                MoldEfficiency efficiency = (MoldEfficiency) alert;
                return AlrEmlNotItem.builder()
                        .columnValue1(efficiency.getMold().getEquipmentCode())
                        .columnValue2(efficiency.getMold().getCompanyName())
                        .columnValue3(efficiency.getMold().getLocationName())
                        .columnValue4(efficiency.getEfficiencyStatus().getTitle())
                        .build();
            case CYCLE_TIME://Cycle time
                MoldCycleTime cycleTime = (MoldCycleTime) alert;
                return AlrEmlNotItem.builder()
                        .columnValue1(cycleTime.getMold().getEquipmentCode())
                        .columnValue2(cycleTime.getMold().getCompanyName())
                        .columnValue3(cycleTime.getMold().getLocationName())
                        .columnValue4(cycleTime.getCycleTimeStatus().getTitle())
                        .build();
            case DISCONNECTED://Disconnection
                MoldDisconnect disconnect = (MoldDisconnect) alert;
                return AlrEmlNotItem.builder()
                        .columnValue1(disconnect.getMold().getEquipmentCode())
                        .columnValue2(disconnect.getMold().getCompanyName())
                        .columnValue3(disconnect.getMold().getLocationName())
                        .columnValue4(DateUtils2.format(disconnect.getMold().getLastShotAt(), DateUtils2.DatePattern.yyyy_MM_dd_HH_mm_ss, DateUtils2.Zone.GMT))
                        .build();
            case TERMINAL_DISCONNECTED://Terminal Disconnection
                TerminalDisconnect tDisconnect = (TerminalDisconnect) alert;
                return AlrEmlNotItem.builder()
                        .columnValue1(tDisconnect.getTerminal().getEquipmentCode())
                        .columnValue2(tDisconnect.getTerminal().getCompanyName())
                        .columnValue3(tDisconnect.getTerminal().getLocationName())
                        .columnValue4(DateUtils2.format(tDisconnect.getNotificationAt(), DateUtils2.DatePattern.yyyy_MM_dd_HH_mm_ss, DateUtils2.Zone.GMT))
                        .build();
            case RELOCATION://Relocation
                MoldLocation moldLocation = (MoldLocation) alert;
                return AlrEmlNotItem.builder()
                        .columnValue1(moldLocation.getMold().getEquipmentCode())
                        .columnValue2(moldLocation.getMold().getCompanyName())
                        .columnValue3(moldLocation.getMold().getLocationName())
                        .columnValue4(MoldLocationStatus.UNAPPROVED.equals(moldLocation.getMoldLocationStatus()) ? MoldLocationStatus.DISAPPROVED.getTitle() :  moldLocation.getMoldLocationStatus().getTitle())                        .build();
            default: return new AlrEmlNotItem();
        }
    }

    private String getTitleDate(Instant instant, PeriodType periodType) {
        switch (periodType) {
            case WEEKLY:
                return instant != null ? "Week " +  DateUtils2.format(instant, DateUtils2.DatePattern.ww_YYYY, DateUtils2.Zone.GMT)
                        : "Week " +  DateUtils2.format(Instant.now(), DateUtils2.DatePattern.ww_YYYY, DateUtils2.Zone.GMT);
            case MONTHLY:
                return instant != null ? DateUtils2.format(instant, DateUtils2.DatePattern.MMM_YYYY, DateUtils2.Zone.GMT)
                        : DateUtils2.format(Instant.now(), DateUtils2.DatePattern.MMM_YYYY, DateUtils2.Zone.GMT);
            case DAILY:
            default:
                return instant != null ? DateUtils2.format(instant, DateUtils2.DatePattern.MMM_dd_YYYY, DateUtils2.Zone.GMT)
                        : DateUtils2.format(Instant.now(), DateUtils2.DatePattern.MMM_dd_YYYY, DateUtils2.Zone.GMT);
        }
    }

    private NotiPostIn toNotiPostIn(User sender, List<Long> receiverIds, Long alertId, String code, String additionalContent) {
        LogicUtils.assertNotNull(alertId, "alertId");

        return new NotiPostIn()//
                .setNotiPriority(PriorityType.LOW)//
                .setSender(sender)//
                .addRecipients(receiverIds)//
                .setDataId(alertId)//
                .setParam("id", alertId)//
                .setParam("senderCompanyName", sender.getCompany().getName())//
                .setParam("code", code)//
                .setParam("additionalContent", additionalContent)//
                .setParam("ctaTitle", "Log In and View Details")//
                ;
    }

    private NotiPostIn toNotiPostIn(User sender, List<Long> receiverIds, Long alertId, Long mainObjectId, String additionalContent, AlrEmlNotData alrEmlNotData) {
        LogicUtils.assertNotNull(alertId, "alertId");

        return new NotiPostIn()//
                .setNotiPriority(PriorityType.LOW)//
                .setSender(sender)//
                .addRecipients(receiverIds)//
                .setDataId(alertId)//
                .setParam("id", mainObjectId)//
                .setParam("senderCompanyName", sender.getCompany().getName())//
                .setParam("additionalContent", additionalContent)//
                .setParam("logo", alrEmlNotData.getLogo())//
                .setParam("dateString", alrEmlNotData.getDateString())//
                .setParam("columnTitle1", alrEmlNotData.getColumnTitle1())//
                .setParam("columnTitle2", alrEmlNotData.getColumnTitle2())//
                .setParam("columnTitle3", alrEmlNotData.getColumnTitle3())//
                .setParam("columnTitle4", alrEmlNotData.getColumnTitle4())//
                .setParam("alerts", alrEmlNotData.getAlerts())//
                .setParam("ctaTitle", alrEmlNotData.getCtaTitle())//
                ;
    }

    private NotiPostIn toNotiPostIn(User sender, List<Long> receiverIds, DataRequest dataRequest) {
        LogicUtils.assertNotNull(dataRequest, "dataRequest");

        return new NotiPostIn()//
                .setNotiPriority(PriorityType.LOW)//
                .setSender(sender)//
                .addRecipients(receiverIds)//
                .setDataId(dataRequest.getId())//
                .setParam("id", dataRequest.getId())//
                .setParam("senderCompanyName", sender.getCompany().getName())//
                .setParam("requestId", dataRequest.getRequestId())//
                .setParam("requestType", dataRequest.getRequestDataType().getTitle())//
                ;
    }

    private NotiPostIn toNotiPostIn(User sender, List<Long> receiverIds, User user) {
        String companyName = user.getCompany() == null ? (user.getMemo().substring(13, (user.getMemo().length() - 3))) : user.getCompany().getName();
        return new NotiPostIn()//
                .setNotiPriority(PriorityType.LOW)//
                .setSender(sender)//
                .addRecipients(receiverIds)//
                .setDataId(user.getId())//
                .setParam("id", user.getId())//
                .setParam("senderCompanyName", sender.getCompany().getName())
                .setParam("logo", "user-access-requested")
                .setParam("name", user.getName())
                .setParam("email", user.getEmail())
                .setParam("companyName", companyName)
                .setParam("position", user.getPosition())
                .setParam("ctaTitle", "Log In and View Details")
                ;
    }

    private NotiPostIn toNotiPostIn(User sender, List<Long> receiverIds, SystemNote systemNote) {
        LogicUtils.assertNotNull(systemNote, "systemNote");

        String pageType = "";
        String pageTypeName = "";
        String menuUrl = "";
        if (PageType.TOOLING_SETTING.equals(systemNote.getSystemNoteFunction())) {
            menuUrl = "/common/dat-fam";
            pageType = "TOOLING";
            pageTypeName = "Tooling";
        } else if (PageType.PART_SETTING.equals(systemNote.getSystemNoteFunction())) {
            menuUrl = "/common/dat-fam";
            pageType = "PART";
            pageTypeName = "Part";
        }
        return new NotiPostIn()//
                .setNotiPriority(PriorityType.LOW)//
                .setSender(sender)//
                .addRecipients(receiverIds)//
                .setDataId(systemNote.getId())//
                .setParam("menuUrl", menuUrl)//
                .setParam("pageType", pageType)//
                .setParam("pageTypeName", pageTypeName)//
                .setParam("noteId", systemNote.getId())//
                .setParam("objectFunctionId", systemNote.getObjectFunctionId())//
                .setParam("senderCompanyName", sender.getCompany().getName())//
                ;
    }

    private NotiPostIn toNotiPostIn(User sender, List<Long> receiverIds, WorkOrder workOrder) {
        LogicUtils.assertNotNull(workOrder, "workOrder");

        String orderType = null;
        String orderTypeLowercase = null;
        String orderTypeName = null;
        String orderTypeDesc = null;
        String orderStatusName = workOrder.getStatus().getTitle();
        if (WorkOrderType.GENERAL.equals(workOrder.getOrderType())) {
            orderType = "General";
            orderTypeLowercase = "general";
            orderTypeName = "General Work Order";
            orderTypeDesc = "a General Work Order";
        } else if (WorkOrderType.INSPECTION.equals(workOrder.getOrderType())) {
            orderType = "Inspection";
            orderTypeLowercase = "inspection";
            orderTypeName = "Inspection Work Order";
            orderTypeDesc = "an Inspection Work Order";
        } else if (WorkOrderType.EMERGENCY.equals(workOrder.getOrderType())) {
            orderType = "Emergency";
            orderTypeLowercase = "emergency";
            orderTypeName = "Emergency Work Order";
            orderTypeDesc = "an Emergency Work Order";
        } else if (WorkOrderType.PREVENTATIVE_MAINTENANCE.equals(workOrder.getOrderType())) {
            orderType = "P.M.";
            orderTypeLowercase = "preventative maintenance";
            orderTypeName = "P.M. Work Order";
            orderTypeDesc = "a P.M. Work Order";
        } else if (WorkOrderType.CORRECTIVE_MAINTENANCE.equals(workOrder.getOrderType())) {
            orderType = "C.M.";
            orderTypeLowercase = "corrective maintenance";
            orderTypeName = "C.M. Work Order";
            orderTypeDesc = "a C.M. Work Order";
        } else if (WorkOrderType.DISPOSAL.equals(workOrder.getOrderType())) {
            orderType = "Disposal";
            orderTypeLowercase = "disposal";
            orderTypeName = "Disposal Work Order";
            orderTypeDesc = "a Disposal Work Order";
        } else if (WorkOrderType.REFURBISHMENT.equals(workOrder.getOrderType())) {
            orderType = "Refurbishment";
            orderTypeLowercase = "refurbishment";
            orderTypeName = "Refurbishment Work Order";
            orderTypeDesc = "a Refurbishment Work Order";
        } else {
            orderType = "Unknown";
            orderTypeLowercase = "unknown";
            orderTypeName = "Unknown Type of Work Order";
            orderTypeDesc = "an Unknown Type of Work Order";
        }

        return new NotiPostIn()//
                .setNotiPriority(workOrder.getPriority())//
                .setSender(sender)//
                .addRecipients(receiverIds)//
                .setDataId(workOrder.getId())//
                .setParam("id", workOrder.getId())//
                .setParam("workOrderId", workOrder.getWorkOrderId())//
                .setParam("orderType", orderType)//
                .setParam("orderTypeLowercase", orderTypeLowercase)//
                .setParam("orderTypeName", orderTypeName)//
                .setParam("orderTypeDesc", orderTypeDesc)//
                .setParam("orderStatusName", orderStatusName)//
                .setParam("orderPriority", workOrder.getPriority().getTitle())//
                .setParam("dueDate", DateUtils2.format(workOrder.getEnd(), DateUtils2.DatePattern.yyyy_MM_dd, DateUtils2.Zone.GMT))//
                .setParam("senderCompanyName", sender.getCompany().getName())//
                ;
    }

    private User getSender(Long senderId) {
        return senderId != null ? userRepository.getOne(senderId)
                : userRepository.findByEmailAndDeletedIsFalse("support@emoldino.com").orElse(userRepository.findAll().get(0));
    }

}
