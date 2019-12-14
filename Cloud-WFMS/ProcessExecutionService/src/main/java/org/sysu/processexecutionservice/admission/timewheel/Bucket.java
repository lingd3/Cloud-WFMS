package org.sysu.processexecutionservice.admission.timewheel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 时间槽
 * @author: Gordan Lin
 * @create: 2019/12/12
 **/
public class Bucket {

    // 当前槽的过期时间
    private AtomicLong expiration = new AtomicLong(-1L);

    private List<TimerTask> taskList = new CopyOnWriteArrayList<>();

    public Bucket() {}

    /**
     * 设置槽的过期时间
     */
    public boolean setExpire(long expire) {
        return expiration.getAndSet(expire) != expire;
    }

    /**
     * 获取槽的过期时间
     */
    public long getExpire() {
        return expiration.get();
    }

    /**
     * 新增任务到bucket
     * @param timerTask
     */
    public void addTask(TimerTask timerTask) {
        taskList.add(timerTask);
    }

    /**
     * 删除任务
     * @param count
     * @return
     */
    public synchronized List<TimerTask> removeTaskAndGet(int count) {
        if (count == -1) {
            List<TimerTask> rtnList = new ArrayList<>(taskList);
            taskList.clear();
            return rtnList;
        }
        List<TimerTask> rtnList = new ArrayList<>();
        Iterator<TimerTask> iterator = taskList.iterator();
        int n = 0;
        while (iterator.hasNext() && n < count) {
            rtnList.add(iterator.next());
            iterator.remove();
            n++;
        }
        return rtnList;
    }

}
