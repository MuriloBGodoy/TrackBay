package com.trackwheel.api;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serve a UI do Scalar em /docs, apontando para o documento OpenAPI gerado pelo springdoc.
 * Alternativa mais agradavel ao Swagger UI (que continua disponivel em /swagger-ui.html).
 */
@RestController
@Hidden
public class ScalarController {

    @GetMapping(value = "/docs", produces = MediaType.TEXT_HTML_VALUE)
    public String scalar() {
        return """
                <!doctype html>
                <html>
                  <head>
                    <title>Track Wheel API</title>
                    <meta charset="utf-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1" />
                    <link rel="icon" href="data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 100 100%22><text y=%22.9em%22 font-size=%2290%22>&#128295;</text></svg>" />
                  </head>
                  <body>
                    <script id="api-reference" data-url="/v3/api-docs"></script>
                    <script>
                      var configuration = {
                        theme: 'purple',
                        layout: 'modern',
                        hideDownloadButton: false,
                        searchHotKey: 'k',
                        metaData: { title: 'Track Wheel API' }
                      };
                      document.getElementById('api-reference')
                        .dataset.configuration = JSON.stringify(configuration);
                    </script>
                    <script src="https://cdn.jsdelivr.net/npm/@scalar/api-reference"></script>
                  </body>
                </html>
                """;
    }
}
