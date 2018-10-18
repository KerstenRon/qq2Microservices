package service.kafka;

//import util.properties packages

import java.util.Properties;

//import simple producer packages
import org.apache.kafka.clients.producer.*;

//import KafkaProducer packages

//import ProducerRecord packages
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

public class SimpleKafkaProducer {

    private static final String SERVICE_NAME = "1_MS4J_2";
    private static final String TOPIC = "logging";
    private static final String URI = "qq2.ddnss.de"; //"qq2.ddnss.de"
    private static final int PORT = 9092;
    private static final String BOOTSTRAP_SERVERS = "qq2.ddnss.de:9092";


    private static Producer<Long, String> createProducer() {

        Properties props = new Properties();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS);

        props.put(ProducerConfig.CLIENT_ID_CONFIG, SERVICE_NAME);

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                LongSerializer.class.getName());

        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        return new KafkaProducer<>(props);
    }

    public static void runProducerSync(String operation, String message) throws Exception {

        final Producer<Long, String> producer = createProducer();
        long time = System.currentTimeMillis();
        try {
            final ProducerRecord<Long, String> record =
                new ProducerRecord<>(TOPIC, "{\"service_name:\"" + SERVICE_NAME + "\"," +
                                                            "\"operation\":\"" + operation + "\"," +
                                                            "\"message\":"+ message +"}");
            RecordMetadata metadata = producer.send(record).get();
                long elapsedTime = System.currentTimeMillis() - time;
                System.out.printf("sent record(key=%s value=%s) " +
                                "meta(partition=%d, offset=%d) time=%d\n",
                        record.key(), record.value(), metadata.partition(),
                        metadata.offset(), elapsedTime);
        } finally {
            producer.flush();
            producer.close();
        }
    }

    public static void runProducerAsync(String operation, String message) throws InterruptedException {
        final Producer<Long, String> producer = createProducer();
        long time = System.currentTimeMillis();
        //final CountDownLatch countDownLatch = new CountDownLatch(sendMessageCount);

        try {
            //for (long index = time; index < time + sendMessageCount; index++) {
                final ProducerRecord<Long, String> record =
                        new ProducerRecord<>(TOPIC, "{\"service_name:\"" + SERVICE_NAME + "\"," +
                                                            "\"operation\":\"" + operation + "\"," +
                                                            "\"message\":"+ message +"}");
                producer.send(record, (metadata, exception) -> {
                    long elapsedTime = System.currentTimeMillis() - time;
                    if (metadata != null) {
                        System.out.printf("sent record(key=%s value=%s) " +
                                        "meta(partition=%d, offset=%d) time=%d\n",
                                record.key(), record.value(), metadata.partition(),
                                metadata.offset(), elapsedTime);
                    } else {
                        exception.printStackTrace();
                    }
                    //countDownLatch.countDown();
                });
            //}
            //countDownLatch.await(25, TimeUnit.SECONDS);
        }finally {
            producer.flush();
            producer.close();
        }
    }

    /*public static void main(String... args) throws Exception {

        if (args.length == 0) {
            runProducer(5);
        } else {
            runProducer(Integer.parseInt(args[0]));
        }
    }*/
}
