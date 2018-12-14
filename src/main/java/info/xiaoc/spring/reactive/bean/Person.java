package info.xiaoc.spring.reactive.bean;

/**
 * Created by ionst on 16/03/2017.
 */
public class Person {

    public Person() {
    }

    public Person(String name) {
        this.name = name;
    }

    public Person(Integer id, String name) {
        this.id = id.longValue();
        this.name = name;
    }

    public Person(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private Long id;
    private String name;

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
