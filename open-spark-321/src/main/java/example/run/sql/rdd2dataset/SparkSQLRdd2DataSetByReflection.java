package example.run.sql.rdd2dataset;

import example.bean.Person;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.*;

public class SparkSQLRdd2DataSetByReflection {
    public static void main(String[] args) {
        SparkSession sparkSql = SparkSession
                .builder()
                .master("local[1]")
                .appName("simple Sql")
                .config("", "")
                .getOrCreate();


//        // 为Java创建编码器
//        Encoder<Person> personBean = Encoders.bean(Person.class);

        // 从text文件创建Person类型的RDD
        JavaRDD<Person> personJavaRDD = sparkSql
                .read().textFile("./data/person.txt")
                .toJavaRDD()
                .map(new Function<String, Person>() {
                    @Override
                    public Person call(String v1) throws Exception {
                        String[] values = v1.split(",");
                        Person person = new Person();
                        person.setId(Long.parseLong(values[0]));
                        person.setName(values[1]);
                        return person;
                    }
                });

        // 将Schema应用到JavaBean类型的RDD获得DataFrame（DataFrame = DataSet<Row>）
        Dataset<Row> personDF = sparkSql.createDataFrame(personJavaRDD, Person.class);
        // 将DataFrame注册为临时视图
        personDF.createOrReplaceTempView("person");
        // 通过使用Spark提供的SQL方法运行SQL语句
        Dataset<Row> resultSql = sparkSql.sql("select name from person where id between 300 and 301");

//        resultSql.show();

        Encoder<String> stringEncoder = Encoders.STRING();
        // 一、通过字段索引访问字段行中的列
        Dataset<String> getResultDFByIndex = resultSql.map(
                new MapFunction<Row, String>() {
                    @Override
                    public String call(Row value) throws Exception {
                        return "Name:" + value.getString(0);
                    }
                },
                stringEncoder
        );

        getResultDFByIndex.show();

        // 二、也可以通过字段名
        Dataset<String> getResultDFByName = resultSql.map(
                new MapFunction<Row, String>() {
                    @Override
                    public String call(Row value) throws Exception {
                        return "Name:" + value.getAs("name");
                    }
                },
                stringEncoder
        );
        getResultDFByName.show();

    }
}
