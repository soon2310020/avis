package vn.com.twendie.avis.api.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import vn.com.twendie.avis.api.model.payload.DriverPayload;
import vn.com.twendie.avis.api.rest.exception.BadRequestException;
import vn.com.twendie.avis.data.enumtype.DriverGroupEnum;
import vn.com.twendie.avis.locale.config.Translator;

import java.util.Objects;

@Component
public class DriverValidation {

    public void validDriverRequireInfo(DriverPayload payload, Long userGroupId) {
        if (!DriverGroupEnum.CUSTOMER.getId().equals(userGroupId)) {
            if (Objects.isNull(payload.getCardType())) {
                throw new BadRequestException("valid_error.card_type_is_blank")
                        .displayMessage(Translator.toLocale("valid_error.card_type_is_blank"));
            } else if (payload.getCardType() > 3 || payload.getCardType() < 1) {
                throw new BadRequestException("valid_error.card_type_wrong_format")
                        .displayMessage(Translator.toLocale("valid_error.card_type_wrong_format"));
            }

            if (StringUtils.isBlank(payload.getIdCard())) {
                throw new BadRequestException("valid_error.id_card_wrong_format")
                        .displayMessage(Translator.toLocale("valid_error.id_card_wrong_format"));
            }

            if (Objects.isNull(payload.getBirthdate())) {
                throw new BadRequestException("valid_error.birth_date_is_blank")
                        .displayMessage(Translator.toLocale("valid_error.birth_date_is_blank"));
            }

            if (StringUtils.isBlank(payload.getAddress())) {
                throw new BadRequestException("valid_error.address_is_blank")
                        .displayMessage(Translator.toLocale("valid_error.address_is_blank"));
            }

            if (StringUtils.isBlank(payload.getDriverLicenseNumber())) {
                throw new BadRequestException("driver.valid_error.driver_license_number_is_blank")
                        .displayMessage(Translator.toLocale("driver.valid_error.driver_license_number_is_blank"));
            }

            if (Objects.isNull(payload.getDriverLicenseExpiryDate())) {
                throw new BadRequestException("driver.valid_error.driver_license_expiry_date_is_blank")
                        .displayMessage(Translator.toLocale("driver.valid_error.driver_license_expiry_date_is_blank"));
            }

            if (Objects.isNull(payload.getDdtCertificate())) {
                throw new BadRequestException("driver.valid_error.ddt_certificate_is_blank")
                        .displayMessage(Translator.toLocale("driver.valid_error.ddt_certificate_is_blank"));
            }

            if (Objects.isNull(payload.getKnowEnglish())) {
                throw new BadRequestException("driver.valid_error.know_english_is_blank")
                        .displayMessage(Translator.toLocale("driver.valid_error.know_english_is_blank"));
            }

            if (Objects.isNull(payload.getUnitOperatorId())) {
                throw new BadRequestException("valid_error.unit_operator_id_is_blank")
                        .displayMessage(Translator.toLocale("valid_error.unit_operator_id_is_blank"));
            }

        }
    }
}
