package weatherscrapper_V2;

import java.io.Console;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.FileWriter;
import java.io.IOException;

public class Main {

	public static void main(String[] args) {
		String url = "https://clearoutside.com/forecast/50.65/3.02"; // Exemple : Lille
        try {
            Document doc = Jsoup.connect(url).get();
            
            // Sélectionne uniquement la section météo (à ajuster selon le site)
            Element weatherSection = doc.getElementById("forecast");

            if (weatherSection != null) {
                String weatherHtml = weatherSection.outerHtml();
                
                // Générer une page HTML standalone
                generateHtml(weatherHtml);
            } else {
                System.out.println("Erreur : Impossible de trouver la section météo !");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	private static void generateHtml(String content) {
        String htmlTemplate = """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Prévisions Météo</title>
                <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
            </head>
            <body>
                <h1 class="text-center">Prévisions Météo (Clear Outside)</h1>
                <div class="container">
                    %s
                </div>
            </body>
            </html>
        """.formatted(content);

        try (FileWriter writer = new FileWriter("weather_embed.html")) {
            writer.write(htmlTemplate);
            System.out.println("Page HTML générée : weather_embed.html");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
