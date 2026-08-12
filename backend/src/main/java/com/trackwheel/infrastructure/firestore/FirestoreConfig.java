package com.trackwheel.infrastructure.firestore;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.trackwheel.config.FirebaseConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Expoe o Firestore como bean fora do perfil dev.
 * Recebe o FirebaseConfig para garantir que o Admin SDK ja foi inicializado.
 */
@Configuration
@Profile("!dev")
public class FirestoreConfig {

    @Bean
    public Firestore firestore(FirebaseConfig firebaseConfig) {
        return FirestoreClient.getFirestore();
    }
}
