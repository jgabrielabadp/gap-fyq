package com.gap.fyq.model.thirdeso.energyelectricity;

import com.gap.fyq.model.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "third_eso_energy_electricity_exercises")
@Getter
@Setter
@NoArgsConstructor
public class ThirdEsoEnergyElectricityExercise extends Exercise {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private ElectricityEnergyType energyType;

    /** Variable que se pide: "Q", "m", "ce", "deltaT", "V", "I", "R", "E_kWh", "coste" */
    @Column(nullable = false, length = 15)
    private String unknownVariable;

    @Column(nullable = false)
    private double correctAnswerValue;

    @Column(nullable = false, length = 120)
    private String correctAnswerDisplay;

    /** Unidad mostrada junto al campo de entrada: "J", "kg", "°C", "V", "A", "Ω", "kWh", "€"… */
    @Column(nullable = false, length = 20)
    private String answerUnit;

    @Column(nullable = false, length = 4000)
    private String explanation;

    @Column(nullable = false)
    private double tolerancePercent = 2.0;

    @Override
    public boolean validateAnswer(String input) {
        if (input == null || input.isBlank()) return false;
        try {
            double parsed = Double.parseDouble(
                    input.trim().replace(",", ".").replace(" ", ""));
            if (correctAnswerValue == 0) return Math.abs(parsed) < 1e-9;
            double relErr = Math.abs(parsed - correctAnswerValue) / Math.abs(correctAnswerValue);
            return relErr <= (tolerancePercent / 100.0);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
