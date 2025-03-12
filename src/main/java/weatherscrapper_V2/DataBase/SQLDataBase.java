package weatherscrapper_V2.DataBase;

import java.sql.*;
import java.util.Calendar;
import java.util.Date;

import io.github.cdimascio.dotenv.Dotenv;
import weatherscrapper_V2.DataModel.IssData;
import weatherscrapper_V2.DataModel.IssPassOver;
import weatherscrapper_V2.DataModel.Localisation;
import weatherscrapper_V2.DataModel.MeteoData;
import weatherscrapper_V2.DataModel.MoonData;

public class SQLDataBase {
	
	private static Connection connection = null;
	
	private static Connection GetInstance()
	{
		if (connection == null)
		{
			Dotenv dotenv = Dotenv.load();
	        
	        String dbUrl = dotenv.get("DB_URL");
	        String dbUser = dotenv.get("DB_USER");
	        String dbPassword = dotenv.get("DB_PASSWORD");
	        
			try
			{
				connection = DriverManager.getConnection(dbUrl,dbUser,dbPassword);
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
		return connection;
	}
	
	public static void CloseInstance()
	{
		if (connection != null)
		{
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			connection = null;
		}
	}

	public static Localisation AjouterLieu(Localisation loc)
	{
		try
		{
			String query = "insert into table_lieu(nom,x,y,bortle,magnitude) values (?,?,?,?,?)";
			PreparedStatement stmt = GetInstance().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, loc.Nom);
			stmt.setString(2, loc.x);
			stmt.setString(3, loc.y);
			stmt.setInt(4, loc.Bortle);
			stmt.setFloat(5, loc.Magnitude);
			int rowsInserted = stmt.executeUpdate();
			if (rowsInserted > 0)
			{
				ResultSet rs = stmt.getGeneratedKeys();
				if(rs.next())
				{
					loc.LocalisationId = rs.getInt(1);
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return loc;
	}
	
	public static Localisation MettreAJourLieu(Localisation loc)
	{
		try
		{
			String query = "update table_lieu set nom = ?, x = ?, y = ?, bortle = ?, magnitude =? where id = ?";
			PreparedStatement stmt = GetInstance().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, loc.Nom);
			stmt.setString(2, loc.x);
			stmt.setString(3, loc.y);
			stmt.setInt(4, loc.Bortle);
			stmt.setFloat(5, loc.Magnitude);
			stmt.setInt(6, loc.LocalisationId);
			int rowsInserted = stmt.executeUpdate();
			if (rowsInserted > 0)
			{
				ResultSet rs = stmt.getGeneratedKeys();
				if(rs.next())
				{
					loc.LocalisationId = rs.getInt(1);
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return loc;
	}
	
	public static Localisation GetLocalisation(String nom)
	{
		Localisation loc = null;
		try
		{
			String query = "select id, nom, x, y, Bortle, Magnitude from table_lieu where nom = ?";
			PreparedStatement stmt = GetInstance().prepareStatement(query);
			stmt.setString(1, nom);
			
			ResultSet rs = stmt.executeQuery();
			
			if (rs.next())
			{
				loc = new Localisation();
				loc.LocalisationId = rs.getInt(1);
				loc.Nom = rs.getString(2);
				loc.x = rs.getString(3);
				loc.y = rs.getString(4);
				loc.Bortle = rs.getInt(5);
				loc.Magnitude = rs.getFloat(6);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return loc;
	}
	
	public static void AjouterMoonData(MoonData moonData)
	{
		try
		{
			String query = "insert into table_moon(localisationId,Date,RiseDate,SetDate,MoonPhase,MoonPhasePercent) values (?,?,?,?,?,?)";
			PreparedStatement stmt = GetInstance().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, moonData.LocalisationId);
			Timestamp tsDate = new Timestamp(moonData.Date.getTime());
			tsDate.setNanos(0);
			stmt.setTimestamp(2, tsDate);
			Timestamp tsRise = new Timestamp(moonData.Rise.getTime());
			tsRise.setNanos(0);
			stmt.setTimestamp(3, tsRise);
			Timestamp tsSet = new Timestamp(moonData.Set.getTime());
			tsSet.setNanos(0);
			stmt.setTimestamp(4, tsSet);
			stmt.setString(5, moonData.MoonPhase);
			stmt.setInt(6, moonData.MoonPhasePercent);
			int rowsInserted = stmt.executeUpdate();
			if (rowsInserted > 0)
			{
				ResultSet rs = stmt.getGeneratedKeys();
				if(rs.next())
				{
					moonData.MoonDataId = rs.getInt(1);
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void MettreAJourMoonData(int id, MoonData moonData)
	{
		try
		{
			String query = "UPDATE table_moon SET localisationId=?,Date=?,RiseDate=?,SetDate=?,MoonPhase=?,MoonPhasePercent=? WHERE Id=?";
			PreparedStatement stmt = GetInstance().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, moonData.LocalisationId);
			Timestamp tsDate = new Timestamp(moonData.Date.getTime());
			tsDate.setNanos(0);
			stmt.setTimestamp(2, tsDate);
			Timestamp tsRise = new Timestamp(moonData.Rise.getTime());
			tsRise.setNanos(0);
			stmt.setTimestamp(3, tsRise);
			Timestamp tsSet = new Timestamp(moonData.Set.getTime());
			tsSet.setNanos(0);
			stmt.setTimestamp(4, tsSet);
			stmt.setString(5, moonData.MoonPhase);
			stmt.setInt(6, moonData.MoonPhasePercent);
			stmt.setInt(7, id);
			int rowsInserted = stmt.executeUpdate();
			if (rowsInserted > 0)
			{
				ResultSet rs = stmt.getGeneratedKeys();
				if(rs.next())
				{
					moonData.MoonDataId = rs.getInt(1);
				}
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public static MoonData GetMoonData(MoonData data)
	{
		MoonData moonData = null;
		
		try
		{
			String query = "select id, localisationId, Date, RiseDate, SetDate, MoonPhase, MoonPhasePercent from table_moon where localisationId = ? and Date = ?";
			PreparedStatement stmt = GetInstance().prepareStatement(query);
			stmt.setInt(1, data.LocalisationId);
			Timestamp ts = new Timestamp(data.Date.getTime());
			ts.setNanos(0);
			stmt.setTimestamp(2, ts);
			
			ResultSet rs = stmt.executeQuery();
			if(rs.next())
			{
				moonData = new MoonData();
				moonData.MoonDataId = rs.getInt(1);
				moonData.LocalisationId = rs.getInt(2);
				moonData.Date = rs.getDate(3);
				moonData.Rise = rs.getDate(4);
				moonData.Set = rs.getDate(5);
				moonData.MoonPhase = rs.getString(6);
				moonData.MoonPhasePercent = rs.getInt(7);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return moonData;
	}
	
	public static MeteoData GetMeteoData(int localisationId, Date date)
	{
		MeteoData meteoData = null;
		
		try
		{
			String query = "SELECT id, LocalisationId, Date, DewPoint, RelativeHumidity, WindSpeed, TotalClouds, LowClouds, MediumClouds, HighClouds FROM table_meteo WHERE localisationId = ? AND Date = ?";
			PreparedStatement stmt = GetInstance().prepareStatement(query);
			stmt.setInt(1, localisationId);
			Timestamp ts = new Timestamp(date.getTime());
			ts.setNanos(0);
			stmt.setTimestamp(2, ts);
			ResultSet rs = stmt.executeQuery();
			if(rs.next())
			{
				meteoData = new MeteoData();
				meteoData.id = rs.getInt(1);
				meteoData.LocalisationId = rs.getInt(2);
				meteoData.Date = rs.getDate(3);
				meteoData.DewPoint = rs.getInt(4);
				meteoData.RelativeHumidity = rs.getInt(5);
				meteoData.WindSpeed = rs.getFloat(6);
				meteoData.TotalClouds = rs.getInt(7);
				meteoData.LowClouds = rs.getInt(8);
				meteoData.MediumClouds = rs.getInt(9);
				meteoData.HighClouds = rs.getInt(10);
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		return meteoData;
	}
	
	public static void AjouterMeteoData(MeteoData meteoData)
	{
		try
		{
			String query = "INSERT INTO table_meteo (LocalisationId, Date, DewPoint, RelativeHumidity, WindSpeed, TotalClouds, LowClouds, MediumClouds, HighClouds) VALUES (?,?,?,?,?,?,?,?,?)";
			PreparedStatement stmt = GetInstance().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, meteoData.LocalisationId);
			Timestamp ts = new Timestamp(meteoData.Date.getTime());
			ts.setNanos(0);
			stmt.setTimestamp(2, ts);
			stmt.setInt(3, meteoData.DewPoint);
			stmt.setInt(4, meteoData.RelativeHumidity);
			stmt.setFloat(5, meteoData.WindSpeed);
			stmt.setInt(6, meteoData.TotalClouds);
			stmt.setInt(7, meteoData.LowClouds);
			stmt.setInt(8, meteoData.MediumClouds);
			stmt.setInt(9, meteoData.HighClouds);
			int rowsInserted = stmt.executeUpdate();
			if (rowsInserted > 0)
			{
				ResultSet rs = stmt.getGeneratedKeys();
				if(rs.next())
				{
					meteoData.id = rs.getInt(1);
				}
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void MettreAJourMeteoData(int id, MeteoData meteoData)
	{
		try
		{
			String query = "UPDATE table_meteo SET LocalisationId=?,Date=?,DewPoint=?,RelativeHumidity=?,WindSpeed=?,TotalClouds=?,LowClouds=?,MediumClouds=?,HighClouds=? WHERE id = ?";
			PreparedStatement stmt = GetInstance().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, meteoData.LocalisationId);
			Timestamp ts = new Timestamp(meteoData.Date.getTime());
			ts.setNanos(0);
			stmt.setTimestamp(2, ts);
			stmt.setInt(3, meteoData.DewPoint);
			stmt.setInt(4, meteoData.RelativeHumidity);
			stmt.setFloat(5, meteoData.WindSpeed);
			stmt.setInt(6, meteoData.TotalClouds);
			stmt.setInt(7, meteoData.LowClouds);
			stmt.setInt(8, meteoData.MediumClouds);
			stmt.setInt(9, meteoData.HighClouds);
			stmt.setInt(10, id);
			stmt.executeUpdate();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void SupprimerIssData(Date date, int localisationId)
	{
		try
		{	
			String query = "Select StartId, MaxId, EndId from table_issData where localisationId = ? and Date >= ? and Date < ?";
			PreparedStatement stmt = GetInstance().prepareStatement(query);
			stmt.setInt(1, localisationId);
			date.setHours(0);
			date.setMinutes(0);
			date.setSeconds(0);
			Timestamp tsDebut = new Timestamp(date.getTime());
			tsDebut.setNanos(0);
			stmt.setTimestamp(2, tsDebut);
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			c.add(Calendar.DATE, 1);
			Timestamp tsFin = new Timestamp(c.getTime().getTime());
			tsFin.setNanos(0);
			stmt.setTimestamp(3, tsFin);
			ResultSet rs = stmt.executeQuery();
			while(rs.next())
			{
				SupprimerIssPassover(rs.getInt(1));
				SupprimerIssPassover(rs.getInt(2));
				SupprimerIssPassover(rs.getInt(3));
				
			}
			
			query = "DELETE from table_issData where localisationId = ? and Date >= ? and Date < ?";
			stmt = GetInstance().prepareStatement(query);
			stmt.setInt(1, localisationId);
			stmt.setTimestamp(2, tsDebut);
			stmt.setTimestamp(3, tsFin);
			stmt.executeUpdate();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void AjouterIssData(IssData issData)
	{
		try
		{
			AjouterIssPassOverData(issData.Start);
			AjouterIssPassOverData(issData.Max);
			AjouterIssPassOverData(issData.End);
			
			String query = "INSERT INTO table_issdata(LocalisationId, Date, StartId, MaxId, EndId, Magnitude) VALUES (?,?,?,?,?,?)";
			PreparedStatement stmt = GetInstance().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, issData.LocalisationId);
			Timestamp ts = new Timestamp(issData.Date.getTime());
			ts.setNanos(0);
			stmt.setTimestamp(2, ts);
			stmt.setInt(3, issData.Start.issPassOverId);
			stmt.setInt(4, issData.Max.issPassOverId);
			stmt.setInt(5, issData.End.issPassOverId);
			stmt.setFloat(6, issData.Magnitude);
			stmt.executeUpdate();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void SupprimerIssPassover(int issPassOverId)
	{
		try
		{
			String query = "delete from table_isspassover where id = ?";
			PreparedStatement stmt = GetInstance().prepareStatement(query);
			stmt.setInt(1, issPassOverId);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void AjouterIssPassOverData(IssPassOver issPassOver)
	{
		try
		{
			String query = "INSERT INTO table_isspassover(Date, Localisation, Altitude) VALUES (?,?,?)";
			PreparedStatement stmt = GetInstance().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			Timestamp ts = new Timestamp(issPassOver.Date.getTime());
			ts.setNanos(0);
			stmt.setTimestamp(1, ts);
			stmt.setString(2, issPassOver.Localisation);
			stmt.setInt(3, issPassOver.Altitude);
			int rowsInserted = stmt.executeUpdate();
			if (rowsInserted > 0)
			{
				ResultSet rs = stmt.getGeneratedKeys();
				if(rs.next())
				{
					issPassOver.issPassOverId = rs.getInt(1);
				}
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
}
