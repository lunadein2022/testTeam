package org.sp.tproject.calendar.model;
//client 테이블에 대한 CRUD만을 담당하기 위한 객체

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.sp.tproject.calendar.domain.Client;

import util.DBManager;

public class ClientDAO {
	String url = "jdbc:oracle:thin:@localhost:1521:XE";
	String user ="pomodoro";
	String password = "1234";
	DBManager dbManager;
	
	public ClientDAO(DBManager dbManager) {
		this.dbManager = dbManager;
	}
	//사용자 레코드 1건 등록
	//사용자 회원 가입 시 사용
	//이름/아이디/비밀번호/비밀번호 확인/닉네임/이메일 주소
	public int insert(Client client) {
		Connection con =null;
		PreparedStatement pstmt = null;
		
		int result = 0;	//DML 에 대한 성공, 실패 판당
		con = dbManager.connect();	
		if(con==null) {
			System.out.println("접속 실패");
		}
		StringBuilder sb = new StringBuilder();
		
		sb.append("insert into client(client_idx, name, id, pass, nickname, email)");
		sb.append(" values(seq_client.nextval, ?,?,?,?,?)");
		
		try {
			pstmt = con.prepareStatement(sb.toString());
			pstmt.setString(1, client.getName());
			pstmt.setString(2, client.getId());
			pstmt.setString(3, client.getPass());
			pstmt.setString(4, client.getNickname());
			pstmt.setString(5, client.getEmail());
			
			result = pstmt.executeUpdate();	//DML 쿼리 실행
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			dbManager.release(con, pstmt);
		}
		
		return result;
	}
	
	//사용자 레코드 1건 검색
	//사용자 로그인 시 사용
	public Client login(Client client) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Client dto = null;	//로그인 후, 해당 관리자 1사람 정보를 담기 위한 객체
		
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			con = DriverManager.getConnection(url,user,password);
			if(con == null) {
				System.out.println("접속 실패");
			}else {
				String sql = "select * from client where id=? and pass=?";
				pstmt = con.prepareStatement(sql);
				pstmt.setString(1, client.getId());
				pstmt.setString(2, client.getPass());
				rs = pstmt.executeQuery();
				
				if(rs.next()) {
					dto = new Client();	//비어있는 dto 인스턴스 생성
					//채워넣기
					//이름,아이디,비밀번호,닉네임,이메일 주소
					dto.setClient_idx(rs.getInt("client_idx"));
					dto.setName(rs.getString("name"));
					dto.setId(rs.getString("id"));
					dto.setPass(rs.getString("pass"));
					dto.setNickname(rs.getString("nickname"));
					dto.setEmail(rs.getString("email"));
				}
			}
			
			
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			dbManager.release(con, pstmt, rs);
		}

		return dto;
	}
}
