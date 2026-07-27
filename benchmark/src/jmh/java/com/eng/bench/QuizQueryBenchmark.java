package com.eng.bench;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * "오답 퀴즈 랜덤 조회" 3가지 방식의 순수 SQL 실행 시간을 JMH로 측정한다.
 *
 *  1) orderByRand        : SELECT ... ORDER BY RAND() LIMIT 10   (개선 전)
 *  2) rowNumberMod       : ROW_NUMBER() + mod(r, rand()) LIMIT 10 (블로그의 개선안 / 현재 findRandomQuiz)
 *  3) countThenRowNumIn  : count 후 Java에서 순번을 뽑아 r IN (...) 로 조회 (새 제안 방식)
 *
 * 실제 quiz 테이블은 건드리지 않고, 벤치마크 전용 quiz_bench 테이블을 만들어
 * BENCH_USER_ID 로 rowCount 개의 오답 데이터를 채운 뒤 측정하고, 끝나면 테이블을 삭제한다.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 6, time = 1)
@Fork(2)
public class QuizQueryBenchmark {

    private static final long BENCH_USER_ID = 999_999L;
    private static final int QUIZ_SIZE = 10;

    @Param({"100", "1000", "10000", "100000"})
    private int rowCount;

    private Connection conn;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        String url = System.getProperty("bench.jdbcUrl");
        String user = System.getProperty("bench.user");
        String password = System.getProperty("bench.password");
        conn = DriverManager.getConnection(url, user, password);
        conn.setAutoCommit(false);

        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS quiz_bench (" +
                    " id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                    " user_id BIGINT," +
                    " correct TINYINT(1)," +
                    " KEY idx_user_correct (user_id, correct))");
            st.execute("DELETE FROM quiz_bench WHERE user_id = " + BENCH_USER_ID);
        }
        conn.commit();

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO quiz_bench (user_id, correct) VALUES (?, 0)")) {
            for (int i = 1; i <= rowCount; i++) {
                ps.setLong(1, BENCH_USER_ID);
                ps.addBatch();
                if (i % 2000 == 0) {
                    ps.executeBatch();
                }
            }
            ps.executeBatch();
        }
        conn.commit();
    }

    @TearDown(Level.Trial)
    public void tearDown() throws Exception {
        try (Statement st = conn.createStatement()) {
            st.execute("DELETE FROM quiz_bench WHERE user_id = " + BENCH_USER_ID);
        }
        conn.commit();
        conn.close();
    }

    /** 1) ORDER BY RAND() — 개선 전 */
    @Benchmark
    public void orderByRand(Blackhole bh) throws Exception {
        String sql = "SELECT id FROM quiz_bench" +
                " WHERE correct = false AND user_id = ?" +
                " ORDER BY RAND() LIMIT " + QUIZ_SIZE;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, BENCH_USER_ID);
            fetch(ps, bh);
        }
    }

    /** 2) ROW_NUMBER() + mod(r, rand()) — 블로그의 개선안 / 현재 findRandomQuiz */
    @Benchmark
    public void rowNumberMod(Blackhole bh) throws Exception {
        String sql = "SELECT * FROM (" +
                " SELECT row_number() over(order by id) r, qb.*" +
                " FROM quiz_bench qb" +
                " WHERE correct = false AND user_id = ?) sub" +
                " WHERE mod(r, floor(rand() * 3) + 1) = 0 LIMIT " + QUIZ_SIZE;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, BENCH_USER_ID);
            fetch(ps, bh);
        }
    }

    /** 3) count → Java에서 순번 추출 → r IN (...) — 새 제안 방식 */
    @Benchmark
    public void countThenRowNumIn(Blackhole bh) throws Exception {
        long total;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT count(*) FROM quiz_bench WHERE correct = false AND user_id = ?")) {
            ps.setLong(1, BENCH_USER_ID);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                total = rs.getLong(1);
            }
        }
        if (total == 0) {
            return;
        }

        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= total; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers);
        List<Integer> picked = new ArrayList<>(
                numbers.subList(0, (int) Math.min(total, QUIZ_SIZE)));

        StringBuilder in = new StringBuilder();
        for (int i = 0; i < picked.size(); i++) {
            in.append(i == 0 ? "?" : ",?");
        }
        String sql = "SELECT * FROM (" +
                " SELECT row_number() over(order by id) r, qb.*" +
                " FROM quiz_bench qb" +
                " WHERE correct = false AND user_id = ?) sub" +
                " WHERE sub.r IN (" + in + ")";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, BENCH_USER_ID);
            for (int i = 0; i < picked.size(); i++) {
                ps.setInt(2 + i, picked.get(i));
            }
            fetch(ps, bh);
        }
        // IN 결과 순서가 보장되지 않으므로 실제 서비스처럼 한 번 더 섞는다
        Collections.shuffle(picked);
        bh.consume(picked);
    }

    private void fetch(PreparedStatement ps, Blackhole bh) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            int n = 0;
            while (rs.next()) {
                bh.consume(rs.getLong(1));
                n++;
            }
            bh.consume(n);
        }
    }
}
