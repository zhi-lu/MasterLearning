package com.luzhi.miniTomcat.pratice;

import java.io.*;
import java.text.MessageFormat;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/6/18
 * <p>Deception : 探究序列化和反序列化的使用操作</>
 */
public class SerializeAndUnSerialize {

    public static void main(String[] args) throws Exception {
        useSerialize();
        Person person = useUnSerialize();
        System.out.println(MessageFormat.format("我老婆的姓名:{0},我老婆的年龄:{1},我老婆的性别:{2}", person.getName(), person.getAge(), person.getSex()));
        System.out.println("<**************************************+===============+*************************************>");
        serializeWife();
        Wife wife = unSerialWife();
        System.out.println(MessageFormat.format("我的老婆:{0},年龄是:{1}", wife.getMyWife(), wife.getAge()));
    }

    /**
     * @see #useSerialize()
     * 进行序列化操作.
     */
    public static void useSerialize() throws IOException {
        Person person = new Person();
        person.setAge(16);
        person.setName("白上吹雪");
        person.setSex("女");
        File file = new File(System.getProperty("user.dir"), "src/com/luzhi/miniTomcat/pratice/test.txt");
        if (!file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
        }
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
        objectOutputStream.writeObject(person);
        System.out.println("对象序列化成功!");
        objectOutputStream.close();
    }

    /**
     * @see #useUnSerialize()
     * 进行反序列化操作.
     */
    private static Person useUnSerialize() throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(new File(System.getProperty("user.dir"),
                "src/com/luzhi/miniTomcat/pratice/test.txt")));
        Person person = (Person) objectInputStream.readObject();
        System.out.println("进行反序列化成功!");
        return person;
    }

    /**
     * @see #serializeWife()
     * <p>
     * 进行序列化将对象转化为二进制字节流.
     */
    private static void serializeWife() throws IOException {
        Wife wife = new Wife();
        wife.setMyWife("刻晴");
        wife.setAge(14);
        File file = new File(System.getProperty("user.dir"), "src/com/luzhi/miniTomcat/pratice/wife.txt");
        if (!file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
        }
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
        objectOutputStream.writeObject(wife);
        System.out.println("对象进行序列化成功");
        objectOutputStream.flush();
        objectOutputStream.close();
    }

    /**
     * @see #unSerialWife()
     * <p>
     * 进行反序列化将二进制字节流对象转化为对象
     */
    private static Wife unSerialWife() throws IOException, ClassNotFoundException {

        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(new File(System.getProperty("user.dir"), "src/com/luzhi/miniTomcat/pratice/wife.txt")));
        Wife wife = (Wife) objectInputStream.readObject();
        System.out.println("进行反序列化成功.");
        return wife;
    }
}

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/6/18
 * <p>Deception: 创建一个序列化对象需要进行反序列化对象需要实现{@link Serializable}</p>接口对象.
 */
class Person implements Serializable {

    private static final Long serialVersionUID = 1L;

    private String name;
    private String sex;
    private int age;

    public int getAge() {
        return age;
    }

    public String getName() {
        return name;
    }

    public String getSex() {
        return sex;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }
}

/**
 * @author apple
 * @version jdk1.8
 * <p>
 * // TODO : 2021/6/20
 * 该类实现{@link Serializable}接口,但不提供SerialVersionUID;探究是否会导致反序列失败.
 */
class Wife implements Serializable {

    private String myWifeName;
    private int age;

    public int getAge() {
        return age;
    }

    public String getMyWife() {
        return myWifeName;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setMyWife(String myWifeName) {
        this.myWifeName = myWifeName;
    }
}