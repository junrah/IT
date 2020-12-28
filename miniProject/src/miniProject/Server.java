package miniProject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
/**
 * 서버 클래스
 */
public class Server {
	/**
	 * Server socket
	 */
	ServerSocket sSocket = null;
	/**
	 * Reads text from a character-input stream
	 */
	BufferedReader br1 = null;
	/**
	 * Writes text to a character-output stream
	 */
	BufferedWriter bw1 = null;
	/**
	 * Reads text from a character-input stream
	 */
	BufferedReader br2 = null;
	/**
	 * Writes text to a character-output stream
	 */
	BufferedWriter bw2  = null;
	/**
	 * Constructor for Server
	 */
	public Server() {
		try {
			sSocket = new ServerSocket(50000);
			System.out.println("서버를 생성했습니다!");
			
			Socket clientSocket1 = sSocket.accept();
			System.out.println("클라이언트와 연결완료. 클라이언트 IP: " + clientSocket1.getInetAddress());
			br1 = new BufferedReader(new InputStreamReader(clientSocket1.getInputStream()));
			bw1 = new BufferedWriter(new OutputStreamWriter(clientSocket1.getOutputStream()));
			
			Socket clientSocket2 = sSocket.accept();
			System.out.println("클라이언트와 연결완료. 클라이언트 IP: " + clientSocket2.getInetAddress());
			br2 = new BufferedReader(new InputStreamReader(clientSocket2.getInputStream()));
			bw2 = new BufferedWriter(new OutputStreamWriter(clientSocket2.getOutputStream()));
			
			bw1.write("당신은 1번 플레이어입니다.\n");
			bw1.flush();
			bw2.write("당신은 2번 플레이어입니다.\n");
			bw2.flush();
			
			System.out.println("게임 준비를 기다립니다");
			
			while(true) {
				String c1 = br1.readLine();
				String c2 = br2.readLine();
				if(c1.equals("READY") && c2.equals("READY")) {
					System.out.println("게임을 시작합니다");
					break;
				}
			}
						
			int turn = (int)(Math.random() * 2);
			System.out.println("선공은 " + (turn+1) + "번 플레이어입니다.");
			bw1.write("선공은 " + (turn+1) + "번 플레이어입니다.\n");
			bw1.flush();
			bw2.write("선공은 " + (turn+1) + "번 플레이어입니다.\n");
			bw2.flush();
			

			BufferedReader[] brs = {br1, br2};
			BufferedWriter[] bws = {bw1, bw2};
			
			bws[turn].write("START!" + "\n");
			bws[turn].flush();
			
			String input;
			while((input = brs[turn].readLine()) != null) { 				
				System.out.println(turn+1 + "번 : " + input);
				
				if(input.contains("빙고!")) {
					System.out.println(turn+1 + "번 플레이어 승리!");
					bws[turn].write("WIN!\n");
					bws[turn].flush();
					if(turn+1 > 1) {
						bws[turn-1].write(input + "\n");
						bws[turn-1].flush();
						break;
					}
					bws[turn+1].write(input + "\n");
					bws[turn+1].flush();
					break;
				}
				bws[(turn+1) % 2].write(input + "\n");
				bws[(turn+1) % 2].flush();
				turn = (turn+1) % 2;
			}
			
			while(true) {
				String c1 = br1.readLine();
				String c2 = br2.readLine();
				if(c1.contains("END") && c2.contains("END")) {
					System.out.println("게임종료");
					break;
				}
			}
						
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {
				if(bw1 != null)
					bw1.close();
				if(br1 != null)
					br1.close();
				if(bw2 != null)
					bw1.close();
				if(br2 != null)
					br1.close();
				if(sSocket != null) {
					sSocket.close();
				}
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Main
	 * @param args main
	 */
	public static void main(String[] args) {
		new Server();		
	}
}