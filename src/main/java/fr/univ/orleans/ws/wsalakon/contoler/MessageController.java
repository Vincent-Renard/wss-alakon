package fr.univ.orleans.ws.wsalakon.contoler;

import fr.univ.orleans.ws.wsalakon.model.Message;
import fr.univ.orleans.ws.wsalakon.model.Utilisateur;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

/**
 * @autor Vincent
 * @date 19/03/2020
 */

@RestController
@RequestMapping("/api")
public class MessageController {

    private static final Logger log = LoggerFactory.getLogger(MessageController.class);
    private List<Message> messages = new ArrayList<>();
    @Getter
    private static Map<String, Utilisateur> usersByPseudo = new TreeMap<>();


    private final AtomicLong counter = new AtomicLong(1L);

    static {

        usersByPseudo.put("fred", new Utilisateur("fred", "fred", false));
        usersByPseudo.put("Vincent", new Utilisateur("Vincent", "vinc", false));
        usersByPseudo.put("admin", new Utilisateur("admin", "admin", true));
    }

    @PostMapping("/messages")
    ResponseEntity<Message> create(Principal principal, @RequestBody Message message) {
        log.debug(message.toString());
        String login = principal.getName();
        Message m = new Message(counter.getAndIncrement(), "[" + login + "] : " + message.getTexte());
        messages.add(m);
        URI loc = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(m.getId()).toUri();

        return ResponseEntity.created(loc).body(m);
    }

    @GetMapping("/messages")
    ResponseEntity<Collection<Message>> getAll(){
        return ResponseEntity.ok(messages);
    }


    @GetMapping("/messages/{id}")
    ResponseEntity<Message> findById(@PathVariable long id){
        Optional<Message> m = messages.stream().filter(message ->message.getId()==id).findAny();
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

    @PostMapping("/users")
    ResponseEntity<Utilisateur> register(@RequestBody Utilisateur user) {
        System.out.println(user.toString());
        Predicate<String> isOk = s -> (s != null) && (s.length() >= 2);
        if (!isOk.test(user.getLogin()) || !isOk.test(user.getPassword())) {
            return ResponseEntity.badRequest().build();
        }
        if (usersByPseudo.containsKey(user.getLogin())) {
            return ResponseEntity.unprocessableEntity().build();
        }
        usersByPseudo.put(user.getLogin(), user);
        System.out.println("put ");

        URI loc = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{login}")
                .buildAndExpand(user.getLogin()).toUri();
        return ResponseEntity.created(loc).body(user);
    }

    @GetMapping("/users/{pseudo}")
    ResponseEntity<Utilisateur> findById(Principal principal, @PathVariable String pseudo) {
        if (!principal.getName().equals(pseudo)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!usersByPseudo.containsKey(pseudo)) {
            return ResponseEntity.notFound().build();
        }

        Utilisateur u = usersByPseudo.get(pseudo);

        return ResponseEntity.ok().body(u);
    }
}
