package edu.cnan.beehive.core.notification.service;

import edu.cnan.beehive.core.config.BootstrapConfigProperties;
import edu.cnan.beehive.core.config.NotifyPlatformsConfig;
import edu.cnan.beehive.core.notification.dto.ThreadPoolAlarmNotifyDTO;
import edu.cnan.beehive.core.notification.dto.ThreadPoolConfigChangeDTO;
import edu.cnan.beehive.core.notification.dto.WebThreadPoolConfigChangeDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Facade that routes notifications to the appropriate platform
 * implementation (DingTalk, etc.) based on the configured
 * {@link NotifyPlatformsConfig#getPlatform()}.
 *
 * <p>Before dispatching alarm messages, applies rate limiting via
 * {@link AlarmRateLimiter}.  Configuration-change messages are
 * forwarded immediately (no rate limit).
 *
 * <p>New platforms are registered in the {@code static} block:
 * <pre>{@code
 * NOTIFY_SERVICE_MAP.put("DING", new DingTalkMessageService());
 * // NOTIFY_SERVICE_MAP.put("LARK", new LarkMessageService());
 * }</pre>
 *
 * @author cnan
 */
public class NotifyDispatcher implements NotifyService{
    /** Platform name → service instance. */
    private static final Map<String, NotifyService> NOTIFY_SERVICE_MAP = new HashMap<>();

    static {
        NOTIFY_SERVICE_MAP.put("DING", new DingTalkMessageService());
    }

    @Override
    public void sendChangeMessage(ThreadPoolConfigChangeDTO configChange) {
        getNotifierService().ifPresent(service -> service.sendChangeMessage(configChange));
    }

    @Override
    public void sendWebChangeMessage(WebThreadPoolConfigChangeDTO configChange) {
        getNotifierService().ifPresent(service -> service.sendWebChangeMessage(configChange));
    }

    /**
     * Sends an alarm notification, subject to rate limiting.
     * If the alarm is suppressed, the {@code supplier} on the DTO
     * is never evaluated, avoiding unnecessary lock acquisition
     * on the thread-pool executor.
     */
    @Override
    public void sendAlarmMessage(ThreadPoolAlarmNotifyDTO alarm) {
        getNotifierService().ifPresent(service -> {
            boolean allowSend = AlarmRateLimiter.allowAlarm(
                    alarm.getThreadPoolId(),
                    alarm.getAlarmType(),
                    alarm.getInterval());

            if (allowSend) {
                service.sendAlarmMessage(alarm.resolve());
            }
        });
    }

    /** Resolves the configured platform to its service instance. */
    private Optional<NotifyService> getNotifierService() {
        return Optional.ofNullable(BootstrapConfigProperties.getInstance().getNotifyPlatforms())
                .map(NotifyPlatformsConfig::getPlatform)
                .map(platform -> NOTIFY_SERVICE_MAP.get(platform));
    }
}
