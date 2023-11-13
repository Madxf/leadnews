package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.apis.schedule.IScheduleClient;
import com.heima.common.constants.ScheduleConstants;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.TaskTypeEnum;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.utils.common.ProtostuffUtil;
import com.heima.wemedia.service.WmNewsAutoScanService;
import com.heima.wemedia.service.WmNewsTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class WmNewsTaskServiceImpl implements WmNewsTaskService {

    @Autowired
    private IScheduleClient scheduleClient;

    /**
     * 添加任务到延迟队列中
     * @param id          文章的id
     * @param publishTime 发布的时间  可以做为任务的执行时间
     */
    @Override
    @Async
    public void addNewsToTask(Integer id, Date publishTime) {
        log.info("添加任务到延迟服务中----begin");

        // 调用ScheduleClient 将任务添加到数据库中
        Task task = new Task();
        task.setExecuteTime(publishTime.getTime());
        task.setTaskType(TaskTypeEnum.NEWS_SCAN_TIME.getTaskType());
        task.setPriority(TaskTypeEnum.NEWS_SCAN_TIME.getPriority());
        // 将一个 WmNews作为参数传入任务中（需要将WmNews对象序列化）
        WmNews wmNews = new WmNews();
        wmNews.setId(id);
        task.setParameters(ProtostuffUtil.serialize(wmNews));
        scheduleClient.addTask(task);
    }


    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;
    /**
     * 消费延迟队列数据
     */
    @Override
    @Scheduled(fixedRate = 1000L)
    public void scanNewsByTask() {
//        log.info("拉取任务并审核----begin");
        // 根据 任务类型 和 优先级 可以指定任务类型
        ResponseResult result = scheduleClient.poll(TaskTypeEnum.NEWS_SCAN_TIME.getTaskType(), TaskTypeEnum.NEWS_SCAN_TIME.getPriority());
        if(result.getCode().equals(200) && result.getData() != null) { // 成功获取到任务
            String jsonString = JSON.toJSONString(result.getData());
            Task task = JSON.parseObject(jsonString, Task.class);
            // 将task参数反序列化为wmNews
            WmNews wmNews = ProtostuffUtil.deserialize(task.getParameters(), WmNews.class);
            System.out.println(wmNews.getId()+"-----------");
            wmNewsAutoScanService.autoScanWmNews(wmNews.getId().longValue());
//            log.info("成功审核----end");
        }
    }
}
