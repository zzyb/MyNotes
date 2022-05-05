package example.run.sql.datasource;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;


public class AddCsvFileFormat {

    public static void main(String[] args) {
        SparkSession sparkSql = SparkSession
                .builder()
                .master("local[1]")
                .appName("simple Sql")
                .config("", "")
                .getOrCreate();

        // 指定读取json类型的数据
        Dataset<Row> jsonData = sparkSql.read().format("json").load("./data/people.json");

        jsonData.show();

        // 将json类型的数据转换为text类型的数据
        jsonData.select("name").write().format("text").save("./data/toPersonDir");


    }


}