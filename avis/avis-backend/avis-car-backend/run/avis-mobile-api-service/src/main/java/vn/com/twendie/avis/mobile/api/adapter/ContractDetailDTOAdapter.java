package vn.com.twendie.avis.mobile.api.adapter;

import com.google.common.base.Function;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.data.enumtype.ContractStatusEnum;
import vn.com.twendie.avis.data.enumtype.DriverContractStatusEnum;
import vn.com.twendie.avis.mobile.api.constant.MobileConstant;
import vn.com.twendie.avis.mobile.api.model.projection.ContractDetail;
import vn.com.twendie.avis.mobile.api.model.response.ContractDetailDTO;

import java.sql.Timestamp;
import java.util.Date;

@AllArgsConstructor
public class ContractDetailDTOAdapter implements Function<ContractDetail, ContractDetailDTO> {

    private DateUtils dateUtils;

    @Override
    public ContractDetailDTO apply(ContractDetail contractDetail) {
        return ContractDetailDTO.builder()
                .id(contractDetail.getId())
                .code(contractDetail.getCode())
                .customerName(contractDetail.getCustomerName())
                .memberName(contractDetail.getMemberName())
                .phoneNumber(StringUtils.isBlank(contractDetail.getMemberMobile()) ? contractDetail.getMobile()
                        : contractDetail.getMemberMobile())
                .address(contractDetail.getAddress())
                .contractPeriodType(contractDetail.getPeriodTypeName())
                .fromDate(contractDetail.getFromDatetime())
                .toDate(contractDetail.getToDatetime())
                .vehicleColor(contractDetail.getVehicleColor())
                .vehicleType(contractDetail.getVehicleType())
                .vehicleNumberSeat(contractDetail.getVehicleNumberSeat())
                .vehicleNumberPlate(contractDetail.getNumberPlate())
                .status(getDriverContractStatus(contractDetail))
                .countryCode(StringUtils.isBlank(contractDetail.getMemberCountryCode()) ? contractDetail.getCountryCode()
                        : contractDetail.getMemberCountryCode())
                .build();
    }

    private String getDriverContractStatus(ContractDetail contractDetail) {
        Timestamp now = new Timestamp(new Date().getTime());
        int compare1 = dateUtils.startOfDay(contractDetail.getFromDatetime()).compareTo(now);
        int compare2 = dateUtils.endOfDay(contractDetail.getToDatetime()).compareTo(now);
        if (contractDetail.getStatus().equals(ContractStatusEnum.CANCELED.getCode())) {
            return MobileConstant.DriverContractStatus.CANCELED;
        } else if (compare2 < 0) {
            return MobileConstant.DriverContractStatus.FINISHED;
        } else if (
                (contractDetail.getDriverContractStatus() != null
                        && DriverContractStatusEnum.POSTPONE.code().equals(contractDetail.getDriverContractStatus()))
                        || contractDetail.getDriverLend() || contractDetail.getVehicleLend()
        ) {
            return MobileConstant.DriverContractStatus.POST_PONE;
        } else {
            if (compare1 > 0) {
                return MobileConstant.DriverContractStatus.UPCOMING;
            } else {
                return MobileConstant.DriverContractStatus.IN_PROGRESS;
            }
        }
    }
}
