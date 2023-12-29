package vn.com.twendie.avis.api.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.model.payload.VehiclePayload;
import vn.com.twendie.avis.api.model.response.VehicleDetailDTO;
import vn.com.twendie.avis.api.rest.exception.BadRequestException;
import vn.com.twendie.avis.api.service.VehicleService;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.security.jdbc.repository.UserRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.*;

@Slf4j
@Service
public class ImportExcelServiceImpl implements ImportExcelService {

    @Autowired
    private DateUtils dateUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleService vehicleService;

    private String storageUrl = "file_upload";

    private Path root;

    @EventListener
    public void init(ApplicationReadyEvent event) {
        root = Paths.get(storageUrl);
        try {
            if (!root.toFile().exists()) Files.createDirectory(root);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            log.info("Couldn't init root directory");
        }
    }

    @Override
    public String saveFile(MultipartFile file) throws Exception {
        String type = getExtension(file.getOriginalFilename());
        String imageName = UUID.randomUUID() + "." + type;
        try {
            Files.copy(file.getInputStream(), this.root.resolve(imageName));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.info("Couldn't save file");
        }
        return imageName;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void importVehicleExcel(MultipartFile file) throws Exception {

        String type = getExtension(file.getOriginalFilename());
        if (!(type.equalsIgnoreCase("xlsx") || type.equalsIgnoreCase("xls")))
            throw new BadRequestException("File format isn't correct");

        String fileName = saveFile(file);

        List<VehiclePayload> vehiclePayloads = readFile(fileName);

        int[] index={0};
        vehiclePayloads.stream().forEach(s -> {
            try{
                index[0]++;
                VehicleDetailDTO vehicleDTO = vehicleService.createVehicle(s);
                if (Objects.nonNull(vehicleDTO)) log.info("STT "+index[0]+" : "+vehicleDTO.getNumberPlate());
                else log.info("Create fail");
            }catch (Exception e){
                log.info("Create fail");
                log.error(e.getMessage(), e);
            }
        });

        deleteOneFile(fileName);
    }

    private String getExtension(String originalFileName) {
        return StringUtils.getFilenameExtension(originalFileName);
    }

    private File loadOneFile(String filename) {
        try {
            Path path = root.resolve(filename);
            File file = path.toFile();
            if (file.exists()) {
                return file;
            } else {
                log.info("File not found");
            }
        } catch (Exception e) {
            log.error("Couldn't load file", e);
        }
        throw new BadRequestException("Couldn't load file");
    }

    private List<VehiclePayload> readFile(String fileName) throws Exception {
        // Get file
        InputStream inputStream = new FileInputStream(loadOneFile(fileName));

        // Get workbook
        Workbook workbook = getWorkbook(inputStream, getExtension(fileName));

        // Get sheet
        Sheet sheet = workbook.getSheetAt(0);

        // Get all rows
        Iterator<Row> iterator = sheet.iterator();

        List<VehiclePayload> vehiclePayloads = new ArrayList<>();

        while (iterator.hasNext()) {
            Row nextRow = iterator.next();
            if (nextRow.getRowNum() < 5) {
                // Ignore header
                continue;
            }

            // Get all cells
            Iterator<Cell> cellIterator = nextRow.cellIterator();

            VehiclePayload vehiclePayload = new VehiclePayload();

            while (cellIterator.hasNext()) {
                //Read cell
                Cell cell = cellIterator.next();
                Object cellValue = getCellValue(cell);
                if (cellValue == null || cellValue.toString().isEmpty()) {
                    continue;
                }
                // Set value for book object
                int columnIndex = cell.getColumnIndex();
                switch (columnIndex) {
                    case 1:
                        vehiclePayload.setNumberPlate((String) getCellValue(cell));
                        break;
                    case 2:
                        vehiclePayload.setType((String) getCellValue(cell));
                        break;
                    case 3:
                        String owner=(String) getCellValue(cell);
                        vehiclePayload.setOwner(owner);
                        vehiclePayload.setVehicleSupplierGroupId(owner.equalsIgnoreCase("avis")?1L:2L);
                        break;
                    case 4:
                        vehiclePayload.setModel((String) getCellValue(cell));
                        break;
                    case 5:
                        String MTAT = (String) getCellValue(cell);
                        vehiclePayload.setTransmissionType(MTAT.equalsIgnoreCase("MT") ? 1 : 2);
                        break;
                    case 6:
                        vehiclePayload.setNumberSeat(new BigDecimal((double) cellValue).intValue());
                        break;
                    case 7:
                        vehiclePayload.setPlaceOfOrigin((String) getCellValue(cell));
                        break;
                    case 8:
                        String OIL = (String) getCellValue(cell);
                        vehiclePayload.setFuelTypeGroupId(OIL.equalsIgnoreCase("xăng") ? 1L : 2L);
                        break;
                    case 9:
                        vehiclePayload.setColor((String) getCellValue(cell));
                        break;
                    case 10:
                        String branch = (String) getCellValue(cell);
                        long i = 2L;
                        if (branch.equalsIgnoreCase("HN")) i = 1L;
                        else if (branch.equalsIgnoreCase("HCM")) i = 3L;
                        vehiclePayload.setBranchId(i);
                        break;
                    case 11:
                        vehiclePayload.setChassisNo((String) getCellValue(cell));
                        break;
                    case 12:
                        try {
                            vehiclePayload.setEngineNo((String) getCellValue(cell));
                        }catch (ClassCastException e){
                            vehiclePayload.setEngineNo(String.valueOf(getCellValue(cell)));
                        }
                        break;
                    case 13:
                        vehiclePayload.setYearManufacture(new BigDecimal((double) cellValue).intValue());
                        break;
                    case 14:
                        vehiclePayload.setStartUsingDate(convertDate(cell.getDateCellValue()));
                        break;
                    case 15:
                        vehiclePayload.setRegistrationNo((String) getCellValue(cell));
                        break;
                    case 16:
                        vehiclePayload.setTravelWarrantExpiryDate(convertDate(cell.getDateCellValue()));
                        break;
                    case 17:
                        vehiclePayload.setRegistrationToDate(convertDate(cell.getDateCellValue()));
                        break;
                    case 18:
                        vehiclePayload.setInsuranceNo((String) getCellValue(cell));
                        break;
                    case 19:
                        vehiclePayload.setInsuranceExpiryDate(convertDate(cell.getDateCellValue()));
                        break;
                    case 20:
                        vehiclePayload.setRoadFeeExpiryDate(convertDate(cell.getDateCellValue()));
                        break;
                    case 21:
                        vehiclePayload.setOperationAdminId(getUserByName((String) getCellValue(cell)).getId());
                        break;
                    case 22:
                        vehiclePayload.setUnitOperatorId(getUserByName((String) getCellValue(cell)).getId());
                        break;
                    case 23:
                        vehiclePayload.setAccountantId(getUserByName((String) getCellValue(cell)).getId());
                        break;
                    default:
                        break;
                }
            }
            vehiclePayload.setStatus(1);
            vehiclePayloads.add(vehiclePayload);
        }

        workbook.close();
        inputStream.close();

        return vehiclePayloads;
    }

    private User getUserByName(String name) {
        User user3 = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(name).get();
        return user3;
    }

    private Timestamp convertDate(Date date) throws Exception {
        return dateUtils.startOfDay(date.getTime());
    }

    private void deleteOneFile(String filename) throws Exception {
        Path path = root.resolve(filename);
        FileSystemUtils.deleteRecursively(path.toFile());
    }

    // Get Workbook
    private static Workbook getWorkbook(InputStream inputStream, String fileExtend) throws IOException {
        Workbook workbook = null;
        if (fileExtend.equalsIgnoreCase("xlsx")) {
            workbook = new XSSFWorkbook(inputStream);
        } else if (fileExtend.equalsIgnoreCase("xls")) {
            workbook = new HSSFWorkbook(inputStream);
        } else {
            throw new IllegalArgumentException("The specified file is not Excel file");
        }

        return workbook;
    }

    // Get cell value
    private static Object getCellValue(Cell cell) {
        CellType cellType = cell.getCellTypeEnum();
        Object cellValue = null;
        switch (cellType) {
            case BOOLEAN:
                cellValue = cell.getBooleanCellValue();
                break;
            case FORMULA:
                Workbook workbook = cell.getSheet().getWorkbook();
                FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                cellValue = evaluator.evaluate(cell).getNumberValue();
                break;
            case NUMERIC:
                cellValue = cell.getNumericCellValue();
                break;
            case STRING:
                cellValue = cell.getStringCellValue();
                break;
            default:
                break;
        }

        return cellValue;
    }
}
