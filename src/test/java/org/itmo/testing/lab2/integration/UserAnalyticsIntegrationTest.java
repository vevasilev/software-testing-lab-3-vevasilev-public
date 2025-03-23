package org.itmo.testing.lab2.integration;

import io.javalin.Javalin;
import io.restassured.RestAssured;
import org.itmo.testing.lab2.controller.UserAnalyticsController;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserAnalyticsIntegrationTest {

    private Javalin app;

    private int port = 7000;

    @BeforeAll
    void setUp() {
        app = UserAnalyticsController.createApp();
        app.start(port);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @AfterAll
    void tearDown() {
        app.stop();
    }

    @Test
    @Order(10)
    @DisplayName("Тест регистрации пользователя")
    void userRegistration_validUser_shouldRegisterSuccessfully() {
        given()
                .queryParam("userId", "user1")
                .queryParam("userName", "Alice")
                .when()
                .post("/register")
                .then()
                .statusCode(200)
                .body(equalTo("User registered: true"));
    }

    @Test
    @Order(15)
    @DisplayName("Тест повторной регистрации пользователя")
    void userRegistration_existingUser_shouldReturnError() {
        given()
                .queryParam("userId", "user1")
                .queryParam("userName", "Alex")
                .when()
                .post("/register")
                .then()
                .statusCode(400)
                .body(equalTo("Invalid input: User already exists"));
    }

    @Test
    @Order(20)
    @DisplayName("Тест регистрации пользователя с отсутствующими параметрами")
    void userRegistration_missingParams_shouldReturnError() {
        given()
                .queryParam("userId", "user1")
                .when()
                .post("/register")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
    }

    @Test
    @Order(30)
    @DisplayName("Тест записи сессии с корректными датами")
    void recordSession_validDates_shouldRecordSession() {
        LocalDateTime now = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
        given()
                .queryParam("userId", "user1")
                .queryParam("loginTime", now.toString())
                .queryParam("logoutTime", now.plusHours(2).toString())
                .when()
                .post("/recordSession")
                .then()
                .statusCode(200)
                .body(equalTo("Session recorded"));
    }

    @Test
    @Order(40)
    @DisplayName("Тест записи сессии для несуществующего пользователя")
    void recordSession_nonExistingUser_shouldReturnError() {
        LocalDateTime now = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
        given()
                .queryParam("userId", "user2")
                .queryParam("loginTime", now.toString())
                .queryParam("logoutTime", now.plusHours(2).toString())
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(equalTo("Invalid data: User not found"));
    }

    @Test
    @Disabled
    @Order(45)
    @DisplayName("Тест записи сессии с датой начала после даты окончания")
    void recordSession_invalidDates_shouldReturnError() {
        LocalDateTime now = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
        given()
                .queryParam("userId", "user1")
                .queryParam("loginTime", now.plusHours(2).toString())
                .queryParam("logoutTime", now.toString())
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(containsString("Invalid data"));
    }

    @Test
    @Order(50)
    @DisplayName("Тест записи сессии с датами некорректного формата")
    void recordSession_invalidFormatDates_shouldReturnError() {
        given()
                .queryParam("userId", "user1")
                .queryParam("loginTime", "date1")
                .queryParam("logoutTime", "date2")
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(containsString("Invalid data"));
    }

    @Test
    @Order(60)
    @DisplayName("Тест записи сессии с отсутствующими параметрами")
    void recordSession_missingParams_shouldReturnError() {
        given()
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
    }

    @Test
    @Order(70)
    @DisplayName("Тест получения общего времени активности")
    void getTotalActivity_validUser_shouldReturnTotalActivity() {
        given()
                .queryParam("userId", "user1")
                .when()
                .get("/totalActivity")
                .then()
                .statusCode(200)
                .body(containsString("Total activity:"))
                .body(containsString("minutes"));
    }

    @Test
    @Order(80)
    @DisplayName("Тест получения активности без userId")
    void getTotalActivity_missingUserId_shouldReturnError() {
        given()
                .when()
                .get("/totalActivity")
                .then()
                .statusCode(400)
                .body(equalTo("Missing userId"));
    }

    @Test
    @Order(90)
    @DisplayName("Тест получения активности для несуществующего пользователя")
    void getTotalActivity_nonExistingUser_shouldReturnError() {
        given()
                .queryParam("userId", "user2")
                .when()
                .get("/totalActivity")
                .then()
                .statusCode(400)
                .body(containsString("Invalid input"));
    }

    @Test
    @Order(100)
    @DisplayName("Тест получения активности для пользователя без сессий")
    void getTotalActivity_userHasNoSessions_shouldReturnError() {
        given()
                .queryParam("userId", "user3")
                .queryParam("userName", "Alex")
                .when()
                .post("/register")
                .then()
                .statusCode(200)
                .body(equalTo("User registered: true"));
        given()
                .queryParam("userId", "user3")
                .when()
                .get("/totalActivity")
                .then()
                .statusCode(400)
                .body(equalTo("Invalid input: No sessions found for user"));
    }

    @Test
    @Order(110)
    @DisplayName("Тест поиска неактивных пользователей")
    void findInactiveUsers_validDays_shouldReturnInactiveUsers() {
        given()
                .queryParam("days", "5")
                .when()
                .get("/inactiveUsers")
                .then()
                .statusCode(200)
                .body(is("[\"user1\"]"));
    }

    @Test
    @Disabled
    @Order(115)
    @DisplayName("Тест поиска неактивных пользователей с некорректным параметром")
    void findInactiveUsers_invalidDays_shouldReturnError() {
        given()
                .queryParam("days", "-5")
                .when()
                .get("/inactiveUsers")
                .then()
                .statusCode(400)
                .body(containsString("Invalid input"));
    }

    @Test
    @Order(120)
    @DisplayName("Тест поиска неактивных пользователей с отсутствующим параметром")
    void findInactiveUsers_missingDaysParam_shouldReturnError() {
        given()
                .when()
                .get("/inactiveUsers")
                .then()
                .statusCode(400)
                .body(equalTo("Missing days parameter"));
    }

    @Test
    @Order(130)
    @DisplayName("Тест поиска неактивных пользователей с некорректным форматом параметров")
    void findInactiveUsers_invalidFormatDaysParam_shouldReturnError() {
        given()
                .queryParam("days", "days")
                .when()
                .get("/inactiveUsers")
                .then()
                .statusCode(400)
                .body(equalTo("Invalid number format for days"));
    }

    @Test
    @Order(140)
    @DisplayName("Тест получения месячной активности")
    void getMonthlyActivity_validUserAndMonth_shouldReturnMonthlyActivity() {
        given()
                .queryParam("userId", "user1")
                .queryParam("month", "2025-01")
                .when()
                .get("/monthlyActivity")
                .then()
                .statusCode(200)
                .body(notNullValue())
                .body("2025-01-01", equalTo(120));
    }

    @Test
    @Order(150)
    @DisplayName("Тест получения месячной активности с отсутствующими параметрами")
    void getMonthlyActivity_missingParams_shouldReturnError() {
        given()
                .when()
                .get("/monthlyActivity")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
    }

    @Test
    @Order(160)
    @DisplayName("Тест получения месячной активности с некорректным форматом параметра")
    void getMonthlyActivity_invalidFormatParam_shouldReturnError() {
        given()
                .queryParam("userId", "user1")
                .queryParam("month", "month")
                .when()
                .get("/monthlyActivity")
                .then()
                .statusCode(400)
                .body(containsString("Invalid data"));
    }

    @Test
    @Order(170)
    @DisplayName("Тест получения месячной активности для несуществующего пользователя")
    void getMonthlyActivity_nonExistingUser_shouldReturnError() {
        given()
                .queryParam("userId", "user4")
                .queryParam("month", "2025-01")
                .when()
                .get("/monthlyActivity")
                .then()
                .statusCode(400)
                .body(containsString("Invalid data"));
    }
}