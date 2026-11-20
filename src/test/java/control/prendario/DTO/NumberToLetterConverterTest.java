package control.prendario.DTO;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NumberToLetterConverterTest {

    @Test
    void convierteCeroUnidadesCentenasYCasosEspeciales() {
        assertEquals("CERO PESOS", NumberToLetterConverter.convertNumberToLetter(0));
        assertEquals("UN PESOS", NumberToLetterConverter.convertNumberToLetter(1));
        assertEquals("CIEN PESOS", NumberToLetterConverter.convertNumberToLetter(100));
        assertEquals("CIENTO ONCE PESOS", NumberToLetterConverter.convertNumberToLetter(111));
        assertEquals("DOSCIENTOS TREINTA Y CUATRO PESOS",
                NumberToLetterConverter.convertNumberToLetter(234));
    }

    @Test
    void convierteMilesMillonesYOmiteGruposVacios() {
        assertEquals("MIL PESOS", NumberToLetterConverter.convertNumberToLetter(1000));
        assertEquals("DOS MIL PESOS", NumberToLetterConverter.convertNumberToLetter(2000));
        assertEquals("UN MILLON UN PESOS", NumberToLetterConverter.convertNumberToLetter(1_000_001));
        assertEquals("DOS MILLONES TRES MIL CUATRO PESOS",
                NumberToLetterConverter.convertNumberToLetter(2_003_004));
    }
}
