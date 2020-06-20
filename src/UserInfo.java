import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.postgresql.util.PSQLException;

public class UserInfo {
	private Connection con;
	private PreparedStatement ps;
	private String sql;
	private String userID;
	private String password;
	private ResultSet rs;
	private BufferedReader user_input;
	private String line;
	private String option;
	private boolean loginSuccess = false;

	public UserInfo(Connection dbconn, PreparedStatement p) {
		this.con = dbconn;
		this.ps = p;
	}

	// ID,Password를 통해 유저를 구별하고 인증함. 
	// userInfo Table의 userID, passwd 칼럼을 사용. 
	// 회원가입시,  userInfo Table에 "Insert"를 사용
	// 로그인 시, userInfo Table에 "Select"를 사용
	String login() {
		BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
		while (!loginSuccess) {
			try {
				System.out.println("로그인과 회원가입 중 선택해주세요");
				option = keyboard.readLine();
				if (option.equals("로그인")) {
					System.out.println("아이디를 입력해주세요");
					userID = keyboard.readLine();
					System.out.println("비밀번호를 입력해주세요");
					password = keyboard.readLine();
					sql = "Select userID, passwd from userInfo where userID ='" + userID + "'and passwd = '" + password
							+ "';";
					try {
						ps = con.prepareStatement(sql);
						rs = ps.executeQuery();
						rs.next();

					} catch (PSQLException e) {
						System.out.println(e);
						System.out.println("아이디 또는 비밀번호가 잘못되었습니다.");
						continue;
					}

					catch (SQLException e) {
						System.out.println(e);
						System.out.println("아이디 또는 비밀번호가 잘못되었습니다.");
						continue;
					}

					if (rs.getString(1).equals(userID) && rs.getString(2).equals(password)) {
						System.out.println("로그인에 성공하셨습니다");
						loginSuccess = true;

					} else {
						System.out.println("아이디 또는 비밀번호가 잘못되었습니다.");
					}
				}

				else if (option.equals("회원가입")) {
					System.out.println("아이디를 입력해주세요");
					userID = keyboard.readLine();
					System.out.println("비밀번호를 입력해주세요");
					password = keyboard.readLine();
					sql = "insert into userInfo values('" + userID + "', '" + password + "');";
					ps = con.prepareStatement(sql);
					ps.executeUpdate();
					System.out.println("ID : " + userID + "\nPassWord : " + password + "\n회원가입에 성공하셨습니다. 로그인을 시도하세요");
				} else {
					System.out.println("잘못 입력하셨습니다.");
				}

			} catch (SQLException e) {

				System.out.println("잘못 입력하셨습니다.");
				continue;
			} catch (IOException e) {

				System.out.println("입력버퍼 문제");
			} catch (Exception e) {
				System.out.println("잘못 입력하셨습니다.");
				continue;
			}
		}
		return userID;

	}
}