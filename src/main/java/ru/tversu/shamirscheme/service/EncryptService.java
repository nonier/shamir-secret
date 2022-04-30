package ru.tversu.shamirscheme.service;

import ru.tversu.shamirscheme.model.Secret;
import ru.tversu.shamirscheme.model.SecretPart;

import java.util.List;

public interface EncryptService {

    List<SecretPart> encrypt(Secret secret, Integer partsCount) throws Exception;

    List<SecretPart> generateNewParts(List<SecretPart> secretParts, Integer partsCount) throws Exception;
}
