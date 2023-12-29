package saleson.api.broadcastNotification;


import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import saleson.api.resource.ResourceHandler;
import saleson.api.user.UserService;
import saleson.common.config.Const;
import saleson.common.enumeration.CycleTimeStatus;
import saleson.common.enumeration.EfficiencyStatus;
import saleson.common.enumeration.MaintenanceStatus;
import saleson.common.enumeration.PageType;
import saleson.common.service.ContextWrapper;
import saleson.common.util.DataUtils;
import saleson.common.util.DateUtils;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.dto.BroadcastNotificationDTO;
import saleson.model.*;
import saleson.service.notification.BroadCastServiceImpl;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BroadcastNotificationService {
    @Autowired
    UserService userService;
    @Autowired
    BroadcastNotificationRepository broadcastNotificationRepository;
    @Autowired
    BroadCastServiceImpl broadCastService;
    @Autowired
    TaskExecutor taskExecutor;

/*
    @Autowired
    private ResourceHandler handler;
*/

    public Page<BroadcastNotification> findAll(Predicate predicate, Pageable pageable) {
        return broadcastNotificationRepository.findAll(predicate, pageable);
    }

/*
    public BroadcastNotificationDTO convertToDTO(BroadcastNotification model){
        String local=handler.getCurrentLang();
        Map<String,String>  resources= handler.getAllMessages(local);
        return convertToDTO(model,resources,local);
    }
*/

    public BroadcastNotificationDTO convertToDTO(BroadcastNotification model) {
        BroadcastNotificationDTO dto = DataUtils.mapper.map(model, BroadcastNotificationDTO.class);
        if (Const.NOTIFICATION_TYPE.ALERT.equals(dto.getNotificationType())) {
            dto.setTitle(dto.getSystemFunction().getTitle());
        }
/*
        String valueList = model.getValues();
//        if (!Const.LANGUAGE.en.equalsIgnoreCase(lang) && model.getValueListZh() != null)
//            valueList = model.getValueListZh();

        if (!StringUtils.isEmpty(valueList)) {
//            String template = handler.getValueInMap(resources, model.getMessage());
            try {
                //first replace
                List<String> values = ListUtils.parseJsonToList(valueList, String.class);
                dto.setValueList(values);
//                if (!values.isEmpty()) {
//                    template = MessageFormat.format(template, values.toArray());
//                }
            } catch (IOException e) {
                e.printStackTrace();
            }
//            dto.setMessage(template);
        }*/
        return dto;
    }

    public List<BroadcastNotificationDTO> convertToDTO(List<BroadcastNotification> list) {
//        String local=handler.getCurrentLang();
//        Map<String,String>  resources= handler.getAllMessages(local);
        return list.stream().map(m -> convertToDTO(m)).collect(Collectors.toList());
    }

    private void saveNotification(BroadcastNotification broadcast) {
        if (broadcast.getUserId() == null && broadcast.getUserTarget() != null)
            broadcast.setUserId(broadcast.getUserTarget().getId());
        broadcastNotificationRepository.save(broadcast);
        BroadcastNotificationDTO dto = convertToDTO(broadcast);
        //second replace
        String message = dto.getMessage();
        try {
            if (message != null && dto.getValueList() != null && !dto.getValueList().isEmpty()) {
                message = String.format(message, dto.getValueList().toArray(new String[dto.getValueList().size()]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        broadCastService.sendNotificationToUser(dto.getUserId(), message, dto);
        System.out.println("Push title: " + dto.getTitle() + " noti: " + message + " to user - " + dto.getUserId());
    }

    public void clearAll() {
        List<BroadcastNotification> notificationList = broadcastNotificationRepository.findAllByUserIdAndIsReadIsFalse(SecurityUtils.getUserId());
        for (BroadcastNotification n : notificationList) {
            n.setRead(true);
            n.setSeenTime(Instant.now());
        }
        broadcastNotificationRepository.saveAll(notificationList);
    }

    //notification for mobile
    public void createNotificationApp(Notification n) {
        try {
            if (PageType.SUPPORT_CENTER.equals(n.getSystemNoteFunction())) return;

            BroadcastNotification b = new BroadcastNotification();
            b.setSystemFunction(n.getSystemNoteFunction());
            b.setUserTarget(n.getUserTarget());
            b.setNotificationType(Const.NOTIFICATION_TYPE.NOTE);
            b.setInfors(Arrays.asList(String.valueOf(n.getObjectFunctionId())));
            //message value
            ResourceHandler resourceHandler = ContextWrapper.getContext().getBean("resourceHandler", ResourceHandler.class);
            String lang = StringUtils.isEmpty(n.getUserTarget().getLanguage()) ? "en" : n.getUserTarget().getLanguage();
            String message = resourceHandler.getMessageByKey("notification.note.mentioned", lang);
            b.setMessage(message);
            List<String> valueList = Arrays.asList(n.getCreator().getDisplayName(), getFullPathName(n));
//            b.setValues(DataUtils.gson.toJson(valueList));
            b.setValueListStr(String.join(",", valueList));
            saveNotification(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFullPathName(Notification n) {
        String first = "Alert";
        String titleMenu = n.getFunctionTitle();
        if (PageType.TOOLING_SETTING.equals(n.getSystemNoteFunction())) titleMenu = "Tooling";
        if (PageType.PART_SETTING.equals(n.getSystemNoteFunction())) titleMenu = "Part";
        titleMenu = titleMenu.replace(" Alert", "").replace(" (Settings)", "");
        if (n.getMenuKey() != null) {
            if (n.getMenuKey().startsWith("admin")) first = "Setting";
            if (Arrays.asList(
                    PageType.PART_DASHBOARD,
                    PageType.TOOLING_DASHBOARD,
                    PageType.TOOLING_SETTING,
                    PageType.PART_SETTING
            ).contains(n.getSystemNoteFunction()))
                first = "Overview";
        }
        String fullPath = first + " > " + titleMenu;
        return fullPath;
    }

    //noti for MoldMaintenance
    public void notificationMoldMaintenance(List<MoldMaintenance> list, User userTarget) {
        if (list == null) return;
        taskExecutor.execute(() -> {
            List<MoldMaintenance> lupm = list.stream().filter(m -> MaintenanceStatus.UPCOMING.equals(m.getMaintenanceStatus())).collect(Collectors.toList());
            List<MoldMaintenance> lovd = list.stream().filter(m -> MaintenanceStatus.OVERDUE.equals(m.getMaintenanceStatus())).collect(Collectors.toList());
            notificationMoldMaintenanceByType(lupm, userTarget);
            notificationMoldMaintenanceByType(lovd, userTarget);
        });
    }

    private void notificationMoldMaintenanceByType(List<MoldMaintenance> list, User userTarget) {
        try {

            if (list == null || list.isEmpty()) return;
            String upm = "upcoming preventive maintenance";
            String ovd = "overdue preventive maintenance";
            BroadcastNotification b = new BroadcastNotification();
            b.setSystemFunction(PageType.MAINTENANCE_ALERT);
            b.setUserTarget(userTarget);
            b.setNotificationType(Const.NOTIFICATION_TYPE.ALERT);
            b.setInfors(list.stream().map(m -> String.valueOf(m.getMoldId())).collect(Collectors.toList()));

//        ResourceHandler resourceHandler = ContextWrapper.getContext().getBean("resourceHandler", ResourceHandler.class);

            String message = "has an %s.";
            List<String> valueList = new ArrayList<>();
            if (MaintenanceStatus.UPCOMING.equals(list.get(0).getMaintenanceStatus())) {
                valueList.add(upm);
            } else
                valueList.add(ovd);

            List<String> codeList = list.stream().map(m -> m.getMold().getEquipmentCode()).collect(Collectors.toList());
            if (list.size() == 1) {
                message = "Tooling " + codeList.get(0) + " has an %s.";
            } else if (list.size() == 2) {
                message = "Tooling " + codeList.get(0) + " and tooling " + codeList.get(1) + " have %s.";
            } else if (list.size() > 2) {
                message = "Tooling " + codeList.get(0) + " and " + (codeList.size() - 1) + " others have %s.";
            }

            b.setMessage(message);
            b.setValueList(valueList);
            saveNotification(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void notificationMoldLocation(List<MoldLocation> list, User userTarget) {
        if (list == null) return;
        taskExecutor.execute(() -> {
            Map<Location, List<MoldLocation>> locationListMap = new HashMap<>();
            list.stream().forEach(m -> {
                if (m.getLocation() == null) return;
                List<MoldLocation> mlist = new ArrayList<>();
                if (locationListMap.containsKey(m.getLocation())) mlist = locationListMap.get(m.getLocation());
                mlist.add(m);
                locationListMap.put(m.getLocation(), mlist);
            });
            locationListMap.keySet().stream().forEach(location -> notificationMoldLocationByLocation(locationListMap.get(location), userTarget));
        });
    }

    public void notificationMoldLocationByLocation(List<MoldLocation> list, User userTarget) {
        try {
            if (list == null || list.isEmpty()) return;

            BroadcastNotification b = new BroadcastNotification();
            b.setSystemFunction(PageType.RELOCATION_ALERT);
            b.setUserTarget(userTarget);
            b.setNotificationType(Const.NOTIFICATION_TYPE.ALERT);
            b.setInfors(list.stream().map(m -> String.valueOf(m.getMoldId())).collect(Collectors.toList()));

//        ResourceHandler resourceHandler = ContextWrapper.getContext().getBean("resourceHandler", ResourceHandler.class);

            String message = "";
            List<String> valueList = new ArrayList<>();
            valueList.add("relocated");

            List<String> codeList = list.stream().map(m -> m.getMold().getEquipmentCode()).collect(Collectors.toList());
            String locationName = list.get(0).getLocation().getName();
            if (list.size() == 1) {
                message = "Tooling " + codeList.get(0) + " was %s to ";
            } else if (list.size() == 2) {
                message = "Tooling " + codeList.get(0) + " and tooling " + codeList.get(1) + " were %s to ";
            } else if (list.size() > 2) {
                message = "Tooling " + codeList.get(0) + " and " + (codeList.size() - 1) + " others were %s to ";
            }
            message += locationName + ".";
            b.setMessage(message);
            b.setValueList(valueList);
            saveNotification(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void notificationMoldDisconnect(List<MoldDisconnect> list, User userTarget) {
        if(true)
            return;
        taskExecutor.execute(() -> {

            try {
                if (list == null || list.isEmpty()) return;

                BroadcastNotification b = new BroadcastNotification();
                b.setSystemFunction(PageType.DISCONNECTION_ALERT);
                b.setUserTarget(userTarget);
                b.setNotificationType(Const.NOTIFICATION_TYPE.ALERT);
                b.setInfors(list.stream().map(m -> String.valueOf(m.getMoldId())).collect(Collectors.toList()));

                String message = "";
                List<String> valueList = new ArrayList<>();
                valueList.add("disconnected");

                List<String> codeList = list.stream().map(m -> m.getMold().getEquipmentCode()).collect(Collectors.toList());

                if (list.size() == 1) {
                    String date = list.get(0).getCreatedAt() != null ? DateUtils.getDateTime(list.get(0).getCreatedAt()) : "";
                    message = "Tooling " + codeList.get(0) + " was %s at " + date;
                } else if (list.size() == 2) {
                    message = "Tooling " + codeList.get(0) + " and tooling " + codeList.get(1) + " were %s";
                } else if (list.size() > 2) {
                    message = "Tooling " + codeList.get(0) + " and " + (codeList.size() - 1) + " others were %s";
                }
                message += ".";
                b.setMessage(message);
                b.setValueList(valueList);
                saveNotification(b);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void notificationTerminalDisconnect(List<TerminalDisconnect> list, User userTarget) {
        taskExecutor.execute(() -> {
            try {
                if (list == null || list.isEmpty()) return;

                BroadcastNotification b = new BroadcastNotification();
                b.setSystemFunction(PageType.DISCONNECTION_ALERT);
                b.setUserTarget(userTarget);
                b.setNotificationType(Const.NOTIFICATION_TYPE.ALERT);
                b.setInfors(list.stream().map(m -> String.valueOf(m.getTerminalId())).collect(Collectors.toList()));

                String message = "";
                List<String> valueList = new ArrayList<>();
                valueList.add("disconnected");

                List<String> codeList = list.stream().map(m -> m.getTerminal().getEquipmentCode()).collect(Collectors.toList());

                if (list.size() == 1) {
                    String date = list.get(0).getCreatedAt() != null ? DateUtils.getDateTime(list.get(0).getCreatedAt()) : "";
                    message = "Terminal " + codeList.get(0) + " was %s at " + date;
                } else if (list.size() == 2) {
                    message = "Terminal " + codeList.get(0) + " and terminal " + codeList.get(1) + " were %s";
                } else if (list.size() > 2) {
                    message = "Terminal " + codeList.get(0) + " and " + (codeList.size() - 1) + " others were %s";
                }
                message += ".";
                b.setMessage(message);
                b.setValueList(valueList);
                saveNotification(b);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    //cycle time
    public void notificationMoldCycleTime(List<MoldCycleTime> list, User userTarget) {
        if (list == null) return;
        taskExecutor.execute(() -> {
            List<MoldCycleTime> l1 = list.stream().filter(m -> CycleTimeStatus.OUTSIDE_L1.equals(m.getCycleTimeStatus())).collect(Collectors.toList());
            List<MoldCycleTime> l2 = list.stream().filter(m -> CycleTimeStatus.OUTSIDE_L2.equals(m.getCycleTimeStatus())).collect(Collectors.toList());
            notificationMoldCycleTimeByStatus(l1, userTarget);
            notificationMoldCycleTimeByStatus(l2, userTarget);
        });

    }

    public void notificationMoldCycleTimeByStatus(List<MoldCycleTime> list, User userTarget) {
        try {
            if (list == null || list.isEmpty()) return;

            BroadcastNotification b = new BroadcastNotification();
            b.setSystemFunction(PageType.CYCLE_TIME_ALERT);
            b.setUserTarget(userTarget);
            b.setNotificationType(Const.NOTIFICATION_TYPE.ALERT);
            b.setInfors(list.stream().map(m -> String.valueOf(m.getMoldId())).collect(Collectors.toList()));

//        ResourceHandler resourceHandler = ContextWrapper.getContext().getBean("resourceHandler", ResourceHandler.class);

            String message = "";
            String l1 = "outside limit 1 (L1)";
            String l2 = "outside limit 2 (L2)";
            List<String> valueList = new ArrayList<>();
            if (CycleTimeStatus.OUTSIDE_L2.equals(list.get(0).getCycleTimeStatus()))
                valueList.add(l2);
            else
                valueList.add(l1);

            List<String> codeList = list.stream().map(m -> m.getMold().getEquipmentCode()).collect(Collectors.toList());
            if (list.size() == 1) {
                message = "Tooling " + codeList.get(0) + " has reached the %s";
            } else if (list.size() == 2) {
                message = "Tooling " + codeList.get(0) + " and tooling " + codeList.get(1) + " have reached the %s";
            } else if (list.size() > 2) {
                message = "Tooling " + codeList.get(0) + " and " + (codeList.size() - 1) + " have reached the %s";
            }
            message += ".";
            b.setMessage(message);
            b.setValueList(valueList);
            saveNotification(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //uptime
    public void notificationMoldEfficiency(List<MoldEfficiency> list, User userTarget) {

        if (list == null) return;
        taskExecutor.execute(() -> {
            List<MoldEfficiency> l1 = list.stream().filter(m -> EfficiencyStatus.OUTSIDE_L1.equals(m.getEfficiencyStatus())).collect(Collectors.toList());
            List<MoldEfficiency> l2 = list.stream().filter(m -> EfficiencyStatus.OUTSIDE_L2.equals(m.getEfficiencyStatus())).collect(Collectors.toList());
            notificationMoldEfficiencyByStatus(l1, userTarget);
            notificationMoldEfficiencyByStatus(l2, userTarget);
        });

    }

    public void notificationMoldEfficiencyByStatus(List<MoldEfficiency> list, User userTarget) {
        try {
            if (list == null || list.isEmpty()) return;

            BroadcastNotification b = new BroadcastNotification();
            b.setSystemFunction(PageType.EFFICIENCY_ALERT);
            b.setUserTarget(userTarget);
            b.setNotificationType(Const.NOTIFICATION_TYPE.ALERT);
            b.setInfors(list.stream().map(m -> String.valueOf(m.getMoldId())).collect(Collectors.toList()));

//        ResourceHandler resourceHandler = ContextWrapper.getContext().getBean("resourceHandler", ResourceHandler.class);

            String message = "";
            String l1 = "outside limit 1 (L1)";
            String l2 = "outside limit 2 (L2)";
            List<String> valueList = new ArrayList<>();
            if (EfficiencyStatus.OUTSIDE_L2.equals(list.get(0).getEfficiencyStatus()))
                valueList.add(l2);
            else
                valueList.add(l1);

            List<String> codeList = list.stream().map(m -> m.getMold().getEquipmentCode()).collect(Collectors.toList());
            if (list.size() == 1) {
                message = "Tooling " + codeList.get(0) + " has reached the %s";
            } else if (list.size() == 2) {
                message = "Tooling " + codeList.get(0) + " and tooling " + codeList.get(1) + " have reached the %s";
            } else if (list.size() > 2) {
                message = "Tooling " + codeList.get(0) + " and " + (codeList.size() - 1) + " have reached the %s";
            }
            message += ".";
            b.setMessage(message);
            b.setValueList(valueList);
            saveNotification(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // refurbisment
    public void notificationMoldRefurbishment(List<MoldRefurbishment> list, User userTarget) {
        taskExecutor.execute(() -> {
            try {
                if (list == null || list.isEmpty()) return;

                BroadcastNotification b = new BroadcastNotification();
                b.setSystemFunction(PageType.REFURBISHMENT_ALERT);
                b.setUserTarget(userTarget);
                b.setNotificationType(Const.NOTIFICATION_TYPE.ALERT);
                b.setInfors(list.stream().map(m -> String.valueOf(m.getMoldId())).collect(Collectors.toList()));

                String message = "";
                List<String> valueList = new ArrayList<>();
                valueList.add("end of life cycle");

                List<String> codeList = list.stream().map(m -> m.getMold().getEquipmentCode()).collect(Collectors.toList());

                if (list.size() == 1) {
                    message = "Tooling " + codeList.get(0) + " is reaching the %s";
                } else if (list.size() == 2) {
                    message = "Tooling " + codeList.get(0) + " and tooling " + codeList.get(1) + " are reaching the %s";
                } else if (list.size() > 2) {
                    message = "Tooling " + codeList.get(0) + " and " + (codeList.size() - 1) + " others are reaching the %s";
                }
                message += ".";
                b.setMessage(message);
                b.setValueList(valueList);
                saveNotification(b);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    //TODO: send notification to app
    public void notificationMoldDowntimeEvent(List<MoldDowntimeEvent> list, User userTarget) {

    }

    public void notificationMoldMisconfigure(List<MoldMisconfigure> list, User userTarget) {
        taskExecutor.execute(() -> {
            try {
                if (list == null || list.isEmpty()) return;

                BroadcastNotification b = new BroadcastNotification();
                b.setSystemFunction(PageType.RESET_ALERT);
                b.setUserTarget(userTarget);
                b.setNotificationType(Const.NOTIFICATION_TYPE.ALERT);
                b.setInfors(list.stream().map(m -> String.valueOf(m.getMold()!=null?m.getMold().getId():"")).collect(Collectors.toList()));

                String message = "";
                List<String> valueList = new ArrayList<>();
                valueList.add("reset request");

                List<String> codeList = list.stream().map(m -> m.getMold().getEquipmentCode()).collect(Collectors.toList());

                if (list.size() == 1) {
                    message = "Tooling " + codeList.get(0) + " has a pending %s";
                } else if (list.size() == 2) {
                    message = "Tooling " + codeList.get(0) + " and tooling " + codeList.get(1) + " have pending %s";
                } else if (list.size() > 2) {
                    message = "Tooling " + codeList.get(0) + " and " + (codeList.size() - 1) + " others have pending %s";
                }
                message += ".";
                b.setMessage(message);
                b.setValueList(valueList);
                saveNotification(b);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void notificationMoldDetachment(List<MoldDetachment> list, User userTarget) {
        taskExecutor.execute(() -> {
            try {
                if (list == null || list.isEmpty()) return;


                BroadcastNotification b = new BroadcastNotification();
                b.setSystemFunction(PageType.DETACHMENT_ALERT);
                b.setUserTarget(userTarget);
                b.setNotificationType(Const.NOTIFICATION_TYPE.ALERT);
                b.setInfors(list.stream().map(m -> String.valueOf(m.getMoldId())).collect(Collectors.toList()));

                String message = "";
                List<String> valueList = new ArrayList<>();
                valueList.add("detached");
                List<String> codeCounterList = list.stream()
                        .map(m -> m.getMold().getCounterCode()).collect(Collectors.toList());
                List<String> codeList = list.stream().map(m -> m.getMold().getEquipmentCode()).collect(Collectors.toList());

                if (list.size() == 1) {
                    message = "Counter " + codeCounterList.get(0) + " has been %s from tooling " + codeList.get(0);
                } else if (list.size() == 2) {
                    message = "Counter " + codeCounterList.get(0) + " and counter " + codeList.get(1) + " have been %s from its tooling";
                } else if (list.size() > 2) {
                    message = "Counter " + codeCounterList.get(0) + " and " + (codeList.size() - 1) + " others have been %s from its tooling";
                }
                message += ".";
                b.setMessage(message);
                b.setValueList(valueList);
                saveNotification(b);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


}
