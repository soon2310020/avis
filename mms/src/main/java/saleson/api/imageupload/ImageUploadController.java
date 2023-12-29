package saleson.api.imageupload;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import saleson.api.imageupload.dto.*;
import saleson.model.Part;

@Api(protocols = "http, https", tags = "Common / Image Uploads")
@RequestMapping("/api/common/image-upload")
public interface ImageUploadController {
    @ApiOperation("Get Tooling Page")
    @GetMapping("/tooling")
    Page<MoldDto> getTooling(ImageUploadIn input, Pageable pageable);

    @ApiOperation("Get Machine Page")
    @GetMapping("/machine")
    Page<MachineDto> getMachine(ImageUploadIn input, Pageable pageable);

    @ApiOperation("Get Terminal Page")
    @GetMapping("/terminal")
    Page<TerminalDto> getTerminal(ImageUploadIn input, Pageable pageable);

    @ApiOperation("Get Sensor Page")
    @GetMapping("/sensor")
    Page<SensorDto> getSensor(ImageUploadIn input, Pageable pageable);

    @ApiOperation("Get Company Page")
    @GetMapping("/company")
    Page<CompanyDto> getCompany(ImageUploadIn input, Pageable pageable);

    @ApiOperation("Get Plant Page")
    @GetMapping("/plant")
    Page<PlantDto> getPlant(ImageUploadIn input, Pageable pageable);

    @ApiOperation("Get Product Page")
    @GetMapping("/product")
    Page<ProductDto> getProduct(ImageUploadIn input, Pageable pageable);

    @ApiOperation("Get Part Page")
    @GetMapping("/part")
    Page<PartDto> getPart(ImageUploadIn input, Pageable pageable);

    @ApiOperation("Get Brand Page")
    @GetMapping("/brand")
    Page<BrandDto> getBrand(ImageUploadIn input, Pageable pageable);

}
