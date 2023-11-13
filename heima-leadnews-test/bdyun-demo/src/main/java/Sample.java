import com.baidu.aip.contentcensor.AipContentCensor;
import org.json.JSONObject;

public class Sample {
    //设置APPID/AK/SK
    public static final String APP_ID = "41721320";
    public static final String API_KEY = "uqTCw2usZVnEdQtUDDoKZrEF";
    public static final String SECRET_KEY = "9zRvv2Q2iPnw2b9XRHEGiuKpU6vOHAsX";

    public static void main(String[] args) {
        // 初始化一个AipContentCensor
        AipContentCensor client = new AipContentCensor(APP_ID, API_KEY, SECRET_KEY);

        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);


        // 调用接口
        String path = "test.jpg";
//        JSONObject res = client.imageCensorUserDefined(path);
        System.out.println(2);
        
    }
}