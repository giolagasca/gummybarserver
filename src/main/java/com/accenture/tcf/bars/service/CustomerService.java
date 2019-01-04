package com.accenture.tcf.bars.service;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.accenture.tcf.bars.domain.Customer;

@Component
public interface CustomerService {
	
	Optional<Customer> findById(int id);

}
