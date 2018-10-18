package service.CustomExceptions;

import service.CustomExceptions.StudentNotFoundException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class StudentNotFoundMapper implements ExceptionMapper<StudentNotFoundException> {
    public Response toResponse(StudentNotFoundException ex) {

        return Response.status(404)
                .entity(ex.getMessage() + " [from StudentNotFoundMapper]")
                .type("text/plain")
                .build();

    }
}
