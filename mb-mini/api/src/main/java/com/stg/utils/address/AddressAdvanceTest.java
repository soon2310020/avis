package com.stg.utils.address;

import com.stg.entity.address.AddressDistrict;
import com.stg.entity.address.AddressProvince;
import com.stg.entity.address.AddressWard;
import com.stg.repository.AddressDistrictRepository;
import com.stg.repository.AddressProvinceRepository;
import com.stg.repository.AddressWardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.CommandLineRunner;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.stg.utils.address.AddressUtil.*;


/**
 * Continue...develop
 * => search and select the maximum matching value
 */
@Slf4j
//@Service
@RequiredArgsConstructor
public class AddressAdvanceTest implements CommandLineRunner {
    private final AddressProvinceRepository addressProvinceRepository;
    private final AddressDistrictRepository addressDistrictRepository;
    private final AddressWardRepository addressWardRepository;

    private static final Map<String, AddressInfo> ADDRESS_INFO_MAP = new ConcurrentHashMap<>();

    @Override
    public void run(String... args) {
        loadAndCachingData();
    }

    /***/
    public void loadAndCachingData() {
//        // 1.get data
//        Map<String, AddressProvince> provinceMap = addressProvinceRepository.findAll()
//                .stream().collect(Collectors.toMap(AddressProvince::getCode, Function.identity()));
//        Map<String, AddressDistrict> districtMap = addressDistrictRepository.findAll()
//                .stream().collect(Collectors.toMap(AddressDistrict::getCode, Function.identity()));
//        Map<String, AddressWard> wardMap = addressWardRepository.findAll()
//                .stream().collect(Collectors.toMap(AddressWard::getCode, Function.identity()));
//
//        // clear cache
//        ADDRESS_INFO_MAP.clear();
//
//        // 2. fill data
//        int countDuplicateName = 0;
//        for (AddressWard ward : wardMap.values()) {
//            AddressDistrict district = districtMap.get(ward.getDistrictCode());
//            AddressProvince province = provinceMap.get(district.getProvinceCode());
//
//            String key = cleanUpperWard(ward.getName()) + "|" + cleanUpperDistrict(district.getName()) + "|" + cleanUpperProvince(province.getName());
//            if (ADDRESS_INFO_MAP.get(key) != null) {
//                countDuplicateName++;
//                AddressInfo info = ADDRESS_INFO_MAP.get(key);
//                System.out.println(
//                        province.getCode().equals(info.getProvinceCode()) + "|"
//                                + district.getCode().equals(info.getDistrictCode()) + "|"
//                                + ward.getCode().equals(info.getWardCode()) + " || "
//                                + province.getCode() + " - " + district.getCode() + " - " + ward.getCode() + " - " + ward.getName() + " || "
//                                + info
//                );
//            }
//            ADDRESS_INFO_MAP.put(key, new AddressInfo(key, province, district, ward));
//        }
//        log.info("countDuplicateName: " + countDuplicateName);
//        log.info("totalAddress: " + ADDRESS_INFO_MAP.size());
        ///////////////////

        List<String> data = readXlsx();
        for (String address : data) {
            splitAddress(address);
        }
    }

    public Map<String, AddressInfo> getAddressInfoMap() {
        return ADDRESS_INFO_MAP;
    }

    public static void main(String[] args) {
        List<String> data = readXlsx();
        int count = 0;
        for (String address : data) {
            if (address.contains("  ")) {
                System.out.println("DEBUG... : " + address);
            }
            count++;
            AddressInfo addressInfo = splitAddress(address);
            //AddressInfo addressInfo = AddressUtil.splitAddress(address);
            System.out.println(addressInfo);
        }
        System.out.println("count1: " + count1);
        System.out.println("count2: " + count2);
        System.out.println("count3: " + count3);
        System.out.println("count4: " + count4);
        System.out.println("ignore: " + ignore);
        System.out.println("total: " + count);
    }


    static List<String> readXlsx() {
        String fileName = "address_test.xlsx";
        log.info("[readXlsx] Start read file {" + fileName + "}");
        List<String> data = new ArrayList<>();
        try {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            InputStream inputStream = classloader.getResourceAsStream("excel/" + fileName);

            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            Iterator<Row> rowIterator = sheet.iterator();
            rowIterator.next(); // next header

            int rowIdx = 1; // start from 1
            while (rowIterator.hasNext()) {
                rowIdx++;
                Row row = rowIterator.next();
                try {
                    Cell cell = row.getCell(0);
                    String cellValue = cell == null ? null : cell.getStringCellValue();
                    if (!StringUtils.hasText(cellValue)) continue;

                    data.add(cellValue);
                } catch (Exception ex) {
                    log.info("[readXlsx] Error when read file {" + fileName + "} at row: " + rowIdx, ex);
                }
            }
        } catch (Exception ex) {
            log.error("[readXlsx] Error when read file " + fileName, ex);
        }

        return data;
    }

    static int count1 = 0;
    static int count2 = 0;
    static int count3 = 0;
    static int count4 = 0;
    static int ignore = 0;

    public static AddressInfo splitAddress(String address) {
        if (!StringUtils.hasText(address)) return new AddressInfo();

        String addressOrg = address;
        address = cleanAddress(address);

        if (address.contains("!-")) {
            count1++;
            address = address.replaceFirst("(!-)", "");
            String[] arrays = address.split("(-)");
            //System.out.println(joinToObject(arrays));
            return joinToObject(arrays);
        }

        if (address.contains("-")) {
            count2++;
            String[] arrays = address.split("(-)");
            //System.out.println(joinToObject(arrays));
            return joinToObject(arrays);
        }

        if (address.contains(",")) {
            count3++;
            String[] arrays = address.split("(,)");
            //System.out.println(joinToObject(arrays));
            return joinToObject(arrays);
        }

        if (address.contains("  ")) {
            count4++;
            String[] arrays = address.split("( {2})");
            //System.out.println(joinToObject(arrays));
            return joinToObject(arrays);
        }

        ignore++;
        /*String[] arrays = address.split(ADDRESS_SEPARATOR);
        System.out.println(address + " || " + joinToObject(arrays));*/

        return joinToObject(address);
    }
}
