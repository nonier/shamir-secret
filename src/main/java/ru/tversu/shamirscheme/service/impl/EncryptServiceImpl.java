package ru.tversu.shamirscheme.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.tversu.shamirscheme.exception.NotSimpleDigitException;
import ru.tversu.shamirscheme.exception.SecretAlreadyExistsException;
import ru.tversu.shamirscheme.model.Secret;
import ru.tversu.shamirscheme.model.SecretPart;
import ru.tversu.shamirscheme.service.DecryptService;
import ru.tversu.shamirscheme.service.EncryptService;
import ru.tversu.shamirscheme.service.FileManager;
import ru.tversu.shamirscheme.utils.Utils;

import java.util.*;

@Service
@Slf4j
public class EncryptServiceImpl implements EncryptService {

    public static final Integer K = 4;

    public static final Random GENERATOR = new Random();

    private final FileManager fileManager;
    private final DecryptService decryptService;

    @Autowired
    public EncryptServiceImpl(FileManager fileManager, DecryptService decryptService) {
        this.fileManager = fileManager;
        this.decryptService = decryptService;
    }


    @Override
    public List<SecretPart> encrypt(Secret secret) throws Exception {
        log.debug("Попытка зашифровать секрет: {}", secret);

        if (secret.getSecret() >= secret.getP()) {
            throw new Exception("Неправильный формат ключа, параметр p должен быть больше secret");
        }
        if (!Utils.isSimple(secret.getP())) {
            throw new NotSimpleDigitException("Параметр p не является простым числом");
        }
        if (fileManager.checkIfSecretExists(secret)) {
            throw new SecretAlreadyExistsException("Невозможно создать части секрета, такой секрет уже существует");
        }
        List<SecretPart> result = generateSecretParts(secret, new ArrayList<>());

        log.debug("Получены части ключа: {}", result);
        fileManager.writeSecret(secret, result);
        return result;
    }

    @Override
    public List<SecretPart> generateNewParts(List<SecretPart> secretParts, Integer partsCount) throws Exception {
        Secret secret = decryptService.decrypt(secretParts);
        secret.setPartsCount(partsCount);
        List<SecretPart> result = generateSecretParts(secret, fileManager.getSecretPartsPoints(secret));
        fileManager.writeSecret(secret, result);
        return result;
    }

    private List<SecretPart> generateSecretParts(Secret secret, List<Integer> secretPartsPoints) {
        LinkedList<Integer> polynomialCoffs = new LinkedList<>();

        int p = secret.getP();
        for (int i = 0; i < K - 1; i++) {
            int param = GENERATOR.nextInt(1000) % p;
            while (param == 0) {
                param = GENERATOR.nextInt(1000) % p;
            }
            polynomialCoffs.add(param);
        }
        log.debug("Сгенерированы коэффициенты полинома: {}", polynomialCoffs);

        List<Integer> partPoints = new ArrayList<>();

        for (int i = 0; i < secret.getPartsCount(); i++) {
            int partPoint = GENERATOR.nextInt(1000) % p;
            while (partPoints.contains(partPoint) ||
                    secretPartsPoints.contains(partPoint) ||
                    partPoint == 0) {
                partPoint = GENERATOR.nextInt(1000) % p;
            }
            partPoints.add(partPoint);
        }

        return partPoints.stream().map(partPoint ->
                        SecretPart.builder()
                                .point(partPoint)
                                .value(getValue(partPoint, secret, polynomialCoffs))
                                .p(p)
                                .build()
                )
                .toList();
    }

    private Integer getValue(int partPoint, Secret secret, List<Integer> functionParam) {
        int p = secret.getP();
        Integer result = 0;
        Integer pow = 1;
        for (int i = 0; i < functionParam.size(); i++) {
            pow = (pow * partPoint) % p;
            result = ((result + ((functionParam.get(i) * pow) % p)) % p);
        }
        result = ((result + secret.getSecret()) % p);
        return result;
    }
}
