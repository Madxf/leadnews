import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.schedule.ScheduleApplication;
import com.heima.schedule.service.TaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = ScheduleApplication.class)
@RunWith(SpringRunner.class)
public class RedisTest {

    @Autowired
    private CacheService cacheService;
    @Autowired
    private TaskService taskService;

    @Test
    public void test() {
        Integer append = cacheService.append("name", "lisi");
        System.out.println(append);
    }

    @Test
    public void addTask() {
        Task task = new Task();
        task.setTaskType(100);
        task.setPriority(1);
        task.setParameters("task test".getBytes());
        task.setExecuteTime(System.currentTimeMillis() + 1000L * 60L * 2);

        long id = taskService.addTask(task);
        System.out.println(id);

    }

    @Test
    public void removeTask() {
        long id = 1718254296721420290L;
        System.out.println(taskService.cancelTask(id));
    }

    @Test
    public void poll() {
        String futureKey = "topic_100_1";
        System.out.println(futureKey.substring(futureKey.indexOf("_") + 1));
    }
}
