package serverrest.test.carrinho;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import serverrest.BaseTest;

import static io.restassured.RestAssured.given;

@Epic("Loja Virtual")
@Feature("Carrinho")
public class ContratoCarrinhoTest extends BaseTest {

    @Test
    @Story("Contrato de listagem de carrinhos")
    @Description("Deve validar o contrato da resposta do endpoint GET /carrinhos")
    public void deveValidarContratoDeCarrinhos() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/carrinhos")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(JsonSchemaValidator
                        .matchesJsonSchemaInClasspath("schemas/carrinhos-schema.json"));
    }
}
