package ghast.database;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import ghast.GhastTools;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.plugin.Plugin;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    static final String JDBC_USER = "sa";
    static final String JDBC_PASSWORD = "";
    static final String JDBC_DB_NAME = "in_mem_db";
    static final String JDBC_URL = "jdbc:h2:mem:" + JDBC_DB_NAME + ";DB_CLOSE_DELAY=-1";

    static final String TABLE_NAME = "TEST_TABLE";
    static final String COLUMN_ID = "ID";
    static final String COLUMN_NAME = "C_NAME";
    static final String COLUMN_VALUE = "C_VALUE";

    static final Object[][] DATA = new Object[][]{
            { "Player 1", 100 }, { "Player 2", 250 },
            { "Player 3", 0 }, { "Player 4", 780 }
    };

    static DataSource dataSource;
    JdbcTemplate jdbcTemplate;

    @BeforeAll
    static void beforeAll() {
        Logger logger = Logger.getLogger(JdbcTemplateTest.class.getName());

        Plugin mockPlugin = mock(Plugin.class);
        when(mockPlugin.getLogger()).thenReturn(logger);

        GhastTools.setPlugin(mockPlugin);

        JdbcDataSource jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUser(JDBC_USER);
        jdbcDataSource.setPassword(JDBC_PASSWORD);
        jdbcDataSource.setURL(JDBC_URL);

        dataSource = jdbcDataSource;
    }

    @BeforeEach
    void before() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        createTable();

        String sql_head = MessageFormat.format("INSERT INTO {0} ({1}, {2}) VALUES ",
                TABLE_NAME, COLUMN_NAME, COLUMN_VALUE);
        StringJoiner sql_sj = new StringJoiner(", ");
        for (Object[] datum : DATA) {
            sql_sj.add(MessageFormat.format("( ''{0}'', {1} )", datum[0], datum[1]));
        }

        jdbcTemplate.execute(sql_head + sql_sj.toString());
    }

    @AfterEach
    void after() {
        dropTable();
    }

    @Test
    void testQuery_Simple_Single() {
        String sql = MessageFormat.format("SELECT {2} FROM {0} WHERE {1} LIKE ''{3}''",
                TABLE_NAME, COLUMN_NAME, COLUMN_VALUE, DATA[0][0]);

        Integer value = jdbcTemplate.query(sql, rs -> {
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return null;
            }
        });

        assertEquals(DATA[0][1], value);
    }

    @Test
    void testQuery_Simple_Optional() {
        String sql = MessageFormat.format("SELECT {2} FROM {0} WHERE {1} LIKE ''{3}''",
                TABLE_NAME, COLUMN_NAME, COLUMN_VALUE, DATA[0][0]);

        Optional<Integer> optValue = jdbcTemplate.queryOne(sql, rs -> rs.getInt(1));

        assertTrue(optValue.isPresent());
        assertEquals(DATA[0][1], optValue.get());
    }

    @Test
    void testQuery_Simple_List() {
        String sql = MessageFormat.format("SELECT {2} FROM {0} WHERE {1} LIKE ''{3}'' OR {1} LIKE ''{4}''",
                TABLE_NAME, COLUMN_NAME, COLUMN_VALUE, DATA[0][0], DATA[1][0]);

        List<Integer> listValues = jdbcTemplate.queryList(sql, (rs, rowNum) -> rs.getInt(1));

        assertIterableEquals(Lists.newArrayList(DATA[0][1], DATA[1][1]), listValues);
    }

    @Test
    void testQuery_Object_Single() {
        String sql = MessageFormat.format("SELECT {1}, {2} FROM {0} WHERE {1} LIKE ''{3}''",
                TABLE_NAME, COLUMN_NAME, COLUMN_VALUE, DATA[0][0]);

        Player actualPlayer = jdbcTemplate.query(sql, rs -> {
            if (rs.next()) {
                Player player0 = new Player();
                player0.name = rs.getString(COLUMN_NAME);
                player0.value = rs.getInt(COLUMN_VALUE);

                return player0;
            } else {
                return null;
            }
        });

        Player expectedPlayer = new Player();
        expectedPlayer.name = (String) DATA[0][0];
        expectedPlayer.value = (int) DATA[0][1];

        assertEquals(expectedPlayer, actualPlayer);
    }

    @Test
    void testQuery_Object_List() {
        String sql = MessageFormat.format("SELECT {1}, {2} FROM {0}",
                TABLE_NAME, COLUMN_NAME, COLUMN_VALUE);

        List<Player> actualPlayers = jdbcTemplate.queryList(sql, (rs, num) -> {
            Player player0 = new Player();
            player0.name = rs.getString(COLUMN_NAME);
            player0.value = rs.getInt(COLUMN_VALUE);

            return player0;
        });


        List<Player> expectedPlayers = Stream.of(DATA)
                .map(datum -> {
                    Player player1 = new Player();
                    player1.name = (String) datum[0];
                    player1.value = (int) datum[1];

                    return player1;
                })
                .collect(Collectors.toList());


        assertIterableEquals(expectedPlayers, actualPlayers);
    }

    @Test
    void testQueryForMap() {
        String sql = MessageFormat.format("SELECT {1}, {2} FROM {0} WHERE {1} LIKE ''{3}''",
                TABLE_NAME, COLUMN_NAME, COLUMN_VALUE, DATA[0][0]);

        Map<String, Object> actualMap = jdbcTemplate.queryForMap(sql);

        Map<String, Object> expectedMap = ImmutableMap.of(
                COLUMN_NAME, DATA[0][0],
                COLUMN_VALUE, DATA[0][1]);

        assertIterableEquals(expectedMap.entrySet(), actualMap.entrySet());
    }

    @Test
    void testQueryForMapList() {
        String sql = MessageFormat.format("SELECT {1}, {2} FROM {0}",
                TABLE_NAME, COLUMN_NAME, COLUMN_VALUE);

        List<Map<String, Object>> actualMapList = jdbcTemplate.queryForMapList(sql);

        List<Map<String, Object>> expectedMapList = Stream.of(DATA)
                .map(datum -> ImmutableMap.of(COLUMN_NAME, datum[0], COLUMN_VALUE, datum[1]))
                .collect(Collectors.toList());

        assertIterableEquals(expectedMapList, actualMapList);

    }

    @Test
    void testUpdate() {
        String newName = "Player X";
        String sql = MessageFormat.format("UPDATE {0} SET {1} = ''{3}'' WHERE {1} LIKE ''{2}''",
                TABLE_NAME, COLUMN_NAME, DATA[0][0], newName);

        int rows = jdbcTemplate.update(sql);

        assertEquals(1, rows);

        sql = MessageFormat.format("SELECT {2} FROM {0} WHERE {1} LIKE ''{3}''",
                TABLE_NAME, COLUMN_NAME, COLUMN_VALUE, newName);

        Integer value = jdbcTemplate.query(sql, rs -> {
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return null;
            }
        });

        assertEquals(DATA[0][1], value);
    }

    private void createTable() {
        jdbcTemplate.execute("CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " bigint auto_increment," +
                COLUMN_NAME + " varchar(16)," +
                COLUMN_VALUE + " integer)");
    }

    private void dropTable() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    class Player {
        String name;
        int value;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Player)) return false;
            Player player = (Player) o;
            return new EqualsBuilder().append(value, player.value).append(name, player.name).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(name).append(value).toHashCode();
        }
    }

    @Nested
    class JdbcTemplateTest_ExecuteTestCase {

        @BeforeEach
        void before() {
            jdbcTemplate = new JdbcTemplate(dataSource);
            dropTable();
        }

        @AfterEach
        void after() {
            dropTable();
        }

        @Test
        void test() throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException {
            createTable();

            //region Check result
            Class.forName("org.h2.Driver").newInstance();
            Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
            ResultSet resultSet = connection.getMetaData().getTables(JDBC_DB_NAME.toUpperCase(), "PUBLIC",
                    TABLE_NAME.toUpperCase(), new String[]{"TABLE"});

            assertTrue(resultSet.next());

            resultSet.close();
            connection.close();
            //endregion
        }
    }
}