package com.example.glight.data.local

import com.example.glight.domain.model.Pole
import com.example.glight.domain.model.PoleStatus

object InitialDataProvider {

    fun getInitialPoles(): List<Pole> = listOf(
        Pole("GL-001", 12.9716, 77.5946, PoleStatus.WORKING, bulbType = "LED 24W"),
        Pole("GL-002", 12.9721, 77.5951, PoleStatus.FUSED, bulbType = "LED 24W"),
        Pole("GL-003", 12.9709, 77.5937, PoleStatus.BURNING_DAYTIME, bulbType = "Sodium 70W"),
        Pole("GL-004", 12.9712, 77.5963, PoleStatus.WORKING, bulbType = "LED 18W"),
        Pole("GL-005", 12.9728, 77.5941, PoleStatus.WORKING, bulbType = "LED 24W"),
        Pole("GL-006", 12.9702, 77.5952, PoleStatus.FUSED, bulbType = "LED 18W"),
        Pole("GL-007", 12.9735, 77.5958, PoleStatus.WORKING, bulbType = "LED 24W"),
        Pole("GL-008", 12.9698, 77.5944, PoleStatus.WORKING, bulbType = "LED 18W"),
        Pole("GL-009", 12.9718, 77.5932, PoleStatus.BURNING_DAYTIME, bulbType = "Sodium 70W"),
        Pole("GL-010", 12.9725, 77.5965, PoleStatus.WORKING, bulbType = "LED 24W"),
        Pole("GL-011", 12.9742, 77.5938, PoleStatus.WORKING, bulbType = "LED 24W"),
        Pole("GL-012", 12.9685, 77.5955, PoleStatus.FUSED, bulbType = "Sodium 70W"),
        Pole("GL-013", 12.9751, 77.5949, PoleStatus.WORKING, bulbType = "LED 24W"),
        Pole("GL-014", 12.9672, 77.5931, PoleStatus.BURNING_DAYTIME, bulbType = "LED 18W"),
        Pole("GL-015", 12.9765, 77.5922, PoleStatus.WORKING, bulbType = "LED 24W"),
        Pole("GL-016", 12.9668, 77.5968, PoleStatus.WORKING, bulbType = "LED 18W"),
        Pole("GL-017", 12.9772, 77.5951, PoleStatus.FUSED, bulbType = "LED 24W"),
        Pole("GL-018", 12.9655, 77.5942, PoleStatus.WORKING, bulbType = "LED 24W"),
        Pole("GL-019", 12.9788, 77.5935, PoleStatus.BURNING_DAYTIME, bulbType = "Sodium 70W"),
        Pole("GL-020", 12.9642, 77.5959, PoleStatus.WORKING, bulbType = "LED 18W")
    )
}
