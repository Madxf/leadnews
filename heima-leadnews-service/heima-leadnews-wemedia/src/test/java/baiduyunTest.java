import com.heima.common.baiduyun.ImageScan;
import com.heima.common.baiduyun.TextScan;
import com.heima.file.service.FileStorageService;
import com.heima.wemedia.WemediaApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@SpringBootTest(classes = WemediaApplication.class)
@RunWith(SpringRunner.class)
@Slf4j
public class baiduyunTest {

    @Autowired
    private TextScan textScan;
    @Autowired
    private ImageScan imageScan;
    @Autowired
    private FileStorageService fileStorageService;

    @Test
    public void test() {
        log.info("test   {}", textScan);
        Map map = textScan.scanContent("你好啊, 冰毒");
        System.out.println(map);
    }

    @Test
    public void testI() {
        log.info("test image");
        byte[] image = fileStorageService.downLoadFile("http://10.211.55.15:9000/leadnews/2023/10/23/67532f9a6159468abc91ef209b1b93ca.jpg");
        Map map = imageScan.scanImage(image);
        System.out.println(map);
    }

}
