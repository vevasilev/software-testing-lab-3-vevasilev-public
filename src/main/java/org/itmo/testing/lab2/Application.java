package org.itmo.testing.lab2;

import io.javalin.Javalin;
import io.restassured.RestAssured;
import org.itmo.testing.lab2.controller.UserAnalyticsController;

public class Application {

    public static void main(String[] args) {
        Javalin app = UserAnalyticsController.createApp();
        int port = 7000;
        app.start(port);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }
}
