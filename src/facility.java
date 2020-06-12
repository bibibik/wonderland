import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.*;
import java.sql.*;

public class facility {
	
	public static int hospital(Connection dbconn, PreparedStatement p, int fid) throws IOException{
		/*서울 응급실 있는 병원 hpid 가져오기, 소아과 있는 것 없는 것 구분해서 */
		String arr[] = {"강남구","강동구","강북구","강서구","관악구","광진구","구로구","금천구","노원구","도봉구","동대문구","동작구","마포구","서대문구","서초구","성동구","성북구","송파구","양천구","영등포구","용산구","은평구","종로구","중구","중랑구"};
		StringBuilder xmlhos = new StringBuilder();
		for(String tmp : arr) {
			StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B552657/ErmctInfoInqireService/getEmrrmRltmUsefulSckbdInfoInqire"); /*URL*/
			urlBuilder.append("?" + URLEncoder.encode("ServiceKey","UTF-8") + "=eiRmigGbmSrIZuGnQi6%2BqbexpI%2BZdyAfLekKDQ6GjwaI6ttotD8Lya9Nx57qpGGkHwEqfcqNcXtgR4%2BMRZaSCw%3D%3D"); /*Service Key*/
			urlBuilder.append("&" + URLEncoder.encode("STAGE1","UTF-8") + "=" + URLEncoder.encode("서울특별시", "UTF-8")); /*주소(시도)*/
			urlBuilder.append("&" + URLEncoder.encode("STAGE2","UTF-8") + "=" + URLEncoder.encode(tmp, "UTF-8")); /*주소(시군구)*/
			urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지 번호*/
			urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("10", "UTF-8")); /*목록 건수*/
			URL url = new URL(urlBuilder.toString());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-type", "application/json");
			BufferedReader rd;
			if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
				rd = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
			} else {
				rd = new BufferedReader(new InputStreamReader(conn.getErrorStream(),"URF-8"));
			}
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) {
				sb.append(line);
			}
			rd.close();
        	conn.disconnect();
        	xmlhos.append(sb.toString());
		}
		String[] xmlsplit = xmlhos.toString().split("</item>");
		/*sarr : 서울 응급실 hpid, yarr : 소아과 있는 응급실 hpid, narr : 소아과 없는 응급식 hpid*/
		List<String> sarr = new ArrayList<String>();
		List<String> yarr = new ArrayList<String>();
		for(int i = 0; i<(xmlsplit.length-1);i++) {
			String tmp = xmlsplit[i];
			sarr.add(tmp.substring(tmp.toString().lastIndexOf("<hpid>")+6,tmp.lastIndexOf("</hpid>")));
			if((tmp.substring(tmp.toString().lastIndexOf("<hv10>")+6,tmp.lastIndexOf("</hv10>"))).equals("Y")) {
				yarr.add(tmp.substring(tmp.toString().lastIndexOf("<hpid>")+6,tmp.lastIndexOf("</hpid>")));
			}
		}
		/*병원의 위도 경도 가져오기*/
		StringBuilder fxmlhos = new StringBuilder();
		for(String str : sarr) {
			StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B552657/ErmctInfoInqireService/getEgytBassInfoInqire"); /*URL*/
			urlBuilder.append("?" + URLEncoder.encode("ServiceKey","UTF-8") + "=eiRmigGbmSrIZuGnQi6%2BqbexpI%2BZdyAfLekKDQ6GjwaI6ttotD8Lya9Nx57qpGGkHwEqfcqNcXtgR4%2BMRZaSCw%3D%3D"); /*Service Key*/
			urlBuilder.append("&" + URLEncoder.encode("HPID","UTF-8") + "=" + URLEncoder.encode(str, "UTF-8")); /*주소(시도)*/
			URL url = new URL(urlBuilder.toString());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-type", "application/json");
			BufferedReader rd;
			if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
				rd = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
			} else {
				rd = new BufferedReader(new InputStreamReader(conn.getErrorStream(),"URF-8"));
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
		List<List<String>> shpid = new ArrayList<List<String>>();
		String[] fxmlsplit = fxmlhos.toString().split("</item>");
		for(int i = 0; i<(fxmlsplit.length-1);i++) {
			List<String> ltmp = new ArrayList<String>();
			String stmp = fxmlsplit[i];
			ltmp.add(stmp.substring(stmp.toString().lastIndexOf("<dutyName>")+10,stmp.lastIndexOf("</dutyName>")));
			ltmp.add(stmp.substring(stmp.toString().lastIndexOf("<dutyAddr>")+10,stmp.lastIndexOf("</dutyAddr>")));
			ltmp.add(stmp.substring(stmp.toString().lastIndexOf("<wgs84Lat>")+10,stmp.lastIndexOf("</wgs84Lat>")));
			ltmp.add(stmp.substring(stmp.toString().lastIndexOf("<wgs84Lon>")+10,stmp.lastIndexOf("</wgs84Lon>")));
			ltmp.add(stmp.substring(stmp.toString().lastIndexOf("<hpid>")+6,stmp.lastIndexOf("</hpid>")));
			shpid.add(ltmp);
		}
		for(List lst : shpid) {
			fid++;
			String addr =(String)lst.get(1);
			String jiaddr = "";
			if(lst.get(4).equals("A1120796") || lst.get(4).equals("A1100025") || lst.get(4).equals("A1100029")) {
				jiaddr = doro2ji_addr(addr.substring(addr.lastIndexOf("특별시")+4,addr.lastIndexOf(",")));
			}else {
				jiaddr = doro2ji_addr(addr.substring(addr.lastIndexOf("특별시")+4,addr.lastIndexOf("(")));
			}
			String tmp = "insert into facility values("+fid+", '응급실', '"+lst.get(0)+"', '"+jiaddr+"', "+Double.parseDouble((String) lst.get(2))+", "+Double.parseDouble((String) lst.get(3))+");";
			System.out.println(tmp);
			//p = dbconn.prepareStatement(tmp);
			//p.executeUpdate();
			for(String str : yarr) {
				if(lst.get(4).equals(str)) {
					tmp = "update facility set ftype = '응급실(소아과)' where fid = "+fid;
					System.out.println(tmp);
					//p = dbconn.prepareStatement(tmp);
					//p.executeUpdate();
				}
			}
			
		}
		
		return fid;
		
	}

	public static int pharmacy(Connection dbconn,PreparedStatement p, int fid) throws IOException{
        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B552657/ErmctInsttInfoInqireService/getParmacyListInfoInqire"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("ServiceKey","UTF-8") + "=eiRmigGbmSrIZuGnQi6%2BqbexpI%2BZdyAfLekKDQ6GjwaI6ttotD8Lya9Nx57qpGGkHwEqfcqNcXtgR4%2BMRZaSCw%3D%3D"); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("Q0","UTF-8") + "=" + URLEncoder.encode("서울특별시", "UTF-8")); /*주소(시도)*/
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        BufferedReader rd;
        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream(),"UTF-8"));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();
        /*한 페이지당 최대 출력 목록 수가 100개이 므로 총 개수에서 100을 나눠 1을 더한 페이지만큼 정보를 받아와야 한다.*/
        String scount = sb.toString().substring(sb.toString().lastIndexOf("<totalCount>")+12,sb.toString().lastIndexOf("</totalCount>"));
        int count = Integer.parseInt(scount)/100+1;
        for(int i = 1; i <= count;i++) {
        	StringBuilder nurlBuilder = new StringBuilder("http://apis.data.go.kr/B552657/ErmctInsttInfoInqireService/getParmacyListInfoInqire"); /*URL*/
        	nurlBuilder.append("?" + URLEncoder.encode("ServiceKey","UTF-8") + "=eiRmigGbmSrIZuGnQi6%2BqbexpI%2BZdyAfLekKDQ6GjwaI6ttotD8Lya9Nx57qpGGkHwEqfcqNcXtgR4%2BMRZaSCw%3D%3D"); /*Service Key*/
            nurlBuilder.append("&" + URLEncoder.encode("Q0","UTF-8") + "=" + URLEncoder.encode("서울특별시", "UTF-8")); /*주소(시도)*/
        	nurlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + i); /*페이지 번호*/
            nurlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("100", "UTF-8")); /*목록 건수*/
            url = new URL(nurlBuilder.toString());
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/json");
            BufferedReader nrd;
            if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                nrd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            } else {
                nrd = new BufferedReader(new InputStreamReader(conn.getErrorStream(),"UTF-8"));
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
    		for(int j = 0; j<(xmlsplit.length-1);j++) {
    			List<String> ltmp = new ArrayList<String>();
    			String stmp = xmlsplit[j];
    			String hpid = stmp.substring(stmp.toString().lastIndexOf("<hpid>")+6,stmp.lastIndexOf("</hpid>"));
    			if(!(stmp.contains("<wgs84Lat>"))) {
    				ltmp.add(stmp.substring(stmp.toString().lastIndexOf("<dutyName>")+10,stmp.lastIndexOf("</dutyName>")));
        			ltmp.add(stmp.substring(stmp.toString().lastIndexOf("<dutyAddr>")+10,stmp.lastIndexOf("</dutyAddr>")));
        			ltmp.add("0.0");
        			ltmp.add("0.0");
        			ltmp.add(stmp.substring(stmp.toString().lastIndexOf("<hpid>")+6,stmp.lastIndexOf("</hpid>")));
        			lphar.add(ltmp);
    			
    			}else {
    				ltmp.add(stmp.substring(stmp.toString().lastIndexOf("<dutyName>")+10,stmp.lastIndexOf("</dutyName>")));
    				ltmp.add(stmp.substring(stmp.toString().lastIndexOf("<dutyAddr>")+10,stmp.lastIndexOf("</dutyAddr>")));
    				ltmp.add(stmp.substring(stmp.toString().lastIndexOf("<wgs84Lat>")+10,stmp.lastIndexOf("</wgs84Lat>")));
    				ltmp.add(stmp.substring(stmp.toString().lastIndexOf("<wgs84Lon>")+10,stmp.lastIndexOf("</wgs84Lon>")));
    				ltmp.add(stmp.substring(stmp.toString().lastIndexOf("<hpid>")+6,stmp.lastIndexOf("</hpid>")));
    				lphar.add(ltmp);
    			}
    		}
    		
    		for(List lst : lphar) {
    			fid++;
    			String tmp = "insert into facility values("+fid+", '약국', '"+lst.get(0)+"', '"+lst.get(1)+"', "+Double.parseDouble((String) lst.get(2))+", "+Double.parseDouble((String) lst.get(3))+");";
    			System.out.println(tmp);
				//p = dbconn.prepareStatement(tmp);
				//p.executeUpdate();
    			
    		}
        }

        return fid;		
	}

	public static int protectarea(Connection dbconn, PreparedStatement p, int fid) throws SQLException {
		try {
			File csv = new File("C:\\Users\\Home\\Downloads\\전국어린이보호구역표준데이터.csv");
			List<List<String>> ret = new ArrayList<List<String>>();
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csv), "UTF-8"));
			String line;
			while((line = br.readLine()) != null) {
				String sarr[] = line.split(",");
				if(sarr[3].isBlank()) {
					sarr[3] = doro2ji_addr(sarr[2]); 
				}
				List<String> arr = Arrays.asList(sarr);
				ret.add(arr);
			}
			for(List str : ret) {
				if(str.get(0).equals("어린이집") || str.get(0).equals("유치원")) {
					fid++;
					String tmp = "insert into facility values("+fid+", '"+str.get(0)+"', '"+str.get(1)+"', '"+str.get(3)+"', "+Double.parseDouble((String) str.get(4))+", "+Double.parseDouble((String) str.get(5))+");";
					System.out.println(tmp);
					//p = dbconn.prepareStatement(tmp);
					//p.executeUpdate();
				}
			}
		} catch (FileNotFoundException  e) { 
			System.out.println(e.getMessage()); 
		} catch (IOException e) { 
			System.out.println(e.getMessage()); 
		}
		
		return fid;
	}
	
	public static String doro2ji_addr(String addr) throws IOException {
		
		StringBuilder urlBuilder = new StringBuilder("http://openapi.epost.go.kr/postal/retrieveNewAdressAreaCdSearchAllService/retrieveNewAdressAreaCdSearchAllService/getNewAddressListAreaCdSearchAll"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("ServiceKey","UTF-8") + "=eiRmigGbmSrIZuGnQi6%2BqbexpI%2BZdyAfLekKDQ6GjwaI6ttotD8Lya9Nx57qpGGkHwEqfcqNcXtgR4%2BMRZaSCw%3D%3D"); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("srchwrd","UTF-8") + "=" + URLEncoder.encode(addr, "UTF-8")); /*검색어*/
        urlBuilder.append("&" + URLEncoder.encode("countPerPage","UTF-8") + "=" + URLEncoder.encode("10", "UTF-8")); /*페이지당 출력될 개수를 지정(최대50)*/
        urlBuilder.append("&" + URLEncoder.encode("currentPage","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*출력될 페이지 번호*/
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        BufferedReader rd;
        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream(),"UTF-8"));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();
        return sb.toString().substring(sb.toString().lastIndexOf("<rnAdres>")+9,sb.toString().lastIndexOf("</rnAdres>"));
		
	}


}
