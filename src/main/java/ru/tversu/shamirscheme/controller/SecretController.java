package ru.tversu.shamirscheme.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tversu.shamirscheme.model.Secret;
import ru.tversu.shamirscheme.model.SecretPart;
import ru.tversu.shamirscheme.service.DecryptService;
import ru.tversu.shamirscheme.service.EncryptService;

import java.util.List;

@RestController
@RequestMapping("/secret")
@Slf4j
public class SecretController {

    private final EncryptService encryptService;
    private final DecryptService decryptService;

    @Autowired
    public SecretController(EncryptService encryptService, DecryptService decryptService) {
        this.encryptService = encryptService;
        this.decryptService = decryptService;
    }

    @PostMapping
    public ResponseEntity encryptSecret(@RequestBody Secret secret) {
       try{
           return ResponseEntity.ok(encryptService.encrypt(secret));
       } catch (Exception e){
           return ResponseEntity.badRequest().body(e.getMessage());
       }
    }

    @GetMapping
    public ResponseEntity decryptSecret(@RequestBody List<SecretPart> secretParts)  {
        try{
            return ResponseEntity.ok(decryptService.decrypt(secretParts));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping
    public ResponseEntity generateNewParts(
            @RequestParam(value = "parts-count") Integer partsCount,
            @RequestBody List<SecretPart> secretParts){
        try{
            return ResponseEntity.ok(encryptService.generateNewParts(secretParts, partsCount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
