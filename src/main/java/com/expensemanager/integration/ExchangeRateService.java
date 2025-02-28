package com.expensemanager.integration;

import com.expensemanager.exception.ResourceNotFoundException;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class ExchangeRateService {
	private static final String EXCHANGE_API_URL = "https://api.exchangerate-api.com/v4/latest/USD";
	private final RestTemplate restTemplate = new RestTemplate();

	public double getExchangeRate(String fromCurrency, String toCurrency) {
		if (fromCurrency.equalsIgnoreCase(toCurrency)) {
			return 1.0;
		}

		ExchangeResponse response = restTemplate.getForObject(EXCHANGE_API_URL, ExchangeResponse.class);

		if (response == null || response.rates() == null) {
			log.error("Error while retrieving exchange rates from API");
			throw new ResourceNotFoundException("Error while retrieving exchange rates from API");
		}

		Double fromRate = response.rates().get(fromCurrency.toUpperCase());
		Double toRate = response.rates().get(toCurrency.toUpperCase());

		if (fromRate == null || toRate == null) {
			log.error("Not found exchange rates for {} and {}", fromCurrency, toCurrency);
			throw new ResourceNotFoundException("Exchange rate not found");
		}

		return (1 / fromRate) * toRate;
	}

	private record ExchangeResponse(
			String provider,
			@JsonProperty("WARNING_UPGRADE_TO_V6") String warningUpgradeToV6,
			String terms,
			String base,
			String date,
			long timeLastUpdated,
			Map<String, Double> rates
	) {
	}
}
