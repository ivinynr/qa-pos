package serverrest.test.carrinho;

import com.github.javafaker.Faker;
import dto.CarrinhoDTO;
import dto.CarrinhoItemDTO;
import dto.LoginDTO;
import dto.ProdutoDTO;
import dto.UsuarioReqDTO;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;
import serverrest.BaseTest;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CarrinhoTest extends BaseTest {

    static String idProduto;
    static String idCarrinho;

    @BeforeAll
    static void gerarMassaDados() {
        UsuarioReqDTO usuarioReqDTO = new UsuarioReqDTO("true");
        doPost(usuarioReqDTO, "/usuarios", HttpStatus.SC_CREATED);

        LoginDTO loginDTO = new LoginDTO(usuarioReqDTO.getEmail(), usuarioReqDTO.getPassword());
        TOKEN = getToken(loginDTO);

        Faker faker = new Faker();
        ProdutoDTO produtoDTO = new ProdutoDTO(
                faker.commerce().productName() + System.currentTimeMillis(), 100, "Produto para teste de carrinho", 10
        );
        idProduto = given()
                .contentType(ContentType.JSON)
                .header("Authorization", TOKEN)
                .body(produtoDTO)
                .when()
                .post("/produtos")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract().jsonPath().get("_id");

        CarrinhoDTO carrinhoDTO = new CarrinhoDTO(
                List.of(new CarrinhoItemDTO(idProduto, 1))
        );
        idCarrinho = given()
                .contentType(ContentType.JSON)
                .header("Authorization", TOKEN)
                .body(carrinhoDTO)
                .when()
                .post("/carrinhos")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract().jsonPath().get("_id");
    }

    @Test
    @Order(1)
    public void deveListarCarrinhos() {
        doGet("/carrinhos", HttpStatus.SC_OK)
                .body("carrinhos", notNullValue())
                .body("quantidade", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(2)
    public void deveBuscarCarrinhoPorId() {
        doGet("/carrinhos/" + idCarrinho, HttpStatus.SC_OK)
                .body("_id", equalTo(idCarrinho))
                .body("produtos", not(empty()));
    }

    @Test
    @Order(3)
    public void naoDeveCriarDoisCarrinhosParaOMesmoUsuario() {
        CarrinhoDTO carrinhoDTO = new CarrinhoDTO(
                List.of(new CarrinhoItemDTO(idProduto, 1))
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", TOKEN)
                .body(carrinhoDTO)
                .when()
                .post("/carrinhos")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", containsString("Não é permitido ter mais de 1 carrinho"));
    }

    @Test
    @Order(4)
    public void naoDeveCriarCarrinhoSemToken() {
        CarrinhoDTO carrinhoDTO = new CarrinhoDTO(
                List.of(new CarrinhoItemDTO(idProduto, 1))
        );

        given()
                .contentType(ContentType.JSON)
                .body(carrinhoDTO)
                .when()
                .post("/carrinhos")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .body("message", containsString("Token de acesso ausente"));
    }

    @AfterAll
    static void limparDados() {
        if (TOKEN != null) {
            given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", TOKEN)
                    .when()
                    .delete("/carrinhos/cancelar-compra")
                    .then()
                    .statusCode(HttpStatus.SC_OK);
        }
    }
}
