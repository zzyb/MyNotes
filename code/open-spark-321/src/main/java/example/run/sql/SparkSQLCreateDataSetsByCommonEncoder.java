package example.run.sql;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;

public class SparkSQLCreateDataSetsByCommonEncoder {
    public static void main(String[] args) {
        SparkSession sparkSql = SparkSession
                .builder()
                .master("local[1]")
                .appName("simple Sql")
                .config("", "")
                .getOrCreate();


        // 为基本类型组成的二元组创建编码器
        Encoder<Tuple2<Integer, Integer>> tuple2Encoder = Encoders.tuple(Encoders.INT(), Encoders.INT());
        // SparkSession的createDataset方法传入集合、编码器
        Dataset<Tuple2<Integer, Integer>> tuple2Dataset = sparkSql.createDataset(
                Arrays.asList(
                        new Tuple2<Integer, Integer>(1, 1),
                        new Tuple2<Integer, Integer>(2, 1),
                        new Tuple2<Integer, Integer>(3, 1)
                ),
                tuple2Encoder
        );

//        tuple2Dataset.show();
        List<Tuple2<Integer, Integer>> collect = tuple2Dataset.toJavaRDD().collect();
        System.out.println(Arrays.toString(collect.toArray()));

    }
}