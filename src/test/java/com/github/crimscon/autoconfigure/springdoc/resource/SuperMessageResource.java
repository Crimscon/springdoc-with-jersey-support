package com.github.crimscon.autoconfigure.springdoc.resource;

import com.github.crimscon.autoconfigure.springdoc.model.Message;
import com.github.crimscon.autoconfigure.springdoc.model.MessageFilter;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.List;

public class SuperMessageResource {

    @POST
    @Path("/filter")
    public List<Message> filter(MessageFilter filter) {
        // emulate db request and find a few messages
        return List.of(filter, filter, filter);
    }
}
