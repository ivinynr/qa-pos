package serverrest.test.carrinho;

import com.github.javafaker.Faker;
import dto.CarrinhoDTO;
import dto.CarrinhoItemDTO;
import dto.LoginDTO;
import dto.ProdutoDTO;
import dto.UsuarioReqDTO;
import io.qameta.allure.*;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;
import serverrest.BaseTest;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("Loja Virtual")
@Feature("Carrinho")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CarrinhoTest extends BaseTest {

    static String produtoId;

    @BeforeAll
    static void gerarMassaDados() {
        UsuarioReqDTO usuarioReqDTO = new UsuarioReqDTO("true");
        doPost(usuarioReqDTO, "/usuarios", HttpStatus.SC_CREATED);

        LoginDTO loginDTO = new LoginDTO(usuarioReqDTO.getEmail(), usuarioReqDTO.getPassword());
        TOKEN = getToken(loginDTO);

        Faker faker = new Faker();
        ProdutoDTO produtoDTO = new ProdutoDTO(
                faker.commerce().productName() + System.currentTimeMillis(),
                100, "Produto para teste de carrinho", 10
        );
        produtoId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", TOKEN)
                .body(produtoDTO)
                .when()
                .post("/produtos")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract().jsonPath().getString("_id");
    }

    @Test
    @Order(1)
    @Story("Listar carrinhos")
    @Description("Deve listar todos os carrinhos cadastrados")
    @Severity(SeverityLevel.NORMAL)
    public void deveListarCarrinhos() {
        doGet("/carrinhos", HttpStatus.SC_OK)
                .body("quantidade", greaterThanOrEqualTo(0))
                .body("carrinhos", notNullValue());
    }

    @Test
    @Order(2)
    @Story("Cadastrar carrinho sem autenticacao")
    @Description("Nao deve cadastrar carrinho sem token de autenticacao")
    @Severity(SeverityLevel.NORMAL)
    public void naoDeveCadastrarCarrinhoSemToken() {
        CarrinhoDTO carrinho = new CarrinhoDTO(List.of(new CarrinhoItemDTO(produtoId, 1)));

        given()
                .contentType(ContentType.JSON)
                .body(carrinho)
                .when()
                .post("/carrinhos")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .body("message", containsString("Token de acesso ausente"));
    }

    @Test
    @Order(3)
    @Story("Cadastrar carrinho")
    @Description("Deve cadastrar um carrinho com produto valido e token valido")
    @Severity(SeverityLevel.CRITICAL)
    public void deveCadastrarCarrinhoComSucesso() {
        CarrinhoDTO carrinho = new CarrinhoDTO(List.of(new CarrinhoItemDTO(produtoId, 1)));

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", TOKEN)
                .body(carrinho)
                .when()
                .post("/carrinhos")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("message", containsString("Cadastro realizado com sucesso"));
    }

    @Test
    @Order(4)
    @Story("Cancelar compra")
    @Description("Deve cancelar a compra, excluir o carrinho e repor o estoque dos produtos")
    @Severity(SeverityLevel.CRITICAL)
    public void deveCancelarCompraERepoeEstoque() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", TOKEN)
                .when()
                .delete("/carrinhos/cancelar-compra")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("message", containsString("Registro excluído com sucesso"));
    }
}
