import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.*;
import java.sql.*;

public class facility {
	
	public static int hospital(Connection dbconn, PreparedStatement p, int fid) throws IOException{
		/*���� ���޽� �ִ� ���� hpid ��������, �Ҿư� �ִ� �� ���� �� �����ؼ� */
		String arr[] = {"������","������","���ϱ�","������","���Ǳ�","������","���α�","��õ��","�����","������","���빮��","���۱�","������","���빮��","���ʱ�","������","���ϱ�","���ı�","��õ��","��������","��걸","����","���α�","�߱�","�߶���"};
		StringBuilder xmlhos = new StringBuilder();
		for(String tmp : arr) {
			StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B552657/ErmctInfoInqireService/getEmrrmRltmUsefulSckbdInfoInqire"); /*URL*/
			urlBuilder.append("?" + URLEncoder.encode("ServiceKey","UTF-8") + "=eiRmigGbmSrIZuGnQi6%2BqbexpI%2BZdyAfLekKDQ6GjwaI6ttotD8Lya9Nx57qpGGkHwEqfcqNcXtgR4%2BMRZaSCw%3D%3D"); /*Service Key*/
			urlBuilder.append("&" + URLEncoder.encode("STAGE1","UTF-8") + "=" + URLEncoder.encode("����Ư����", "UTF-8")); /*�ּ�(�õ�)*/
			urlBuilder.append("&" + URLEncoder.encode("STAGE2","UTF-8") + "=" + URLEncoder.encode(tmp, "UTF-8")); /*�ּ�(�ñ���)*/
			urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*������ ��ȣ*/
			urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("10", "UTF-8")); /*��� �Ǽ�*/
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
		/*sarr : ���� ���޽� hpid, yarr : �Ҿư� �ִ� ���޽� hpid, narr : �Ҿư� ���� ���޽� hpid*/
		List<String> sarr = new ArrayList<String>();
		List<String> yarr = new ArrayList<String>();
		for(int i = 0; i<(xmlsplit.length-1);i++) {
			String tmp = xmlsplit[i];
			sarr.add(tmp.substring(tmp.toString().lastIndexOf("<hpid>")+6,tmp.lastIndexOf("</hpid>")));
			if((tmp.substring(tmp.toString().lastIndexOf("<hv10>")+6,tmp.lastIndexOf("</hv10>"))).equals("Y")) {
				yarr.add(tmp.substring(tmp.toString().lastIndexOf("<hpid>")+6,tmp.lastIndexOf("</hpid>")));
			}
		}
		/*������ ���� �浵 ��������*/
		StringBuilder fxmlhos = new StringBuilder();
		for(String str : sarr) {
			StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B552657/ErmctInfoInqireService/getEgytBassInfoInqire"); /*URL*/
			urlBuilder.append("?" + URLEncoder.encode("ServiceKey","UTF-8") + "=eiRmigGbmSrIZuGnQi6%2BqbexpI%2BZdyAfLekKDQ6GjwaI6ttotD8Lya9Nx57qpGGkHwEqfcqNcXtgR4%2BMRZaSCw%3D%3D"); /*Service Key*/
			urlBuilder.append("&" + URLEncoder.encode("HPID","UTF-8") + "=" + URLEncoder.encode(str, "UTF-8")); /*�ּ�(�õ�)*/
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
				jiaddr = doro2ji_addr(addr.substring(addr.lastIndexOf("Ư����")+4,addr.lastIndexOf(",")));
			}else {
				jiaddr = doro2ji_addr(addr.substring(addr.lastIndexOf("Ư����")+4,addr.lastIndexOf("(")));
			}
			String tmp = "insert into facility values("+fid+", '���޽�', '"+lst.get(0)+"', '"+jiaddr+"', "+Double.parseDouble((String) lst.get(2))+", "+Double.parseDouble((String) lst.get(3))+");";
			System.out.println(tmp);
			//p = dbconn.prepareStatement(tmp);
			//p.executeUpdate();
			for(String str : yarr) {
				if(lst.get(4).equals(str)) {
					tmp = "update facility set ftype = '���޽�(�Ҿư�)' where fid = "+fid;
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
        urlBuilder.append("&" + URLEncoder.encode("Q0","UTF-8") + "=" + URLEncoder.encode("����Ư����", "UTF-8")); /*�ּ�(�õ�)*/
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
        /*�� �������� �ִ� ��� ��� ���� 100���� �Ƿ� �� �������� 100�� ���� 1�� ���� ��������ŭ ������ �޾ƿ;� �Ѵ�.*/
        String scount = sb.toString().substring(sb.toString().lastIndexOf("<totalCount>")+12,sb.toString().lastIndexOf("</totalCount>"));
        int count = Integer.parseInt(scount)/100+1;
        for(int i = 1; i <= count;i++) {
        	StringBuilder nurlBuilder = new StringBuilder("http://apis.data.go.kr/B552657/ErmctInsttInfoInqireService/getParmacyListInfoInqire"); /*URL*/
        	nurlBuilder.append("?" + URLEncoder.encode("ServiceKey","UTF-8") + "=eiRmigGbmSrIZuGnQi6%2BqbexpI%2BZdyAfLekKDQ6GjwaI6ttotD8Lya9Nx57qpGGkHwEqfcqNcXtgR4%2BMRZaSCw%3D%3D"); /*Service Key*/
            nurlBuilder.append("&" + URLEncoder.encode("Q0","UTF-8") + "=" + URLEncoder.encode("����Ư����", "UTF-8")); /*�ּ�(�õ�)*/
        	nurlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + i); /*������ ��ȣ*/
            nurlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("100", "UTF-8")); /*��� �Ǽ�*/
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
    			String tmp = "insert into facility values("+fid+", '�౹', '"+lst.get(0)+"', '"+lst.get(1)+"', "+Double.parseDouble((String) lst.get(2))+", "+Double.parseDouble((String) lst.get(3))+");";
    			System.out.println(tmp);
				//p = dbconn.prepareStatement(tmp);
				//p.executeUpdate();
    			
    		}
        }

        return fid;		
	}

	public static int protectarea(Connection dbconn, PreparedStatement p, int fid) throws SQLException {
		try {
			File csv = new File("C:\\Users\\Home\\Downloads\\������̺�ȣ����ǥ�ص�����.csv");
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
				if(str.get(0).equals("�����") || str.get(0).equals("��ġ��")) {
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
        urlBuilder.append("&" + URLEncoder.encode("srchwrd","UTF-8") + "=" + URLEncoder.encode(addr, "UTF-8")); /*�˻���*/
        urlBuilder.append("&" + URLEncoder.encode("countPerPage","UTF-8") + "=" + URLEncoder.encode("10", "UTF-8")); /*�������� ��µ� ������ ����(�ִ�50)*/
        urlBuilder.append("&" + URLEncoder.encode("currentPage","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*��µ� ������ ��ȣ*/
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
