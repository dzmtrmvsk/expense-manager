package com.expensemanager.filter;

import com.expensemanager.service.VisitCounterService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsFilter implements Filter {

	private final VisitCounterService visitCounterService;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (request instanceof HttpServletRequest httpRequest) {
			String uri = httpRequest.getRequestURI();
			visitCounterService.recordVisit(uri);
		}
		chain.doFilter(request, response);
	}
}
