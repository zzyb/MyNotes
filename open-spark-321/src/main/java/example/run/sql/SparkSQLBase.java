package example.run.sql;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class SparkSQLBase {
    public static void main(String[] args) {
        SparkSession sparkSql = SparkSession
                .builder()
                .master("local[1]")
                .appName("simple Sql")
                .config("", "")
                .getOrCreate();

        // 从json获取数据。
//        getDataFromJson(sparkSql);
        // 运行简单的sql。(临时视图)
//        runSimpleSQLFromJsonFile(sparkSql);
        // 运行简单的sql。(全局临时视图)
        runSQLWithGlobalTempViewFromJsonFile(sparkSql);

    }

    // 从json获取数据。
    private static void getDataFromJson(SparkSession spark) {
        Dataset<Row> fromJsonDataFrame = spark.read().json("./data/people.json");
        fromJsonDataFrame.show();
    }

    private static void runSimpleSQLFromJsonFile(SparkSession spark) {
        Dataset<Row> fromJsonDataFrame = spark.read().json("./data/people.json");

        //将DataFrame注册为SQL临时视图
        fromJsonDataFrame.createOrReplaceTempView("people");

        Dataset<Row> sqlDF = spark.sql("select name,1 as num from people");
        sqlDF.show();

        // 这里使用了newSession来创建新的会话。
        // 临时视图，只在session会话范围内；如果使用新会话则报错！！！
        // org.apache.spark.sql.AnalysisException: Table or view not found: people;
        // Dataset<Row> newSqlDF = spark.newSession().sql("select name,'newSession' as type from people");
        // newSqlDF.show();
    }

    private static void runSQLWithGlobalTempViewFromJsonFile(SparkSession spark) {
        Dataset<Row> fromJsonDataFrame = spark.read().json("./data/people.json");

        //将DataFrame注册为SQL全局临时视图
        fromJsonDataFrame.createOrReplaceGlobalTempView("people");

        // 这里要在视图前加global_temp前缀！！！
        Dataset<Row> sqlDF = spark.sql("select name,'global' as type from global_temp.people");
        sqlDF.show();

        // 全局临时视图是跨越会话的。(如果创建为临时视图，下面代码将查不到，报错);
        // 此处不报错!!!
        // org.apache.spark.sql.AnalysisException: Table or view not found: people;
        // 这里使用了newSession来创建新的会话。
        Dataset<Row> newSqlDF = spark.newSession().sql("select name,'newSession' as type from global_temp.people");
        newSqlDF.show();
    }
}
