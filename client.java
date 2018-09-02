
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

	public class client extends JFrame implements ActionListener {

		public static final int BOARD_SIZE = 3;

		public static enum gamestatus {
			XWins, ZWins, Incomplete, Tie;
		}

		private JButton[][] buttons = new JButton[BOARD_SIZE][BOARD_SIZE];

		boolean crossturn = true;

	    private static int PORT = 8004;
	    private Socket socket;
	    private BufferedReader in;
	    private PrintWriter out;

	    // Constructs the client by connecting to a server, laying out the GUI and registering GUI listeners.
	  
	    public client(String serverAddress) throws Exception {

	        // Setup networking
	        socket = new Socket(serverAddress, PORT);
	        in = new BufferedReader(new InputStreamReader(
	            socket.getInputStream()));
	        out = new PrintWriter(socket.getOutputStream(), true);

	        // Layout GUI
	        super.setSize(500, 500);
			super.setTitle("Tic Tac Toe !!");
			GridLayout grid = new GridLayout(BOARD_SIZE, BOARD_SIZE);
			super.setLayout(grid);
	       
			Font font = new Font("Comic Sans", 2, 100);
			for (int row = 0; row < BOARD_SIZE; row++) {
				for (int col = 0; col < BOARD_SIZE; col++) {
					JButton button = new JButton("");
					buttons[row][col] = button;
					button.setFont(font);
					button.setBackground(Color.white);
					
					button.addActionListener(this);
					super.add(button);

				}
			}
			super.setResizable(false);
			super.setVisible(true);

	    }

	   //* The main thread of the client will listen for messages from the server.  
	   //The first message will be a "WELCOME" message in which we receive our mark.  
	   //Then we go into a loop listening for: 
	   //--> "VALID_MOVE", --> "OPPONENT_MOVED", --> "VICTORY", --> "DEFEAT", --> "TIE", --> "OPPONENT_QUIT, --> "MESSAGE" messages, and handling each message appropriately.
	   //The "VICTORY","DEFEAT" and "TIE" ask the user whether or not to play another game. 
	   //If the answer is no, the loop is exited and the server is sent a "QUIT" message.  If an OPPONENT_QUIT message is recevied then the loop will exit and the server will be sent a "QUIT" message also.
	    @Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			JButton clickedbutton = (JButton) e.getSource();
			makemove(clickedbutton);
			gamestatus gs = getgamestatus();
			if (gs == gamestatus.Incomplete) {
				return;
			}
			declareWinner(gs);
			int ans=JOptionPane.showConfirmDialog(this,"Do you want to restart the game ?");
			if(ans==JOptionPane.YES_OPTION){
				for(int row=0;row<BOARD_SIZE;row++){
					for(int col=0;col<BOARD_SIZE;col++){
						buttons[row][col].setText("");
					}
				}
				crossturn=true;
			}
			else{
				super.dispose();
			}
		}

		public void makemove(JButton clickedbutton) {
			String btntext = clickedbutton.getText();
			if (btntext.length() > 0) {
				JOptionPane.showMessageDialog(this, "Invalid Move");
				return;
			}
			if (crossturn) {
				clickedbutton.setText("X");

			} else {
				clickedbutton.setText("O");
			}
			crossturn = !crossturn;
		}

		private gamestatus getgamestatus() {
			String text1 = "", text2 = "";
			int row = 0;
			int col = 0;
			// ROW
			while (row < BOARD_SIZE) {
				col = 0;
				while (col < BOARD_SIZE - 1) {
					text1 = buttons[row][col].getText();
					text2 = buttons[row][col + 1].getText();

					if (!text1.equals(text2) || text1 == "" || text2 == "") {
						break;
					}
					col++;
				}
				//Reaching here => 3 in a row
				if (col == BOARD_SIZE - 1) {
					String btntext = buttons[row][col].getText();
					if (btntext == "X") {
						return gamestatus.XWins;
					} else {
						return gamestatus.ZWins;
					}
				}
				row++;

			}
			// COL
			row = 0;
			col = 0;
			while (col < BOARD_SIZE) {
				row = 0;
				while (row < BOARD_SIZE - 1) {
					text1 = buttons[row][col].getText();
					text2 = buttons[row + 1][col].getText();

					if (!text1.equals(text2) || text1 == "" || text2 == "") {
						break;
					}
					row++;
				}
				if (row == BOARD_SIZE - 1) {
					String btntext = buttons[row][col].getText();
					if (btntext == "X") {
						return gamestatus.XWins;
					} else {
						return gamestatus.ZWins;
					}
				}
				col++;

			}
			// \DIAG
			row = 0;
			col = 0;
			while (row < BOARD_SIZE - 1) {

				text1 = buttons[row][col].getText();
				text2 = buttons[row + 1][col + 1].getText();

				if (!text1.equals(text2) || text1 == "" || text2 == "") {
					break;
				}
				col++;
				row++;
			}
			if (row == BOARD_SIZE - 1) {
				String btntext = buttons[row][col].getText();
				if (btntext == "X") {
					return gamestatus.XWins;
				} else {
					return gamestatus.ZWins;
				}
			}

			/// DIAG
			row = BOARD_SIZE - 1;
			col = 0;
			while (row > 0) {

				text1 = buttons[row][col].getText();
				text2 = buttons[row - 1][col + 1].getText();

				if (!text1.equals(text2) || text1 == "" || text2 == "") {
					break;
				}
				col++;
				row--;
			}
			if (row == 0) {
				String btntext = buttons[row][col].getText();
				if (btntext == "X") {
					return gamestatus.XWins;
				} else {
					return gamestatus.ZWins;
				}
			}
            // NO WINS YET CHECK FOR INCOMPLETE OR TIE
			row = 0;
			col = 0;
			for (row = 0; row < BOARD_SIZE; row++) {
				for (col = 0; col < BOARD_SIZE; col++) {
					String text = buttons[row][col].getText();
					if (text.length() == 0) {
						return gamestatus.Incomplete;
					}
				}
			}
			return gamestatus.Tie;

		}

	public void declareWinner(gamestatus gs){
		if(gs==gamestatus.XWins){
			JOptionPane.showMessageDialog(this,"X WINS");
		}
		else if(gs==gamestatus.ZWins){
			JOptionPane.showMessageDialog(this, "O WINS");
		}
		else{
			JOptionPane.showMessageDialog(this, "Tie");
		}
	}
	


	    
	    //main
	    public static void main(String[] args) throws Exception {
	        
	            String serverAddress = (args.length == 0) ? "localhost" : args[1];
	            client c = new client(serverAddress);
	            
	        
	    }
	}
