package example.run.sql;

import example.bean.Person;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SparkSession;

import java.util.Collections;

public class SparkSQLCreateDataSetsByJavaBeanEncoder {
    public static void main(String[] args) {
        SparkSession sparkSql = SparkSession
                .builder()
                .master("local[1]")
                .appName("simple Sql")
                .config("", "")
                .getOrCreate();

        // 创建一个Bean类实例。
        Person person = new Person();
        person.setId(200);
        person.setName("goodNight");

        // 为Java创建编码器
        Encoder<Person> personBean = Encoders.bean(Person.class);
        // SparkSession的createDataset方法传入集合、编码器
        Dataset<Person> personDataset = sparkSql.createDataset(Collections.singletonList(person), personBean);

        personDataset.show();

    }
}

//    Spark 需要公共 JavaBean 类。
//    Spark 需要公共 JavaBean 类。
//    Spark 需要公共 JavaBean 类。
//class Person implements Serializable {
//    private int id;
//    private String name;
//
//    public Person() {
//    }
//
//    public Person(int id, String name) {
//        this.id = id;
//        this.name = name;
//    }
//
//    public int getId() {
//        return id;
//    }
//
//    public void setId(int id) {
//        this.id = id;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//}
