
package com.onedao.authentication.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.net.InetAddress;
import java.util.Locale;
import java.util.Set;

@Service
public class CountryService {

    private final RestClient restClient = RestClient.create();

    private final Set<String> restrictedCountries =
            Set.of("SY", "AF", "IR");

    public void validateCountry(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Unable to determine registration country"
            );
        }

        try {
            InetAddress address = InetAddress.getByName(clientIp);

            // Local/private IP addresses cannot reliably identify a country.
            if (address.isAnyLocalAddress()
                    || address.isLoopbackAddress()
                    || address.isSiteLocalAddress()
                    || address.isLinkLocalAddress()) {
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Country lookup is unavailable for this IP address"
                );
            }

            String countryCode = restClient.get()
                    .uri("https://ipapi.co/{ip}/country/", clientIp)
                    .retrieve()
                    .body(String.class);

            if (countryCode == null || countryCode.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Unable to determine registration country"
                );
            }

            countryCode = countryCode.trim()
                    .toUpperCase(Locale.ROOT);

            if (!countryCode.matches("[A-Z]{2}")) {
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Country lookup returned an invalid result"
                );
            }

            if (restrictedCountries.contains(countryCode)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Registration is not allowed from your country"
                );
            }

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Country verification is temporarily unavailable"
            );
        }
    }
}
