package miniProject;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

/**
 * 게임 및 GUI 통합 클래스
 */
public class Client extends JFrame {
	/**
	 * 빙고판 SIZE
	 */
	private static final int SIZE = 5;
	/**
	 * 목표 빙고 개수
	 */
	private static final int BINGO = 12;
	/**
	 * 빙고판 버튼
	 */
	private JButton[][] btnMap;
	/**
	 * This class implements client socket
	 */
	private Socket socket = null;
	/**
	 * Reads text from a character-input stream
	 */
	private BufferedReader br = null;
	/**
	 * Writes text to a character-output stream
	 */
	private BufferedWriter bw = null;
	/**
	 * A display area for bingo
	 */
	private JLabel bingoLabel;
	/**
	 * A multi-line area that displays plain text
	 */
	private JTextArea textArea;
	/**
	 * A JScrollPane manages a textArea
	 */
	private JScrollPane scroll;
	/**
	 * Victory
	 */
	private boolean win = false;
	/**
	 * Order
	 */
	private boolean myTurn = false;
	/**
	 * 버튼입력 방지를 위한 정규식
	 */
	private Pattern pattern = Pattern.compile("[ !@#$%^&*(),.?\":{}|<>]");
	/**
	 * An engine that performs match operations on a character sequence by interpreting a Pattern. 
	 */
	private Matcher matcher;
	/**
	 * Button color
	 */
	private static final Color[] COLOR = { 
			new Color(255, 204, 204),  
			new Color(255, 255, 204),
			};
		
