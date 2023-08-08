package com.github.crimscon.autoconfigure.springdoc.resource;

import com.github.crimscon.autoconfigure.springdoc.model.Message;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Service
public class CrudMessageSubResource {

    @GET
    @Path("/{id}")
    public Message getMessage(@PathParam("id") String id) {
        return new Message(id, "Hello", "John");
    }

    @POST
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_JSON)
    public String createMessage(@Parameter(hidden = true) Model model, Message message) {
        message.setId("some new id");
        model.addAttribute("message", message);

        return "index.html";
    }

    @PUT
    @Path("/{id}")
    public Message updateMessage(@PathParam("id") String id, Message message) {
        message.setId(id);
        return message;
    }

    @DELETE
    @Path("/{id}")
    public void deleteMessage(@PathParam("id") String id) {
        // some logic;
    }

}
