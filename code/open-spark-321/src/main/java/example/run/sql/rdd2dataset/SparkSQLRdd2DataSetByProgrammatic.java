package example.run.sql.rdd2dataset;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;

public class SparkSQLRdd2DataSetByProgrammatic {
    public static void main(String[] args) {
        SparkSession sparkSql = SparkSession
                .builder()
                .master("local[1]")
                .appName("simple Sql")
                .config("", "")
                .getOrCreate();


        // 从text文件创建Person类型的RDD
        JavaRDD<String> stringJavaRDD = sparkSql.read().textFile("./data/person.txt").toJavaRDD();

        // 使用字符串编码Schema
        String schemaString = "id name";

        // 基于字符串Schema生成Schema。
        ArrayList<StructField> structFields = new ArrayList<>();
        for (String value : schemaString.split(" ")) {
            StructField field = DataTypes.createStructField(value, DataTypes.StringType, true);
            structFields.add(field);
        }
        StructType schema = DataTypes.createStructType(structFields);

        // 将RDD转换为Rows
        JavaRDD<Row> rowJavaRDD = stringJavaRDD.map(new Function<String, Row>() {
            @Override
            public Row call(String v1) throws Exception {
                String[] values = v1.split(",");
                return RowFactory.create(values[0], values[1].trim());
            }
        });

        // 将schema应用到Rows的RDD，返回DataSet
        Dataset<Row> personDF = sparkSql.createDataFrame(rowJavaRDD, schema);

        // 将DataFrame注册为临时视图
        personDF.createOrReplaceTempView("person");
        // 通过使用Spark提供的SQL方法运行SQL语句
        Dataset<Row> resultSql = sparkSql.sql("select name from person where id between 300 and 301");

        resultSql.show();

        // SQL查询的结果是Dataframe并支持所有正常的RDD操作
        // 结果中的行列可以由字段索引或字段名称访问
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
