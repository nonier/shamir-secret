package ru.tversu.shamirscheme.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.tversu.shamirscheme.exception.BadKeyFormatException;
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
    public static final Integer RANDOM_GEN_BOUND = 1000;

    public static final Random GENERATOR = new Random();

    private final FileManager fileManager;
    private final DecryptService decryptService;

    @Autowired
    public EncryptServiceImpl(FileManager fileManager, DecryptService decryptService) {
        this.fileManager = fileManager;
        this.decryptService = decryptService;
    }


    @Override
    public List<SecretPart> encrypt(Secret secret, Integer partsCount) throws Exception {
        log.debug("Попытка зашифровать секрет: {}", secret);
        Integer p = secret.getP();
        if (p == null) {
            while (true) {
                p = GENERATOR.nextInt(RANDOM_GEN_BOUND) + secret.getSecret() + 1;
                if (Utils.isSimple(p)) {
                    break;
                }
            }
        }
        secret.setP(p);
        if (secret.getSecret() >= secret.getP()) {
            throw new BadKeyFormatException("Неправильный формат ключа, параметр p должен быть больше secret");
        }
        if (!Utils.isSimple(secret.getP())) {
            throw new NotSimpleDigitException("Параметр p не является простым числом");
        }
        if (fileManager.checkIfSecretExists(secret)) {
            throw new SecretAlreadyExistsException("Невозможно создать части секрета, такой секрет уже существует");
        }
        List<Integer> polynomialCoffs = new ArrayList<>();
        polynomialCoffs.add(secret.getSecret());
        List<SecretPart> result = generateSecretParts(secret.getP(), polynomialCoffs, partsCount, new ArrayList<>());

        log.debug("Получены части ключа: {}", result);
        fileManager.writeSecret(secret, result);
        return result;
    }

    @Override
    public List<SecretPart> generateNewParts(List<SecretPart> secretParts, Integer partsCount) throws Exception {
        List<Integer> recoveredPolynomialCoffs = decryptService.recoverPolynomialCoefficients(secretParts);
        List<Integer> points = secretParts.stream().map(SecretPart::getPoint).toList();
        List<SecretPart> newSecretParts = generateSecretParts(secretParts.get(0).getP(), recoveredPolynomialCoffs, partsCount, points);
        return newSecretParts;
    }

    private List<SecretPart> generateSecretParts(Integer p, List<Integer> polynomialCoffs, Integer partsCount, List<Integer> points) {

        if (polynomialCoffs.size() == 1) {
            for (int i = 0; i < K - 1; i++) {
                int param = GENERATOR.nextInt(RANDOM_GEN_BOUND) % p;
                while (param == 0) {
                    param = GENERATOR.nextInt(RANDOM_GEN_BOUND) % p;
                }
                polynomialCoffs.add(param);
            }
        }
        log.debug("Сгенерированы коэффициенты полинома: {}", polynomialCoffs);











































        List<Integer> partPoints = new ArrayList<>();

        for (int i = 0; i < partsCount; i++) {
            int partPoint = GENERATOR.nextInt(RANDOM_GEN_BOUND) % p;
            while (partPoints.contains(partPoint) ||
                    points.contains(partPoint) ||
                    partPoint == 0) {
                partPoint = GENERATOR.nextInt(RANDOM_GEN_BOUND) % p;
            }
            partPoints.add(partPoint);
        }

        return partPoints.stream().map(partPoint ->
                        SecretPart.builder()
                                .point(partPoint)
                                .value(getValue(partPoint, p, polynomialCoffs))
                                .p(p)
                                .build()
                )
                .toList();
    }

    private Integer getValue(int partPoint, Integer p, List<Integer> functionParam) {
        Integer result = 0;
        Integer pow = 1;
        for (int i = 1; i < functionParam.size(); i++) {
            pow = (pow * partPoint) % p;
            result = (result + functionParam.get(i) * pow) % p;
        }
        result = (result + functionParam.get(0)) % p;
        return result;
    }
}
