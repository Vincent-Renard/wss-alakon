package fr.univ.orleans.ws.wsalakon.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * @autor Vincent
 * @date 19/03/2020
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Utilisateur {

     String login;
     String password;
     boolean isAdmin;
}
