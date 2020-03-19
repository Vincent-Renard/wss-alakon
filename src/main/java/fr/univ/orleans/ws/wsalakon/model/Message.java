package fr.univ.orleans.ws.wsalakon.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * @autor Vincent
 * @date 19/03/2020
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@AllArgsConstructor
@ToString
public class Message {
    long id;
    final String texte;

    public Message(String texte) {
        this.texte = texte;
    }
}
