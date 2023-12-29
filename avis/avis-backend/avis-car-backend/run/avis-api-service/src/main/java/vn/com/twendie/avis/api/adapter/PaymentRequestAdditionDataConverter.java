package vn.com.twendie.avis.api.adapter;

import lombok.AllArgsConstructor;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.model.response.PaymentRequestAdditionDataWrapper;
import vn.com.twendie.avis.api.service.UserService;
import vn.com.twendie.avis.api.service.VehicleService;
import vn.com.twendie.avis.data.enumtype.MemberCustomerRoleEnum;
import vn.com.twendie.avis.data.model.Contract;
import vn.com.twendie.avis.data.model.MemberCustomer;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.data.model.Vehicle;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.function.Function;

@AllArgsConstructor
public class PaymentRequestAdditionDataConverter implements Function<Contract, PaymentRequestAdditionDataWrapper> {

    private final String title;
    private final Timestamp from;
    private final Timestamp to;
    private final String totalPriceInWords;
    private final UserService userService;
    private final VehicleService vehicleService;
    private final DateUtils dateUtils;
    private String nameFinds;
    @Override
    public PaymentRequestAdditionDataWrapper apply(Contract contract) {
        MemberCustomer memberCustomer = contract.getMemberCustomer();
        Timestamp lastDayOfMonth = dateUtils.getLastDayOfMonth(to);

        User driver = userService.getContractDriverAtTime(contract, lastDayOfMonth);
        Vehicle vehicle = vehicleService.getContractVehicleAtTime(contract, lastDayOfMonth);

        return PaymentRequestAdditionDataWrapper
                .builder()
                .title(title)
                .nameFinds(nameFinds)
                .customerName(Objects.isNull(contract.getCustomer()) ? "" : contract.getCustomer().getName())
                .adminName(Objects.isNull(memberCustomer) ? "" : memberCustomer.getRole()
                        .equals(MemberCustomerRoleEnum.ADMIN.getCode()) ? memberCustomer.getName() : "")
                .from(from)
                .to(to)
                .customerAddress(Objects.isNull(contract.getCustomer()) ? "" : contract.getCustomer().getAddress())
                .driverName(Objects.nonNull(driver) ? driver.getName() : null)
                .vehicleNumberPlate(Objects.nonNull(vehicle) ? vehicle.getNumberPlate() : null)
                .vehicleModel(Objects.nonNull(vehicle) ? vehicle.getModel() : null)
                .totalPriceInWords(totalPriceInWords)
                .build();
    }
}
