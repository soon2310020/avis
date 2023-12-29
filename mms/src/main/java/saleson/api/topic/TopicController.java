package saleson.api.topic;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import saleson.api.filestorage.payload.MultiFileStoragePayload;
import saleson.api.topic.payload.CorrespondencePayload;
import saleson.api.topic.payload.TopicPayload;
import saleson.api.user.UserService;
import saleson.common.config.Const;
import saleson.common.domain.MultipartFormData;
import saleson.common.enumeration.CustomerSupportStatus;
import saleson.common.enumeration.RoleUserTopic;
import saleson.common.enumeration.PageType;
import saleson.common.enumeration.TopicType;
import saleson.common.payload.ApiResponse;
import saleson.common.service.FileStorageService;
import saleson.common.util.SecurityUtils;
import saleson.dto.Item;
import saleson.dto.RestData;
import saleson.model.Correspondence;
import saleson.model.FileStorage;
import saleson.model.Topic;
import saleson.model.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/topics")
public class TopicController {

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    TopicService topicService;
    @Autowired
    UserService userService;
    @Autowired
    FileStorageService fileStorageService;
    @Autowired
    CorrespondenceRepository correspondenceRepository;

    @GetMapping
    public ResponseEntity<Page<Topic>> topics(TopicPayload payload,
                                              Pageable pageable) {
        payload.setTopicType(TopicType.HELP_CENTER);
        Page<Topic> pageContent = topicService.findAll(payload.getPredicate(), pageable);

        return new ResponseEntity<>(pageContent, HttpStatus.OK);
    }

    @GetMapping("/totalNewRecode")
    public Long totalNewRecode() {
        TopicPayload payload = new TopicPayload();
        payload.setTopicType(TopicType.HELP_CENTER);
        payload.setTopicStatus(CustomerSupportStatus.NEW);
        Page<Topic> pageContent = topicService.findAll(payload.getPredicate(), PageRequest.of(0, 1));
        return pageContent.getTotalElements();

    }

    @PostMapping("/create-topic")
    public ApiResponse create(MultipartFormData formData) {
        try {
            TopicPayload payload = objectMapper.readValue(formData.getPayload(), TopicPayload.class);
            payload.setFiles(formData.getFiles());
            payload.setTopicType(TopicType.HELP_CENTER);
            Topic topic = topicService.save(payload.getModel(), payload);

            //create correspondence
            CorrespondencePayload correspondencePayload = new CorrespondencePayload();
            correspondencePayload.setMessage(payload.getMessage());
            correspondencePayload.setTopicId(topic.getId());
            correspondencePayload.setFiles(payload.getFiles());
            correspondencePayload.setUserId(SecurityUtils.getUserId());
            Correspondence correspondenceNew = correspondencePayload.getModel();
            correspondenceNew.setIsQuestion(true);
            Correspondence correspondence = topicService.saveCorrespondence(correspondenceNew, correspondencePayload);

            topicService.sendMail(topic, correspondence);

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse(false, "Fail!");
        }
        return new ApiResponse(true, "OK!!");
    }

    // On hold
    @PostMapping("/update")
    public ApiResponse update(@RequestBody TopicPayload payload) {

//        Role role = payload.getModel();

        try {
//            role = roleService.save(role);
//            roleGraphService.save(role.getId(), payload.getGraphTypes());
        } catch (Exception e) {
            return new ApiResponse(false, "Fail!");
        }
        return new ApiResponse(true, "OK!!");
    }

    @PostMapping("/{id}/resolve")
    public ApiResponse resolve(@PathVariable(value = "id", required = true) Long id) {

//        Role role = payload.getModel();

        try {
            Topic topic = topicService.findById(id);
            topic.setStatus(CustomerSupportStatus.RESOLVED);
            topicService.save(topic, null);
//            role = roleService.save(role);
//            roleGraphService.save(role.getId(), payload.getGraphTypes());
        } catch (Exception e) {
            return new ApiResponse(false, "Fail!");
        }
        return new ApiResponse(true, "OK!!");
    }

