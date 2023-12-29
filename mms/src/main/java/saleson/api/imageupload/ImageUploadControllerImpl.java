package saleson.api.imageupload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import saleson.api.imageupload.dto.BrandDto;
import saleson.api.imageupload.dto.CompanyDto;
import saleson.api.imageupload.dto.ImageUploadIn;
import saleson.api.imageupload.dto.MachineDto;
import saleson.api.imageupload.dto.MoldDto;
import saleson.api.imageupload.dto.PartDto;
import saleson.api.imageupload.dto.PlantDto;
import saleson.api.imageupload.dto.ProductDto;
import saleson.api.imageupload.dto.SensorDto;
import saleson.api.imageupload.dto.TerminalDto;
import saleson.api.imageupload.service.ImageUploadService;

@RestController
public class ImageUploadControllerImpl implements ImageUploadController{
    @Autowired
    ImageUploadService imageUploadService;


    @Override
    public Page<MoldDto> getTooling(ImageUploadIn input, Pageable pageable) {
        return imageUploadService.getTooling(input,pageable);
    }

    @Override
    public Page<MachineDto> getMachine(ImageUploadIn input, Pageable pageable) {
        return imageUploadService.getMachine(input,pageable);
    }

    @Override
    public Page<TerminalDto> getTerminal(ImageUploadIn input, Pageable pageable) {
        return imageUploadService.getTerminal(input,pageable);
    }

    @Override
    public Page<SensorDto> getSensor(ImageUploadIn input, Pageable pageable) {
        return imageUploadService.getSensor(input,pageable);
    }

    @Override
    public Page<CompanyDto> getCompany(ImageUploadIn input, Pageable pageable) {
        return imageUploadService.getCompany(input,pageable);
    }

    @Override
    public Page<PlantDto> getPlant(ImageUploadIn input, Pageable pageable) {
        return imageUploadService.getPlant(input,pageable);
    }

    @Override
    public Page<ProductDto> getProduct(ImageUploadIn input, Pageable pageable) {
        return imageUploadService.getProduct(input,pageable);
    }

    @Override
    public Page<PartDto> getPart(ImageUploadIn input, Pageable pageable) {
        return imageUploadService.getPart(input,pageable);
    }

    @Override
    public Page<BrandDto> getBrand(ImageUploadIn input, Pageable pageable) {
        return imageUploadService.getBrand(input,pageable);
    }
}
