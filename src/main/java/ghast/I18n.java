package ghast;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
@SuppressWarnings("unused")
public class I18n {

	private final Map<String, String> messagesMap = new HashMap<>();

	@SuppressWarnings("java:S112")
	public void loadMessages(Reader reader) {
		try {
			BufferedReader bufferedReader = new BufferedReader(reader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				String[] split = line.split("=", 2);
				messagesMap.put(split[0].trim(), split[1].trim());
			}
		} catch (IOException e) {
			throw new RuntimeException("Error load messages: " + e.getMessage(), e);
		}
	}

	public void loadMessages(Map<String, String> messages) {
		messagesMap.putAll(messages);
	}

	public String getMessage(String key) {
		return messagesMap.getOrDefault(key, StringUtils.EMPTY);
	}

	public String getMessage(String key, Map<String, Object> params) {
		return StringSubstitutor.replace(getMessage(key), params, "{", "}");
	}

	public ParamBuilder paramBuilder() {
		return new ParamBuilder();
	}

	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	public static class ParamBuilder {

		private final Map<String, Object> params = new HashMap<>();

		public ParamBuilder add(String key, Object value) {
			params.put(key, value);
			return this;
		}

		public Map<String, Object> build() {
			return params;
		}
	}
}
