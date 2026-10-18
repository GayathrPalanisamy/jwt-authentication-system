
package com.onedao.authentication.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CountryServiceTest {

    @Test
    void shouldRejectSyria() {
        verifyRestrictedCountry("SY");
    }

    @Test
    void shouldRejectAfghanistan() {
        verifyRestrictedCountry("AF");
    }

    @Test
    void shouldRejectIran() {
        verifyRestrictedCountry("IR");
    }

    @Test
    void shouldAllowIndia() {
        verifyAllowedCountry("IN");
    }

    private void verifyRestrictedCountry(String countryCode) {

        RestClient.Builder builder = RestClient.builder();

        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        CountryService service = new CountryService(builder);

        server.expect(requestTo(
                        "https://ipapi.co/8.8.8.8/country/"))
                .andRespond(withSuccess(
                        countryCode,
                        MediaType.TEXT_PLAIN
                ));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validateCountry("8.8.8.8")
        );

        assertEquals(
                HttpStatus.FORBIDDEN,
                exception.getStatusCode()
        );

        server.verify();
    }

    private void verifyAllowedCountry(String countryCode) {

        RestClient.Builder builder = RestClient.builder();

        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        CountryService service = new CountryService(builder);

        server.expect(requestTo(
                        "https://ipapi.co/8.8.8.8/country/"))
                .andRespond(withSuccess(
                        countryCode,
                        MediaType.TEXT_PLAIN
                ));

        service.validateCountry("8.8.8.8");

        server.verify();
    }
}
