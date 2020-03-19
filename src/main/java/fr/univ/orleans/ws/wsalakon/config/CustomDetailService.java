package fr.univ.orleans.ws.wsalakon.config;

import fr.univ.orleans.ws.wsalakon.contoler.MessageController;
import fr.univ.orleans.ws.wsalakon.model.Utilisateur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

/**
 * @autor Vincent
 * @date 19/03/2020
 */

public class CustomDetailService implements UserDetailsService {

    private static final String[] ADMIN_ROLES = {"USER", "ADMIN"};
    private static final String[] USER_ROLES = {"USER"};

    @Autowired
    private PasswordEncoder encoder;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        // s = login
        Optional<Utilisateur> u = Optional.ofNullable(MessageController.getUsersByPseudo().get(s));
        if (u.isEmpty()) {
            throw new UsernameNotFoundException("User " + s + " inconnu");
        }

        Utilisateur user = u.get();
        String[] roles = user.isAdmin() ? ADMIN_ROLES : USER_ROLES;
        UserDetails detail = User.builder().username(user.getLogin())
                .password(encoder.encode(user.getPassword()))
                .roles(roles).build();
        return detail;
    }
}
