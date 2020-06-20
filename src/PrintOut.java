import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PrintOut {

	private Connection con;
	private PreparedStatement ps;
	private String sql;
	private String userID;
	private ResultSet rs;
	private BufferedReader user_input;
	private String line;
	private int count = 0;

	public PrintOut(Connection dbconn, PreparedStatement p, String userID) {
		this.con = dbconn;
		this.ps = p;
		this.userID = userID;
	}

	// 기본 설정으로 townScore 상위 10개 마을(동)을 출력함
	// townScore Table 의 townScore,townname_gu,townname_dong 칼럼을 Select을 사용하여 정보를 출력함
	void printFirst() {
		try {
			sql = "Select townname_gu, townname_dong, townScore from scoreTown  where userID ='" + userID
					+ "' order by townScore desc;";
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			System.out.println("=========상위 10개 마을과 마을 점수========= ");
			while (rs.next() && count < 10) {
				System.out.printf("%s\t%s\t%d\t\n", rs.getString("townname_gu"), rs.getString("townname_dong"),
						rs.getInt("townScore"));

				count++;
			}
			System.out.println("================================== ");
			count = 0;
			return;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 추가적으로 마을(동), 시설, 시설타입 과 주소(동,구)를 통해 정보를 선택해 출력함
	// 마을(동) 선택 시, 주소(동,구) 입력 시, 해당 지역에 시설들을 출력함
	// 시설 선택시, 시설 이름 입력시 , 해당 이름이 포함된 시설들을 출력함
	// 시설 타입 선택 시, 시설타입과, 주소(동,구) 입력 시, 해당 지역에, 해당 타입 시설들을 출력함
	

	public void run() {
		try {
			int option = 0;
			user_input = new BufferedReader(new InputStreamReader(System.in));
			while (option != 'q' || option != 'Q') {
				System.out.println(
						"마을(동) 정보를 알고싶다면  '1', 시설의 정보를 알고싶다면 '2', 시설 종류에 따른 정보를 알고싶다면 '3' , 종료시 'q' 또는 'Q'을 입력해주세요");
				option = user_input.read();
				user_input = new BufferedReader(new InputStreamReader(System.in));
				switch (option) {
				case '1':
					System.out.println("구이름 과 동 이름을 입력해주세요. ex) 마포구,성산동");
					line = user_input.readLine();
					String[] guandDong = line.split(",");
					while (guandDong[1] == null) {
						System.out.println("잘못 입력하셨습니다.");
						System.out.println("시설 종류와 동이름을 를 입력해주세요. ex) 마포구,성산동");
						line = user_input.readLine();
						guandDong = line.split(",");
					}
					townInfo(guandDong[0], guandDong[1]);
					break;
				case '2':
					System.out.println("시설 이름을 입력해주세요. ex) 서울서초초등학교 or 서초초등학교");
					line = user_input.readLine();
					facilityInfo(line);
					break;
				case '3':
					System.out.println("시설 종류와 동이름을 를 입력해주세요. ex) 초등학교,서초구,서초동");
					line = user_input.readLine();
					String[] fTypeandTown = line.split(",");
					while (fTypeandTown[1] == null) {
						System.out.println("잘못 입력하셨습니다.");
						System.out.println("시설 종류와 구이름, 동이름을 를 입력해주세요. ex) 초등학교,서초구,서초동");
						line = user_input.readLine();
						fTypeandTown = line.split(",");
					}
					fTypeInfo(fTypeandTown[0], fTypeandTown[1], fTypeandTown[2]);
					break;
				case 'q':
				case 'Q':
					return;
				default:
					System.out.println("잘못 입력하셨습니다.");
				}
			}

		} catch (Exception e) {

		}

	}
	
	// 마을(동) 선택
	// 주소를 조건으로 scoreTown Table의 townScore를 출력함
	// Select, Where 
	void townInfo(String town_gu, String town_dong) {
		String townsql = "Select townScore from scoreTown where townname_gu  = '" + town_gu + "'and townname_dong  ='"
				+ town_dong + "'and userID ='" + userID + "';";
		try {
			ps = con.prepareStatement(townsql);
			rs = ps.executeQuery();
			rs.next();
			System.out.println("=========" + town_gu + " " + town_dong + "의 정보========= ");
			System.out.println(town_gu + " " + town_dong + "의 점수 : " + rs.getInt("townScore"));
			townsql = "Select name from facility where addr LIKE '%" + town_gu + "%" + town_dong + "%' ;";
			ps = con.prepareStatement(townsql);
			rs = ps.executeQuery();
			while (rs.next()) {
				System.out.println(rs.getString("name"));

			}
			System.out.println("===================================================");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;
	}
	// 시설 선택
	// 시설 이름을 조건으로 facility Table의 name, addr, fType (이름, 주소, 시설 타입)의 정보를 출력함 
	// Select, Where Like
	void facilityInfo(String name) throws SQLException {
		String facilitySql = "Select name, addr, fType from facility where name LIKE '%" + name + "%';";
		try {
			ps = con.prepareStatement(facilitySql);
			rs = ps.executeQuery();
			System.out.println("=========" + name + "의 정보========= ");
			while (rs.next()) {
				System.out.printf("%s\t%s\t%s\t\n", rs.getString("name"), rs.getString("addr"), rs.getString("fType"));
			}
			System.out.println("=====================================");

		} catch (SQLException e) {
			
			e.printStackTrace();
		}
		return;
	}
	// 시설 타입 선택
	// 시설 타입과 주소를 조건으로 facility Table의 name, addr, fType (이름, 주소, 시설 타입)의 정보를 출력함 
	// Select, Where Like
	void fTypeInfo(String fType, String town_gu, String town_dong) {
		String fTypeSql = "Select name, addr, fType from facility where fType ='" + fType + "'and addr LIKE '%"
				+ town_gu + "%" + town_dong + "%';";
		try {
			ps = con.prepareStatement(fTypeSql);
			rs = ps.executeQuery();
			System.out.println("=========" + town_gu + " " + town_dong + "의 " + fType + "========= ");
			while (rs.next()) {
				System.out.printf("%s\t%s\t%s\t\n", rs.getString("name"), rs.getString("addr"), rs.getString("fType"));
			}
			System.out.println("====================================");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;

	}
}
