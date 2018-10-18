package dataObjects;

//import org.json.simple.JSONObject;

import javax.persistence.*;
import com.google.gson.*;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;


@Entity
@Table(name = "students")
public class Student {

    @Id @GeneratedValue
    @Column(name = "id")
    private int id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "matriculation_number")
    private int matriculationNumber;

    @Column(name = "course")
    private String course;

    @Column(name = "semester")
    private int semester;

    @Column(name = "email")
    private String email;

    public Student() {

    }

    public Student(String firstName, String lastName, int matriculationNumber, String course, int semester, String email) {
        setFirstName(firstName);
        setLastName(lastName);
        setMatriculationNumber(matriculationNumber);
        setCourse(course);
        setSemester(semester);
        setEmail(email);
    }

    public int getId() {
        return id;
    }

    private void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getMatriculationNumber() {
        return matriculationNumber;
    }

    public void setMatriculationNumber(int matriculationNumber) {
        this.matriculationNumber = matriculationNumber;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public JsonObject getJSON() {

        JsonObject student = new JsonObject();
        student.addProperty("id", this.getId());
        student.addProperty("firstName", this.getFirstName());
        student.addProperty("lastName", this.getLastName());
        student.addProperty("matriculationNumber", this.getMatriculationNumber());
        student.addProperty("course", this.getCourse());
        student.addProperty("semester", this.getSemester());
        student.addProperty("email", this.getEmail());

        return (JsonObject) student;
    }

    public boolean checkIfExists (SessionFactory factory, Student student) {
        boolean studentExists = true;
        Transaction tx = null;

        try (Session session = factory.openSession()) {

            tx = session.beginTransaction();
            Query query = session.createQuery(
                    "FROM Student WHERE" +
                            " firstName = :firstName " +
                            "AND lastName = :lastName " +
                            "AND matriculationNumber = :matriculationNumber " +
                            "AND course = :course " +
                            "AND email = :email"
            );

            query.setParameter("firstName", student.getFirstName());
            query.setParameter("lastName", student.getLastName());
            query.setParameter("matriculationNumber", student.getMatriculationNumber());
            query.setParameter("course", student.getCourse());
            query.setParameter("email", student.getEmail());

            if (query.list().isEmpty()) {
                studentExists = false;
            }

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.getStackTrace();
        }

        return studentExists;
    }
}
