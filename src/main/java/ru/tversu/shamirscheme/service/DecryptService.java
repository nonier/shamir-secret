package ru.tversu.shamirscheme.service;

import ru.tversu.shamirscheme.model.Secret;
import ru.tversu.shamirscheme.model.SecretPart;

import java.util.List;

public interface DecryptService {

    Secret decrypt(List<SecretPart> secretParts) throws Exception;

    public List<Integer> recoverPolynomialCoefficients(List<SecretPart> secretParts);

}
