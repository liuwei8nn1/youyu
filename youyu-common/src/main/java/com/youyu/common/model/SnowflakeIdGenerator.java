package com.youyu.common.model;

/**
 * 雪花算法ID生成器
 * <p>
 * 基于Twitter的Snowflake算法实现，用于生成分布式环境下的唯一ID。
 * ID结构（64位长整型）：
 * <pre>
 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
 * |                                                                                           |
 * | 1位符号位(固定为0)                                                                         |
 * | 41位时间戳(毫秒级，可使用约69年)                                                           |
 * | 5位数据中心ID(支持32个数据中心)                                                             |
 * | 5位机器ID(每个数据中心支持32台机器)                                                         |
 * | 12位序列号(每毫秒最多生成4096个ID)                                                          |
 * </pre>
 * <p>
 * 自定义起始时间(epoch)：2026-01-01 00:00:00
 *
 * @since 2026-01-01
 */
public class SnowflakeIdGenerator {

    /**
     * 自定义起始时间：2026-01-01 00:00:00 (毫秒时间戳)
     */
    private static final long CUSTOM_EPOCH = 1767225600000L;

    /**
     * 机器ID所占位数
     */
    private static final long WORKER_ID_BITS = 5L;

    /**
     * 数据中心ID所占位数
     */
    private static final long DATA_CENTER_ID_BITS = 5L;

    /**
     * 序列号所占位数
     */
    private static final long SEQUENCE_BITS = 12L;

    /**
     * 最大机器ID (2^5 - 1 = 31)
     */
    private static final int MAX_WORKER_ID = ~(-1 << WORKER_ID_BITS);

    /**
     * 最大数据中心ID (2^5 - 1 = 31)
     */
    private static final int MAX_DATA_CENTER_ID = ~(-1 << DATA_CENTER_ID_BITS);

    /**
     * 获取最大机器ID
     *
     * @return 最大机器ID (31)
     */
    public static int getMaxWorkerId() {
        return MAX_WORKER_ID;
    }

    /**
     * 获取最大数据中心ID
     *
     * @return 最大数据中心ID (31)
     */
    public static int getMaxDataCenterId() {
        return MAX_DATA_CENTER_ID;
    }

    /**
     * 序列号掩码 (2^12 - 1 = 4095)
     */
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    /**
     * 机器ID左移位数 (12位)
     */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /**
     * 数据中心ID左移位数 (12 + 5 = 17位)
     */
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /**
     * 时间戳左移位数 (12 + 5 + 5 = 22位)
     */
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;

    /**
     * 数据中心ID
     */
    private final long dataCenterId;

    /**
     * 机器ID
     */
    private final long workerId;

    /**
     * 毫秒内序列号
     */
    private long sequence = 0L;

    /**
     * 上次生成ID的时间戳
     */
    private long lastTimestamp = -1L;

    /**
     * 构造函数
     *
     * @param workerId     机器ID (0 ~ 31)
     * @param dataCenterId 数据中心ID (0 ~ 31)
     * @throws IllegalArgumentException 当workerId或dataCenterId超出范围时抛出
     */
    public SnowflakeIdGenerator(long workerId, long dataCenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format("Worker ID can't be greater than %d or less than 0", MAX_WORKER_ID)
            );
        }
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException(
                    String.format("Data Center ID can't be greater than %d or less than 0", MAX_DATA_CENTER_ID)
            );
        }
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }

    /**
     * 获取下一个唯一ID
     *
     * @return 生成的雪花ID
     * @throws RuntimeException 当时钟回拨时抛出异常
     */
    public synchronized long nextId() {
        long timestamp = getCurrentTimestamp();

        // 时钟回拨检测
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("Clock moved backwards. Refusing to generate id for %d milliseconds",
                            lastTimestamp - timestamp)
            );
        }

        // 如果是同一毫秒内生成的，则递增序列号
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            // 序列号溢出，等待下一毫秒
            if (sequence == 0) {
                timestamp = waitForNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒，序列号重置为0
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        // 组装ID：时间戳部分 | 数据中心部分 | 机器ID部分 | 序列号部分
        return ((timestamp - CUSTOM_EPOCH) << TIMESTAMP_LEFT_SHIFT)
                | (dataCenterId << DATA_CENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /**
     * 等待下一毫秒
     *
     * @param lastTimestamp 上次生成ID的时间戳
     * @return 下一毫秒的时间戳
     */
    protected long waitForNextMillis(long lastTimestamp) {
        long timestamp = getCurrentTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentTimestamp();
        }
        return timestamp;
    }

    /**
     * 获取当前系统时间戳（毫秒）
     *
     * @return 当前时间戳
     */
    protected long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 解析雪花ID，提取时间戳部分
     *
     * @param snowflakeId 雪花ID
     * @return 生成该ID时的时间戳（相对于CUSTOM_EPOCH的毫秒数）
     */
    public static long parseTimestamp(long snowflakeId) {
        return (snowflakeId >> TIMESTAMP_LEFT_SHIFT) + CUSTOM_EPOCH;
    }

    /**
     * 解析雪花ID，提取数据中心ID
     *
     * @param snowflakeId 雪花ID
     * @return 数据中心ID
     */
    public static long parseDataCenterId(long snowflakeId) {
        return (snowflakeId >> DATA_CENTER_ID_SHIFT) & MAX_DATA_CENTER_ID;
    }

    /**
     * 解析雪花ID，提取机器ID
     *
     * @param snowflakeId 雪花ID
     * @return 机器ID
     */
    public static long parseWorkerId(long snowflakeId) {
        return (snowflakeId >> WORKER_ID_SHIFT) & MAX_WORKER_ID;
    }

    /**
     * 解析雪花ID，提取序列号
     *
     * @param snowflakeId 雪花ID
     * @return 序列号
     */
    public static long parseSequence(long snowflakeId) {
        return snowflakeId & SEQUENCE_MASK;
    }
}
