package com.BajajSpring.Bajaj;

//import com.BajajSpring.Bajaj.Service.WebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class BajajApplication implements CommandLineRunner {
//	@Autowired
//	private WebhookService webhookService;
	public static void main(String[] args) {
		SpringApplication.run(BajajApplication.class, args);
	}
	@Override

	public void run(String... args) {
		RestTemplate restTemplate = new RestTemplate();

		// Step 1: Register Webhook
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		Map<String, Object> request = new HashMap<>();
		request.put("regNo", "REG12347");
		request.put("name", "Ishan Goyal");
		request.put("email", "ishan@example.com");

		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

		String url = "https://bfhldevapigw.healthrx.co.in/hiring/apis/webhook/register/JAVA";

		Map<String, String> response = restTemplate.postForObject(url, entity, Map.class);

		String webhookUrl = response.get("webhookurl");
		String accessToken = response.get("accessToken");

		System.out.println("Webhook URL: " + webhookUrl);
		System.out.println("Access Token: " + accessToken);

		// Step 2: If webhook received, solve SQL task and send it
		if (webhookUrl != null && !webhookUrl.isEmpty()) {
			String sqlQuery =
					"SELECT DISTINCT p.product_name\n" +
							"FROM Products p\n" +
							"JOIN Orders o ON p.product_id = o.product_id\n" +
							"WHERE o.order_date BETWEEN '2022-01-01' AND '2022-12-31'\n" +
							"GROUP BY p.product_name\n" +
							"HAVING COUNT(DISTINCT o.customer_id) >= 3;";

			Map<String, Object> solution = new HashMap<>();
			solution.put("regNo", "REG12347");
			solution.put("sql", sqlQuery);

			HttpHeaders sqlHeaders = new HttpHeaders();
			sqlHeaders.setContentType(MediaType.APPLICATION_JSON);
			sqlHeaders.setBearerAuth(accessToken);

			HttpEntity<Map<String, Object>> sqlEntity = new HttpEntity<>(solution, sqlHeaders);

			ResponseEntity<String> result = restTemplate.postForEntity(webhookUrl, sqlEntity, String.class);

			System.out.println("SQL Submission Response: " + result.getBody());
		} else {
			System.out.println("Webhook URL is null or empty. Aborting.");
		}
	}
}
