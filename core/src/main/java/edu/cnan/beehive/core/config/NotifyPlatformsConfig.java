package edu.cnan.beehive.core.config;

import lombok.Data;

/**
 * 作者：cnan
 * 开发时间：2026-06-08
 */
@Data
public class NotifyPlatformsConfig {
    /**
     * 通知类型，比如：DING
     */
    private String platform;

    /**
     * 完整 WebHook 地址
     */
    private String url;
}
