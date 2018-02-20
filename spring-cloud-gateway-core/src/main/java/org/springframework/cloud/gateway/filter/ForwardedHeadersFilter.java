/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.springframework.cloud.gateway.filter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jetbrains.annotations.Nullable;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

public class ForwardedHeadersFilter implements HttpHeadersFilter, Ordered {

	public static final String FORWARDED_HEADER = "Forwarded";

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public HttpHeaders filter(HttpHeaders original) {
		HttpHeaders updated = new HttpHeaders();

		// copy all headers except Forwarded
		original.entrySet().stream()
				.filter(entry -> !entry.getKey().toLowerCase().equalsIgnoreCase(FORWARDED_HEADER))
				.forEach(entry -> updated.addAll(entry.getKey(), entry.getValue()));

		//TODO: read Forwarded if exists
		List<Forwarded> forwardeds = parse(original.get(FORWARDED_HEADER));

		for (Forwarded f : forwardeds) {
			updated.add(FORWARDED_HEADER, f.toString());
		}

		return updated;
	}

    /* for testing */ static List<Forwarded> parse(List<String> values) {
		ArrayList<Forwarded> forwardeds = new ArrayList<>();
    	for (String value : values) {
			Forwarded forwarded = parse(value);
			forwardeds.add(forwarded);
		}
		return forwardeds;
	}

	/* for testing */ static Forwarded parse(String value) {
		String[] pairs = StringUtils.tokenizeToStringArray(value, ";");

		LinkedCaseInsensitiveMap<String> result = splitIntoCaseInsensitiveMap(pairs);
		if (result == null) return null;

		Forwarded forwarded = new Forwarded(result);

		return forwarded;
	}

	@Nullable
	/* for testing */ static LinkedCaseInsensitiveMap<String> splitIntoCaseInsensitiveMap(String[] pairs) {
		if (ObjectUtils.isEmpty(pairs)) {
			return null;
		}

		LinkedCaseInsensitiveMap<String> result = new LinkedCaseInsensitiveMap<>();
		for (String element : pairs) {
			String[] splittedElement = StringUtils.split(element, "=");
			if (splittedElement == null) {
				continue;
			}
			result.put(splittedElement[0].trim(), splittedElement[1].trim());
		}
		return result;
	}

	/* for testing */ static class Forwarded {

		private static final String COMMA = ", ";
		private static final char EQUALS = '=';
		private final Map<String, String> values;

		public Forwarded(Map<String, String> values) {
			this.values = values;
		}

		public String get(String key) {
			return this.values.get(key);
		}

		/* for testing */ Map<String, String> getValues() {
			return this.values;
		}

		@Override
		public String toString() {
			return "Forwarded{" +
					"values=" + this.values +
					'}';
		}

		private void appendList(StringBuilder s, String key, List<String> list) {
			boolean addComma = false;
			for (String value : list) {
				if (addComma) {
					s.append(COMMA);
				}
				s.append(key).append(EQUALS).append(value);
			}
		}
	}

}
