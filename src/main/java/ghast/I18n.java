package ghast;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
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

/**
 * @deprecated use {@link Messages}
 */
@UtilityClass
@SuppressWarnings("unused")
@Deprecated
public class I18n {

    private final String DEFAULT_LANG = "en";
    private final Table<String/*Lang*/, String/*Key*/, String/*Template|Message*/> messagesMap = HashBasedTable.create();

    //region Load messages
    public void loadMessages(String lang, Reader reader) {
        Map<String, String> map = messagesMap.row(lang.toLowerCase());

        try {
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] split = line.split("=", 2);
                map.put(split[0].trim().toLowerCase(), split[1].trim());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error load messages: " + e.getMessage(), e);
        }
    }

    public void loadMessages(String lang, Map<String, String> messages) {
        Map<String, String> map = messagesMap.row(lang.toLowerCase());
        messages.forEach((k, v) -> map.put(k.toLowerCase(), v));
    }

    public void loadMessages(Reader reader) {
        loadMessages(DEFAULT_LANG, reader);
    }

    public void loadMessages(Map<String, String> messages) {
        loadMessages(DEFAULT_LANG, messages);
    }
    //endregion

    //region Get message
    public String get(String lang, String key) {
        return messagesMap.row(lang.toLowerCase()).getOrDefault(key.toLowerCase(), StringUtils.EMPTY);
    }

    public String get(String lang, String key, Map<String, Object> params) {
        return StringSubstitutor.replace(get(lang, key.toLowerCase()), params, "{", "}");
    }

    public String get(String key) {
        return get(DEFAULT_LANG, key);
    }

    public String get(String key, Map<String, Object> params) {
        return get(DEFAULT_LANG, key, params);
    }
    //endregion

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
