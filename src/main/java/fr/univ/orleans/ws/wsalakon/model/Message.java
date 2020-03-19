package fr.univ.orleans.ws.wsalakon.model;


import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * @autor Vincent
 * @date 19/03/2020
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    long id;
    String texte;

}
