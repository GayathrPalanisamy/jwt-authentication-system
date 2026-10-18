
package com.onedao.authentication.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.net.InetAddress;
import java.util.Locale;
import java.util.Set;

@Service
public class CountryService {

    private static final Logger logger =
            LoggerFactory.getLogger(CountryService.class);

    private final RestClient restClient;

    private final Set<String> restrictedCountries =
            Set.of("SY", "AF", "IR");

    @Value("${app.country.allow-local-ip:false}")
    private boolean allowLocalIp;

    public CountryService(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public void validateCountry(String clientIp) {

        if (clientIp == null || clientIp.isBlank()) {
            logger.warn("Country verification failed: client IP is missing");

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Unable to determine registration country"
            );
        }

        try {
            InetAddress address = InetAddress.getByName(clientIp);

            // Local/private IP addresses cannot identify a country reliably.
            if (address.isAnyLocalAddress()
                    || address.isLoopbackAddress()
                    || address.isSiteLocalAddress()
                    || address.isLinkLocalAddress()) {

                if (allowLocalIp) {
                    logger.warn(
                            "Skipping country lookup for local/private IP in development"
                    );
                    return;
                }

                logger.warn(
                        "Country verification unavailable for local/private IP"
                );

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
                logger.warn("Country lookup returned an empty result");

                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Unable to determine registration country"
                );
            }

            countryCode = countryCode.trim()
                    .toUpperCase(Locale.ROOT);

            if (!countryCode.matches("[A-Z]{2}")) {
                logger.warn("Country lookup returned an invalid country code");

                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Country lookup returned an invalid result"
                );
            }

            logger.debug("Country lookup completed: {}", countryCode);

            if (restrictedCountries.contains(countryCode)) {
                logger.warn(
                        "Registration blocked for restricted country code: {}",
                        countryCode
                );

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Registration is not allowed from your country"
                );
            }

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Country verification failed", ex);

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Country verification is temporarily unavailable"
            );
        }
    }
}
