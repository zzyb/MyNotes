package example.run.sql;

import example.bean.Person;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SparkSession;

public class SparkSQLCreateDataSetsByJavaBeanEncoderAndDataFrame {
    public static void main(String[] args) {
        SparkSession sparkSql = SparkSession
                .builder()
                .master("local[1]")
                .appName("simple Sql")
                .config("", "")
                .getOrCreate();


        // 为Java创建编码器
        Encoder<Person> personBean = Encoders.bean(Person.class);
        // 对DataFrame设置编码器返回DataSet
        Dataset<Person> personDataset = sparkSql.read().json("./data/person.json").as(personBean);

        personDataset.show();

    }
}
