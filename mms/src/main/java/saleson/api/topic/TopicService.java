package saleson.api.topic;

import com.querydsl.core.types.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import saleson.api.menu.MenuService;
import saleson.api.notification.NotificationService;
import saleson.api.role.RoleService;
import saleson.api.topic.payload.CorrespondencePayload;
import saleson.api.topic.payload.TopicPayload;
import saleson.api.user.UserService;
import saleson.common.config.Const;
import saleson.common.enumeration.*;
import saleson.common.notification.MailService;
import saleson.common.service.FileInfo;
import saleson.common.service.FileStorageService;
import saleson.common.util.DateUtils;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.dto.Item;
import saleson.model.*;
import saleson.service.mail.SupportAnsClientMailService;
import saleson.service.mail.SupportQuestionClientMailService;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TopicService {
    @Autowired
    TopicRepository topicRepository;
    @Autowired
    UserService userService;
    @Autowired
    CorrespondenceRepository correspondenceRepository;
    @Autowired
    FileStorageService fileStorageService;

    @Autowired
    SupportAnsClientMailService supportAnsClientMailService;
    @Autowired
    SupportQuestionClientMailService supportQuestionClientMailService;

    @Autowired
    MailService mailService;
    @Autowired
    RoleService roleService;

    @Value("${customer.server.name.initial}")
    String serverNameInitial;
    @Value("${mail.support}")
    String mailSupport;

    @Value("${mail.emoldino.account}")
    String mailEmoldinoAccount;

    @Value("${host.url}")
    String host;

    @Autowired
    NotificationService notificationService;
    @Autowired
    private MenuService menuService;

    public Page<Topic> findAll(Predicate predicate, Pageable pageable) {
        return topicRepository.findAll(predicate, pageable);
    }

    public Topic findById(Long id){
        return topicRepository.findById(id).orElse(null);
    }

    public Page<Correspondence> findAllCorrespondence(Predicate predicate, Pageable pageable){
        return correspondenceRepository.findAll(predicate, pageable);
    }
    public Page<Correspondence> findAllCorrespondenceByTopic(Long topicId,
                                                             Pageable pageable){
        CorrespondencePayload payload = new CorrespondencePayload();
        payload.setTopicId(topicId);
        Page<Correspondence> pageContent = findAllCorrespondence(payload.getPredicate(), pageable);
        return pageContent;
    }

    @Transactional
    public Topic save(Topic topic, TopicPayload payload) {
        if (payload == null) {
            topicRepository.save(topic);
            return topic;
        }
        if (topic.getTopicId() == null) {
            String topicId = serverNameInitial;
            String date = DateUtils.getDate(Instant.now(), "yyyyMMdd");
            topicId += date.substring(2) + "-";
            //find last topic id
            Long sequence = 1l;
            Topic lastTopic = topicRepository.findFirstByTopicIdStartingWithOrderByTopicIdDesc(topicId).orElse(null);
            if(lastTopic!=null){
                try {
                    String lastSeq= lastTopic.getTopicId().replace(topicId,"");
                    sequence = Long.valueOf(lastSeq) + 1;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            topicId += sequence;
            topic.setTopicId(topicId);
        }
        if (topic.getStatus() == null) topic.setStatus(CustomerSupportStatus.NEW);
        if (topic.getCreator() == null) {
            User creator = userService.findById(SecurityUtils.getUserId());
            topic.setCreator(creator);
        }
        if (topic.getNumberOfReply() == null) topic.setNumberOfReply(0);
        if (topic.getLastReplyAt() == null) topic.setLastReplyAt(Instant.now());
        if(!StringUtils.isEmpty(payload.getRecipientId())){
            User recipient = userService.findById(payload.getRecipientId());
            topic.setRecipient(recipient);
        }
        topicRepository.save(topic);
        notificationService.createListNotification(topic);
        return topic;
    }


    @Transactional
    public Correspondence saveCorrespondence(Correspondence correspondence, CorrespondencePayload payload) {
        if (payload == null) {
            correspondenceRepository.save(correspondence);
            return correspondence;
        }
        User creator = userService.findById(SecurityUtils.getUserId());
        correspondence.setUser(creator);
        if(correspondence.getIsQuestion() == null) {
            Topic topic = topicRepository.findById(correspondence.getTopicId()).orElse(null);
            if (checkEmoldinoAccount(creator) && (topic == null || !topic.getCreator().getId().equals(creator.getId())))
                correspondence.setIsQuestion(false);
            else
                correspondence.setIsQuestion(true);
            //update status topic
//            if(!correspondence.getIsQuestion()){
//                if(CustomerSupportStatus.NEW.equals(topic.getStatus())){
//                    topic.setStatus(CustomerSupportStatus.IN_PROGRESS);
//                }
//            }
        }
        correspondenceRepository.save(correspondence);

        if (payload.getFiles() != null && payload.getFiles().length > 0) {
            fileStorageService.save(new FileInfo(StorageType.CORRESPONDENCE, correspondence.getId(), payload.getFiles()));
        }
        return correspondence;
    }
    @Transactional
    public void updateTopicWhenCreateCorrespondence(Topic topic,Correspondence correspondence){
        if(topic == null )
            topic = topicRepository.findById(correspondence.getTopicId()).orElse(null);
        if(!correspondence.getIsQuestion()){
            if(CustomerSupportStatus.NEW.equals(topic.getStatus())){
                topic.setStatus(CustomerSupportStatus.IN_PROGRESS);
            }
        }
        if(topic.getStatus().equals(CustomerSupportStatus.RESOLVED)){
            topic.setStatus(CustomerSupportStatus.IN_PROGRESS);
        }
        if (topic.getNumberOfReply() == null) topic.setNumberOfReply(0);
        else {
            topic.setLastReplyAt(Instant.now());
            topic.setLastReplyBy(correspondence.getUser());
            topic.setNumberOfReply(topic.getNumberOfReply() + 1);
            topic.setLastReplyIsQuestion(correspondence.getIsQuestion());
        }
        topicRepository.save(topic);

    }

    public boolean checkEmoldinoAccount(User user) {
        if (user != null && user.getEmail() != null && user.getEmail().toLowerCase().endsWith(mailEmoldinoAccount)) {
            return true;
        }
        return false;
    }

    public String getTitle(boolean isAns, Topic topic) {
        String title = "[eMoldino Support Team] This is the answer to your question";
        if (!isAns) {
            title = "[" + topic.getTopicId() + "] " + topic.getSubject();
        }
        return title;
    }

    // TODO: to fix with new definition
    public void sendMail(Topic topic,Correspondence correspondence) {
        try {
//            Correspondence correspondence = correspondenceRepository.findById(correspondenceId).orElse(null);
            if (topic == null)
                topic = topicRepository.findById(correspondence.getTopicId()).orElse(null);

            boolean isAnswer = !correspondence.getIsQuestion();
            List<String> receivers = new ArrayList<>();

            Correspondence lastQuestion = null;
            if (isAnswer) {
                CorrespondencePayload payload = new CorrespondencePayload();
                payload.setTopicId(correspondence.getTopicId());
                payload.setIsQuestion(true);
                Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt"));
                Page<Correspondence> pageContent = findAllCorrespondence(payload.getPredicate(), pageable);
                List<Correspondence> correspondenceList = pageContent.getContent();
                if (correspondenceList.size() > 0) {
                    lastQuestion = correspondenceList.get(0);
                }
            }

            String title = getTitle(isAnswer, topic);
            String content;
            String linkTopic =  host+"/support/customer-support/detail/"+topic.getId();
            if (isAnswer) {
                if(lastQuestion!=null){
                    receivers.add(lastQuestion.getUser().getEmail());
                }
                content = supportAnsClientMailService.generateMailContent(new Object[]{
                        correspondence.getUser().getName(),
                        correspondence.getUser().getCompany().getName(),
                        lastQuestion != null ? lastQuestion.getMessage() : "",
                        correspondence.getMessage(),
                        linkTopic
                });
            } else {
                receivers.add(topic.getRecipient().getEmail());
                content = supportQuestionClientMailService.generateMailContent(new Object[]{
                        correspondence.getUser().getName(),
                        correspondence.getUser().getCompany().getName(),
                        correspondence.getMessage(),
                        linkTopic
                });
            }
            mailService.sendMailByContent(receivers, title, content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public List<Item> tagsTopicOfUser(){
        List<Item> resList = new ArrayList<>();
        User user = userService.findById(SecurityUtils.getUserId());
        if (user != null && user.getRoles() != null) {
            Set<Role> roleSet = user.getRoles();
/*
            List<Role> roles = roleService.myRoles();
            if (roles != null) {
                roleSet.addAll(roles);
            }
*/

            Set<PageType> systemNoteFunctionSet = new HashSet<>();
/*
            if(roleSet!=null)
            roleSet.stream().forEach(r -> {
                if (r.getMenus() != null)
                    r.getMenus().stream().forEach(m -> {
                        if (m.getMenuKey() != null) {
                            if (Const.menuSystemNoteMap.get(m.getMenuKey()) != null) {
                                systemNoteFunctionSet.add(Const.menuSystemNoteMap.get(m.getMenuKey()));
                            }
                        }
                        //level 2
                        if (m.getChildren() != null && !m.getChildren().isEmpty()) {
                            m.getChildren().stream().forEach(m2 -> {
                                if (m2.getMenuKey() != null) {
                                    if (Const.menuSystemNoteMap.get(m2.getMenuKey()) != null) {
                                        systemNoteFunctionSet.add(Const.menuSystemNoteMap.get(m2.getMenuKey()));
                                    }
                                }
                                //level 3
                                if (m2.getChildren() != null && !m2.getChildren().isEmpty()) {
                                    m2.getChildren().stream().forEach(m3 -> {
                                        if (m3.getMenuKey() != null) {
                                            if (Const.menuSystemNoteMap.get(m3.getMenuKey()) != null) {
                                                systemNoteFunctionSet.add(Const.menuSystemNoteMap.get(m3.getMenuKey()));
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    });
            });
*/
            //by menu
            List<Long> menuIds = new ArrayList<>();
            if(roleSet!=null)
                menuIds = roleSet.stream().flatMap(role -> role.getMenus().stream())
                    .map(m -> m.getId()).distinct().collect(Collectors.toList());

            List<Menu> allMenus = menuService.findMenusByParentId(0L);
            for (Menu m : allMenus) {
                if (!SecurityUtils.isAdmin() && 3000L != m.getId() && 6000L != m.getId()) {        // admin은 모든 메뉴 접근 가능.

                    // 3. 기본 메뉴에서 menu id 가 일치하지 않는 메뉴는 remove()
                    // 		-> admin page && ROLE_ADMIN 이면 권한체크를 하지 않음.
                    menuService.processAccessableMenu(m, menuIds);
                }
            }
            allMenus.stream().forEach(m -> {
                if (m.getMenuKey() != null) {
                    if (Const.menuSystemNoteMap.get(m.getMenuKey()) != null) {
                        systemNoteFunctionSet.add(Const.menuSystemNoteMap.get(m.getMenuKey()));
                    }
                }
                //level 2
                if (m.getChildren() != null && !m.getChildren().isEmpty()) {
                    m.getChildren().stream().forEach(m2 -> {
                        if (m2.getMenuKey() != null) {
                            if (Const.menuSystemNoteMap.get(m2.getMenuKey()) != null) {
                                systemNoteFunctionSet.add(Const.menuSystemNoteMap.get(m2.getMenuKey()));
                            }
                        }
                        //level 3
                        if (m2.getChildren() != null && !m2.getChildren().isEmpty()) {
                            m2.getChildren().stream().forEach(m3 -> {
                                if (m3.getMenuKey() != null) {
                                    if (Const.menuSystemNoteMap.get(m3.getMenuKey()) != null) {
                                        systemNoteFunctionSet.add(Const.menuSystemNoteMap.get(m3.getMenuKey()));
                                    }
                                }
                            });
                        }
                    });
                }
            });


            if(SecurityUtils.isAdmin()){
                systemNoteFunctionSet.addAll(Const.menuSystemNoteMap.values());
            }
            systemNoteFunctionSet.stream().sorted().forEach(s -> {
                String menuKey = Const.menuSystemNoteMap.entrySet().stream().filter(map -> s.equals(map.getValue())).map(m -> m.getKey()).findFirst().orElse(null);
                resList.add(new Item(s.getCode(), s.getTitle(), menuKey));
            });
        }
        return resList.stream().sorted(Comparator.comparing(Item::getName)).collect(Collectors.toList());
    }

    public List<Item> tagsItemOfUser(){
        List<Item> resList = new ArrayList<>();
        User user = userService.findById(SecurityUtils.getUserId());
        if (user != null && user.getRoles() != null) {
            Set<ObjectType> objectTypeSet = new HashSet<>();
            Set<Role> roleSet = user.getRoles();
/*

            user.getRoles().stream().forEach(r -> {
                if (r.getMenus() != null)
                    r.getMenus().stream().forEach(m -> {
                        if (m.getMenuKey() != null) {
                            if (Const.menuObjectTypeMap.get(m.getMenuKey()) != null) {
                                objectTypeSet.add(Const.menuObjectTypeMap.get(m.getMenuKey()));
                            }
                        }
                        //level 2
                        if (m.getChildren() != null && !m.getChildren().isEmpty()) {
                            m.getChildren().stream().forEach(m2 -> {
                                if (m2.getMenuKey() != null) {
                                    if (Const.menuObjectTypeMap.get(m2.getMenuKey()) != null) {
                                        objectTypeSet.add(Const.menuObjectTypeMap.get(m2.getMenuKey()));
                                    }
                                }
                                //level 3
                                if (m2.getChildren() != null && !m2.getChildren().isEmpty()) {
                                    m2.getChildren().stream().forEach(m3 -> {
                                        if (m3.getMenuKey() != null) {
                                            if (Const.menuObjectTypeMap.get(m3.getMenuKey()) != null) {
                                                objectTypeSet.add(Const.menuObjectTypeMap.get(m3.getMenuKey()));
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    });
            });
*/

            //by menu
            List<Long> menuIds = new ArrayList<>();
            if(roleSet!=null)
                menuIds = roleSet.stream().flatMap(role -> role.getMenus().stream())
                        .map(m -> m.getId()).distinct().collect(Collectors.toList());

            List<Menu> allMenus = menuService.findMenusByParentId(0L);
            for (Menu m : allMenus) {
                if (!SecurityUtils.isAdmin() && 3000L != m.getId() && 6000L != m.getId()) {        // admin은 모든 메뉴 접근 가능.

                    // 3. 기본 메뉴에서 menu id 가 일치하지 않는 메뉴는 remove()
                    // 		-> admin page && ROLE_ADMIN 이면 권한체크를 하지 않음.
                    menuService.processAccessableMenu(m, menuIds);
                }
            }
            allMenus.stream().forEach(m -> {
                if (m.getMenuKey() != null) {
                    if (Const.menuObjectTypeMap.get(m.getMenuKey()) != null) {
                        objectTypeSet.add(Const.menuObjectTypeMap.get(m.getMenuKey()));
                    }
                }
                //level 2
                if (m.getChildren() != null && !m.getChildren().isEmpty()) {
                    m.getChildren().stream().forEach(m2 -> {
                        if (m2.getMenuKey() != null) {
                            if (Const.menuObjectTypeMap.get(m2.getMenuKey()) != null) {
                                objectTypeSet.add(Const.menuObjectTypeMap.get(m2.getMenuKey()));
                            }
                        }
                        //level 3
                        if (m2.getChildren() != null && !m2.getChildren().isEmpty()) {
                            m2.getChildren().stream().forEach(m3 -> {
                                if (m3.getMenuKey() != null) {
                                    if (Const.menuObjectTypeMap.get(m3.getMenuKey()) != null) {
                                        objectTypeSet.add(Const.menuObjectTypeMap.get(m3.getMenuKey()));
                                    }
                                }
                            });
                        }
                    });
                }
            });

            if(objectTypeSet.contains(ObjectType.ACCESS_GROUP)){
                objectTypeSet.add(ObjectType.ACCESS_FEATURE);
            }
            objectTypeSet.stream().sorted().forEach(s -> {
                String menuKey = Const.menuObjectTypeMap.entrySet().stream().filter(map -> s.equals(map.getValue())).map(m -> m.getKey()).findFirst().orElse(null);
                resList.add(new Item(s.getCode(), s.getTitle(), menuKey));
            });
        }
        return resList.stream().sorted(Comparator.comparing(Item::getName)).collect(Collectors.toList());
    }



}
