package edu.cnan.beehive.core.config;

import lombok.Data;

/**
 * 作者：cnan
 * 开发时间：2026-06-08
 */
@Data
public class MonitorConfig {
    /**
     * 默认开启监控配置
     */
    private Boolean enable = Boolean.TRUE;

    /**
     * 监控类型
     */
    private String collectType = "micrometer";

    /**
     * 采集间隔，默认 10 秒
     */
    private Long collectInterval = 10L;
}
