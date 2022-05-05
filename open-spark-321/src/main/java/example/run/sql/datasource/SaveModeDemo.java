package example.run.sql.datasource;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;


public class SaveModeDemo {

    public static void main(String[] args) {
        SparkSession sparkSql = SparkSession
                .builder()
                .master("local[1]")
                .appName("simple Sql")
                .config("", "")
                .getOrCreate();

        // ./data/parquet_data 是准备的一个测试parquet文件。
        // sql + fileType.+`filePath`
        Dataset<Row> parquetData = sparkSql.sql("select one from parquet.`./data/parquet_data`");

        parquetData.write()
                .format("text")
//                .mode(SaveMode.ErrorIfExists) // 默认行为
//                .mode(SaveMode.Append)
//                .mode(SaveMode.Ignore)
                .mode(SaveMode.Overwrite)
                .save("./data/toText");
    }


}