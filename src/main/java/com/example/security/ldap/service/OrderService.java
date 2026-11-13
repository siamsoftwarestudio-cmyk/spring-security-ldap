package com.example.security.ldap.service;

import com.example.security.ldap.dto.OrderRequest;
import com.example.security.ldap.dto.OrderResponse;
import com.example.security.ldap.dto.OrderStatusRequest;
import com.example.security.ldap.entity.Order;
import com.example.security.ldap.entity.Role;
import com.example.security.ldap.entity.User;
import com.example.security.ldap.exception.ApiException;
import com.example.security.ldap.mapper.OrderMapper;
import com.example.security.ldap.repository.OrderRepository;
import com.example.security.ldap.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        User current = currentUser();

        List<Order> orders = current.getRole() == Role.ADMINS
                ? orderRepository.findAll()
                : orderRepository.findAllByOwnerOrderByCreatedAtDesc(current);

        return orderMapper.toResponseList(orders);
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        Order order = getOrderOrThrow(id);
        ensureCanRead(order);
        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse create(OrderRequest request) {
        Order order = orderMapper.toEntity(request, currentUser());
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse update(Long id, OrderRequest request) {
        Order order = getOrderOrThrow(id);
        ensureCanModify(order);
        orderMapper.update(order, request);
        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatusRequest request) {
        Order order = getOrderOrThrow(id);
        order.setStatus(request.status());
        return orderMapper.toResponse(order);
    }

    @Transactional
    public void delete(Long id) {
        Order order = getOrderOrThrow(id);
        ensureCanModify(order);
        orderRepository.delete(order);
    }

    private Order getOrderOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ApiException("Order not found: " + id, HttpStatus.NOT_FOUND));
    }

    private void ensureCanRead(Order order) {
        User current = currentUser();
        if (current.getRole() != Role.ADMINS && !order.getOwner().getId().equals(current.getId())) {
            throw new ApiException("You can only view your own orders", HttpStatus.FORBIDDEN);
        }
    }

    private void ensureCanModify(Order order) {
        User current = currentUser();
        if (current.getRole() != Role.ADMINS && !order.getOwner().getId().equals(current.getId())) {
            throw new ApiException("You can only modify your own orders", HttpStatus.FORBIDDEN);
        }
    }

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException("Authenticated user not found", HttpStatus.UNAUTHORIZED));
    }
}
