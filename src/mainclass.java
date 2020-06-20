import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class mainclass {

	public static void main(String[] args) throws IOException, SQLException {
		// TODO Auto-generated method stub
		try {

			int fid = 0;
			String dburl = "jdbc:postgresql://localhost:5432/postgres";
			String dbacct = "postgres", passwd = "1234";
			Connection dbconn = DriverManager.getConnection(dburl, dbacct, passwd);
			PreparedStatement p = null;

			// Create Facility, Town, UserInfo, ScoreType table

			System.out.println("Creating Facility table and insert data.....");
			p = dbconn.prepareStatement(
					"create table Facility(fID int, ftype varchar(10), name varchar(40), addr varchar(70), latitude numeric(10,8), longitude numeric(10,7));");
			p.executeUpdate();
			fid = facility.pharmacy(dbconn, p, fid);
			fid = facility.hospital(dbconn, p, fid);
			fid = facility.protectarea(dbconn, p, fid);
			System.out.println("Facility table Done");
			List<String> ftype = other.ftype(dbconn, p);

			// Town table create

			System.out.println("Creating Town and insert data....");
			other.town(dbconn, p, ftype);
			System.out.println("Town table Done");

			// UserInfo table create
			System.out.println("Creating UserInfo, ScoreType, ScoreTown table...");
			p = dbconn.prepareStatement("create table userinfo(userID varchar(50), passwd varchar(100));");
			p.executeUpdate();
			// ScoreType table create
			p = dbconn
					.prepareStatement("create table scoretype(userID varchar(50), fType varchar(10), typeScore int);");
			p.executeUpdate();
			// ScoreTown table create
			p = dbconn.prepareStatement(
					"create table scoretown(userID varchar(50), townname_gu varchar(10), townname_dong varchar(10), townScore bigint);");
			p.executeUpdate();

			/* Get user id */
			String suid = new UserInfo(dbconn, p).login();

			/* Get user input */
			String y_n = other.scoreType(dbconn, p, ftype, suid);
			System.out.println("Creating ScoreTown...");
			List<List<String>> dist_town = other.dist_town(dbconn, p);
			other.scoreTown(dbconn, p, ftype, suid, dist_town, y_n);
			System.out.println("ScoreTown Done");

			/* Print Out */
			PrintOut po = new PrintOut(dbconn, p, suid);
			po.printFirst();
			po.run();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

}