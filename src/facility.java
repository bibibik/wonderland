import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.*;
import java.sql.*;

public class facility {

   /*서울에 응급실이 있는 병원과 그 병원 중 소아과가 있는 병원의 이름, 주소, 위도, 경도를 가져온다.*/
   public static int hospital(Connection dbconn, PreparedStatement p, int fid) throws IOException, SQLException {
	   System.out.println("Creating hospital...");
	   //서울에 있는 구
      String arr[] = { "강남구", "강동구", "강북구", "강서구", "관악구", "광진구", "구로구", "금천구", "노원구", "도봉구", "동대문구", "동작구", "마포구",
            "서대문구", "서초구", "성동구", "성북구", "송파구", "양천구", "영등포구", "용산구", "은평구", "종로구", "중구", "중랑구" };
      /*최대 가져올 수 있는 응급실의 개수는 10개이므로 for문을 통해 각 구에 있는 응급실을 모두 가져온다. */
      StringBuilder xmlhos = new StringBuilder();
      for (String tmp : arr) {
         StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B552657/ErmctInfoInqireService/getEmrrmRltmUsefulSckbdInfoInqire"); /*URL*/
         urlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8")+ "=eiRmigGbmSrIZuGnQi6%2BqbexpI%2BZdyAfLekKDQ6GjwaI6ttotD8Lya9Nx57qpGGkHwEqfcqNcXtgR4%2BMRZaSCw%3D%3D"); /* Service Key*/
         urlBuilder.append("&" + URLEncoder.encode("STAGE1", "UTF-8") + "=" + URLEncoder.encode("서울특별시", "UTF-8")); /* 주소(시도) */
         urlBuilder.append("&" + URLEncoder.encode("STAGE2", "UTF-8") + "=" + URLEncoder.encode(tmp, "UTF-8")); /* 주소(시군구) */
         urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /* 페이지 번호 */
         urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("10", "UTF-8")); /* 목록 건수 */
         URL url = new URL(urlBuilder.toString());
         HttpURLConnection conn = (HttpURLConnection) url.openConnection();
         conn.setRequestMethod("GET");
         conn.setRequestProperty("Content-type", "application/json");
         BufferedReader rd;
         //HTTP 응답코드를 검사하여 connection error를 검사한다.
         if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
         } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "URF-8"));
         }
         //openAPI에서 가져온 xml 파일을 line에 저장하고 StringBuilder를 이용해 sb String에 계속해서 append 해준다.
         StringBuilder sb = new StringBuilder();
         String line;
         while ((line = rd.readLine()) != null) {
            sb.append(line);
         }
         rd.close();
         conn.disconnect();
         //각 구의 xml String을 xmlhos에 계속해서 append 해준다.
         xmlhos.append(sb.toString());
      }
      //각 병원은 <item> </item> tag를 기준으로 나눌 수 있으므로 </item>으로 구분해서 String 배열에 저장한다.
      String[] xmlsplit = xmlhos.toString().split("</item>");
      /* sarr : 서울 응급실 hpid, yarr : 소아과 있는 응급실 hpid*/
      List<String> sarr = new ArrayList<String>();
      List<String> yarr = new ArrayList<String>();
      for (int i = 0; i < (xmlsplit.length - 1); i++) {
         String tmp = xmlsplit[i];
         sarr.add(tmp.substring(tmp.toString().lastIndexOf("<hpid>") + 6, tmp.lastIndexOf("</hpid>")));
         if ((tmp.substring(tmp.toString().lastIndexOf("<hv10>") + 6, tmp.lastIndexOf("</hv10>"))).equals("Y")) {	// <hv10> 태그에는 소아과 응급실이 있으면 Y가 있다.
            yarr.add(tmp.substring(tmp.toString().lastIndexOf("<hpid>") + 6, tmp.lastIndexOf("</hpid>")));
         }
      }
      /* 병원의 위도 경도 가져오기 */
      /*위의 openAPI에서는 병원의 위도 경도를 가져올 수 없으므로 같은 기관에서 발행한 위도 경도를 알 수 있는 다른 openAPI에서 hpid(sarr)를 기준으로 검색한다 */
      StringBuilder fxmlhos = new StringBuilder();
      for (String str : sarr) {
         StringBuilder urlBuilder = new StringBuilder(
               "http://apis.data.go.kr/B552657/ErmctInfoInqireService/getEgytBassInfoInqire"); /* URL */
         urlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8")
               + "=eiRmigGbmSrIZuGnQi6%2BqbexpI%2BZdyAfLekKDQ6GjwaI6ttotD8Lya9Nx57qpGGkHwEqfcqNcXtgR4%2BMRZaSCw%3D%3D"); /*
                                                                                                 * Service
                                                                                                 * Key
                                                                                                 */
         urlBuilder.append(
               "&" + URLEncoder.encode("HPID", "UTF-8") + "=" + URLEncoder.encode(str, "UTF-8")); /* 주소(시도) */
         URL url = new URL(urlBuilder.toString());
         HttpURLConnection conn = (HttpURLConnection) url.openConnection();
         conn.setRequestMethod("GET");
         conn.setRequestProperty("Content-type", "application/json");
         BufferedReader rd;
         if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
         } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "URF-8"));
         }
         StringBuilder sb = new StringBuilder();
         String line;
         while ((line = rd.readLine()) != null) {
            sb.append(line);
         }
         rd.close();
         conn.disconnect();
         fxmlhos.append(sb.toString());
      }
      //위와 동일한 방법으로 xml파일을 한줄로 가져오고 </item>을 기준으로 나눈다.
      List<List<String>> shpid = new ArrayList<List<String>>();
      String[] fxmlsplit = fxmlhos.toString().split("</item>");
      for (int i = 0; i < (fxmlsplit.length - 1); i++) {
         List<String> ltmp = new ArrayList<String>();
         String stmp = fxmlsplit[i];
         ltmp.add(stmp.substring(stmp.toString().lastIndexOf("<dutyName>") + 10, stmp.lastIndexOf("</dutyName>"))); // 병원 이름
         ltmp.add(stmp.substring(stmp.toString().lastIndexOf("<dutyAddr>") + 10, stmp.lastIndexOf("</dutyAddr>"))); // 병원 주소
         ltmp.add(stmp.substring(stmp.toString().lastIndexOf("<wgs84Lat>") + 10, stmp.lastIndexOf("</wgs84Lat>"))); // 병원 위도
         ltmp.add(stmp.substring(stmp.toString().lastIndexOf("<wgs84Lon>") + 10, stmp.lastIndexOf("</wgs84Lon>"))); // 병원 경도
         ltmp.add(stmp.substring(stmp.toString().lastIndexOf("<hpid>") + 6, stmp.lastIndexOf("</hpid>"))); // 병원 id
         shpid.add(ltmp);
      }
      for (List lst : shpid) {
         fid++;
         String addr = (String) lst.get(1);
         String jiaddr = "";
         System.out.println(lst.get(0)+" "+lst.get(1)+" "+ lst.get(2)+" "+lst.get(3)+" "+lst.get(4));
			if (lst.get(4).equals("A1120796") || lst.get(4).equals("A1100025") || lst.get(4).equals("A1100029")) {
				jiaddr = doro2ji_addr(addr.substring(addr.lastIndexOf("특별시") + 4, addr.lastIndexOf(","))); // 강남세브란스병원, 이화여대서울병원, 에이치플러스양지병원
				// 특별시 뒤에부터 , 앞까지(도로명 주소)의 String을 받아 지번 주소로 바꾼다.
			} else {
				jiaddr = doro2ji_addr(addr.substring(addr.lastIndexOf("특별시") + 4, addr.lastIndexOf("(")));
				// 특별시 뒤에부터 ( 앞까지(도로명 주소)의 String을 받아 지번 주소로 바꾼다.
			}

			String tmp = "insert into facility values(" + fid + ", '응급실', '" + lst.get(0) + "', '" + jiaddr + "', "
					+ Double.parseDouble((String) lst.get(2)) + ", " + Double.parseDouble((String) lst.get(3)) + ");";
			p = dbconn.prepareStatement(tmp);
			p.executeUpdate();
			//만약 소아과 응급실이 있다면 ftype을 응급실(소아과)로 update
			for (String str : yarr) {
				if (lst.get(4).equals(str)) {
					tmp = "update facility set ftype = '응급실(소아과)' where fid = " + fid;
					p = dbconn.prepareStatement(tmp);
					p.executeUpdate();
				}
			}
      }
      
      System.out.println("Done");

      return fid;

   }

   public static int pharmacy(Connection dbconn, PreparedStatement p, int fid) throws IOException, SQLException {
	   System.out.println("Creating Pharmacy....");
	   //약국의 개수는 4000개가 넘는데 한 페이지 최대 목록은 100개 이다 따라서 <totalcount>에 있는 총 목록 수를 가져와서 100으로 나눠 페이지를 하나씩 증가시키면서 약국의 정보를 가져온다.
      StringBuilder urlBuilder = new StringBuilder(
            "http://apis.data.go.kr/B552657/ErmctInsttInfoInqireService/getParmacyListInfoInqire"); /* URL */
      urlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8")
            + "=eiRmigGbmSrIZuGnQi6%2BqbexpI%2BZdyAfLekKDQ6GjwaI6ttotD8Lya9Nx57qpGGkHwEqfcqNcXtgR4%2BMRZaSCw%3D%3D"); /*
                                                                                              * Service
                                                                                              * Key
                                                                                              */
      urlBuilder.append(
            "&" + URLEncoder.encode("Q0", "UTF-8") + "=" + URLEncoder.encode("서울특별시", "UTF-8")); /* 주소(시도) */
      URL url = new URL(urlBuilder.toString());
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Content-type", "application/json");
      BufferedReader rd;
      if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
         rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
      } else {
         rd = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
      }
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = rd.readLine()) != null) {
         sb.append(line);
      }
      rd.close();
      conn.disconnect();
      /* 한 페이지당 최대 출력 목록 수가 100개이 므로 총 개수에서 100을 나눠 1을 더한 페이지만큼 정보를 받아와야 한다. */
      String scount = sb.toString().substring(sb.toString().lastIndexOf("<totalCount>") + 12,
            sb.toString().lastIndexOf("</totalCount>"));
      int count = Integer.parseInt(scount) / 100 + 1;
      for (int i = 1; i <= count; i++) {
         StringBuilder nurlBuilder = new StringBuilder(
               "http://apis.data.go.kr/B552657/ErmctInsttInfoInqireService/getParmacyListInfoInqire"); /* URL */
         nurlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8")
               + "=eiRmigGbmSrIZuGnQi6%2BqbexpI%2BZdyAfLekKDQ6GjwaI6ttotD8Lya9Nx57qpGGkHwEqfcqNcXtgR4%2BMRZaSCw%3D%3D"); /*
                                                                                                 * Service
                                                                                                 * Key
                                                                                                 */
         nurlBuilder.append(
               "&" + URLEncoder.encode("Q0", "UTF-8") + "=" + URLEncoder.encode("서울특별시", "UTF-8")); /* 주소(시도) */
         nurlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + i); /* 페이지 번호 */
         nurlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "="
               + URLEncoder.encode("100", "UTF-8")); /* 목록 건수 */
         url = new URL(nurlBuilder.toString());
         conn = (HttpURLConnection) url.openConnection();
         conn.setRequestMethod("GET");
         conn.setRequestProperty("Content-type", "application/json");
         BufferedReader nrd;
         if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            nrd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
         } else {
            nrd = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
         }
         StringBuilder nsb = new StringBuilder();
         String nline;
         while ((nline = nrd.readLine()) != null) {
            nsb.append(nline);
         }
         nrd.close();
         conn.disconnect();
         List<List<String>> lphar = new ArrayList<List<String>>();
         String[] xmlsplit = (nsb.toString()).split("</item>");
         for (int j = 0; j < (xmlsplit.length - 1); j++) {
            List<String> ltmp = new ArrayList<String>();
            String stmp = xmlsplit[j];
            String hpid = stmp.substring(stmp.toString().lastIndexOf("<hpid>") + 6, stmp.lastIndexOf("</hpid>"));
            if (!(stmp.contains("<wgs84Lat>"))) { // oepan api에 위도 경도가 없는 것이 있는데 이것은 0으로 넣어준다.
               ltmp.add(stmp.substring(stmp.toString().lastIndexOf("<dutyName>") + 10, // 약국 이름
                     stmp.lastIndexOf("</dutyName>")));
               ltmp.add(stmp.substring(stmp.toString().lastIndexOf("<dutyAddr>") + 10, // 약국 주소
                     stmp.lastIndexOf("</dutyAddr>")));
               ltmp.add("0.0");
               ltmp.add("0.0");
               ltmp.add(stmp.substring(stmp.toString().lastIndexOf("<hpid>") + 6, stmp.lastIndexOf("</hpid>"))); // 약국 id
               lphar.add(ltmp);

            } else { 
               ltmp.add(stmp.substring(stmp.toString().lastIndexOf("<dutyName>") + 10,
                     stmp.lastIndexOf("</dutyName>")));
               ltmp.add(stmp.substring(stmp.toString().lastIndexOf("<dutyAddr>") + 10,
                     stmp.lastIndexOf("</dutyAddr>")));
               ltmp.add(stmp.substring(stmp.toString().lastIndexOf("<wgs84Lat>") + 10, // 약국 위도
                     stmp.lastIndexOf("</wgs84Lat>")));
               ltmp.add(stmp.substring(stmp.toString().lastIndexOf("<wgs84Lon>") + 10, // 약국 경도
                     stmp.lastIndexOf("</wgs84Lon>")));
               ltmp.add(stmp.substring(stmp.toString().lastIndexOf("<hpid>") + 6, stmp.lastIndexOf("</hpid>")));
               lphar.add(ltmp);
            }
         }

         for (List lst : lphar) {
            fid++;
            String tmp = "insert into facility values(" + fid + ", '약국', '" + lst.get(0) + "', '" + lst.get(1)
                  + "', " + Double.parseDouble((String) lst.get(2)) + ", "
                  + Double.parseDouble((String) lst.get(3)) + ");";
            p = dbconn.prepareStatement(tmp);
            p.executeUpdate();

         }
      }
      System.out.println("Done");
      return fid;
   }

    //csv 파일 기반의 시설 정보를 facility Table에 insert함
 	public static int protectarea(Connection dbconn, PreparedStatement p, int fid) throws SQLException {
 		try {

 			String sql = "insert into Facility values (?,?,?,?,?,?)";
 			p = dbconn.prepareStatement(sql);

 			// csv 파일 리스트
 			ArrayList<File> csv = new ArrayList<>();
 			List<String> arr;
 			csv.add(new File("전국도시공원정보표준데이터.csv"));
 			csv.add(new File("전국어린이보호구역표준데이터.csv"));
 			csv.add(new File("전국초중등학교위치표준데이터.csv"));
 			csv.add(new File("키즈카페.csv"));
 			csv.add(new File("역 정보.csv"));

 			String tmp;
 			int fileNumber = csv.size();
 			List<List<String>> ret;

 			for (int i = 0; i < fileNumber; i++) {
 				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csv.get(i)), "UTF-8"));
 				String line;
 				arr = null;
 				ret = new ArrayList<List<String>>();

 				// 줄 단위로 읽은 정보를 ','를 기준으로 sarr[]에 저장함, 그후 List<List<String>> ret에 저장
 				while ((line = br.readLine()) != null) {
 					String sarr[] = line.split(",");
 					arr = Arrays.asList(sarr);
 					ret.add(arr);
 				}

 				// 한줄씩 정보를 facility Table에 Insert 함.
 				// facility Table의 성분은 (fID int, ftype varchar(10), name varchar(40), addr
 				// varchar(70), latitude numeric(10,8), longitude numeric(10,7)
 				for (List str : ret) {
 					if (str.get(0).equals("어린이집") || str.get(0).equals("유치원")) {
 						fid++;
 						tmp = "insert into facility values(" + fid + ", '" + str.get(0) + "', '" + str.get(1) + "', '"
 								+ str.get(2) + "', " + Double.parseDouble((String) str.get(3)) + ", "
 								+ Double.parseDouble((String) str.get(4)) + ");";
 						System.out.println(tmp);
 						p = dbconn.prepareStatement(tmp);
 						p.executeUpdate();
 					}

 					else if (str.get(0).equals("초등학교") || str.get(0).equals("중학교") || str.get(0).equals("고등학교")) {
 						fid++;
 						tmp = "insert into facility values(" + fid + ", '" + str.get(0) + "', '" + str.get(1) + "', '"
 								+ str.get(2) + "', " + Double.parseDouble((String) str.get(3)) + ", "
 								+ Double.parseDouble((String) str.get(4)) + ");";
 						System.out.println(tmp);
 						p = dbconn.prepareStatement(tmp);
 						p.executeUpdate();
 					}

 					else if (str.get(0).equals("키즈카페")) {
 						fid++;
 						tmp = "insert into facility values(" + fid + ", '" + str.get(0) + "', '" + str.get(1) + "', '"
 								+ str.get(2) + "', " + Double.parseDouble((String) str.get(3)) + ", "
 								+ Double.parseDouble((String) str.get(4)) + ");";
 						System.out.println(tmp);
 						p = dbconn.prepareStatement(tmp);
 						p.executeUpdate();

 					} else if (str.get(0).equals("공원")) {
 						fid++;
 						tmp = "insert into facility values(" + fid + ", '" + str.get(0) + "', '" + str.get(1) + "', '"
 								+ str.get(2) + "', " + Double.parseDouble((String) str.get(3)) + ", "
 								+ Double.parseDouble((String) str.get(4)) + ");";
 						System.out.println(tmp);
 						p = dbconn.prepareStatement(tmp);
 						p.executeUpdate();
 					}

 					else if (str.get(0).equals("지하철 역")) {
 						fid++;
 						tmp = "insert into facility values(" + fid + ", '" + str.get(0) + "', '" + str.get(1) + "', '"
 								+ str.get(2) + "', " + Double.parseDouble((String) str.get(3)) + ", "
 								+ Double.parseDouble((String) str.get(4)) + ");";
 						System.out.println(tmp);
 						p = dbconn.prepareStatement(tmp);
 						p.executeUpdate();
 					} else {

 						System.out.println("Error : File name " + csv.get(i).getName() + " invalid value" + str.get(0));
 					}

 				}

 			}

 		} catch (FileNotFoundException e) {
 			System.out.println(e.getMessage());
 		} catch (IOException e) {
 			System.out.println(e.getMessage());
 		}

 		return fid;
 	}


   /*우편번호, 도로명 주소, 지번 주소를 지번 주소로 바꾼다.*/
   public static String doro2ji_addr(String addr) throws IOException {

      StringBuilder urlBuilder = new StringBuilder(
            "http://openapi.epost.go.kr/postal/retrieveNewAdressAreaCdSearchAllService/retrieveNewAdressAreaCdSearchAllService/getNewAddressListAreaCdSearchAll"); /*
                                                                                                                               * URL
                                                                                                                               */
      urlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8")
            + "=eiRmigGbmSrIZuGnQi6%2BqbexpI%2BZdyAfLekKDQ6GjwaI6ttotD8Lya9Nx57qpGGkHwEqfcqNcXtgR4%2BMRZaSCw%3D%3D"); /*
                                                                                              * Service
                                                                                              * Key
                                                                                              */
      urlBuilder
            .append("&" + URLEncoder.encode("srchwrd", "UTF-8") + "=" + URLEncoder.encode(addr, "UTF-8")); /* 검색어 */
      urlBuilder.append("&" + URLEncoder.encode("countPerPage", "UTF-8") + "="
            + URLEncoder.encode("10", "UTF-8")); /* 페이지당 출력될 개수를 지정(최대50) */
      urlBuilder.append("&" + URLEncoder.encode("currentPage", "UTF-8") + "="
            + URLEncoder.encode("1", "UTF-8")); /* 출력될 페이지 번호 */
      URL url = new URL(urlBuilder.toString());
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Content-type", "application/json");
      BufferedReader rd;
      if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
         rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
      } else {
         rd = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
      }
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = rd.readLine()) != null) {
         sb.append(line);
      }
      rd.close();
      conn.disconnect();
      return sb.toString().substring(sb.toString().lastIndexOf("<rnAdres>") + 9,
            sb.toString().lastIndexOf("</rnAdres>"));

   }
}