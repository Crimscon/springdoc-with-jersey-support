package com.github.crimscon.autoconfigure.springdoc.resource;

import org.springframework.stereotype.Service;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Service
@Path("/message")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MessageResource extends SuperMessageResource {

    private final CrudMessageSubResource messageSubResource;

    public MessageResource(CrudMessageSubResource messageSubResource) {
        this.messageSubResource = messageSubResource;
    }

    @Path("/")
    public CrudMessageSubResource messageSubResource() {
        return messageSubResource;
    }

}
