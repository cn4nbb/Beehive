package edu.cnan.beehive.core.config;

import edu.cnan.beehive.core.executor.support.NotifyConfig;
import lombok.Data;

/**
 * 作者：cnan
 * 开发时间：2026-06-08
 */
@Data
public class WebThreadPoolExecutorConfig {
    /**
     * 核心线程数
     */
    private Integer corePoolSize;

    /**
     * 最大线程数
     */
    private Integer maximumPoolSize;

    /**
     * 线程空闲存活时间（单位：秒）
     */
    private Long keepAliveTime;

    /**
     * 通知配置
     */
    private NotifyConfig notify;
}
