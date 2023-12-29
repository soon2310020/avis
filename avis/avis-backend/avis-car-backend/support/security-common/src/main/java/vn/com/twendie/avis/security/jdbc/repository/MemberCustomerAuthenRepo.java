package vn.com.twendie.avis.security.jdbc.repository;

import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.twendie.avis.data.model.MemberCustomer;
import vn.com.twendie.avis.data.model.User;

public interface MemberCustomerAuthenRepo extends JpaRepository<MemberCustomer, Long> {
    MemberCustomer findFirstByUser(User user);
}
