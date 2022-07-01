package com.github.crimscon.autoconfigure.springdoc.resource;

import org.springframework.stereotype.Service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Service
@Path("test/")
public class MessageResource {

    @GET
    @Path("/message")
    public String getMessage() {
        return "message";
    }

}
