package com.learning.spring.repositories;

import java.io.IOException;

import com.learning.spring.models.Employee;

public interface EmployeeRepository {

	Employee insertEmployee(Employee employee) throws IOException;
	Employee getEmployeeById(String id) throws IOException;
	Employee updateEmployeeById(String id, Employee employee) throws IOException;
	
}
