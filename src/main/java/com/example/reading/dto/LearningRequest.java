package com.example.reading.dto;

import com.example.reading.credential.Provider;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO containing user instruction and text content to process.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningRequest {

    @NotBlank(message = "Instruction must not be blank")
    private String instruction;

    @NotBlank(message = "Text must not be blank")
    @Size(max = 50000, message = "Text must not exceed 50000 characters")
    private String text;

    private Provider provider;
}
