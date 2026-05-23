package dto;

import java.util.List;

public class CarrinhoDTO {
    private List<CarrinhoItemDTO> produtos;

    public CarrinhoDTO(List<CarrinhoItemDTO> produtos) {
        this.produtos = produtos;
    }

    public List<CarrinhoItemDTO> getProdutos() {
        return produtos;
    }
}
