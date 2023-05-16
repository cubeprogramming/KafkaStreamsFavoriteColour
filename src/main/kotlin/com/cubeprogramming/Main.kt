import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.Serdes.LongSerde
import org.apache.kafka.common.serialization.Serdes.IntegerSerde
import org.apache.kafka.common.serialization.Serdes.StringSerde
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.KeyValueMapper
import org.apache.kafka.streams.kstream.Named
import org.apache.kafka.streams.kstream.Produced
import org.apache.logging.log4j.LogManager
import java.lang.invoke.MethodHandles
import java.util.*


fun main(args: Array<String>) {
    val log = LogManager.getLogger(MethodHandles.lookup().lookupClass())

   //TODO 1. Initialize properties
    val config = Properties().apply {
        put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092")
        put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OffsetResetStrategy.EARLIEST.name.lowercase())
        put(StreamsConfig.APPLICATION_ID_CONFIG, "favourite-colour-kotlin")

        put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().javaClass)
        put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().javaClass)
    }

   //TODO 2. Initialize Stream
    val builder = StreamsBuilder()
    builder.stream<String, String>("favourite-colour-input")
   //TODO 3. Flat map  by using first string as key and second as a value
    .flatMap{_,value ->
        val splitString =  value.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        mutableListOf<KeyValue<String, String>>(KeyValue.pair(splitString[0].lowercase(Locale.getDefault()), try { splitString[1].lowercase(Locale.getDefault()) } catch (err: IndexOutOfBoundsException) { null }  ))
    }
   //TODO 4. Filter unwanted colors
        .filter{_,color ->
            listOf("green", "blue", "red").contains(color)
        }
   //TODO 5. Save to intermediary compacted topic
        .to("user-keys-and-colours")

    //TODO 6. Read from intermediary topic as table
    builder.table<String, String>("user-keys-and-colours")
        //TODO 7. Calculate color count
        .groupBy { _, color ->
            KeyValue.pair(color,color)
        }
        .count(Named.`as`("CountsByColours"))
        .mapValues { count ->
            log.info("COLOR COUNT: $count")
//            count.toString()
            count.toInt()
        }
        .toStream()
        .map { color, count ->
            log.info("COLOR: $color, COUNT: $count")
            KeyValue.pair(color,count)
        }

    //TODO 7. Send to output topics
//        .to("favourite-colour-output", Produced.with(Serdes.String(),Serdes.Long()))
//        .to("favourite-colour-output", Produced.valueSerde(Serdes.StringSerde()))
        .to("favourite-colour-output", Produced.valueSerde(IntegerSerde()))

    builder.table<String, String>("favourite-colour-output")
        .mapValues { count ->
            log.info("COLOR COUNT from topic: $count")
//            count.toString()
            count.toString()
        }
        .toStream()
        .peek { color, count ->
            log.info("COLOR from topic: $color, COUNT from topic: $count")
        }

        //TODO 7. Send to output topics
//        .to("favourite-colour-output", Produced.with(Serdes.String(),Serdes.Long()))
//        .to("favourite-colour-output", Produced.valueSerde(Serdes.StringSerde()))
        .to("favourite-colour-output-string-value", Produced.valueSerde(StringSerde()))

    //TODO 9. Start the stream
    val kafkaStreams = KafkaStreams(builder.build(),config)
    kafkaStreams.cleanUp()
    kafkaStreams.start()

    //TODO 10. Print topology
    log.info(kafkaStreams)

    //TODO 11. Shutdown gracefully
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            log.warn("Running Shutdown Hook")
//            println("Running Shutdown Hook")
        }
    })

}
