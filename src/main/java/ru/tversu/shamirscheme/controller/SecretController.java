package ru.tversu.shamirscheme.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tversu.shamirscheme.model.Secret;
import ru.tversu.shamirscheme.model.SecretPart;
import ru.tversu.shamirscheme.service.DecryptService;
import ru.tversu.shamirscheme.service.EncryptService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/secret")
@Tag(name = "Secret controller", description = "Контроллер для создания и довыпуска частей секрета, восстановления секрета")
public class SecretController {

    private final EncryptService encryptService;
    private final DecryptService decryptService;

    @Autowired
    public SecretController(EncryptService encryptService, DecryptService decryptService) {
        this.encryptService = encryptService;
        this.decryptService = decryptService;
    }

    @Operation(
            summary = "Генерирование частей секрета",
            description = "Передается сам секрет, количество генерируемых частей и параметр p - простое число"
    )
    @PostMapping
    public ResponseEntity encryptSecret(
            @Parameter(description = "Сущность для генерирования частей секрета") @RequestBody Secret secret,
            @Parameter(description = "Количество генерируемых частей") @RequestParam(value = "partsCount") Integer partsCount) {
        try {
            return ResponseEntity.ok(encryptService.encrypt(secret, partsCount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Восстановление секрета из частей",
            description = "Восстанавливает секрет из частей, их должно быть минимум 4"
    )
    @GetMapping
    public ResponseEntity decryptSecret(
            @Parameter(description = "Части секрета для его восстановления") @RequestBody List<SecretPart> secretParts) {
        try {
            return ResponseEntity.ok(decryptService.decrypt(secretParts));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Довыпуск частей секрета",
            description = "Генерирует дополнительные части секрета, для этого нужно передать минимум 4 части"
    )
    @PutMapping
    public ResponseEntity generateNewParts(
            @Parameter(description = "Количество генерируемых частей") @RequestParam(value = "parts-count") Integer partsCount,
            @Parameter(description = "Части секрета для генерации новых") @RequestBody List<SecretPart> secretParts) {
        try {
            return ResponseEntity.ok(encryptService.generateNewParts(secretParts, partsCount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
