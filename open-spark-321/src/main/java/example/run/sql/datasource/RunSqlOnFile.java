package example.run.sql.datasource;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;


public class RunSqlOnFile {

    public static void main(String[] args) {
        SparkSession sparkSql = SparkSession
                .builder()
                .master("local[1]")
                .appName("simple Sql")
                .config("", "")
                .getOrCreate();

        // ./data/parquet_data 是准备的一个测试parquet文件。
        // sql + fileType.+`filePath`
        Dataset<Row> parquetData = sparkSql.sql("select * from parquet.`./data/parquet_data`");

        parquetData.show();
    }


}