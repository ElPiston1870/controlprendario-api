package control.prendario.client.dto;

public record ClientHistoryQuery(Integer pagina, Integer tamano, Integer paginaPrestamosVehiculares,
        Integer paginaPrestamosMaquinaria, Integer paginaPagosVehiculares, Integer paginaPagosMaquinaria) {
    public Integer orDefault(Integer number) {
        return number == null ? pagina : number;
    }
}
