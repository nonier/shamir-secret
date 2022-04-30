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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DecryptServiceImpl implements DecryptService {

    public static final Integer K = 4;

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
        List<Integer> polynomialCoefficients = recoverPolynomialCoefficients(secretParts);

        Secret result = Secret
                .builder()
                .secret(polynomialCoefficients.get(0) % p)
                .p(p)
                .build();

        log.debug("Восстановлен ключ: {}", result);
        if (!fileManager.checkIfSecretExists(result)) {
            throw new SecretNotExistException("Восстановленного секрета не существует", result);
        }
        return result;
    }

    @Override
    public List<Integer> recoverPolynomialCoefficients(List<SecretPart> secretParts) {

        List<Integer> polynomialCoffs = Arrays.asList(0, 0, 0, 0);
        List<Integer> points = secretParts.stream().map(SecretPart::getPoint).limit(K).toList();
        Integer p = secretParts.get(0).getP();

        for (int i = 0; i < K; i++) {
            List<Integer> liCoffs = findLiCoffs(i, points, p);
            for (int j = 0; j < K; j++) {
                liCoffs.set(j, ((liCoffs.get(j) * secretParts.get(i).getValue()) % p));
                polynomialCoffs.set(j, (polynomialCoffs.get(j) + liCoffs.get(j)) % p);
            }
        }
        log.debug("Восстановлены коэффициенты полинома: {}", polynomialCoffs);
        return polynomialCoffs;
    }


    private List<Integer> findLiCoffs(int i, List<Integer> points, int p) {
        List<Integer> numerator = Arrays.asList(1, 0, 0, 1);
        Integer denominator = 1;
        for (Integer point : points) {
            if (!Objects.equals(point, points.get(i))) {
                denominator *= (points.get(i) - point);
                numerator.set(0, numerator.get(0) * (-point));
                numerator.set(2, numerator.get(2) - point);
            }
        }
        switch (i) {
            case 0 -> numerator.set(1, (((((-points.get(1)) + (-points.get(2))) * (-points.get(3)))
                    + ((-points.get(1)) * (-points.get(2)))) % p + p) % p);
            case 1 -> numerator.set(1, (((((-points.get(0)) + (-points.get(2))) * (-points.get(3)))
                    + ((-points.get(0)) * (-points.get(2)))) % p + p) % p);
            case 2 -> numerator.set(1, (((((-points.get(0)) + (-points.get(1))) * (-points.get(3)))
                    + ((-points.get(0)) * (-points.get(1)))) % p + p) % p);
            case 3 -> numerator.set(1, (((((-points.get(0)) + (-points.get(1))) * (-points.get(2)))
                    + ((-points.get(0)) * (-points.get(1)))) % p + p) % p);
        }
        final Integer finalDenominator = getOpposite(denominator, p);

        numerator = numerator.stream()
                .map(x -> ((((x % p) + p) % p) * finalDenominator) % p)
                .collect(Collectors.toList());

        return numerator;
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
