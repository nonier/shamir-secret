package ru.tversu.shamirscheme.service;

import ru.tversu.shamirscheme.model.Secret;
import ru.tversu.shamirscheme.model.SecretPart;

import java.util.List;

public interface FileManager {

    void writeSecret(Secret secret, List<SecretPart> secretParts);

    List<Integer> getSecretPartsPoints(Secret secret);

    boolean checkIfSecretExists(Secret result);

}
