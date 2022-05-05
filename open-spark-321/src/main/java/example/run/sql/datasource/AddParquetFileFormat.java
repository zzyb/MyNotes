package example.run.sql.datasource;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;


/**
 * 待完善
 */
public class AddParquetFileFormat {

    public static void main(String[] args) {
        SparkSession sparkSql = SparkSession
                .builder()
                .master("local[1]")
                .appName("simple Sql")
                .config("", "")
                .getOrCreate();

        // 指定读取json类型的数据
        Dataset<Row> csvData = sparkSql.read()
                .format("parquet")
                .option("","")
                .option("","")
                .option("","")
                .load("./data/xxx");

        csvData.show();

    }


}