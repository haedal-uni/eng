package com.eng.bench;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.sql.*;
import java.util.concurrent.TimeUnit;

/**
 * Quiz 저장을 saveAll() 방식과 Batch Insert 방식으로 나눠 실행 시간을 JMH로 측정한다.
 *
 *  1) saveAll        : 행마다 INSERT 를 한 번씩 실행 (IDENTITY 전략이라 JPA saveAll 이 배치가 안 되는 상황과 동일)
 *  2) batchInsert    : PreparedStatement addBatch/executeBatch (앱과 동일하게 rewriteBatchedStatements 없음)
 *  3) batchRewrite   : addBatch/executeBatch + rewriteBatchedStatements=true (드라이버가 실제로 묶어서 보내는 경우)
 *
 * 실제 quiz 테이블은 건드리지 않고 전용 quiz_bench 테이블에 넣고, 매 실행 후 비운다.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class QuizInsertBenchmark {

    private static final long STUDY_ID = 1L;
    private static final long USER_ID = 1L;
    private static final String INSERT =
            "INSERT INTO quiz_bench (study_id, correct, user_id) VALUES (?, ?, ?)";

    @Param({"10", "100", "1000", "10000"})
    private int rowCount;

    private Connection conn;         // 앱과 동일 (rewriteBatchedStatements 없음)
    private Connection connRewrite;  // rewriteBatchedStatements=true

    @Setup(Level.Trial)
    public void setup() throws Exception {
        String url = System.getProperty("bench.jdbcUrl");
        String user = System.getProperty("bench.user");
        String password = System.getProperty("bench.password");

        conn = DriverManager.getConnection(url, user, password);
        conn.setAutoCommit(false);
        connRewrite = DriverManager.getConnection(withRewrite(url), user, password);
        connRewrite.setAutoCommit(false);

        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS quiz_bench (" +
                    " id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                    " study_id BIGINT," +
                    " correct TINYINT(1)," +
                    " user_id BIGINT)");
            st.execute("TRUNCATE TABLE quiz_bench");
        }
        conn.commit();
    }

    @TearDown(Level.Invocation)
    public void clear() throws Exception {
        try (Statement st = conn.createStatement()) {
            st.execute("TRUNCATE TABLE quiz_bench");
        }
        conn.commit();
    }

    @TearDown(Level.Trial)
    public void tearDown() throws Exception {
        try (Statement st = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS quiz_bench");
        }
        conn.commit();
        conn.close();
        connRewrite.close();
    }

    /** 1) saveAll 방식 — 행마다 INSERT 를 한 번씩 실행 */
    @Benchmark
    public void saveAll(Blackhole bh) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(INSERT)) {
            for (int i = 0; i < rowCount; i++) {
                ps.setLong(1, STUDY_ID);
                ps.setBoolean(2, false);
                ps.setLong(3, USER_ID);
                bh.consume(ps.executeUpdate());
            }
        }
        conn.commit();
    }

    /** 2) Batch Insert — addBatch/executeBatch (앱과 동일 설정) */
    @Benchmark
    public void batchInsert(Blackhole bh) throws Exception {
        batch(conn, bh);
    }

    /** 3) Batch Insert + rewriteBatchedStatements=true */
    @Benchmark
    public void batchRewrite(Blackhole bh) throws Exception {
        batch(connRewrite, bh);
    }

    private void batch(Connection c, Blackhole bh) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(INSERT)) {
            for (int i = 0; i < rowCount; i++) {
                ps.setLong(1, STUDY_ID);
                ps.setBoolean(2, false);
                ps.setLong(3, USER_ID);
                ps.addBatch();
            }
            bh.consume(ps.executeBatch());
        }
        c.commit();
    }

    private static String withRewrite(String url) {
        return url + (url.contains("?") ? "&" : "?") + "rewriteBatchedStatements=true";
    }
}
