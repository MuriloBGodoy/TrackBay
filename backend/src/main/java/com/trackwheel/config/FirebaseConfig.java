package com.trackwheel.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import jakarta.annotation.PostConstruct;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Inicializa o Firebase Admin SDK a partir da credencial de service account.
 * Fora do perfil dev a credencial e obrigatoria: sem ela nao ha validacao de token.
 */
@Configuration
@Profile("!dev")
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${trackwheel.firebase.credentials-path:}")
    private String credentialsPath;

    @Value("${trackwheel.firebase.project-id:}")
    private String projectId;

    @PostConstruct
    public void inicializar() throws Exception {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        GoogleCredentials credenciais;
        if (credentialsPath != null && !credentialsPath.isBlank()) {
            try (InputStream in = new FileInputStream(credentialsPath)) {
                credenciais = GoogleCredentials.fromStream(in);
            }
        } else {
            // Cai no GOOGLE_APPLICATION_CREDENTIALS / metadata do ambiente.
            credenciais = GoogleCredentials.getApplicationDefault();
        }

        FirebaseOptions.Builder options = FirebaseOptions.builder().setCredentials(credenciais);
        if (projectId != null && !projectId.isBlank()) {
            options.setProjectId(projectId);
        }

        FirebaseApp.initializeApp(options.build());
        log.info("Firebase Admin SDK inicializado");
    }
}
