package ru.tversu.shamirscheme.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SecretPart {
    private Integer point;
    private Integer value;
    private Integer p;
}
