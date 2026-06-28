package edu.cnan.beehive.core.notification.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import edu.cnan.beehive.core.config.BootstrapConfigProperties;
import edu.cnan.beehive.core.notification.dto.ThreadPoolAlarmNotifyDTO;
import edu.cnan.beehive.core.notification.dto.ThreadPoolConfigChangeDTO;
import edu.cnan.beehive.core.notification.dto.WebThreadPoolConfigChangeDTO;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.cnan.beehive.core.constant.Constants.*;

/**
 * Sends notifications via DingTalk (钉钉) custom-robot webhook.
 *
 * <p>All three notification types are sent as Markdown messages
 * via HTTP POST to the URL configured in
 * {@link BootstrapConfigProperties#getNotifyPlatforms()}.
 *
 * @author cnan
 */
public class DingTalkMessageService implements NotifyService{
    private static final Logger log = LoggerFactory.getLogger(DingTalkMessageService.class);

    @Override
    public void sendChangeMessage(ThreadPoolConfigChangeDTO configChange) {
        Map<String, ThreadPoolConfigChangeDTO.ChangePair<?>> changes = configChange.getChanges();
        String text = String.format(
                DING_CONFIG_CHANGE_MESSAGE_TEXT,
                configChange.getActiveProfile().toUpperCase(),
                configChange.getThreadPoolId(),
                configChange.getIdentify() + ":" + configChange.getApplicationName(),
                changes.get("corePoolSize").getBefore() + " ➲ " + changes.get("corePoolSize").getAfter(),
                changes.get("maximumPoolSize").getBefore() + " ➲ " + changes.get("maximumPoolSize").getAfter(),
                changes.get("keepAliveTime").getBefore() + " ➲ " + changes.get("keepAliveTime").getAfter(),
                configChange.getWorkQueue(),
                changes.get("queueCapacity").getBefore() + " ➲ " + changes.get("queueCapacity").getAfter(),
                changes.get("rejectedHandler").getBefore(),
                changes.get("rejectedHandler").getAfter(),
                configChange.getReceives(),
                configChange.getUpdateTime()
        );

        List<String> atMobiles = CollectionUtil.newArrayList(configChange.getReceives().split(","));
        sendDingTalkMarkdownMessage("动态线程池通知", text, atMobiles);
    }

    @Override
    public void sendWebChangeMessage(WebThreadPoolConfigChangeDTO configChange) {
        Map<String, WebThreadPoolConfigChangeDTO.ChangePair<?>> changes = configChange.getChanges();
        String webContainerName = configChange.getWebContainerName();
        String text = String.format(
                DING_CONFIG_WEB_CHANGE_MESSAGE_TEXT,
                configChange.getActiveProfile().toUpperCase(),
                webContainerName,
                configChange.getIdentify() + ":" + configChange.getApplicationName(),
                changes.get("corePoolSize").getBefore() + " ➲ " + changes.get("corePoolSize").getAfter(),
                changes.get("maximumPoolSize").getBefore() + " ➲ " + changes.get("maximumPoolSize").getAfter(),
                changes.get("keepAliveTime").getBefore() + " ➲ " + changes.get("keepAliveTime").getAfter(),
                configChange.getReceives(),
                webContainerName,
                configChange.getUpdateTime()
        );

        List<String> atMobiles = CollectionUtil.newArrayList(configChange.getReceives().split(","));
        sendDingTalkMarkdownMessage(webContainerName + "线程池通知", text, atMobiles);
    }

    @Override
    public void sendAlarmMessage(ThreadPoolAlarmNotifyDTO alarm) {
        String text = String.format(
                DING_ALARM_NOTIFY_MESSAGE_TEXT,
                alarm.getActiveProfile().toUpperCase(),
                alarm.getThreadPoolId(),
                alarm.getIdentify() + ":" + alarm.getApplicationName(),
                alarm.getAlarmType(),
                alarm.getCorePoolSize(),
                alarm.getMaximumPoolSize(),
                alarm.getCurrentPoolSize(),
                alarm.getActivePoolSize(),
                alarm.getLargestPoolSize(),
                alarm.getCompletedTaskCount(),
                alarm.getWorkQueueName(),
                alarm.getWorkQueueCapacity(),
                alarm.getWorkQueueSize(),
                alarm.getWorkQueueRemainingCapacity(),
                alarm.getRejectedHandlerName(),
                alarm.getRejectCount(),
                alarm.getReceives(),
                alarm.getInterval(),
                alarm.getCurrentTime()
        );

        List<String> atMobiles = CollectionUtil.newArrayList(alarm.getReceives().split(","));
        sendDingTalkMarkdownMessage("线程池告警通知", text, atMobiles);
    }

    /** Builds the DingTalk Markdown payload and POSTs it to the webhook URL. */
    private void sendDingTalkMarkdownMessage(String title, String text, List<String> atMobiles) {
        Map<String, Object> markdown = new HashMap<>();
        markdown.put("title", title);
        markdown.put("text", text);

        Map<String, Object> at = new HashMap<>();
        at.put("atMobiles", atMobiles);

        Map<String, Object> request = new HashMap<>();
        request.put("msgtype", "markdown");
        request.put("markdown", markdown);
        request.put("at", at);

        try {
            String url = BootstrapConfigProperties.getInstance().getNotifyPlatforms().getUrl();
            String reponseBody = HttpUtil.post(url, JSON.toJSONString(request));
            DingRobotResponse response = JSON.parseObject(reponseBody, DingRobotResponse.class);
            if (response.getErrcode() != 0) {
                log.error("Ding failed to send message, reason: {}", response.errmsg);
            }
        } catch (Exception ex) {
            log.error("Ding failed to send message.", ex);
        }
    }

    /** Response envelope from the DingTalk robot API. */
    @Data
    static class DingRobotResponse {
        private Long errcode;
        private String errmsg;
    }
}