	/**
	 * 버튼을 클릭했을 때 입력받은 단어를 해당 버튼에 저장
	 */
	ActionListener setButton = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			JButton btn = (JButton)e.getSource(); 
			String s = JOptionPane.showInputDialog(null, "단어를 입력해주세요");
			if(null == s) {
				return;
			}
			if(s.equals("")) {
				btn.setText("");
				return;
			}
			if ((matcher = pattern.matcher(s)).find()) {
				JOptionPane.showMessageDialog(null, "공백 및 특수문자는 입력할 수 없습니다.");
				return;
			}
			for(int i=0; i<SIZE; ++i) {
				for(int j=0; j<SIZE; ++j) {
					String s2 = btnMap[i][j].getText();
					if(s.equals(s2)) {
						JOptionPane.showMessageDialog(null, "중복된 값은 입력할 수 없습니다.");
						return;
					}
				}
			}
			btn.setText(s);
		}
	};
	/**
	 * 버튼을 클릭했을 때 빙고판 버튼 확인후 준비완료 신호를 서버로 보냄
	 */
	ActionListener readyButton = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			for(int i=0; i<SIZE; ++i) {
				for(int j=0; j<SIZE; ++j) {
					String s = btnMap[i][j].getText();
					if(null == s | "".equals(s)) {
						JOptionPane.showMessageDialog(null, "빈칸을 채워주세요");
						return;
					}
				}
			}
			JButton btn = (JButton)e.getSource(); 
			btn.setText("GOAL BINGO : " + BINGO);
			btn.setEnabled(false);
			for(int i=0; i<SIZE; ++i) {
				for(int j=0; j<SIZE; ++j) {
					btnMap[i][j].removeActionListener(setButton);
					btnMap[i][j].setBackground(COLOR[0]);
					btnMap[i][j].addActionListener(chooseButton);
				}
			}
			exData("READY");
		}
	};
	/**
	 * 버튼을 클릭햇을 때 해당 플레이엉의 턴인지 확인한 후 빙고판 버튼 세팅, 서버로 전송
	 */
	ActionListener chooseButton = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if(myTurn) {
				JButton btn = (JButton)e.getSource();
				String s = btn.getText();
				setTextArea("나: " + s);
				setBingo(s);
				exData(s);
			}
		}
	};
	/**
	 * 서버와 연결
	 */
	private void connect() {
		try {
			socket = new Socket("127.0.0.1", 50000);
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			textArea.setText("상대를 기다리는 중입니다.");
			setTextArea(br.readLine());
			setTextArea(br.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 서버와 연결 종료
	 */
	private void close() {
		try {
			if(bw != null) bw.close();
			if(br != null) br.close();
			if(socket != null) socket.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 게임 내용 및 서버와의 커뮤니케이션 기록
	 * @param s 기록할 내용
	 */
	private void setTextArea(String s) {
		textArea.setText(textArea.getText() +"\n"+ s);
	}
	/**
	 * 서버로 정보 전달
	 * @param s 전달할 내용
	 */
	private void exData(String s) {
		try {
			if(win) s = s + "빙고!";
			bw.write(s + "\n");
			bw.flush();
			myTurn = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 빙고판 버튼 세팅
	 * @param s 세팅할 버튼
	 */
	private void setBingo(String s) {
		for(int i=0; i<SIZE; ++i) {
			for(int j=0; j<SIZE; ++j) {
				String s2 = btnMap[i][j].getText();
				if(s.equals(s2)) {
					btnMap[i][j].setBackground(COLOR[1]);
					btnMap[i][j].setEnabled(false);
				}
			}
		}
		check();
	}
	/**
	 * 빙고 확인
	 */
	private void check() {
		int bingo=0;
		int garo=0;
		int sero=0;
		int cross1=0;
		int cross2=0;
		
		for(int i=0; i<SIZE; ++i) {
			for(int j=0; j<SIZE; ++j) {
				if(false == btnMap[i][j].isEnabled()) {
					++garo;
					if(garo==SIZE) {
						++bingo;
						garo = 0;
					}
				}
				if(false == btnMap[j][i].isEnabled()) {
					++sero;
					if(sero==SIZE) {
						++bingo;
						sero = 0;
					}
				}
				if(false == btnMap[i][j].isEnabled() && i==j) {
					++cross1;
					if(cross1==SIZE) {
						++bingo;
					}
				}
				if(false == btnMap[i][j].isEnabled() && i+j==SIZE-1) {
					++cross2;
					if(cross2==SIZE) {
						++bingo;
					}
				}
			}
			garo = 0;
			sero = 0;
		}
		bingoLabel.setText("MY BINGO: " + bingo);
		if(bingo >= BINGO) win = true;
		
	}
	/**
	 * 중앙 빙고판 패널
	 * @return {@link JPanel}
	 */
	private JPanel center() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(SIZE,SIZE));
		btnMap = new JButton[SIZE][SIZE];
		for (int i = 0; i < SIZE; ++i) {
			for (int j = 0; j < SIZE; ++j) {
				JButton btn = new JButton("");
				btnMap[i][j] = btn;
				btnMap[i][j].addActionListener(setButton);
			}
		}
		for (JButton[] b : btnMap) { 
			for (JButton bb : b) {
				panel.add(bb);
			}
		}
		return panel;
	}
	/**
	 * 하단 준비완료버튼 패널
	 * @return {@link JPanel}
	 */
	private JPanel south() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout());
		JButton btn = new JButton("준비완료");
		btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
		btn.addActionListener(readyButton);
		panel.add(btn);
		return panel;
	}	
	/**
	 * 게임 내용 기록 패널
	 * @return {@link JPanel}
	 */
	private JPanel east() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		textArea = new JTextArea(1,17);
		textArea.setEditable(false);
		scroll = new JScrollPane(textArea);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		bingoLabel = new JLabel("MY BINGO");
		bingoLabel.setHorizontalAlignment(JLabel.CENTER);
		bingoLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
		panel.add(bingoLabel, BorderLayout.NORTH);
		panel.add(scroll, BorderLayout.CENTER);
		return panel;
	}
	/**
	 * Constructor for ImageFrame
	 */
	private Client() {
		super("BINGO GAME!");
		setSize(600, 500);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		add(center(), BorderLayout.CENTER);
		add(south(), BorderLayout.SOUTH);
		add(east(), BorderLayout.EAST);
				
		setVisible(true);
		connect();
		
		String input;
		loop:while(true) {
			try {
				while((input = br.readLine()) != null) {
					if(input.equals("START!")){
						myTurn = true;
						break;
					}
					
					if(input.equals("WIN!")) {
						setTextArea("빙고를 완성하였습니다");
						break loop;
					}
					
					if(input.contains("빙고!")) {
						if(input.equals("빙고!")) {
							setTextArea("상대가 빙고를 완성했습니다");
							break loop;
						}
						String s = input.replace("빙고!", "");
						setTextArea("상대가 " + s + "(을)를 입력하고\n빙고를 완성하였습니다.");
						break loop;
					}
					
					setTextArea("상대: " + input);
					setBingo(input);
					myTurn = true;
					if(win) exData("");
				}
				
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
		textArea.setCaretPosition(textArea.getDocument().getLength());
		String s = win? "승리" : "패배";
		JOptionPane.showMessageDialog(this, s);
		exData("END");
		close();
	}
	/**
	 * Main
	 * @param args main
	 */
	public static void main(String[] args) {
		new Client();
	}
}