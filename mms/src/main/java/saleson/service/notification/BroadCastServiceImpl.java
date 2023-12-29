package saleson.service.notification;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import saleson.api.auth.AccessTokenRepository;
import saleson.api.broadcastNotification.BroadcastNotificationRepository;
import saleson.dto.BroadcastNotificationDTO;
import saleson.model.BroadcastNotification;
import saleson.model.AccessToken;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BroadCastServiceImpl {
    protected static final Logger LOGGER = LoggerFactory.getLogger(BroadCastServiceImpl.class);


    @Value("${notification.fcmKey}")
    private String fcmKey;

    @Value("${notification.fcmUrl}")
    private String fcmUrl;

    @Autowired
    TaskExecutor taskExecutor;
    @Autowired
    AccessTokenRepository accessTokenRepository;

    @Autowired
    BroadcastNotificationRepository broadcastNotificationRepository;

    @Value("${api.expired.time}")
    private Integer expiredTime;

    public void sendNotificationToUser(Long userId, String message, BroadcastNotificationDTO broadcastNotification)
    {
        taskExecutor.execute(() -> {
            System.out.println("===run sendNotificationByMultiThread===");
            generateTemplateAndSendToDevices(userId, message, broadcastNotification);
        });

    }

    void generateTemplateAndSendToDevices(Long userId, String message, BroadcastNotificationDTO broadcastNotification)
    {
        try
        {
            Thread.sleep(100);
            Long numberNotification = broadcastNotificationRepository.countAllByUserIdAndIsReadIsFalse(userId);
/*
            SearchBroadCastDTO searchBroadCastDTO = new SearchBroadCastDTO();
            searchBroadCastDTO.setUserId(userId);
            searchBroadCastDTO.setSeen(false);
            int numberNotification = countListBroadCast(searchBroadCastDTO);

*/
            Instant validDate=Instant.now().minus(expiredTime, ChronoUnit.MINUTES);
//            List<AccessToken> sessionUserList = accessTokenRepository.findAllByUserIdEqualsAndExpiredTimeGreaterThanEqualAndDeviceTokenIsNotNullAndDeviceTypeIsNotNull(userId, Instant.now());
            List<AccessToken> sessionUserList = accessTokenRepository.findAllByUserIdEqualsAndCreatedAtGreaterThanEqualAndDeviceTokenIsNotNullAndDeviceTypeIsNotNull(userId, validDate);
            Set<String> totalDeviceTokenList = sessionUserList.stream().filter(s-> StringUtils.isNotEmpty(s.getDeviceToken())).map(s->s.getDeviceToken()).collect(Collectors.toSet());
/*
            List<String> iOSDeviceTokenList = new ArrayList<>();
            List<String> androidDeviceTokenList = new ArrayList<>();
//            List<UserDeviceConfig> listDisable = userDeviceConfigRepository.findByUserIdAndDisabledNotificationIsTrueAndDeletedIsFalse(userId);
            //check ignore device
//            if (listDisable == null || listDisable.isEmpty())
//            {
                for (AccessToken session : sessionUserList)
                {
                    if (session.getDeviceType().equals(Const.DEVICE_TYPE.IOS) && !iOSDeviceTokenList.contains(session.getDeviceToken()))
                    {
                        iOSDeviceTokenList.add(session.getDeviceToken());
                    }
                    else if (session.getDeviceType().equals(Const.DEVICE_TYPE.ANDROID) && !androidDeviceTokenList.contains(session.getDeviceToken()))
                    {
                        androidDeviceTokenList.add(session.getDeviceToken());
                    }
                }
//            }
            if (!iOSDeviceTokenList.isEmpty())
            {

                //TODO, need to format message
                sendNotificationiOS(iOSDeviceTokenList, message, numberNotification, broadCast);
            }

            if (!androidDeviceTokenList.isEmpty())
            {
                sendPushNotificatinFcm(androidDeviceTokenList, message, numberNotification, broadCast);
            }
*/
            if(!totalDeviceTokenList.isEmpty())
                sendPushNotificatinFcm(totalDeviceTokenList, broadcastNotification!=null ? broadcastNotification.getTitle(): null, message, numberNotification.intValue(), broadcastNotification);

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            LOGGER.error("Thread send notification", ex);
        }
    }

    public void sendPushNotificatinFcm(Collection<String> deviceTokens, String title,String message, Integer numberNotification, BroadcastNotificationDTO broadcastNotification)
    {

        sendPushNotificatinFcmWithKey(fcmKey, fcmUrl, deviceTokens, title, message, numberNotification, broadcastNotification);
    }

    /***
     * send notification to FCN
     * @param fcmKey
     * @param fcmUrl
     * @param deviceTokens
     * @param message
     */
    public static void sendPushNotificatinFcmWithKey(String fcmKey, String fcmUrl, Collection<String> deviceTokens
            , String title, String message, Integer numberNotification, BroadcastNotificationDTO broadcastNotification)
    {
        try
        {
            if(deviceTokens.isEmpty()) return;
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Authorization", "key=" + fcmKey);
            httpHeaders.set("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);

            JSONObject msg = new JSONObject();
            JSONObject json = new JSONObject();

//            msg.put("messageId", messageId);
            msg.put("title", title);
            msg.put("body", message);
            msg.put("badge", numberNotification);
//            msg.put("messageCount", numberNotification);
//            msg.put("number", numberNotification);
            msg.put("icon", "ic_launcher");

//            json.put("data", msg);
            json.put("notification", msg);
//            json.put("icon", "https://user-images.githubusercontent.com/48935436/120588362-3ed5c180-c461-11eb-96f8-4818515e49a2.png");
//            json.put("to", deviceTokens.toArray()[0]);
            json.put("registration_ids", deviceTokens);
            //            json.put("sound", "");
            //data
            if (broadcastNotification != null)
            {
                JSONObject data = new JSONObject();
                if (broadcastNotification.getInfors() != null && !broadcastNotification.getInfors().isEmpty())
                {
                    data.put("infors", broadcastNotification.getInfors());
                }
                data.put("notificationType", broadcastNotification.getNotificationType());
                data.put("systemFunction", broadcastNotification.getSystemFunction());
                data.put("id", broadcastNotification.getId());
                json.put("data", data);
            }

            HttpEntity<String> httpEntity = new HttpEntity<String>(json.toString(), httpHeaders);
            String response = restTemplate.postForObject(fcmUrl, httpEntity, String.class);
            System.out.println(response);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        BroadcastNotification broadcastNotification = new BroadcastNotification();
        broadcastNotification.setInfors(Arrays.asList("Info 1","Info 2"));
        broadcastNotification.setNotificationType("Notification type");
        List<String> deviceTokensA = new ArrayList<>();
        deviceTokensA.add("dQCUcf4yQfSvC6klICtqr0:APA91bHIlMRem9qIMh_p3Iu3EG34XRz7AP1sAQ0Y7tr4TlbKpbR4nEFAroCW3CX-37yHsxpmjm679T1w11YqEHhitECINd1DDah1W8gtKTDhj3Y7TgroL-b-9EawrBZmJtAAQZu4cNbZ");
        deviceTokensA.add("ekefqGyfSdW-WFR1owMjTd:APA91bHPN4S5yPqoj4hVjMnq_CAbdQC4-jk3Elcx_O6jG0f2tw9y6CJjsaARCHPcOn9MVNgNbwLlSgqcQEeIqmDIHsUMZUAmEGoCwsnIsA4UigVE9D-IAgFeDMUbxC02To8v5xloS6f6");
//        sendPushNotificatinFcmWithKey("AAAAlmufxpw:APA91bEpDXv9sas1WeG7qZ68DkzntEqrnsuZ6huQJ4ToBJ1sjZt-SliF48FXx2xUxZdhMgTz3U0GDukx25et7yWEd9hrKDetX7OlFLu3fENnPPeJRZY3YGZQHophb5Qhkte3QZPJN3sh"
//            , "https://fcm.googleapis.com/fcm/send"
//            ,deviceTokensA
//            ,  "Viet test thong bao cong diem", 10, null);

        sendPushNotificatinFcmWithKeyTest(deviceTokensA, broadcastNotification);
/*
        try
        {
            List<String> deviceTokens = new ArrayList<>();
//        deviceTokens.add("8403b6173ed2b152230b32d36c1d17d83c6d91248eb766f5449037b41a2df5a0"); //Hung's iPhone
            deviceTokens.add("937f35bfc48d241da9ef774851039cf042453a724d79628551a4eae23889273e"); //Hien's iPhone
//
//        sendNotificationiOS(
//            deviceTokens, "[Test-Message] - You just got 100 points after uploading", 10);

            int numberNotification=10;
            String message="[Test-Message] - Viet test You just got 100 points after uploading";
            String p12Path="mobile\\mss-prod.p12";
            String p12Password="123";
            InputStream inputStream = new ClassPathResource(p12Path).getInputStream();
            ApnsService service;

            service = APNS.newService().withCert(inputStream, p12Password)
                    .withProductionDestination().build();

            String payload = APNS.newPayload()
                    .alertBody(message)
                    .badge(numberNotification)
                    .build();
            service.push(deviceTokens, payload);

        }catch (Exception e){
            e.printStackTrace();
        }
*/
    }
    public static void sendPushNotificatinFcmWithKeyTest(Collection<String> deviceTokens, BroadcastNotification broadcastNotification)
    {
        try
        {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Authorization", "key=" + "AAAAlmufxpw:APA91bEpDXv9sas1WeG7qZ68DkzntEqrnsuZ6huQJ4ToBJ1sjZt-SliF48FXx2xUxZdhMgTz3U0GDukx25et7yWEd9hrKDetX7OlFLu3fENnPPeJRZY3YGZQHophb5Qhkte3QZPJN3sh");
            httpHeaders.set("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);

            JSONObject msg = new JSONObject();
            JSONObject json = new JSONObject();

//            msg.put("messageId", messageId);
            msg.put("title", "title 1");
            msg.put("body", "body message");
            msg.put("badge", 101);
//            msg.put("messageCount", 102);
//            msg.put("number", 103);
//            msg.put("icon", "ic_launcher");

//            json.put("icon", "https://user-images.githubusercontent.com/48935436/120588362-3ed5c180-c461-11eb-96f8-4818515e49a2.png");
//            json.put("icon", "ic_launcher");
            json.put("notification", msg);
//            json.put("to", deviceTokens.toArray()[0]);
            json.put("registration_ids", deviceTokens);
//            json.put("sound", "");
            //data
            if (broadcastNotification != null)
            {
                JSONObject data = new JSONObject();
                if (broadcastNotification.getInfors() != null && !broadcastNotification.getInfors().isEmpty())
                {
                    data.put("infors", broadcastNotification.getInfors());
                }
                data.put("notificationType", broadcastNotification.getNotificationType());
                json.put("data", data);
            }

            HttpEntity<String> httpEntity = new HttpEntity<String>(json.toString(), httpHeaders);
            String response = restTemplate.postForObject("https://fcm.googleapis.com/fcm/send", httpEntity, String.class);
            System.out.println(response);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


}
