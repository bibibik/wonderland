import java.io.*;
import java.sql.*;

public class mainclass {

	public static void main(String[] args) throws IOException, SQLException {
		// TODO Auto-generated method stub
		try{
			
			int fid = 0;
			String dburl = "jdbc:postgresql://localhost:5432/postgres";
			String dbacct = "postgres", passwd = "gksrhkd573";
			Connection dbconn = DriverManager.getConnection(dburl, dbacct, passwd);
			ResultSet rs = null;
			PreparedStatement p = null;
			//p = dbconn.prepareStatement("create table Facility(fID int, ftype varchar(10), name varchar(20), addr varchar(50), latitude numeric(10,8), longitude numeric(10,7));");
			//p.executeUpdate();
			//System.out.println(doro2ji_addr("서울특별시 성동구 왕십리로 410"));
			facility f = new facility();
			
			fid = f.pharmacy(dbconn, p, fid);
			fid = f.hospital(dbconn, p, fid);
			fid = f.protectarea(dbconn, p, fid);
			
			
		}catch (SQLException e){
            System.out.println(e.getMessage());
        }
	}


}
