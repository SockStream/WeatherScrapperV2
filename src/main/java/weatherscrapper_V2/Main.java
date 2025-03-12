package weatherscrapper_V2;

import java.io.Console;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import weatherscrapper_V2.DataModel.Localisation;
import weatherscrapper_V2.Scrapper.ClearOutsideScrapper;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

	public static void main(String[] args) {
		List<Localisation> listeLocalisations = new ArrayList<Localisation>();
		
		Localisation Lambersart = new Localisation();
		Lambersart.Nom = "Lambersart";
		Lambersart.x = "50.65";
		Lambersart.y = "3.02";
		listeLocalisations.add(Lambersart);
		
		Localisation VilleneuveDAscq = new Localisation();
		VilleneuveDAscq.Nom = "Villeneuve-d'ascq";
		VilleneuveDAscq.x = "50.62";
		VilleneuveDAscq.y = "3.14";
		listeLocalisations.add(VilleneuveDAscq);
		
		Localisation Hermin = new Localisation();
		Hermin.Nom = "Hermin";
		Hermin.x = "50.42";
		Hermin.y = "2.56";
		listeLocalisations.add(Hermin);
		
		Localisation Steenvoorde = new Localisation();
		Steenvoorde.Nom = "Steenvoorde";
		Steenvoorde.x = "50.82";
		Steenvoorde.y = "2.58";
		listeLocalisations.add(Steenvoorde);
		
		Localisation Epinoy = new Localisation();
		Epinoy.Nom = "Epinoy";
		Epinoy.x = "50.23";
		Epinoy.y = "3.16";
		listeLocalisations.add(Epinoy);
		
		Localisation Gouzeaucourt = new Localisation();
		Gouzeaucourt.Nom = "Gouzeaucourt";
		Gouzeaucourt.x = "50.05";
		Gouzeaucourt.y = "3.12";
		listeLocalisations.add(Gouzeaucourt);
		
		Localisation Rimboval = new Localisation();
		Rimboval.Nom = "Rimboval";
		Rimboval.x = "50.51";
		Rimboval.y = "1.99";
		listeLocalisations.add(Rimboval);
		
		Localisation Meteren = new Localisation();
		Meteren.Nom = "Meteren";
		Meteren.x = "50.74";
		Meteren.y = "2.69";
		listeLocalisations.add(Meteren);
		
		Localisation Coutiches = new Localisation();
		Coutiches.Nom = "Coutiches";
		Coutiches.x = "50.45";
		Coutiches.y = "3.20";
		listeLocalisations.add(Coutiches);
		
		Localisation Renwez = new Localisation();
		Renwez.Nom = "Renwez";
		Renwez.x = "49.84";
		Renwez.y = "4.60";
		listeLocalisations.add(Renwez);
		
		Localisation Grevilliers = new Localisation();
		Grevilliers.Nom = "Grevilliers";
		Grevilliers.x = "50.11";
		Grevilliers.y = "2.81";
		listeLocalisations.add(Grevilliers);
		
		Localisation Buissy = new Localisation();
		Buissy.Nom = "Buissy";
		Buissy.x = "50.21";
		Buissy.y = "3.04";
		listeLocalisations.add(Buissy);
		
		for(Localisation loc : listeLocalisations)
		{
			System.out.println(loc.Nom);
			ClearOutsideScrapper scrapper = new ClearOutsideScrapper(loc.Nom, loc.x, loc.y);
			
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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
