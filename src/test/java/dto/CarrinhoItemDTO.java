package dto;

public class CarrinhoItemDTO {
    private String idProduto;
    private int quantidade;

    public CarrinhoItemDTO(String idProduto, int quantidade) {
        this.idProduto = idProduto;
        this.quantidade = quantidade;
    }

    public String getIdProduto() {
        return idProduto;
    }

    public int getQuantidade() {
        return quantidade;
    }
}
