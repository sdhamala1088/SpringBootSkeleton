package com.learning.spring.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.learning.spring.models.Employee;
import com.learning.spring.repositories.EmployeeRepositoryImp;



@RestController
public class EmployeeController {

	private EmployeeRepositoryImp employeeRepositoryImp;

	public EmployeeController(EmployeeRepositoryImp employeeRepositoryImp) {
		this.employeeRepositoryImp = employeeRepositoryImp;
	}

	// get all employees
	@GetMapping("/employees")
	public List<Employee> all() throws IOException {
		return employeeRepositoryImp.getAllEmployees();
	}

	// add an employee
	@PostMapping("/employee")
	public Employee newEmployee(@RequestBody Employee newEmployee) throws IOException {
		return employeeRepositoryImp.insertEmployee(newEmployee);
	}

	// get an employee by id
	@GetMapping("/employees/{id}")
	public Employee employee (@PathVariable String id) throws IOException {
		return employeeRepositoryImp.getEmployeeById(id);
	}

	// Update an employee
	@PutMapping("/employees/{id}")
	public void updateEmployee(@RequestBody Employee newEmployee, @PathVariable String id) throws IOException {
		employeeRepositoryImp.updateEmployeeById(id, newEmployee);
	}

	@DeleteMapping("/employees/{id}")
	public void deleteEmployee(@PathVariable String id) {
		employeeRepositoryImp.deleteEmployeeById(id);
	}

}
