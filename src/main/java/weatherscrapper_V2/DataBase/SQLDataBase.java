package weatherscrapper_V2.DataBase;

import java.sql.*;

import io.github.cdimascio.dotenv.Dotenv;
import weatherscrapper_V2.DataModel.Localisation;

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

	public static void AjouterLieu(Localisation loc)
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
	}
	
	public static Localisation GetLocalisation(String nom)
	{
		Localisation loc = null;
		try
		{
			String query = "select id, nom, x, y, Bortle, Magnitude from table_lieu where nom = ?";
			PreparedStatement stmt = GetInstance().prepareStatement(query);
			
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
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return loc;
	}
}