    @PostMapping("/{id}/unresolve")
    public ApiResponse unresolve(@PathVariable(value = "id", required = true) Long id) {

//        Role role = payload.getModel();

        try {
            Topic topic = topicService.findById(id);
            topic.setStatus(CustomerSupportStatus.NEW);
            if (topic.getNumberOfReply() > 0) {
                Long numAns = correspondenceRepository.countByTopicIdAndIsQuestion(id, false);
                if (numAns > 0)
                    topic.setStatus(CustomerSupportStatus.IN_PROGRESS);
            }

            topicService.save(topic, null);
//            role = roleService.save(role);
//            roleGraphService.save(role.getId(), payload.getGraphTypes());
        } catch (Exception e) {
            return new ApiResponse(false, "Fail!");
        }
        return new ApiResponse(true, "OK!!");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Topic> topic(@PathVariable(value = "id") Long id) {
        Topic topic = topicService.findById(id);

        return new ResponseEntity<>(topic, HttpStatus.OK);
    }

    // On hold
    @DeleteMapping("{id}")
    public ApiResponse deleteTopic(@PathVariable("id") Long id) {
//        userService.deleteById(id);
        return new ApiResponse(true, "성공.");
    }

    @GetMapping("/{topicId}/correspondences")
    public ResponseEntity<Page<Correspondence>> correspondences(
            @PathVariable(value = "topicId") Long topicId,
            Pageable pageable) {
//        CorrespondencePayload payload = new CorrespondencePayload();
//        payload.setTopicId(topicId);
//        Page<Correspondence> pageContent = topicService.findAllCorrespondence(payload.getPredicate(), pageable);
        Page<Correspondence> pageContent = topicService.findAllCorrespondenceByTopic(topicId, PageRequest.of(0,Integer.MAX_VALUE,pageable.getSort()));
        //add info file
        for (Correspondence correspondence : pageContent.getContent()) {
            MultiFileStoragePayload payload = new MultiFileStoragePayload();
            payload.setRefId(correspondence.getId());
            Iterable<FileStorage> files = fileStorageService.findAll(payload.getPredicate());
            correspondence.setFiles(files);
        }

        return new ResponseEntity<>(pageContent, HttpStatus.OK);
    }

    // Generate correspondence
    @PostMapping("/create-correspondence")
    public ApiResponse createCorrespondence(MultipartFormData formData) {
//        Role role = payload.getModel();

        try {
            CorrespondencePayload payload = objectMapper.readValue(formData.getPayload(), CorrespondencePayload.class);
            payload.setFiles(formData.getFiles());
            Correspondence correspondence = topicService.saveCorrespondence(payload.getModel(), payload);

            //topic
            topicService.updateTopicWhenCreateCorrespondence(null, correspondence);

//            role = roleService.save(role);
//            roleGraphService.save(role.getId(), payload.getGraphTypes());
            topicService.sendMail(null, correspondence);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse(false, "Fail!");
        }
        return new ApiResponse(true, "OK!!");
    }

    // On hold
    @PostMapping("/update-correspondence")
    public ApiResponse updateCorrespondence(@RequestBody CorrespondencePayload payload) {
//        Role role = payload.getModel();

        try {
//            role = roleService.save(role);
//            roleGraphService.save(role.getId(), payload.getGraphTypes());
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse(false, "Fail!");
        }
        return new ApiResponse(true, "OK!!");
    }

    // On hold
    @DeleteMapping("/correspondence/{id}")
    public ApiResponse deleteCorrespondence(@PathVariable("id") Long id) {
//        userService.deleteById(id);
        return new ApiResponse(true, "성공.");
    }

    // TODO: return type of user (owner - host - normal)
    @GetMapping("/{topicId}/role-user-topic")
    public String roleUserTopic(@PathVariable(value = "topicId") Long topicId) {
        String role = RoleUserTopic.NORMAL.getCode();
        Long userId = SecurityUtils.getUserId();
        User user = userService.findById(userId);
        Topic topic = topicService.findById(topicId);
        if (topic != null && topic.getCreator() != null && topic.getCreator().getId().equals(userId)) {
            role = RoleUserTopic.OWNER.getCode();
        }
        if (topicService.checkEmoldinoAccount(user)) {
            role = role == RoleUserTopic.OWNER.getCode() ?  RoleUserTopic.HOST_OWNER.getCode() : RoleUserTopic.HOST.getCode();
        }
        return role;
//        if(user!=null && user.getEmail()!=null && user.getEmail().toLowerCase().endsWith("emoldino.com")){
//            return true;
//        }
//        return false;
    }

    @GetMapping("/correspondence/{id}")
    public ResponseEntity<Correspondence> getCorrespondence(@PathVariable("id") Long id) {
        Correspondence correspondence = correspondenceRepository.findById(id).orElse(null);
        if (correspondence != null) {
            MultiFileStoragePayload multiFileStoragePayload = new MultiFileStoragePayload();
            multiFileStoragePayload.setRefId(correspondence.getId());
            Iterable<FileStorage> files = fileStorageService.findAll(multiFileStoragePayload.getPredicate());
            correspondence.setFiles(files);
        }
        return new ResponseEntity<>(correspondence, HttpStatus.OK);
    }

    @GetMapping("/tags-topic-of-user")
    public ResponseEntity tagsTopicOfUser() {
        List<Item> resList = topicService.tagsTopicOfUser();

        return ResponseEntity.ok(new RestData(resList.size(), resList));
    }

    @GetMapping("/tags-item-of-user")
    public ResponseEntity tagsItemOfUser() {
        List<Item> resList = topicService.tagsItemOfUser();
        return ResponseEntity.ok(new RestData(resList.size(), resList));
    }


}
