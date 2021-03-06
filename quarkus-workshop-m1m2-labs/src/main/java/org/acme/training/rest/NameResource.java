package org.acme.training.rest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.reactivestreams.Publisher;

@Path("/names")
public class NameResource {

    @Inject
    @Channel("my-data-stream")
    Publisher<String> names;

    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Publisher<String> stream() {
        return names;
    }
}
