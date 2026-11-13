package com.example.security.ldap.repository;

import com.example.security.ldap.entity.Order;
import com.example.security.ldap.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByOwnerOrderByCreatedAtDesc(User owner);
}
