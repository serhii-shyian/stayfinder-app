package com.example.stayfinder.repository.address;

import com.example.stayfinder.model.Address;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
    Optional<Address> findByAddress(String address);
}
