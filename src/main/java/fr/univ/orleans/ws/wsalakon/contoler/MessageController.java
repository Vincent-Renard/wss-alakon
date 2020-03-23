package fr.univ.orleans.ws.wsalakon.contoler;

import fr.univ.orleans.ws.wsalakon.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @autor Vincent
 * @date 19/03/2020
 */

@RestController
@RequestMapping("/api")
public class MessageController {

    private static final Logger log = LoggerFactory.getLogger(MessageController.class);
    private List<Message> messages = new ArrayList<>();
    private final AtomicLong counter = new AtomicLong(1L);
    private ReplayProcessor<Message> notifications = ReplayProcessor.create(0, false);

    @PostMapping("/messages")
    ResponseEntity<Message> create(@RequestBody Message message, UriComponentsBuilder baseUrl) {
        log.debug(message.toString());
        Message m = new Message(counter.getAndIncrement(), message.getTexte());
        messages.add(m);
        notifications.onNext(m);
        URI loc = baseUrl
                .path("api/messages/{id}")
                .buildAndExpand(m.getId()).toUri();

        return ResponseEntity.created(loc).body(m);
    }

    @GetMapping("/messages")
    ResponseEntity<Collection<Message>> getAll() {
        return ResponseEntity.ok(messages);
    }

    @GetMapping(value = "/messages/subscribe", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    Flux<Message> subscribe() {
        return Flux.from(notifications);
    }

    @GetMapping("/messages/{id}")
    ResponseEntity<Message> findById(@PathVariable long id) {
        Optional<Message> m = messages.stream().filter(message -> message.getId() == id).findAny();
        if (m.isPresent())
            return ResponseEntity.ok(m.get());
        else
            return ResponseEntity.notFound().build();

    }

    @DeleteMapping("/messages/{id}")
    ResponseEntity deleteById(@PathVariable long id){

        for (int idx = 0; idx < messages.size(); idx++) {
            if (messages.get(idx).getId() == id) {
                messages.remove(idx);
                return ResponseEntity.noContent().build();
            }
        }
        return ResponseEntity.notFound().build();

    }

    @PatchMapping("/messages/{id}")
    ResponseEntity<Message> patch(@PathVariable long id, @RequestBody Message mtopatch) {

        for (int idx = 0; idx < messages.size(); idx++) {
            if (messages.get(idx).getId() == id) {
                Message m = new Message(id, mtopatch.getTexte());
                messages.set(idx, m);
                return ResponseEntity.ok(m);
            }
        }
        return ResponseEntity.notFound().build();
    }

}
