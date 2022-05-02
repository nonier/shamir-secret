package ru.tversu.shamirscheme.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import ru.tversu.shamirscheme.model.Secret;
import ru.tversu.shamirscheme.model.SecretPart;
import ru.tversu.shamirscheme.service.FileManager;

import java.io.*;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class FileManagerImpl implements FileManager {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void writeSecret(Secret secret, List<SecretPart> secretParts) {
        Path secretSource = Path.of("src", "main", "resources", "secrets", secret.getSecret().toString(), "secret");
        secretSource.toFile().mkdirs();
        Path secretPartsSource = Path.of(secretSource.getParent().toString(), "secretParts");
        secretPartsSource.toFile().mkdir();
        File secretFile = Path.of(secretSource.toString(), "secret.json").toFile();
        try {
            FileWriter fileWriter = new FileWriter(secretFile);
            fileWriter.write(OBJECT_MAPPER.writeValueAsString(secret));
            fileWriter.close();
            for (int i = 0; i < secretParts.size(); i++) {
                File secretPartFile = Path.of(secretPartsSource.toString(), secretParts.get(i).hashCode() + ".json").toFile();
                secretPartFile.createNewFile();
                fileWriter = new FileWriter(secretPartFile);
                fileWriter.write(OBJECT_MAPPER.writeValueAsString(secretParts.get(i)));
                fileWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Integer> getSecretPartsPoints(Secret secret) {
        File secretPartsFolder = Path.of("src", "main", "resources", "secrets", secret.getSecret().toString(), "secretParts").toFile();
        List<Integer> result = new ArrayList<>();
        for (File secretPartFile : secretPartsFolder.listFiles()) {
            try (FileReader fileReader = new FileReader(secretPartFile);
                 BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    json.append(line);
                }
                SecretPart secretPart = OBJECT_MAPPER.readValue(json.toString(), SecretPart.class);
                result.add(secretPart.getPoint());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public boolean checkIfSecretExists(Secret result) {
        return Path.of("src", "main", "resources", "secrets", result.getSecret().toString()).toFile().exists();
    }
}
