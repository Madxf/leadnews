package com.heima.schedule.service;

import com.heima.model.schedule.dtos.Task;

public interface TaskService {

    /**
     * 创建一个延时任务
     * @param task
     * @return taskId
     */
    public long addTask(Task task);

    /**
     * 取消一个延时任务
     * @param taskId
     * @return 是否取消成功
     */
    public boolean cancelTask(long taskId);

    /**
     * 根据类型、优先级拉取任务
     * @param type
     * @param priority
     * @return
     */
    public Task poll(int type, int priority);
}
