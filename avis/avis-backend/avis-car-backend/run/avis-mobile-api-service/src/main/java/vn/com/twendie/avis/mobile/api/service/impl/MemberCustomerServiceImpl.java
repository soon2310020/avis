package vn.com.twendie.avis.mobile.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import vn.com.twendie.avis.data.model.MemberCustomer;
import vn.com.twendie.avis.mobile.api.repository.MemberCustomerRepo;
import vn.com.twendie.avis.mobile.api.service.MemberCustomerService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class MemberCustomerServiceImpl implements MemberCustomerService {

    @Autowired
    private MemberCustomerRepo memberCustomerRepo;

//    private MemberCustomerServiceImpl(MemberCustomerRepo memberCustomerRepo){
//        this.memberCustomerRepo = memberCustomerRepo;
//    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<String> generateMemberCode(int count) {
        String prefixCode = "US";
        String code = memberCustomerRepo.getNewestCodeByPrefix(prefixCode);
        List<String> result = new ArrayList<>();
        int suffixNumber;

        if (Objects.isNull(code)) {
            code = prefixCode + "0001";
            result.add(code);
            String suffixCode = code.split(prefixCode)[1];
            suffixNumber = Integer.parseInt(suffixCode);
        } else {
            String suffixCode = code.split(prefixCode)[1];
            suffixNumber = Integer.parseInt(suffixCode) + 1;
            code = String.format(prefixCode + "%04d", suffixNumber);
            result.add(code);
        }

        if (count > 1) {
            for (int i = 1; i < count; i++) {
                result.add(String.format(prefixCode + "%04d", suffixNumber + i));
            }
        }

        return result;
    }

    @Override
    public MemberCustomer save(MemberCustomer memberCustomer) {
        return memberCustomerRepo.save(memberCustomer);
    }

    @Override
    public int countByNameAndDepartment(String name, String department) {
        return memberCustomerRepo.countByNameAndDepartment(name, department);
    }

    @Override
    public Optional<MemberCustomer> findById(Long id) {
        return memberCustomerRepo.findById(id);
    }

    @Override
    public Optional<MemberCustomer> findByUserId(Long userId) {
        return memberCustomerRepo.findByUserId(userId);
    }

    @Override
    public List<MemberCustomer> findByUserIdIn(List<Long> userIds) {
        return memberCustomerRepo.findByUserIdIn(userIds);
    }


}
