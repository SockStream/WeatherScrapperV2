package weatherscrapper_V2.Scrapper;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Calendar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import weatherscrapper_V2.DataBase.SQLDataBase;
import weatherscrapper_V2.DataModel.IssData;
import weatherscrapper_V2.DataModel.IssPassOver;
import weatherscrapper_V2.DataModel.Localisation;
import weatherscrapper_V2.DataModel.MeteoData;
import weatherscrapper_V2.DataModel.MoonData;

public class ClearOutsideScrapper
{

	public ClearOutsideScrapper(String Nom, String x, String y)
	{
		String baseUrl = "https://clearoutside.com/forecast/_X_/_Y_?view=midnight";
		
		baseUrl = baseUrl.replace("_X_", x);
		baseUrl = baseUrl.replace("_Y_", y);
		
		ScrapperUrl(Nom, baseUrl);
	}
	
	private void ScrapperUrl(String nom, String Url)
	{
		try {
            Document doc = Jsoup.connect(Url).get();
            
            Localisation loc = SQLDataBase.GetLocalisation(nom);
            if (loc == null)
            {
            	loc = new Localisation();
            	loc.Nom = nom;
            }
            	
        	//Sélectionne les informations X et Y de la localisation
        	Element GeoDataSection = doc.selectFirst("h1");
        	String GeoData = GeoDataSection.text();
        	GeoData = GeoData.substring(GeoData.indexOf("(")+1);
        	GeoData = GeoData.substring(0, GeoData.indexOf(")"));
        	
        	String[] Data = GeoData.split(",");
        	loc.x = Data[0];
        	loc.y = Data[1];
        	
        	//Sélectionne les informations relatives à la qualité du ciel
            Element SkyQualitySection = doc.selectFirst(".btn-primary");
            
            String Magnitude = SkyQualitySection.select("strong").get(0).text();
            loc.Magnitude = Float.parseFloat(Magnitude);
            String Bortle = SkyQualitySection.select("strong").get(1).text();
            loc.Bortle = Integer.parseInt(Bortle.replace("Class ", ""));
            
            if(loc.LocalisationId == -1)
            {
            	loc = SQLDataBase.AjouterLieu(loc);
            }
            else
            {
            	SQLDataBase.MettreAJourLieu(loc);
            }
            
            // Sélectionne uniquement la section météo (à ajuster selon le site)
            Element weatherSection = doc.getElementById("forecast");
            
            String day = "day_i";
            for (int i =0; i <= 6; i++)
            {
            	Element daySection = weatherSection.getElementById(day.replace("i", Integer.toString(i)));
            	
            	//on récupère les données lunaires
            	Element MoonSection = daySection.selectFirst(".fc_moon");
            	MoonData moonData = new MoonData();
            	moonData.LocalisationId = loc.LocalisationId;
            	
            	Calendar c = Calendar.getInstance();
            	Date date = new Date();
            	date.setHours(0);
            	date.setMinutes(0);
            	date.setSeconds(0);
            	c.setTime(date);
            	c.add(Calendar.DATE, i);
            	moonData.Date = c.getTime();
            	moonData.MoonPhase = MoonSection.selectFirst(".fc_moon_phase").text();
            	
            	moonData.MoonPhasePercent = Integer.parseInt(MoonSection.selectFirst(".fc_moon_percentage").text().replace("%",""));
            	
            	String strDateRise = MoonSection.selectFirst(".fc_moon_riseset").text().split(" ")[0];
            	Date dateRise = new Date();
            	dateRise.setHours(Integer.parseInt(strDateRise.split(":")[0]));
            	dateRise.setMinutes(Integer.parseInt(strDateRise.split(":")[1]));
            	dateRise.setSeconds(0);
            	Calendar cRise = Calendar.getInstance();
            	cRise.setTime(dateRise);
            	cRise.add(Calendar.DATE, i);
            	moonData.Rise = cRise.getTime();
            	
            	String strDateSet = MoonSection.selectFirst(".fc_moon_riseset").text().split(" ")[0];
            	Date dateSet = new Date();
            	dateSet.setHours(Integer.parseInt(strDateSet.split(":")[0]));
            	dateSet.setMinutes(Integer.parseInt(strDateSet.split(":")[1]));
            	dateSet.setSeconds(0);
            	Calendar cSet = Calendar.getInstance();
            	cSet.setTime(dateSet);
            	cSet.add(Calendar.DATE, i);
            	moonData.Set = cSet.getTime();
            	
            	MoonData moData = SQLDataBase.GetMoonData(moonData);
            	if(moData == null)
            	{
            		SQLDataBase.AjouterMoonData(moonData);
            	}
            	else
            	{
            		SQLDataBase.MettreAJourMoonData(moData.MoonDataId,moonData);
            	}
                
                //on récupère les données par heure
            	Element HourRating = daySection.selectFirst(".fc_hour_ratings");
            	List<MeteoData> listeMeteoData = new ArrayList<MeteoData>();
            	
            	//on récupère les heures des données
            	for (Element elt: HourRating.select("li"))
            	{
            		MeteoData meteo = new MeteoData();
            		meteo.LocalisationId = loc.LocalisationId;
            		
            		Calendar cMeteo = Calendar.getInstance();
                	Date dateMeteo = new Date();
                	String heure = elt.text().split(" ")[0];
                	dateMeteo.setHours(Integer.parseInt(heure));
                	dateMeteo.setMinutes(0);
                	dateMeteo.setSeconds(0);
                	cMeteo.setTime(dateMeteo);
                	cMeteo.add(Calendar.DATE, i);
                	meteo.Date = cMeteo.getTime();
                	listeMeteoData.add(meteo);
            	}
            	
            	Element detailSection = daySection.selectFirst(".fc_detail");
            	//TotalClouds
            	Element totalClouds = detailSection.select(".fc_detail_row").get(0);
            		//par heure
            	Element hourlyTotalClouds = totalClouds.selectFirst(".fc_hours");
            	int j = 0;
            	for(Element elt : hourlyTotalClouds.select("li"))
            	{
            		listeMeteoData.get(j).TotalClouds = Integer.parseInt(elt.text());
            		j++;
            	}
            	
            	//LowClouds
            	Element lowClouds = detailSection.select(".fc_detail_row").get(1);
            	//par heure
            	Element hourlyLowClouds = lowClouds.selectFirst(".fc_hours");
            	j = 0;
            	for(Element elt : hourlyLowClouds.select("li"))
            	{
            		listeMeteoData.get(j).LowClouds = Integer.parseInt(elt.text());
            		j++;
            	}
            	
            	//MediumClouds
            	Element mediumClouds = detailSection.select(".fc_detail_row").get(2);
            	//par heure
            	Element hourlyMediumClouds = mediumClouds.selectFirst(".fc_hours");
            	j = 0;
            	for(Element elt : hourlyMediumClouds.select("li"))
            	{
            		listeMeteoData.get(j).MediumClouds = Integer.parseInt(elt.text());
            		j++;
            	}
            	
            	//HighClouds
            	Element highClouds = detailSection.select(".fc_detail_row").get(3);
            	//par heure
            	Element hourlyHighClouds = highClouds.selectFirst(".fc_hours");
            	j = 0;
            	for(Element elt : hourlyHighClouds.select("li"))
            	{
            		listeMeteoData.get(j).HighClouds = Integer.parseInt(elt.text());
            		j++;
            	}
            	
            	//WindSpeed
            	Element windSpeed = detailSection.select(".fc_detail_row").get(10);
            	//par heure
            	Element hourlyWindSpeed = windSpeed.selectFirst(".fc_hours");
            	j = 0;
            	for(Element elt : hourlyWindSpeed.select("li"))
            	{
            		listeMeteoData.get(j).WindSpeed = Float.parseFloat(elt.text());
            		j++;
            	}
            	
            	//Dew Point
            	Element dewPoint = detailSection.select(".fc_detail_row").get(14);
            	//par heure
            	Element hourlyDewPoint = dewPoint.selectFirst(".fc_hours");
            	j = 0;
            	for(Element elt : hourlyDewPoint.select("li"))
            	{
            		listeMeteoData.get(j).DewPoint = Integer.parseInt(elt.text());
            		j++;
            	}
            	
            	//RelativeHumidity
            	Element humidity = detailSection.select(".fc_detail_row").get(15);
            	//par heure
            	Element hourlyHumidity = humidity.selectFirst(".fc_hours");
            	j = 0;
            	for(Element elt : hourlyHumidity.select("li"))
            	{
            		listeMeteoData.get(j).RelativeHumidity = Integer.parseInt(elt.text());
            		j++;
            	}
            	
            	for (MeteoData meteoData : listeMeteoData)
            	{
            		MeteoData mData = SQLDataBase.GetMeteoData(meteoData.LocalisationId, meteoData.Date);
            		if (mData == null)
            		{
            			SQLDataBase.AjouterMeteoData(meteoData);
            		}
            		else
            		{
            			SQLDataBase.MettreAJourMeteoData(mData.id,meteoData);
            		}
            	}
            	
            	//IssPassOver
            	List<IssData> listeIssData = new ArrayList<IssData>();
            	Element issElement = detailSection.select(".fc_detail_row").get(4);
            	//par heure
            	Element hourlyISS = issElement.selectFirst(".fc_hours");
            	j = 0;
            	for(Element elt : hourlyISS.select("li"))
            	{
            		String data_Content = elt.attr("data-content");
            		if (!data_Content.trim().isEmpty())
            		{
            			IssData issData = new IssData();
            			issData.LocalisationId = loc.LocalisationId;
            			IssPassOver start = new IssPassOver();
            			IssPassOver max = new IssPassOver();
            			IssPassOver end = new IssPassOver();
            			Pattern hoursPattern = Pattern.compile("(\\d{2}:\\d{2}:\\d{2})");
            			Pattern altitudeLocPattern = Pattern.compile("(\\(([A-Z]+) - (\\d+)°\\))");
            			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            			
            			String startStr = data_Content.split("<br />")[0];
            			Matcher matcherHeures = hoursPattern.matcher(startStr);
            			if (matcherHeures.find())
            			{
            				String startHour = matcherHeures.group(1);
            				try {
								Date startDate = new SimpleDateFormat("HH:mm:ss").parse(startHour);
								Calendar cIss = Calendar.getInstance();
		                    	cIss.add(Calendar.DATE, i);
		                    	issData.Date = cIss.getTime();
		                    	start.Date = cIss.getTime();
		                    	start.Date.setHours(startDate.getHours());
		                    	start.Date.setMinutes(startDate.getMinutes());
		                    	start.Date.setSeconds(startDate.getSeconds());
							} catch (ParseException e) {
								e.printStackTrace();
							}
            			}
            			Matcher matcherAltLoc = altitudeLocPattern.matcher(startStr);
            			if(matcherAltLoc.find())
            			{
            				start.Localisation = matcherAltLoc.group(2);
            				start.Altitude = Integer.parseInt(matcherAltLoc.group(3));
            			}
            			
            			String maxStr = data_Content.split("<br />")[1];
            			matcherHeures = hoursPattern.matcher(maxStr);
            			if (matcherHeures.find())
            			{
            				String maxHour = matcherHeures.group(1);
            				try {
								Date maxDate = new SimpleDateFormat("HH:mm:ss").parse(maxHour);
								Calendar cIss = Calendar.getInstance();
		                    	cIss.add(Calendar.DATE, i);
		                    	max.Date = cIss.getTime();
		                    	start.Date.setHours(maxDate.getHours());
		                    	start.Date.setMinutes(maxDate.getMinutes());
		                    	start.Date.setSeconds(maxDate.getSeconds());
							} catch (ParseException e) {
								e.printStackTrace();
							}
            			}
            			matcherAltLoc = altitudeLocPattern.matcher(maxStr);
            			if(matcherAltLoc.find())
            			{
            				max.Localisation = matcherAltLoc.group(2);
            				max.Altitude = Integer.parseInt(matcherAltLoc.group(3));
            			}
            			
            			String endStr = data_Content.split("<br />")[2];
            			matcherHeures = hoursPattern.matcher(endStr);
            			if (matcherHeures.find())
            			{
            				String endHour = matcherHeures.group(1);
            				try {
								Date endDate = new SimpleDateFormat("HH:mm:ss").parse(endHour);
								Calendar cIss = Calendar.getInstance();
								cIss.setTime(endDate);
		                    	cIss.add(Calendar.DATE, i);
		                    	end.Date = cIss.getTime();
		                    	start.Date.setHours(endDate.getHours());
		                    	start.Date.setMinutes(endDate.getMinutes());
		                    	start.Date.setSeconds(endDate.getSeconds());
							} catch (ParseException e) {
								e.printStackTrace();
							}
            			}
            			matcherAltLoc = altitudeLocPattern.matcher(endStr);
            			if(matcherAltLoc.find())
            			{
            				end.Localisation = matcherAltLoc.group(2);
            				end.Altitude = Integer.parseInt(matcherAltLoc.group(3));
            			}
                    	
            			String magnitudeStr = data_Content.split("<br />")[3];
            			magnitudeStr = magnitudeStr.split("</strong>")[1];
            			issData.Magnitude = Float.parseFloat(magnitudeStr);
                    	
            			issData.Start = start;
            			issData.Max = max;
            			issData.End = end;
                    	
            			listeIssData.add(issData);
            		}
            		j++;
            	}
            	
            	for(IssData issData : listeIssData)
            	{
            		SQLDataBase.SupprimerIssData(issData.Date, issData.LocalisationId);
            	}
            	for(IssData issData : listeIssData)
            	{
            		SQLDataBase.AjouterIssData(issData);
            	}
            	
            }
            
            /*if (weatherSection != null) {
                String weatherHtml = weatherSection.outerHtml();
                
            } else {
                System.out.println("Erreur : Impossible de trouver la section météo !");
            }*/

        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
