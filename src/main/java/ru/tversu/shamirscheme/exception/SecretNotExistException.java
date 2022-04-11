package ru.tversu.shamirscheme.exception;

import lombok.Getter;
import ru.tversu.shamirscheme.model.Secret;

@Getter
public class SecretNotExistException extends Exception{

    private Secret secret;

    public SecretNotExistException(String message, Secret secret){
        super(message);
        this.secret = secret;
    }
}
