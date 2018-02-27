package org.springframework.cloud.gateway.filter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.List;

@FunctionalInterface
public interface HttpHeadersFilter {

	HttpHeaders filter(ServerHttpRequest request);

	static HttpHeaders filter(List<HttpHeadersFilter> filters, ServerHttpRequest request) {
		HttpHeaders filtered = request.getHeaders();
		if (filters != null) {
			for (HttpHeadersFilter filter: filters) {
				filtered = filter.filter(request);
			}
		}
		return filtered;
	}
}
