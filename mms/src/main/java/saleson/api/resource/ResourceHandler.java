package saleson.api.resource;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import saleson.api.user.UserRepository;
import saleson.common.config.Const;
import saleson.common.service.ContextWrapper;
import saleson.model.User;
import saleson.common.context.EmoldinoResourceMessageBundleSource;
import saleson.common.security.UserPrincipal;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;


@Component
public class ResourceHandler {
	@Autowired
	UserRepository userRepository;
	@Autowired
	ObjectFactory<HttpSession> httpSessionFactory;
	private static Map<String,Map<String,String>> mapAllLocalMessage= null;

//	private HashMap<String, Locale> locals = new HashMap();
	private final MessageSource messageSource;
	public ResourceHandler(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public String getMessage(String code, String lang, Object[] params) {
		return messageSource.getMessage(code, params, StringUtils.parseLocale(lang));
	}

	public Map<String,String> getAllMessagesOfCurrentUser() {
		SecurityContext securityContext = SecurityContextHolder.getContext();


		if (securityContext.getAuthentication().getPrincipal() instanceof UserPrincipal) {
			UserPrincipal up = (UserPrincipal) securityContext.getAuthentication().getPrincipal();
			User user = userRepository.findByLoginIdAndDeletedIsFalse(up.getEmail()).orElse(null);
			return getAllMessages(user.getLanguage() == null ? "en" : user.getLanguage());
		}
		return getAllMessages("en");
	}

	public Map<String, String> getAllMessages(String localeValue) {
		return getAllMessages(StringUtils.parseLocale(localeValue));
	}

	public Map<String, String> getAllMessages(Locale locale){
		Properties properties = ((EmoldinoResourceMessageBundleSource) messageSource).getMessages(locale);

		Map<String, String> messagesMap = new HashMap<>();
		for(Map.Entry<Object,Object> entry: properties.entrySet()){
			if(entry.getKey() != null && entry.getValue() != null) {
				messagesMap.put(entry.getKey().toString(), entry.getValue().toString());
			}
		}
		return messagesMap;
	}
	public void updateLang(String lang){
		HttpSession session = httpSessionFactory.getObject();
		if(session!=null) session.setAttribute(Const.SESSION.language,lang.toLowerCase());
	}
	public String getCurrentLang(){
		SecurityContext securityContext = SecurityContextHolder.getContext();
		if (securityContext.getAuthentication().getPrincipal() instanceof UserPrincipal) {
			HttpSession session = httpSessionFactory.getObject();
			if(session!=null){
				if(session.getAttribute(Const.SESSION.language)!=null){
					return (String) session.getAttribute(Const.SESSION.language);
				}
			}
			UserPrincipal up = (UserPrincipal) securityContext.getAuthentication().getPrincipal();
			User user = userRepository.findByLoginIdAndDeletedIsFalse(up.getEmail()).orElse(null);
			String lang= user.getLanguage() == null ? Const.LANGUAGE.en : user.getLanguage();
			if(session!=null) session.setAttribute(Const.SESSION.language,lang.toLowerCase());
			return lang.toLowerCase();
		}
		return Const.LANGUAGE.en;
	}
/*
	public String getValueInMap(Map<String,String>map,String key){
		if(map.containsKey(key)) return map.get(key);
		return key;
	}
*/
	public String getMessageByKey(String key,String localValue){
//		Map<String,String>map=StringUtils.isEmpty(localValue) ? getAllMessagesOfCurrentUser(): getAllMessages(localValue);
		Map<String,String>map=StringUtils.isEmpty(localValue) ? getAllMessagesOfCurrentUser(): getInstanceMapAllLocalMessage().get(localValue.toLowerCase());
		if(map.containsKey(key)) return map.get(key);
		if(!Const.LANGUAGE.en.equalsIgnoreCase(localValue)
				&& getInstanceMapAllLocalMessage().get(Const.LANGUAGE.en).containsKey(key))
			return getInstanceMapAllLocalMessage().get(Const.LANGUAGE.en).get(key);

		return key;
	}
	public String getMessageByKey(String key){
		return getMessageByKey(key,getCurrentLang());
	}
//	public String getMessageEnByKey(String key){
//		return getMessageByKey(key,Const.LANGUAGE.en);
//	}
	public static Map<String,Map<String,String>> getInstanceMapAllLocalMessage(){
		if (mapAllLocalMessage == null) {
			ResourceHandler resourceHandler = ContextWrapper.getContext().getBean("resourceHandler", ResourceHandler.class);
			mapAllLocalMessage = new HashMap<>();
			mapAllLocalMessage.put(Const.LANGUAGE.en,resourceHandler.getAllMessages(Const.LANGUAGE.en));
			mapAllLocalMessage.put(Const.LANGUAGE.zh,resourceHandler.getAllMessages(Const.LANGUAGE.zh));
			mapAllLocalMessage.put(Const.LANGUAGE.de,resourceHandler.getAllMessages(Const.LANGUAGE.de));
		}
		return mapAllLocalMessage;
	}
}
