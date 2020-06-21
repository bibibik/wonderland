import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class other {

	/*
	 * facility에 있는 ftype을 List에 저장한다. town, scoretype, scoretown table의 data를
	 * insert하거나 update할 때 loop로 쓰인다.
	 */
	public static List<String> ftype(Connection dbconn, PreparedStatement p) throws SQLException {
		List<String> ftype = new ArrayList<String>();
		p = dbconn.prepareStatement("select distinct(ftype) from facility;");
		ResultSet rs = null;
		rs = p.executeQuery();
		while (rs.next()) {
			ftype.add(rs.getString(1));
		}

		return ftype;
	}
	
	/* 서울에 있는 구와 동을 List에 저장한다. scoretown tabe의 townscore를 update할 때 loop로 쓰인다. */
	public static List<List<String>> dist_town(Connection dbconn, PreparedStatement p) throws SQLException {

		List<List<String>> dis_town = new ArrayList<List<String>>();
		p = dbconn.prepareStatement("select distinct(townname_gu) , townname_dong from town;");
		ResultSet rs = null;
		rs = p.executeQuery();
		while (rs.next()) {
			List<String> gu_dong = new ArrayList<String>();
			gu_dong.add(rs.getString(1));
			gu_dong.add(rs.getString(2));
			dis_town.add(gu_dong);
		}
		return dis_town;

	}

	/* user가 ftype에 점수를 준 것을 저장한다. */
	/* @ftype : facility에 있는 시설물들의 타입들, @suid : 로그인한 userid */
	public static String scoreType(Connection dbconn, PreparedStatement p, List<String> ftype, String suid)
			throws SQLException, IOException {
		BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("이전에 시설물에 점수를 부여하셨나요? [Y/N]: ");
		String y_n = keyboard.readLine();
		String replace = "";
		if (y_n.equals("Y")) {
			System.out.println("수정하실건가요? [Y/N]: ");
			replace = keyboard.readLine();
			if (replace.equals("N"))
				return "noReplace";
		}
		System.out.println("시설물들에 대해 점수를 부여하세요 (0~10)- " + ftype);
		p = dbconn.prepareStatement("select userid from scoretype where userid = '" + suid + "';");
		for (String str : ftype) {
			System.out.print(str + " : ");
			String score = keyboard.readLine();
			if (replace.equals("Y"))
				p = dbconn.prepareStatement("update scoretype set typeScore = " + score + " where userID = '" + suid
						+ "' and fType = '" + str + "';");
			else
				p = dbconn.prepareStatement(
						"insert into scoretype values ('" + suid + "', '" + str + "', " + score + ");");
			p.executeUpdate();
		}

		return y_n;
	}

	/*
	 * user의 입력을 바탕으로 scoretown의 townscore(town의 시설물 개수(시설들의 점수 영향을 최대화 하기위해 시설물 개수를
	 * 최대 10개로 제한) * user의 시설물 점수)를 update해준다.
	 */
	/*
	 * @ftype : 시설물에 있는 시설들의 타입들, @suid : 로그인한 userid, @town : 서울에 있는 모든 법정동, @y_f :
	 * user가 이전에 ftype에 점수를 줬는지 여부
	 */
	public static void scoreTown(Connection dbconn, PreparedStatement p, List<String> ftype, String suid,
			List<List<String>> town, String y_n) throws SQLException {

		if(y_n.equals("noReplace")) {
			return;
		}
		for (List<String> tnnm : town) {
			if (y_n.equals("N")) { // 만약 이전에 시설들에 대해 점수를 주지 않았다면 uid, townname, townscore = 0 을 insert
				p = dbconn.prepareStatement("insert into scoretown values ('" + suid + "', '" + tnnm.get(0) + "', '"
						+ tnnm.get(1) + "', 0);");
				p.executeUpdate();
			} 
			for (String ft : ftype) {
				String getscore = "select typescore from scoreType where ftype ='" + ft + "' and userID = '" + suid
						+ "'";
				String getnum = "select num from town where ftype ='" + ft + "'and townname_gu = '" + tnnm.get(0)
						+ "' and townname_dong ='" + tnnm.get(1) + "'";
				p = dbconn.prepareStatement(getnum + ";");
				ResultSet rs = null;
				rs = p.executeQuery();
				rs.next();
				int numtype = rs.getInt(1);
				String upd = " ";
				/* 동당 개수는 5개면 적당하므로 최대 계산 개수는 5로 한다. */
				if (numtype <= 5) {
					upd = "update scoreTown set townscore = townscore + ((" + getscore + ")*(" + getnum + "))"
							+ "where userID = '" + suid + "' and townname_gu = '" + tnnm.get(0)
							+ "' and townname_dong = '" + tnnm.get(1) + "';";
				} else {
					upd = "update scoreTown set townscore = townscore + ((" + getscore + ")*(5))" + "where userID = '"
							+ suid + "' and townname_gu = '" + tnnm.get(0) + "' and townname_dong = '" + tnnm.get(1)
							+ "';";
				}
				p = dbconn.prepareStatement(upd);
				p.executeUpdate();
			}
		}

		System.out.println("Done");
	}

	/* facility table에서 각 town당 있는 시설물들과 그 시설물들의 개수를 구한다. */
	/* @ftype : facility table에 있는 시설물의 타입들 */
	public static void town(Connection dbconn, PreparedStatement p, List<String> ftype) throws SQLException {
		try {
			File csv = new File("서울시 법정동 현황.csv");
			List<List<String>> ret = new ArrayList<List<String>>();
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csv), "UTF-8"));
			String line;
			while ((line = br.readLine()) != null) {
				String sarr[] = line.split(",");
				List<String> arr = Arrays.asList(sarr);
				ret.add(arr);
			}
			for (List<String> str : ret) {
				for (String ft : ftype) {
					String tmp = "insert into town values('" + str.get(1) + "', '" + str.get(2) + "', '" + ft
							+ "', (select count(*) from facility where addr LIKE '%" + str.get(1) + "%" + str.get(2)
							+ "%' and ftype = '" + ft + "' group by ftype));";
					p = dbconn.prepareStatement(tmp);
					p.executeUpdate();
				}
			}

			p = dbconn.prepareStatement("update town set num = 0 where num is null;");
			p.executeUpdate();

		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

	}

}