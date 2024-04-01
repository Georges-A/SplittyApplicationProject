/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client.utils;

import com.google.gson.JsonObject;
import com.moandjiezana.toml.Toml;
import commons.Event;
import commons.Expense;
import commons.Participant;
import commons.Quote;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

public class ServerUtils {

    private final String serverAddress;
    private ExecutorService exec = Executors.newSingleThreadExecutor();

    public ServerUtils() {
        URL resource = getClass().getClassLoader().getResource("client/server_config.toml");

        File config = null;

        if (resource == null) {
            throw new IllegalArgumentException("File not found!");
        } else {
            try {
                config = new File(resource.toURI());
            } catch (Exception e) {
                throw new IllegalArgumentException("URI not parsable");
            }
        }

        Toml toml = null;

        try {
            toml = new Toml().read(config);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse toml file: " + e.getMessage());
        }

        String address = toml.getString("address");
        long port = toml.getLong("port");

        serverAddress = address + ":" + port + "/";
        System.out.println(serverAddress);
    }

    public List<Quote> getQuotes() {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverAddress).path("api/quotes")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<List<Quote>>() {
                });
    }

    public Participant addParticipant(UUID eventId, Participant participant) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverAddress).path("api/events/" + eventId + "/participants")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .post(Entity.entity(participant, APPLICATION_JSON), Participant.class);
    }

    public Participant getParticipant(UUID eventId, UUID participantId) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverAddress).path("api/events/" + eventId + "/participants/" + participantId)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<Participant>(){});
    }

    public Participant updateParticipant(Event event, Participant participant) {
        participant.setEventPartOf(event);
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverAddress).path("api/events/" + event.getId() + "/participants/" + participant.getId())
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .put(Entity.entity(participant, APPLICATION_JSON), Participant.class);
    }

    public Event addEvent(Event event) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverAddress).path("api/events")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .post(Entity.entity(event, APPLICATION_JSON), Event.class);
    }

    public void removeParticipant(Event event, Participant participant) {
        ClientBuilder.newClient(new ClientConfig())
                .target(serverAddress).path("api/events/" + event.getId() + "/participants/" + participant.getId())
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .delete();
    }

    public Event updateEvent(Event event) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverAddress).path("api/events/" + event.getId())
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .put(Entity.entity(event, APPLICATION_JSON), Event.class);
    }

    public List<Event> getEvents() {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(serverAddress).path("api/events") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<List<Event>>() {
                });
    }

    public Event getEvent(UUID id) {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(serverAddress).path("api/events/" + id) //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<Event>() {
                });
    }

    public Event joinEvent(String inviteCode) {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(serverAddress).path("api/events/join/" + inviteCode) //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<Event>() {
                });
    }

    public Event removeEvent(UUID id) {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(serverAddress).path("api/events/" + id) //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .delete(new GenericType<Event>() {
                });
    }

    public Expense addExpense(UUID eventId, Expense exp) {
        System.out.println("ServerUtils: " + exp.getPaidBy());
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverAddress).path("/api/events/" + eventId + "/expenses")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .post(Entity.entity(exp, APPLICATION_JSON), Expense.class);
    }

    public void updateExpense(UUID eventId, UUID expenseId, Expense exp){
        System.out.println(eventId);
        ClientBuilder.newClient(new ClientConfig())
                .target(serverAddress).path("/api/events/" + eventId + "/expenses/" + expenseId)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .put(Entity.entity(exp, APPLICATION_JSON), Expense.class);
    }

    public List<Expense> getExpensesByEvent(UUID eventId) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverAddress).path("/api/events/" + eventId + "/expenses")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<List<Expense>>() {
                });
    }

    /**
     * Listen for new events
     */
    public void listenEvents(Consumer<Event> eventConsumer) {
        exec.submit(() -> {
            System.out.println("Listening for updates");
            while (!Thread.currentThread().isInterrupted()) {
                var res = ClientBuilder.newClient(new ClientConfig())
                        .target(serverAddress).path("/api/events/subscribe")
                        .request(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .get(Response.class);
                System.out.println("Got response");
                System.out.println(res.toString());
                if (res.getStatus() == 204) {
                    System.out.println("No content");
                    continue;
                }
                if (res.getStatus() != 200) {
                    System.out.println("Error: " + res.getStatus());
                    continue;
                }
                var event = res.readEntity(Event.class);
                if (event == null) {
                    System.out.println("No event");
                    continue;
                }

                eventConsumer.accept(event);
            }
            System.out.println("Stopped listening for updates");
        });
        System.out.println("Stopped listening for updates 2");
    }

    public void stopThread() {
        exec.shutdownNow();
    }


    public String checkPassword(String password) {
        return ClientBuilder.newClient(new ClientConfig())//
                .target(serverAddress).path("admin/login")//
                .request(APPLICATION_JSON)//
                .accept(APPLICATION_JSON)//
                .post(Entity.entity(password, APPLICATION_JSON), String.class);
    }

    public String getAdmin() {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(serverAddress).path("admin/") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<String>() {
                });
    }

    public void sendEmail(String toEmail, String inviteCode, String creator) {
        JsonObject body = new JsonObject();
        body.addProperty("toEmail", toEmail);
        body.addProperty("inviteCode", inviteCode);
        body.addProperty("creator", creator);
        System.out.println(body);
        ClientBuilder.newClient(new ClientConfig())//
                .target(serverAddress).path("api/mail")//
                .request(APPLICATION_JSON)//
                .accept(APPLICATION_JSON)//
                .post(Entity.entity(body.toString(), APPLICATION_JSON), String.class);
    }
}