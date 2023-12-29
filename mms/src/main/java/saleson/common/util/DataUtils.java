package saleson.common.util;

import java.util.*;
import java.util.stream.Collectors;

import org.dozer.DozerBeanMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.emoldino.framework.util.ValueUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.Gson;
import saleson.model.*;
import saleson.model.support.DateAudit;
import saleson.model.support.Equipment;
import saleson.model.support.UserDateAudit;

import saleson.model.Category;
import saleson.model.Location;
import saleson.model.Machine;
import saleson.model.Mold;
import saleson.model.Terminal;
import saleson.model.User;
import saleson.model.support.DateAudit;
import saleson.model.support.Equipment;
import saleson.model.support.UserDateAudit;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataUtils {
    static String PHONE_PATTERN ="^((([-|.|\\s])?\\(\\d+\\))?(\\d*([-|.|\\s])?\\d+)?)*$";
    static String PHONE_PATTERN_FULL ="^(\\+)?((([-|.|\\s])?\\(\\d+\\))?(\\d*([-|.|\\s])?\\d+)?)*$";

    //    public static Mapper mapper = new DozerBeanMapper();
    public static DozerBeanMapper mapper = new DozerBeanMapper();
    static {
        mapper.setMappingFiles(Collections.singletonList("dozerJdk8Converters.xml"));
    }
    public static Gson gson = new Gson();


    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
    public static Double roundDouble(Double value, int places) {
        if(value!=null){
            return round(value,places);
        }
        return null;
    }

    public static Pageable getPageable(Integer pageIndex, Integer pageSize, Pageable pageable){
        if(pageSize==null && pageIndex ==null){
            pageSize = Integer.MAX_VALUE;
            pageIndex = 0;
        }
        if(pageSize==null){
            pageSize=50;
        }
        return pageable == null ? PageRequest.of(pageIndex, pageSize) :
                PageRequest.of(pageIndex, pageSize, pageable.getSort());
    }

    public static <T> T deepCopy(Object object, Class<T> type) {
        try {
            return gson.fromJson(gson.toJson(object), type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static <T> T deepCopyJackSon(Object object, Class<T> type) {
        try {
            return ValueUtils.fromJsonStr(ValueUtils.toJsonStr(object),type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static Integer integerVal(Double v){
        if(v!=null) return v.intValue();
        return null;
    }
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            long d = Long.parseLong(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
    public static List<Long> getNumericElements(List<String> input) {
        if (input!=null)
        return input
				.stream()
                .filter(DataUtils::isNumeric)
                .mapToLong(Long::parseLong)
                .boxed()
                .collect(Collectors.toList());
        return new ArrayList<Long>();
    }

    public static List<Long> getRawNumericElements(String prefix, List<String> input) {
        return input
                .stream()
                .filter(s -> s.contains(prefix))
                .map(s -> s.substring(5))
                .filter(DataUtils::isNumeric)
                .mapToLong(Long::parseLong)
                .boxed()
                .collect(Collectors.toList());
    }

    public static boolean deepCompare(Object o1, Object o2) throws JsonProcessingException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String o1Json = ow.writeValueAsString(o1);
        String o2Json = ow.writeValueAsString(o2);
        return o1Json.equals(o2Json);
    }

	public static void mapCreateAndUpdateInfo(UserDateAudit dest, UserDateAudit orig) {
		if (dest == null) {
			return;
		}

		dest.setCreatedAt(orig.getCreatedAt());
		dest.setUpdatedAt(orig.getUpdatedAt());
		dest.setCreatedBy(orig.getCreatedBy());
		dest.setUpdatedBy(orig.getUpdatedBy());

		if (orig instanceof Category) {
			((Category) dest).setSortOrder(((Category) orig).getSortOrder());
			((Category) dest).setLevel(((Category) orig).getLevel());
//			((Category) dest).setChildren(null);
		}
	}

	public static void mapCreateAndUpdateInfo(Location dest, Location orig) {
        if(dest == null) return;
        dest.setCreatedAt(orig.getCreatedAt());
		dest.setUpdatedAt(orig.getUpdatedAt());
		dest.setCreatedBy(orig.getCreatedBy());
		dest.setUpdatedBy(orig.getUpdatedBy());

		if (orig instanceof Location) {
			((Location) dest).setCompany(((Location) orig).getCompany());
		}
	}

	public static void mapCreateAndUpdateInfo(Equipment dest, Equipment orig) {
        if(dest == null) return;
        dest.setCreatedAt(orig.getCreatedAt());
		dest.setUpdatedAt(orig.getUpdatedAt());
		dest.setCreatedBy(orig.getCreatedBy());
		dest.setUpdatedBy(orig.getUpdatedBy());

		if (orig instanceof Terminal) {
			((Terminal) dest).setOperatingStatus(((Terminal) orig).getOperatingStatus());
			((Terminal) dest).setOperatedAt(((Terminal) orig).getOperatedAt());
			((Terminal) dest).setCompanyId(((Terminal) orig).getCompanyId());
			((Terminal) dest).setLocation(((Terminal) orig).getLocation());
		} else if (orig instanceof Mold) {
			((Mold) dest).setOperatingStatus(((Mold) orig).getOperatingStatus());
			((Mold) dest).setOperatedAt(((Mold) orig).getOperatedAt());
			((Mold) dest).setCompanyId(((Mold) orig).getCompanyId());
			((Mold) dest).setWeightedAverageCycleTime(((Mold) orig).getWeightedAverageCycleTime());
			((Mold) dest).setLastCycleTime(((Mold) orig).getLastCycleTime());
			((Mold) dest).setLastShotAt(((Mold) orig).getLastShotAt());
			((Mold) dest).setLastShotAtVal(((Mold) orig).getLastShotAtVal());
			((Mold) dest).setLastShotMadeAt(((Mold) orig).getLastShotMadeAt());
			((Mold) dest).setFamilyTool(((Mold) orig).getFamilyTool());
			((Mold) dest).setShotSize(((Mold) orig).getShotSize());
			((Mold) dest).setEngineersInCharge(((Mold) orig).getEngineersInCharge());
			((Mold) dest).setEngineer(((Mold) orig).getEngineer());
			((Mold) dest).setCycleTimeStatus(((Mold) orig).getCycleTimeStatus());
			((Mold) dest).setEfficiencyStatus(((Mold) orig).getEfficiencyStatus());
			((Mold) dest).setMisconfigureStatus(((Mold) orig).getMisconfigureStatus());
			((Mold) dest).setUtilizationRate(((Mold) orig).getUtilizationRate());
			((Mold) dest).setSupplier(((Mold) orig).getSupplier());
			((Mold) dest).setToolMaker(((Mold) orig).getToolMaker());
			((Mold) dest).setMaxCapacityPerWeek(((Mold) orig).getMaxCapacityPerWeek());
			((Mold) dest).setDailyMaxCapacity(((Mold) orig).getDailyMaxCapacity());
			((Mold) dest).setPassedDays(((Mold) orig).getPassedDays());
			((Mold) dest).setDataSubmission(((Mold) orig).getDataSubmission());
			((Mold) dest).setLocationChanged(((Mold) orig).getLocationChanged());
			((Mold) dest).setMaintenanced(((Mold) orig).getMaintenanced());
			((Mold) dest).setMaintenanceCount(((Mold) orig).getMaintenanceCount());
			((Mold) dest).setCounterId(((Mold) orig).getCounterId());
			((Mold) dest).setOperatedStartAt(((Mold) orig).getOperatedStartAt());
			((Mold) dest).setMoldAuthorities(((Mold) orig).getMoldAuthorities());
			((Mold) dest).setDay(((Mold) orig).getDay());
			((Mold) dest).setWeek(((Mold) orig).getWeek());
			((Mold) dest).setMonth(((Mold) orig).getMonth());
			((Mold) dest).setMoldParts(((Mold) orig).getMoldParts());
			((Mold) dest).setLocation(((Mold) orig).getLocation());
		}
	}

	public static void mapCreateAndUpdateInfoBaseDate(DateAudit dest, DateAudit orig) {
        if(dest == null) return;
        dest.setCreatedAt(orig.getCreatedAt());
		dest.setUpdatedAt(orig.getUpdatedAt());
		if (orig instanceof User) {
			((User) dest).setLoginId(((User) orig).getLoginId());
			((User) dest).setPassword(((User) orig).getPassword());
			((User) dest).setSsoId(((User) orig).getSsoId());
			((User) dest).setFailedAttempt(((User) orig).getFailedAttempt());
			((User) dest).setLockTime(((User) orig).getLockTime());
			((User) dest).setCompany(((User) orig).getCompany());
			((User) dest).setLastLogin(((User) orig).getLastLogin());
			((User) dest).setPwdExpiredAt(((User) orig).getPwdExpiredAt());
			((User) dest).setRoles(((User) orig).getRoles());
			((User) dest).setPublic(((User) orig).isPublic());
			((User) dest).setNotify(((User) orig).getNotify());
			((User) dest).setAccessRequest(((User) orig).getAccessRequest());
			((User) dest).setDisableNotificationSystemNode(((User) orig).getDisableNotificationSystemNode());
		}
	}

    public static String intNumberToDay(int i) {
        return String.valueOf(i < 10 ? "0" + i : i);
    }

    public static boolean isPhoneNumberFull(String phone){
        Pattern regex = Pattern.compile(PHONE_PATTERN_FULL);
        Matcher matcher = regex.matcher(phone.trim());
        return phone.length()>2 && matcher.matches();
    }
    public static boolean isPhoneNumber(String phone){
        Pattern regex = Pattern.compile(PHONE_PATTERN);
        Matcher matcher = regex.matcher(phone.trim());
        return phone.length()>2 && matcher.matches();
    }

    public static boolean isInterger(Class<?> type){
        if (Integer.class.equals(type) || int.class.equals(type) //
            || Long.class.equals(type) || long.class.equals(type)) {
            return true;
        }
        return false;
    }

    public static boolean compareExactlyNumberValue(Object rawValue, Object convertedValue) {
        String str1 = "";
        String str2 = "";
        if (rawValue != null && !StringUtils.isEmpty(rawValue.toString())) {
            str1 = rawValue.toString().trim();
        }
        if (convertedValue != null && !StringUtils.isEmpty(convertedValue.toString())) {
            str2 = convertedValue.toString().trim();
        }
        return str1.equals(str2);
    }
}
