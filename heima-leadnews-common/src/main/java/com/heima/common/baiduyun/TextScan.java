package com.heima.common.baiduyun;

import com.baidu.aip.contentcensor.AipContentCensor;
import com.baidu.aip.contentcensor.EImgType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@ConfigurationProperties(prefix = "baiduyun")
@Getter
@Setter
public class TextScan {
    //设置APPID/AK/SK
    public static final String APP_ID = "41721320";
    public static final String API_KEY = "uqTCw2usZVnEdQtUDDoKZrEF";
    public static final String SECRET_KEY = "9zRvv2Q2iPnw2b9XRHEGiuKpU6vOHAsX";

    public Map scanContent(String content) {
        log.info("内容审核 ： {}", content);
        // 初始化一个AipContentCensor
        AipContentCensor client = new AipContentCensor(APP_ID, API_KEY, SECRET_KEY);

        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);
        /**
         *  参数为待审核的文本字符串
         *  审核结果类型conclusionType，可取值
         *      1.合规，
         *      2.不合规，
         *      3.疑似，
         *      4.审核失败
         */
        JSONObject response = client.textCensorUserDefined(content);
        HashMap map = new HashMap<>();
        map.put("conclusion", response.get("conclusion"));
        map.put("logId", response.get("log_id"));
        map.put("conclusionType", response.get("conclusionType"));
        return map;
    }
}