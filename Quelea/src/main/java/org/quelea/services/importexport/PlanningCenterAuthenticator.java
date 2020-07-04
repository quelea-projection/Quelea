/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quelea.services.importexport;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.application.Platform;
import org.quelea.planningcenter.auth.AuthToken;
import org.quelea.planningcenter.auth.ClientDetails;
import org.quelea.planningcenter.auth.OAuthRedirectFlow;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.utils.DesktopApi;

/**
 *
 * @author Michael
 */
public class PlanningCenterAuthenticator {

    private static final String CLIENT_ID = "894f896c547874179ec8c68d997fd97f8b06af8bfced24b88ff29476c584f88a";
    private static final String CLIENT_SECRET = "7733176ce41c85cd82cfee297292ded6a99d5a57ae9cfa105159d1467d708cac"; //Doesn't need to be secret for desktop apps
    private static final String REDIRECT = "http://localhost:61937";
    
    public static ClientDetails getClientDetails() {
        return ClientDetails.builder()
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .build();
    }

    public void authenticate(Consumer<Optional<AuthToken>> callback) {
        new Thread(() -> {
            Optional<AuthToken> opt = OAuthRedirectFlow.builder()
                    .clientDetails(getClientDetails())
                    .redirect(REDIRECT)
                    .build()
                    .listenLocally(Duration.ofSeconds(60), DesktopApi::browse)
                    .map(tok -> tok.withRefreshTokenUpdater(t -> QueleaProperties.get().setPlanningCenterRefreshToken(t)));

            Platform.runLater(() -> callback.accept(opt));
        }).start();
    }

}
