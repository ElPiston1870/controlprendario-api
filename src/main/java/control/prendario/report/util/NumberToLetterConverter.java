package control.prendario.report.util;

public class NumberToLetterConverter {
    private static final String[] UNITS = {
            "", "UN ", "DOS ", "TRES ", "CUATRO ", "CINCO ", "SEIS ", "SIETE ", "OCHO ", "NUEVE "
    };
    private static final String[] TENS = {
            "", "DIEZ ", "VEINTE ", "TREINTA ", "CUARENTA ", "CINCUENTA ", "SESENTA ", "SETENTA ", "OCHENTA ", "NOVENTA "
    };
    private static final String[] HUNDREDS = {
            "", "CIENTO ", "DOSCIENTOS ", "TRESCIENTOS ", "CUATROCIENTOS ", "QUINIENTOS ", "SEISCIENTOS ",
            "SETECIENTOS ", "OCHOCIENTOS ", "NOVECIENTOS "
    };
    private static final String[] SPECIAL_NUMBERS = {
            "DIEZ ", "ONCE ", "DOCE ", "TRECE ", "CATORCE ", "QUINCE ", "DIECISEIS ", "DIECISIETE ", "DIECIOCHO ", "DIECINUEVE "
    };

    public static String convertNumberToLetter(double number) {
        StringBuilder result = new StringBuilder();
        long integerPart = (long) number;

        if (integerPart == 0) {
            return "CERO PESOS";
        }

        if (integerPart >= 1000000) {
            int millions = (int) (integerPart / 1000000);
            integerPart = integerPart % 1000000;
            if (millions == 1) {
                result.append("UN MILLON ");
            } else {
                result.append(convertGroup(millions)).append("MILLONES ");
            }
        }

        if (integerPart >= 1000) {
            int thousands = (int) (integerPart / 1000);
            integerPart = integerPart % 1000;
            if (thousands == 1) {
                result.append("MIL ");
            } else {
                result.append(convertGroup(thousands)).append("MIL ");
            }
        }

        if (integerPart > 0) {
            result.append(convertGroup((int) integerPart));
        }

        return result.append("PESOS").toString().trim();
    }

    private static String convertGroup(int number) {
        StringBuilder builder = new StringBuilder();

        int hundreds = number / 100;
        int tens = (number % 100) / 10;
        int units = number % 10;

        // Manejo de centenas
        if (hundreds == 1 && tens == 0 && units == 0) {
            return "CIEN ";
        }
        if (hundreds > 0) {
            builder.append(HUNDREDS[hundreds]);
        }

        // Manejo de decenas y unidades
        if (tens == 1) {
            builder.append(SPECIAL_NUMBERS[units]);
        } else if (tens == 2 && units > 0) {
            builder.append("VEINTI").append(UNITS[units].toLowerCase());
        } else {
            if (tens > 0) {
                builder.append(TENS[tens]);
                if (units > 0) {
                    builder.append("Y ").append(UNITS[units]);
                }
            } else if (units > 0) {
                builder.append(UNITS[units]);
            }
        }

        return builder.toString();
    }
}