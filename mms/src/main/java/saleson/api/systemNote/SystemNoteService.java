package saleson.api.systemNote;

import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import saleson.api.counter.CounterRepository;
import saleson.api.mold.MoldRepository;
import saleson.api.notification.NotificationRepository;
import saleson.api.notification.NotificationService;
import saleson.api.part.PartRepository;
import saleson.api.systemNote.payload.SystemNotePayload;
import saleson.api.terminal.TerminalRepository;
import saleson.api.user.UserService;
import saleson.common.constant.CommonMessage;
import saleson.common.enumeration.ObjectType;
import saleson.common.payload.ApiResponse;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.dto.SystemNoteParam;
import saleson.model.SystemNote;
import saleson.model.SystemNoteRead;
import saleson.model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SystemNoteService {
    @Autowired
    SystemNoteRepository systemNoteRepository;
    @Autowired
    UserService userService;

    @Autowired
    PartRepository partRepository;

    @Autowired
    MoldRepository moldRepository;

    @Autowired
    TerminalRepository terminalRepository;

    @Autowired
    CounterRepository counterRepository;
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    NotificationService notificationService;
    @Autowired
    SystemNoteReadRepository systemNoteReadRepository;

    public Page<SystemNote> findAll(Predicate predicate, Pageable pageable) {
        Page<SystemNote> page = systemNoteRepository.findAll(predicate, pageable);
        checkUnreadStatus(page.getContent(), SecurityUtils.getUserId());
        return page;
    }

    public Long countTotalUnread(Predicate predicate) {
        return systemNoteRepository.count(predicate);
    }

    public void checkUnreadStatus(List<SystemNote> systemNotes, Long userId) {
        if (systemNotes != null && userId != null)
            systemNotes.forEach(systemNote -> {
                boolean read = systemNoteRepository.isReadSystemNote(userId, systemNote.getId());
                systemNote.setRead(read);
                checkUnreadStatus(systemNote.getReplies(), userId);
            });
    }


    @Transactional
    public SystemNote save(SystemNote systemNote, SystemNotePayload payload) {
        if (payload == null) {
            systemNoteRepository.save(systemNote);
            return systemNote;
        }
        if (systemNote.getCreator() == null) {
            User creator = userService.findById(SecurityUtils.getUserId());
            systemNote.setCreator(creator);
        }
        systemNoteRepository.save(systemNote);
        notificationService.createNoteNotification(systemNote, null);
        updateRead(Arrays.asList(systemNote.getId()));
        return systemNote;
    }

    public ApiResponse batchSave(SystemNotePayload payload) {
        payload.getListModels().forEach(note -> save(note, payload));
        return ApiResponse.success(CommonMessage.OK);
    }


    public SystemNote deleteSystemNote(SystemNote systemNote) {
        if (systemNote != null) {
            systemNote.setDeleted(true);
            systemNoteRepository.save(systemNote);
        }
        return systemNote;
    }
    public SystemNote restoreSystemNote(SystemNote systemNote) {
        if (systemNote != null) {
            systemNote.setDeleted(false);
            systemNoteRepository.save(systemNote);
        }
        return systemNote;
    }
    private void changeStatusNotification(SystemNote systemNote){
//        List<Notification> notificationList = notificationRepository.find
    }

    public List<SystemNoteParam> searchObjectByCode(String code) {
        List<SystemNoteParam> noteParamList = new ArrayList<>();
        if (StringUtils.isEmpty(code)) return noteParamList;

        noteParamList.addAll(partRepository.findAllByPartCodeUpperCase(code.toUpperCase()).stream().map(o -> {
            o.setObjectType(ObjectType.PART);
            return o;
        }).collect(Collectors.toList()));

        noteParamList.addAll(moldRepository.findAllByEquipmentCodeUpperCase(code.toUpperCase()).stream().map(o -> {
            o.setObjectType(ObjectType.TOOLING);
            return o;
        }).collect(Collectors.toList()));

        noteParamList.addAll(terminalRepository.findAllByEquipmentCodeUpperCase(code.toUpperCase()).stream().map(o -> {
            o.setObjectType(ObjectType.TERMINAL);
            return o;
        }).collect(Collectors.toList()));

        noteParamList.addAll(counterRepository.findAllByEquipmentCodeUpperCase(code.toUpperCase()).stream().map(o -> {
            o.setObjectType(ObjectType.COUNTER);
            return o;
        }).collect(Collectors.toList()));


        return noteParamList;
    }

    public List<SystemNoteRead>  updateRead(List<Long> ids){
        if(ids ==null) return new ArrayList<>();
        List<SystemNote> systemNotes = systemNoteRepository.findAllUnread(SecurityUtils.getUserId(),ids);
        List<SystemNoteRead> systemNoteReads =  new ArrayList<>();
        systemNotes.forEach(systemNote -> systemNoteReads.add(new SystemNoteRead(SecurityUtils.getUserId(), systemNote.getId())));
        systemNoteReadRepository.saveAll(systemNoteReads);
        return systemNoteReads;
    }
    public void create(SystemNotePayload payload) {
        save(payload.getModel(), payload);
    }

}
