/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package service;

import com.google.gson.Gson;
import dataObjects.Student;

import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.List;

import org.hibernate.*;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.wso2.msf4j.interceptor.annotation.RequestInterceptor;
import org.wso2.msf4j.interceptor.annotation.ResponseInterceptor;
import service.requestlogger.RequestLoogerInterceptor;
import service.requestlogger.ResponseLoggerInterceptor;
import service.kafka.SimpleKafkaProducer;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;


/**
 * This is the Microservice resource class.
 * See <a href="https://github.com/wso2/msf4j#getting-started">https://github.com/wso2/msf4j#getting-started</a>
 * for the usage of annotations.
 *
 * @since 1.0-SNAPSHOT
 */
@Path("/students")
public class StudentsMicroservice {

    private SessionFactory factory;

    StudentsMicroservice(SessionFactory factory) {
        this.factory = factory;
        initialiseFirstDBEntry(this.factory);
    }

    @GET
    @Path("/")
    @Produces("application/json")
    @RequestInterceptor(RequestLoogerInterceptor.class)
    @ResponseInterceptor(ResponseLoggerInterceptor.class)
    public Response get(@QueryParam("id") Integer id ) {
        String methode = "GET";
        List students = Collections.emptyList();
        Transaction tx = null;
        Response response;

        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            if (id == null) {
                students = session.createQuery("FROM Student").list();
            } else {
                Query query = session.createQuery("FROM Student WHERE id = :id");
                query.setParameter("id", id);
                students = query.list();
            }
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.getStackTrace();
        }

        Gson gson = new Gson();
        String studentJson = gson.toJson(students);


        if (students.isEmpty()) {
                response = Response.status(404).entity("{\"result\":\"Student not found.\"}").build();
        } else {
            response = Response.status(200).entity(studentJson).build();
        }

        sendKafkaMessage(response, methode);

        return response;

    }

    @POST
    @Path("/")
    @Consumes("application/json")
    @Produces("application/json")
    @RequestInterceptor(RequestLoogerInterceptor.class)
    @ResponseInterceptor(ResponseLoggerInterceptor.class)
    public Response post(JsonObject studentJSON) {

        Integer studentId = null;
        Response response;
        String methode = "POST";

        Student student = new Student(
                studentJSON.get("firstName").getAsString(),
                studentJSON.get("lastName").getAsString(),
                studentJSON.get("matriculationNumber").getAsInt(),
                studentJSON.get("course").getAsString(),
                studentJSON.get("semester").getAsInt(),
                studentJSON.get("email").getAsString()
        );

        if(student.checkIfExists(this.factory, student)) {
            response = Response.status(Response.Status.CONFLICT).entity("{\"result\":\"Student already exists.\"}").build();
        } else {
            Transaction tx = null;
            try(Session session = factory.openSession()) {
                tx = session.beginTransaction();
                studentId = (Integer) session.save(student);
                tx.commit();
            } catch (HibernateException e) {
                if (tx != null) tx.rollback();
                e.getStackTrace();
            }
            response = Response.status(Response.Status.CREATED)
                    .entity("{\"result\":\"Added student with ID = " + studentId + "\"}").build();
        }

        sendKafkaMessage(response, methode);
        
        return response;

    }

    @PUT
    @Path("/")
    @Consumes("application/json")
    @Produces("application/json")
    @RequestInterceptor(RequestLoogerInterceptor.class)
    @ResponseInterceptor(ResponseLoggerInterceptor.class)
    public Response put(@QueryParam("id") int id, JsonObject studentJSON) {

        int updatedRows = 0;
        Response response;
        String methode = "PUT";

        if(id != 0) {
        Student student = new Student(
                studentJSON.get("firstName").getAsString(),
                studentJSON.get("lastName").getAsString(),
                studentJSON.get("matriculationNumber").getAsInt(),
                studentJSON.get("course").getAsString(),
                studentJSON.get("semester").getAsInt(),
                studentJSON.get("email").getAsString()
        );

        Transaction tx = null;
        try (Session session = factory.openSession()) {

            tx = session.beginTransaction();

            Query query = session.createQuery("UPDATE Student SET  firstName = :firstName" +
                    ", lastName = :lastName " +
                    ", matriculationNumber = :matriculationNumber " +
                    ", course = :course " +
                    ", email = :email" +
                    " WHERE id = :id");

            query.setParameter("id", id);
            query.setParameter("firstName", student.getFirstName());
            query.setParameter("lastName", student.getLastName());
            query.setParameter("matriculationNumber", student.getMatriculationNumber());
            query.setParameter("course", student.getCourse());
            query.setParameter("email", student.getEmail());
            updatedRows = query.executeUpdate();
            tx.commit();

        } catch (HibernateException e) {

            if (tx != null) tx.rollback();
            e.getStackTrace();

        }
            response = Response.status(200).entity("{\"result\":\""+ updatedRows + " row/s updated.\"}").build();
        } else {
            response = Response.status(Response.Status.CONFLICT).entity("{\"result\":\"Invalid ID submitted - ID = "+ id +"\"}").build();
        }

        sendKafkaMessage(response, methode);

        return response;
    }

    @DELETE
    @Path("/")
    @Produces("application/json")
    @RequestInterceptor(RequestLoogerInterceptor.class)
    @ResponseInterceptor(ResponseLoggerInterceptor.class)
    public Response delete(@QueryParam("id") int id) {
        int updatedRows = 0;
        Response response;
        String methode = "DELETE";

        Transaction tx = null;
        try (Session session = factory.openSession()) {

            tx = session.beginTransaction();
            Query query = session.createQuery("DELETE Student WHERE id = :id");
            query.setParameter("id", id);
            updatedRows = query.executeUpdate();
            tx.commit();

        } catch (HibernateException e) {

            if (tx != null) tx.rollback();
            e.getStackTrace();

        }

        if(updatedRows > 0) {
             response = Response.status(Response.Status.OK).entity("{\"result\":\""+ updatedRows +" row/s deleted - Student with ID = " + id + " was successfully deleted.\"}").build();
        } else {
            response = Response.status(Response.Status.OK).entity("{\"result\":\"No student deleted - can't find student with ID = " + id +"\"}").build();
        }

        sendKafkaMessage(response, methode);

        return response;
    }

    private void initialiseFirstDBEntry(SessionFactory factory) {
        Transaction tx = null;
        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            List checkDB = session.createQuery("FROM Student").list();
            if (checkDB.isEmpty()) {
                System.out.println("Table empty - inserting initial data.");
                Student initalStudent = new Student(
                        "Initial",
                        "Student",
                        10000000,
                        "MI",
                        1,
                        "initial.student@testmail.com"
                );
                session.save(initalStudent);
            }
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.getStackTrace();
        }
    }

    private void sendKafkaMessage(Response response, String methode) {
        String operation = "{\"http_methode\":\"" + methode +"\",\"status\":\""+ response.getStatus() + "\"}";
        //String operation = "status: " + response.getStatus();
        String message = response.getEntity().toString();
        try {
            SimpleKafkaProducer.runProducerSync(operation, message);
        } catch (InterruptedException e) {
            e.getStackTrace();
        } catch (Exception e) {
            e.getStackTrace();
        }

    }

}
