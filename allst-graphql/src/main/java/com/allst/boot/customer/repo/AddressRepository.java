package com.allst.boot.customer.repo;

import com.allst.boot.customer.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByCustomer_IdOrderByIdAsc(Long customerId);

    List<Address> findByCustomer_IdIn(Collection<Long> customerIds);

    Optional<Address> findByCustomer_IdAndDefaultAddress(Long customerId, boolean defaultAddress);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Address a set a.defaultAddress = false where a.customer.id = :customerId")
    void clearDefault(@Param("customerId") Long customerId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Address a where a.customer.id = :customerId")
    void deleteByCustomer_Id(@Param("customerId") Long customerId);
}
