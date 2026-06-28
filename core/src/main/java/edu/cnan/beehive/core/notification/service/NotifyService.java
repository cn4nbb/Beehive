package edu.cnan.beehive.core.notification.service;

import edu.cnan.beehive.core.notification.dto.ThreadPoolAlarmNotifyDTO;
import edu.cnan.beehive.core.notification.dto.ThreadPoolConfigChangeDTO;
import edu.cnan.beehive.core.notification.dto.WebThreadPoolConfigChangeDTO;

/**
 * Strategy interface for sending thread-pool notifications.
 *
 * <p>Implementations handle specific platforms (DingTalk, Lark, etc.).
 * Methods do not declare checked exceptions — implementations
 * catch and log failures internally.
 *
 * @author cnan
 */
public interface NotifyService {
    /** Sends a thread-pool configuration-change notification. */
    void sendChangeMessage(ThreadPoolConfigChangeDTO configChange);

    /** Sends a web-container thread-pool configuration-change notification. */
    void sendWebChangeMessage(WebThreadPoolConfigChangeDTO configChange);

    /** Sends a runtime alarm notification (rate-limited by the dispatcher). */
    void sendAlarmMessage(ThreadPoolAlarmNotifyDTO alarm);
}
