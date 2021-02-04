package ghast;

import lombok.experimental.UtilityClass;
import org.apache.commons.text.StringSubstitutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@UtilityClass
public class Messages {

	private final Map<String/*Key*/, String/*Template|Message*/> MESSAGES_MAP = new HashMap<>();

	//region Load messages

	/**
	 * Загрузка сообщений из {@link Properties}
	 *
	 * @param properties список сообщений и шаблонов
	 */
	public void load(Properties properties) {
		MESSAGES_MAP.clear();
		properties.forEach((key, value) -> MESSAGES_MAP.put(key.toString().trim().toLowerCase(), value.toString().trim()));
	}

	/**
	 * Загрузка сообщений из {@link Reader}.
	 * <p>
	 * Формат строк: {@code key=value}
	 * </p>
	 *
	 * @param reader {@link Reader} со списоком сообщений и шаблонов
	 */
	public void load(Reader reader) {
		MESSAGES_MAP.clear();
		try {
			BufferedReader bufferedReader = new BufferedReader(reader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				String[] split = line.split("=", 2);
				MESSAGES_MAP.put(split[0].trim().toLowerCase(), split[1].trim());
			}
		} catch (IOException e) {
			//TODO заменить на специализированный Exception
			throw new RuntimeException("Error load messages: " + e.getMessage(), e);
		}
	}

	/**
	 * Загрузка сообщений из {@link Map}<{@link String}, {@link String}>.
	 *
	 * @param messages список сообщений и шаблонов
	 */
	public void load(Map<String, String> messages) {
		MESSAGES_MAP.clear();
		MESSAGES_MAP.putAll(messages);
	}
	//endregion

	//region Get messages
	/**
	 * Получить обычное сообщение по ключу/коду.
	 *
	 * @param key ключ/код
	 * @return сообщение, если таковое задано. Иначе - ключ
	 */
	public String get(String key) {
		String keyLc = key.toLowerCase();
		return MESSAGES_MAP.getOrDefault(keyLc, keyLc);
	}

	/**
	 * Получить параметизированное сообщение по ключу/коду.
	 *
	 * @param key    ключ/код
	 * @param params список параметров
	 * @return сообщение, если таковое задано. Иначе - ключ
	 */
	public String get(String key, Map<String, Object> params) {
		String keyLc = key.toLowerCase();

		if (MESSAGES_MAP.containsKey(keyLc)) {
			return formatMessage(MESSAGES_MAP.get(keyLc), params);
		} else {
			return keyLc;
		}
	}

	/**
	 * Получить параметизированное сообщение по ключу/коду.
	 *
	 * @param key    ключ/код
	 * @param params чередующийся по парный список параметров: {@link String (str)param_name}, {@link Object (obj)param_value} и т.д.
	 * @return сообщение, если таковое задано. Иначе - ключ
	 */
	public String get(String key, Object... params) {
		String keyLc = key.toLowerCase();

		if (MESSAGES_MAP.containsKey(keyLc)) {
			int len;
			if ((params.length % 2) == 1) {
				len = params.length - 1;
			} else {
				len = params.length;
			}

			Map<String, Object> mapParams = new HashMap<>(len / 2);
			for (int i = 0; i < len; i = i + 2) {
				mapParams.put((String) params[i], params[i + 1]);
			}

			return formatMessage(MESSAGES_MAP.get(keyLc), mapParams);
		} else {
			return keyLc;
		}
	}
	//endregion

	private String formatMessage(String format, Map<String, Object> params) {
		return StringSubstitutor.replace(format, params, "{", "}");
	}
}
