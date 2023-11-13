
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.protocol.Message;

import java.util.Properties;

/**
 * 生产者
 */
public class ProducerQuickStart {

    public static void main(String[] args) {
        // 配置 Kafka 生产者参数
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "10.211.55.15:9092"); // 替换为你的 Kafka 集群地址
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        // 创建 Kafka 生产者实例
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
        // 创建消息对象
        String topic = "test-topic"; // 替换为你的主题名称
        String message = "Hello, Kafka!";
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, "test", message);
        // 发送消息
        producer.send(record);
        // 关闭生产者
        producer.close();

        System.out.println(1231);
    }

}