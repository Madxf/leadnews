package com.heima.schedule.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.common.constants.ScheduleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.schedule.pojos.Taskinfo;
import com.heima.model.schedule.pojos.TaskinfoLogs;
import com.heima.schedule.mapper.TaskinfoLogsMapper;
import com.heima.schedule.mapper.TaskinfoMapper;
import com.heima.schedule.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class TaskServiceImpl implements TaskService {

    /**
     * 创建一个延时任务
     * @param task
     * @return taskId
     */
    @Transactional
    public long addTask(Task task) {
        // 1. 添加任务到数据库
        boolean toDBSuccessed = addTaskToDB(task);
        if(toDBSuccessed) {
            // 2. 添加任务到 redis缓存
            addTaskToRedis(task);
        }

        return task.getTaskId();
    }


    @Autowired
    private CacheService cacheService;

    /**
     * 将任务添加到Redis中
     * @param task
     */
    private void addTaskToRedis(Task task) {
        final String KEY = task.getTaskType() + "_" + task.getPriority();
        long executeTime = task.getExecuteTime();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        // 2.1 若任务为立即完成的任务（即执行时间 < 当前时间） 存入 list数据类型中
        if(executeTime <= System.currentTimeMillis()) {
            cacheService.lLeftPush(ScheduleConstants.TOPIC + KEY, JSON.toJSONString(task));
        } else if(executeTime < calendar.getTime().getTime()) {
            // 2.2 若任务为延时任务（即执行时间 > 当前时间 && 执行时间 < 预设时间） 存入zset数据类型中
            cacheService.zAdd(ScheduleConstants.FUTURE + KEY, JSON.toJSONString(task), executeTime);
        }

    }

    @Autowired
    private TaskinfoMapper taskinfoMapper;
    @Autowired
    private TaskinfoLogsMapper taskinfoLogsMapper;
    /**
     * 将任务添加到数据库中
     * @param task
     * @return
     */
    private boolean addTaskToDB(Task task) {
        boolean flag = true;
        // 1. 添加到任务表
        try {
            Taskinfo taskinfo = new Taskinfo();
            BeanUtils.copyProperties(task, taskinfo);
            taskinfo.setExecuteTime(new Date(task.getExecuteTime()));
            taskinfoMapper.insert(taskinfo);
            // 设置taskId
            task.setTaskId(taskinfo.getTaskId());
            // 2. 添加到任务日志表
            TaskinfoLogs taskinfoLogs = new TaskinfoLogs();
            BeanUtils.copyProperties(taskinfo, taskinfoLogs);
            taskinfoLogs.setVersion(1);
            taskinfoLogs.setStatus(ScheduleConstants.SCHEDULED);
            taskinfoLogsMapper.insert(taskinfoLogs);
        } catch (Exception e) {
            e.printStackTrace();
            flag = false;
        }
        return flag;
    }

    /**
     * 取消一个延时任务
     * @param taskId
     * @return 是否取消成功
     */
    @Override
    @Transactional
    public boolean cancelTask(long taskId) {
        boolean flag = false;
        // 1. 删除任务，并更新任务日志
        Task task = updateTask(taskId, ScheduleConstants.CANCELLED);
        if(task != null) {
            // 2. 删除Redis中的任务
            flag = removeTaskFromRedis(task);
        }
        return flag;
    }

    /**
     * 删除Redis中的任务
     * @param task
     * @return
     */
    private boolean removeTaskFromRedis(Task task) {
        final String KEY = task.getTaskType() + "_" + task.getPriority();
        long executeTime = task.getExecuteTime();
        boolean flag;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        try {
            // 2.1 若任务为立即完成的任务（即执行时间 < 当前时间） 存入 list数据类型中
            if(executeTime <= System.currentTimeMillis()) {
                cacheService.lRemove(ScheduleConstants.TOPIC + KEY, 0, JSON.toJSONString(task));
            } else if(executeTime < calendar.getTime().getTime()) {
                // 2.2 若任务为延时任务（即执行时间 > 当前时间 && 执行时间 < 预设时间） 存入zset数据类型中
                cacheService.zRemove(ScheduleConstants.FUTURE + KEY, JSON.toJSONString(task));
            }
            flag = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return flag;
    }

    /**
     * 删除任务，并更新任务日志
     * @param taskId
     * @param status
     * @return task
     */
    private Task updateTask(long taskId, int status) {
        Task task = null;
        try {
            TaskinfoLogs taskinfoLogs = taskinfoLogsMapper.selectById(taskId);
            // 删除任务
            taskinfoMapper.deleteById(taskId);
            // 更新日志
            taskinfoLogs.setStatus(status);
            taskinfoLogsMapper.updateById(taskinfoLogs);
            // 返回task
            task = new Task();
            BeanUtils.copyProperties(taskinfoLogs, task);
            task.setExecuteTime(taskinfoLogs.getExecuteTime().getTime());
        } catch (Exception e) {
            log.error("Cancel task exception, taskId = {}", taskId);
            throw new RuntimeException(e);
        }
        return task;
    }


    /**
     * 根据类型、优先级拉取任务
     * @param type
     * @param priority
     * @return task
     */
    @Override
    public Task poll(int type, int priority) {
        final String KEY = type + "_" + priority;
        Task task = null;
        try {
            String taskJSON = cacheService.lRightPop(ScheduleConstants.TOPIC + KEY);
            if(StringUtils.isNotBlank(taskJSON)) {
                task = JSON.parseObject(taskJSON, Task.class);
                updateTask(task.getTaskId(), ScheduleConstants.EXECUTED);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return task;
    }

    /**
     * 每分钟一次
     * 将延时任务从 zset 刷新到 list中
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void refresh() {
        // 根据redis锁来确保只有一个服务执行该任务
        String lock = cacheService.tryLock("FUTURE_TASK_SYNC", 1000 * 30);
        if (StringUtils.isBlank(lock)) return;
        log.info("refresh");
        // 获取 FutureKeys
        Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");
        for (String futureKey : futureKeys) {
            // 获取当前任务的 key （topic）
            String topicKey = ScheduleConstants.TOPIC + futureKey.substring(futureKey.indexOf("_") + 1);
            // 按照 key 和 score查询符合条件的 task
            Set<String> tasks = cacheService.zRangeByScore(futureKey, 0, System.currentTimeMillis());
            if(!tasks.isEmpty()) {
                // 同步数据 （ 从 zset中删除 && 传入 list中
                cacheService.refreshWithPipeline(futureKey, topicKey, tasks);
                log.info("成功刷新");
            }
        }
    }

    /**
     * 数据库任务定时到redis中
     */
    @Scheduled(cron = "0 */5 * * * ?")
    @PostConstruct // 服务一启动就执行该任务
    public void reloadData2Redis() {
        // 采用分布式锁确保只有一个服务运行该任务
        String lock = cacheService.tryLock("RELOAD_DATA", 60 * 1000 * 3);
        if(StringUtils.isBlank(lock)) return;
        log.info("数据库任务定时到redis中");
        // 清空缓存中的数据 list，zset
        clearCache();
        // 查询符合条件的任务（小于未来5min的任务）
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        List<Taskinfo> taskinfos = taskinfoMapper.selectList(Wrappers.<Taskinfo>lambdaQuery().lt(Taskinfo::getExecuteTime, calendar.getTime()));
        // 把任务添加到 Redis中
        if(!taskinfos.isEmpty()) {
            for (Taskinfo taskinfo : taskinfos) {
                Task task = new Task();
                BeanUtils.copyProperties(taskinfo, task);
                task.setExecuteTime(taskinfo.getExecuteTime().getTime());
                addTaskToRedis(task);
            }
        }
    }

    /**
     * 清空缓存中的数据
     */
    public void clearCache() {
        // 删除所有 以 topic_ 和 future_ 开头的KEY
        Set<String> topic = cacheService.scan(ScheduleConstants.TOPIC + "*");
        Set<String> future = cacheService.scan(ScheduleConstants.FUTURE + "*");
        cacheService.delete(topic);
        cacheService.delete(future);
    }



}
