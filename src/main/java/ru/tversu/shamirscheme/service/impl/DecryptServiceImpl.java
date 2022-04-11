package ru.tversu.shamirscheme.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.tversu.shamirscheme.exception.DifferentPException;
import ru.tversu.shamirscheme.exception.NotEnoughPartsException;
import ru.tversu.shamirscheme.exception.NotSimpleDigitException;
import ru.tversu.shamirscheme.exception.SecretNotExistException;
import ru.tversu.shamirscheme.model.Secret;
import ru.tversu.shamirscheme.model.SecretPart;
import ru.tversu.shamirscheme.service.DecryptService;
import ru.tversu.shamirscheme.service.FileManager;
import ru.tversu.shamirscheme.utils.Utils;

import java.util.List;

@Slf4j
@Service
public class DecryptServiceImpl implements DecryptService {

    public static final Integer K=4;

    private final FileManager fileManager;

    @Autowired
    public DecryptServiceImpl(FileManager fileManager) {
        this.fileManager = fileManager;
    }


    @Override
    public Secret decrypt(List<SecretPart> secretParts) throws Exception {
        log.debug("Попытка восстановить ключ из частей: {}", secretParts);

        if (secretParts.size() < K) {
            throw new NotEnoughPartsException("Недостаточно частей для восстановления ключа");
        }
        Integer p = secretParts.get(0).getP();
        if (secretParts.stream().map(SecretPart::getP).anyMatch(x -> !p.equals(x))) {
            throw new DifferentPException("Несовпадающий параметр p в частях ключа");
        }
        if (!Utils.isSimple(secretParts.get(0).getP())) {
            throw new NotSimpleDigitException("Параметр p не является простым числом");
        }
        Integer secret = 0;
        Integer l;
        for (int i = 0; i < K; i++) {
            l = 1;
            for (int j = 0; j < K; j++) {
                if (j == i) {
                    continue;
                }
                int numerator = (((secretParts.get(i).getPoint() - secretParts.get(j).getPoint()) % p + p) % p);
                l = ((l * (((-secretParts.get(j).getPoint() % p + p) % p)
                        * getOpposite(numerator, p) % p) % p) % p);
            }
            secret = (secret + ((l * secretParts.get(i).getValue()) % p) % p);
        }
        Secret result = Secret
                .builder()
                .secret(secret % p)
                .p(p)
                .build();

        log.debug("Восстановлен ключ: {}", secret);
        if (!fileManager.checkIfSecretExists(result)) {
            throw new SecretNotExistException("Восстановленного секрета не существует", result);
        }
        return result;
    }

    @Override
    public List<Integer> getPolynomialCoefficients(List<SecretPart> secretParts) {
        Integer p = secretParts.get(0).getP();
        Integer secret = 0;
        Integer l;
        Integer x1;
        Integer x2;
        Integer x3;
        for (int i = 0; i < K; i++) {
            l = 1;
            for (int j = 0; j < K; j++) {
                if (j == i) {
                    continue;
                }
                int numerator = (((secretParts.get(i).getPoint() - secretParts.get(j).getPoint()) % p + p) % p);
                l = ((l * (((-secretParts.get(j).getPoint() % p + p) % p)
                        * getOpposite(numerator, p) % p) % p) % p);
            }
        }
        return null;
    }

    private int getOpposite(int x, int p) {
        int result = 0;
        for (int i = 1; i > 0; i++) {
            if (((x * i) % p) == 1) {
                result = i;
                break;
            }
        }
        return result;
    }


}
