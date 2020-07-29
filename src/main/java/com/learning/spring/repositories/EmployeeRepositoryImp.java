package com.learning.spring.repositories;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.spring.models.Employee;

@Repository
public class EmployeeRepositoryImp implements EmployeeRepository {

	private static RestHighLevelClient restHighLevelClient;
	private static ObjectMapper objectMapper = new ObjectMapper();

	private static final String INDEX = "employee";

	public EmployeeRepositoryImp(@Value("${elasticsearch.host.server}") String host,
			@Value("${elasticsearch.host.port.one}") int portOne,
			@Value("${elasticsearch.host.port.two}") int portTwo,
			@Value("${elasticsearch.scheme}") String scheme) {
		makeConnection(host, portOne, portTwo, scheme);
	}

	public synchronized RestHighLevelClient makeConnection(String host, int portOne, int portTwo, String scheme) {

		if (restHighLevelClient == null) {
			restHighLevelClient = new RestHighLevelClient(
					RestClient.builder(new HttpHost(host, portOne, scheme), new HttpHost(host, portTwo, scheme)));
		}

		return restHighLevelClient;
	}

	public synchronized void closeConnection() throws IOException {
		restHighLevelClient.close();
		restHighLevelClient = null;
	}

	public Employee insertEmployee(Employee employee) throws IOException {
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("id", employee.getId());
		dataMap.put("name", employee.getName());
		dataMap.put("salary", employee.getSalary());
		IndexRequest indexRequest = new IndexRequest(INDEX).id(employee.getId()).source(dataMap);
		try {
			restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
		} catch (ElasticsearchException e) {
			e.getDetailedMessage();
		} catch (java.io.IOException ex) {
			ex.getLocalizedMessage();
		}
		closeConnection();
		return employee;
	}

	public Employee getEmployeeById(String id) throws IOException {
		GetRequest getEmployeeRequest = new GetRequest(INDEX, id);
		GetResponse getResponse = null;
		try {
			getResponse = restHighLevelClient.get(getEmployeeRequest, RequestOptions.DEFAULT);
		} catch (java.io.IOException e) {
			e.getLocalizedMessage();
		}
		closeConnection();
		return getResponse != null ? objectMapper.convertValue(getResponse.getSourceAsMap(), Employee.class) : null;
	}

	public Employee updateEmployeeById(String id, Employee employee) throws IOException  {
		UpdateRequest updateRequest = new UpdateRequest(INDEX, id).fetchSource(true); // Fetch Object after its update
		try {
			String employeeJson = objectMapper.writeValueAsString(employee);
			updateRequest.doc(employeeJson, XContentType.JSON);
			UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
			return objectMapper.convertValue(updateResponse.getGetResult().sourceAsMap(), Employee.class);
		} catch (JsonProcessingException e) {
			e.getMessage();
		} catch (java.io.IOException e) {
			e.getLocalizedMessage();
		}
		System.out.println("Updated Employee");
		closeConnection();
		return null;
	}

	public void deleteEmployeeById(String id) {
		DeleteRequest deleteRequest = new DeleteRequest(INDEX, id);
		try {
			restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
			closeConnection();
		} catch (java.io.IOException e) {
			e.getLocalizedMessage();
		}
	}

	public List<Employee> getAllEmployees() throws IOException {

		final Scroll scroll = new Scroll(TimeValue.timeValueSeconds(1L));
		SearchRequest searchRequest = new SearchRequest("employee");
		searchRequest.scroll(scroll);

		SearchResponse searchResponse = null;
		List<Employee> employees = new ArrayList<>();
		try {
			searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
		} catch (java.io.IOException e) {
			e.getLocalizedMessage();
		}
		SearchHit[] hits = searchResponse.getHits().getHits();
		Arrays.stream(hits).forEach(hit -> {
			employees.add(objectMapper.convertValue(hit.getSourceAsMap(), Employee.class));
		});
		closeConnection();
		return employees;
	}
}
