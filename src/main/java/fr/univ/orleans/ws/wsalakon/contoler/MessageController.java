package fr.univ.orleans.ws.wsalakon.contoler;

import fr.univ.orleans.ws.wsalakon.model.Message;
import fr.univ.orleans.ws.wsalakon.model.Utilisateur;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @autor Vincent
 * @date 19/03/2020
 */

@RestController
@RequestMapping("/api")
public class MessageController {

    private static final Logger log = LoggerFactory.getLogger(MessageController.class);
    private Map<Long, Message> messages = new TreeMap<>();
    @Getter
    private static Map<String, Utilisateur> usersByPseudo = new TreeMap<>();
    Predicate<String> verifMessage = ch -> ch.length() > 1 && ch.length() < 256;

    private final AtomicLong counter = new AtomicLong(1L);

    static {

        usersByPseudo.put("fred", new Utilisateur("fred", "fred", false));
        usersByPseudo.put("Vincent", new Utilisateur("Vincent", "vinc", false));
        usersByPseudo.put("admin", new Utilisateur("admin", "admin", true));
    }

    @PostMapping("/messages")
    ResponseEntity<Message> create(Principal principal, @RequestBody Message message) {
        if (!verifMessage.test(message.getTexte())) {
            ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
        }
        String login = principal.getName();
        long id = counter.getAndIncrement();
        Message m = new Message(id, message.getTexte(), login, LocalDateTime.now());
        messages.put(id, m);
        URI loc = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(m.getId()).toUri();

        return ResponseEntity.created(loc).body(m);
    }

    @GetMapping("/messages")
    ResponseEntity<Collection<Message>> getAll(){
        return ResponseEntity.ok(messages.values());
    }


    @GetMapping("/messages/{id}")
    ResponseEntity<Message> findById(@PathVariable long id) {
        // Optional<Message> m = messages.stream().filter(message ->message.getId()==id).findAny();
        if (messages.containsKey(id))
            return ResponseEntity.ok(messages.get(id));
        else
            return ResponseEntity.notFound().build();

    }

    @DeleteMapping("/messages/{id}")
    ResponseEntity<Void> deleteById(Principal up, @PathVariable long id) {

        if (messages.containsKey(id)) {
            if (messages.get(id).getExp().equals(up.getName()) || usersByPseudo.get(up.getName()).isAdmin()) {

                messages.remove(id);
                return ResponseEntity.noContent().build();
            } else return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.notFound().build();

    }

    @PatchMapping("/messages/{id}")
    ResponseEntity<Message> patch(Principal principal, @PathVariable long id, @RequestBody @NotNull Message message) {

        if (messages.containsKey(id)) {
            if (!verifMessage.test(message.getTexte())) {
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
            }
            Message oldOne = messages.get(id);
            if (oldOne.getExp().equals(principal.getName())) {
                oldOne.setTexte(message.getTexte());
                messages.put(id, oldOne);
                return ResponseEntity.ok(oldOne);
            } else ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
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

    @GetMapping("/users")
    ResponseEntity<Collection<String>> getUsers() {

        return ResponseEntity.ok().body(usersByPseudo.keySet());
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

    @GetMapping("/users/{login}/messages")
    ResponseEntity<Collection<Message>> findByUser(@PathVariable("login") String pseudo) {
        if (usersByPseudo.containsKey(pseudo)) {
            return ResponseEntity.ok().body(messages.values()
                    .stream().filter(m -> m.getExp().equals(pseudo))
                    .collect(Collectors.toList()));
        }
        return ResponseEntity.notFound().build();

    }

    @GetMapping("/users2/{login}")
    @PreAuthorize("#pseudo == authentication.principal.username")
    ResponseEntity<Utilisateur> findById2(@PathVariable("login") String pseudo) {

        if (!usersByPseudo.containsKey(pseudo)) {
            return ResponseEntity.notFound().build();
        }

        Utilisateur u = usersByPseudo.get(pseudo);

        return ResponseEntity.ok().body(u);
    }

    //@PreAuthorize("#pseudo == authentication.principal.username")
    @DeleteMapping("/users/{login}")
    ResponseEntity<Void> delUser(Principal p, @PathVariable("login") String pseudo) {
        System.out.println(usersByPseudo.get(pseudo));
        System.out.println(usersByPseudo.get(p.getName()));
        System.out.println(!usersByPseudo.get(p.getName()).isAdmin());
        System.out.println(!pseudo.equals(p.getName()));
        System.out.println(!pseudo.equals(p.getName()) && !usersByPseudo.get(p.getName()).isAdmin());
        if (!pseudo.equals(p.getName()) && !usersByPseudo.get(p.getName()).isAdmin()) {
            System.out.println("pas admin");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!usersByPseudo.containsKey(pseudo)) {
            return ResponseEntity.notFound().build();
        }
        Collection<Long> mOfUser = messages.values()
                .stream()
                .filter(m -> m.getExp().equals(pseudo))
                .map(Message::getId)
                .collect(Collectors.toSet());
        System.out.println();
        mOfUser.forEach(System.out::println);
        mOfUser.forEach(id -> messages.remove(id));
        //.forEach(i-> messages.remove(i));
        usersByPseudo.remove(pseudo);
        return ResponseEntity.noContent().build();
    }
}
