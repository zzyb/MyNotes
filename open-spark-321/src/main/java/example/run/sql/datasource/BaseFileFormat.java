package example.run.sql.datasource;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;


public class BaseFileFormat {

    public static void main(String[] args) {
        SparkSession sparkSql = SparkSession
                .builder()
                .master("local[1]")
                .appName("simple Sql")
                .config("", "")
                .getOrCreate();

        // "./data/parquet_data" 是准备的一个测试parquet文件。
        Dataset<Row> parquetData = sparkSql.read().load("./data/parquet_data");

        parquetData.show();



        // 通过select 筛选数据
        parquetData.select("two").show();

    }


}