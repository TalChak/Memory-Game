
package MAMAN_16_1;
import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.Timer;
import javax.swing.text.*;
/**
 *
 * @author tchaklai
 */
public class MemoryGameClient extends JFrame{
    private JFrame mainFrame;					//main frame
    private JPanel mainPanel;			//all cards
    private JPanel textPanel;                       //send information about points and status
   // private TurnCounterLabel turnCounterLabel;
    private ArrayList<ImageIcon> cardIcon=new ArrayList<ImageIcon>(); //icon array
   
    private ArrayList<String> animalArray=new ArrayList<String>();
    private int myPoints=0;  //set points
    private int opponentPoints=0;  
   
    private JTextField text;
    private  JLabel jlabel;
    private static String hostName;
    private static  int portNumber;
    private Socket connection; // connection to server
    private int player;
    private boolean myTurn=false;
    private Scanner input; // input from server
    private Formatter output; // output to server
    private boolean  gameOn=true;
    public Card currentCard;
    public ArrayList<Card> allCardsArray=new ArrayList<Card>();///represents the layout of the cards on the board
    public int allMatchesArray=0;
    int n;    //how many cards, chosen by the 
    int myIndex;
    int firstPlayerIndex;
    int currentPlayerIndex;
    public int turnedCardsCounter=0;
   
    
    public MemoryGameClient( String host , int port )
    {
	
        
        setConnection();
     
        setFrame();
	
	playGame();
	
	
    }
    
  
	public void setConnection()
	{
		try 
                {
                     hostName = JOptionPane.showInputDialog("Please enter Host Name\n(Press cancel for default: \"localhost\")");
                     portNumber = Integer.parseInt(JOptionPane.showInputDialog("Please enter port\n(Press cancel for default: \"22222\")"));
                   
                    if(!(hostName.equalsIgnoreCase("localhost"))||(!(portNumber==22222)) )
                    {
                        JOptionPane.showMessageDialog(null,
                        "Couldn't Connect",
                        "Connection Error",
                        JOptionPane.ERROR_MESSAGE);
                    }
                   
			// make connection to server	
		    connection = new Socket(InetAddress.getByName( hostName ), portNumber );
		
		    // get streams for input and output
		    input = new Scanner( connection.getInputStream() );
		    output = new Formatter( connection.getOutputStream() );
		 }  //catch exception
		catch ( IOException ioException )
		{
			JOptionPane.showConfirmDialog(null, "Connection Error");
			System.exit(1);
		}
                
	}
	/**
	 * 
	*/
	public void setFrame()
	{
		//set the frame, panels, text field, location, size
             
            this.mainFrame = new JFrame("Memory Game");
            this.mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.mainFrame.setSize(1000,1200);
            this.mainFrame.setLocation(500,0); 
            this.mainPanel = new JPanel();
            this.mainPanel.setSize(1000,1000);
            this.mainPanel.setLayout(new FlowLayout()); 
            this.mainPanel.setLocation(0,200 ); 
            this.textPanel=new JPanel();
            this.textPanel.setSize(1000,200);
            this.textPanel.setLocation(0,0);
            text= new JTextField();
            this.cardIcon = setCards();
            text.setText("Welcome!");  //just to put some string here
            text.setEditable( false );   
            text.setFont(new Font("Verdana",1,20));
            text.setBorder(new LineBorder(Color.BLACK)); 
            mainFrame.add(text, BorderLayout.NORTH);
            this.newGame();

	}
  
	//public void actionPerformed(ActionEvent e)
	//{
	
	//}

	 /*Makes a new set of cards and puts them in a new card field (a JPanel)
	 *
	 * @return new panel populated with new cards
	*/
	public JPanel makeCards()
	{
		// create the cards
		JPanel panel = new JPanel(new GridLayout(4, 4));
                panel.setBackground(Color.WHITE);
		// set the turned manager
                TurnedCardsManager turnedManager = new TurnedCardsManager();
		
		// all cards have same back
		ImageIcon backIcon = new ImageIcon(getClass().getResource("/images/365.png"));
	
		for(int i = 0; i <this.animalArray.size(); i++)
		{
			
                    
			// set card icons
                    Card newCard = new Card(turnedManager, cardIcon.get(i), backIcon, i);
                    allCardsArray.add(newCard);
			// add it to the panel
                    newCard.setPreferredSize(new Dimension(250, 250)); 
                    panel.add(newCard);
		}
		// return the filled panel
		return panel;
	}
	
	/**
	 * Prepares a new game (first game or non-first game)
	*/
	public void newGame()
	{
          
           // this.mainPanel.removeAll();
		// make a new card field with cards, and add it to the window
            
            this.mainPanel.add(makeCards());
		// add the turn counter label back in again
           // this.mainPanel.add(this.turnCounterLabel);
		// show the window (in case this is the first game)
            
            mainFrame.add(mainPanel, BorderLayout.WEST);
            this.mainFrame.setVisible(true);
  
	}
        public ArrayList<ImageIcon> setCards()
        {
            ArrayList<ImageIcon> icon= new ArrayList<ImageIcon>();
                n= input.nextInt();
                System.out.println(n);
              
                for(int i=0;i<n;i++)
                {                   
                        String animal=input.next();
                        this.animalArray.add(animal); 
                        System.out.print(this.animalArray.get(i)+" ,");
                }
             
                for(int j = 0; j <n ; j++ )
                {
			// make a new icon from a cardX.gif file
                    String fileName = "/images/"+this.animalArray.get(j) + ".png"; 
                   
                    icon.add(new ImageIcon(getClass().getResource(fileName)));
                }
                return icon;
        }
        
        public void playGame()
	{
		// Initial connection: get cards quantity and player's index		
		//get n
                 n= input.nextInt();  //cards quantity
                myIndex= input.nextInt();    //what is my index
		 
                  
                text.setText("Memory game" );    

		// receive messages sent to client and output them
		while ( gameOn )
		{
			if ( input.hasNextLine() )
                        {
				processMessage(input.nextLine());
                        }
		}
	}
        
        public boolean isGameOver()
        {
            if(allMatchesArray==n)
                
                return true;
            else
                return false;
                
        }
                
        private void processMessage( String message )
	{
		int card1=0;
                int card2=0;
                Card card;
                
		switch (message) 
                {
                    case "You Start the Game!":    //randomaly chosen by the server
                         myTurn = true;         ///my turn now
                         text.setText(message);
                         break;
                    case "Opponent Turned Cards":   // notify that the other player had turned two cards
                        System.out.println("Opponent Turned Cards");
                         card1 = input.nextInt();  
                         card2 = input.nextInt();
                         input.nextLine();
                         allCardsArray.get(card1).turnUp(); 
                         allCardsArray.get(card2).turnUp();  
                         if(checkForMatch(card1, card2))          //if he had a match, he gets one more point
                          {
                              opponentPoints++;
                          }
                          allMatchesArray=allMatchesArray+2;
                          myTurn = true; 
                          text.setText(yourTurnMsg());
                          break;
                       
                    case "Valid Move":
                        text.setText(message+ ", Wait for The Opponents' Turn");
                        allCardsArray.get(card1).turnUp(); 
                        allCardsArray.get(card2).turnUp(); //turn these cards up
                        break;
                        
                    case "Invalid Move":
                          text.setText(message+", Try Again");
                          myTurn = true;
                          break;
        
                       
                    default:
                            text.setText(message);
		}

			
	} 
        //set text messages for any case
        public String yourTurnMsg()
        {
            return " .Your turn  |   Your Points: "+myPoints+"     Opponents' Points: "+opponentPoints;
        }
        
        public String opponentTurnMsg()
        {
            return " .Your turn  |   Your Points: "+myPoints+"  Points     Opponents' Points: "+opponentPoints+"  points";
        }
        public String youLostMsg()
        {
            return " .Game Over, You Lose  |   Your Points: "+myPoints+"  Points     Opponents' Points: "+opponentPoints+"  points";
        }
        
         public String youWonMsg()
         {
             return " .congratulations! You Win!  |   Your Points: "+myPoints+"  Points     Opponents' Points: "+opponentPoints+"  points";
         }
           
       
        public boolean checkForMatch(int card1,int card2 )    //compare twu turned cards by its animal
        {
            if(this.animalArray.get(card1).equalsIgnoreCase(this.animalArray.get(card2)))
                return true;
            else
                return false;
        }
        public class TurnedCardsManager implements ActionListener
        {
	
            private Timer turnDownTimer;  //timer 
            private final int turnDownDelay = 3000; //set to 3 seconds
            private ArrayList<Card> turnedCardsArray;  //if a card is turned, it gets here
	
	 // Constructor

            public TurnedCardsManager()
            {
                    // create the list
                    this.turnedCardsArray = new  ArrayList<>();
                    // make timer object
                    this.turnDownTimer = new Timer(this.turnDownDelay, this);
                    this.turnDownTimer.setRepeats(false);
            }

            private boolean doAddCard(Card card)
            {
                    // add the card to the list
                    this.turnedCardsArray.add(card);
                    // there are two cards
                    if(this.turnedCardsArray.size() == 2)
                    {
                        
                            // get the other card (which was already turned up)
                            Card otherCard = (Card)this.turnedCardsArray.get(0);
                            // the cards match, so remove them from the list (they will remain face up)
                            int num1=otherCard.getNum();
                            int num2=card.getNum(); 
                            
                            if( checkForMatch(num1, num2))
                            {
                                    this.turnedCardsArray.clear();
                                    allMatchesArray=allMatchesArray+2;
                                    myPoints++;
                                    
                                    
                            }
                            // the cards do not match, so start the timer and afetr 3 seconds turn them down
                            else 
                                this.turnDownTimer.start();
                             
                                  
                    }
                    return true;
            }

      
            public boolean turnUp(Card card)    //only if the user haven't turned 2 cards yet, he can turn a card.
            {
                    // the card may be turned
                    if(this.turnedCardsArray.size() < 2) 
                        //return 
                        return doAddCard(card);
                    // there are already 2 cards in the list
                    return false;
            }

        
            public void actionPerformed(ActionEvent e)
            {
                    // turn each card down
                    for(int i = 0; i < this.turnedCardsArray.size(); i++ )
                    {
                            Card card = (Card)this.turnedCardsArray.get(i);
                            card.turnDown();
                    }
                    //after the two cards turned down, clear the list to start over
                    this.turnedCardsArray.clear();
                   
            }
        
     }   
        
        public class Card extends JLabel implements MouseListener
       {
	TurnedCardsManager turnedManager;   //manage the turned cards
	Icon faceIcon;  //animal
	Icon backIcon;  //cover
	boolean faceUp = false; // card is initially face down
	int num; 	// number corresponding to the face of the card
	int iconWidthHalf, iconHeightHalf; 	// half the dimensions of the back face icon
	boolean mousePressedOnMe = false;
	int turnUpCounter=0;   ///we  need a counter for the player that doesn't plick on the cards
	/**

	*/
	public Card(TurnedCardsManager turnedManager, Icon face, Icon back, int num)
	{
		// initially show face down
		super(back);
		// save parameters
		this.turnedManager = turnedManager;
		this.faceIcon = face;
		this.backIcon = back;
		this.num = num; // number associated with face icon, used to compare cards for equality
		// catch mouse clicks and events
		this.addMouseListener(this);
		// store icon dimensions for mouse click testing
		this.iconWidthHalf = back.getIconWidth() / 2;
		this.iconHeightHalf = back.getIconHeight() / 2;
	}
	
	/**
	 * Try to turn face up
	*/
	public void turnUp()
	{
		if(this.faceUp)  //already turned
                    return;
		// ask manager if we can turn this card
		this.faceUp = this.turnedManager.turnUp(this);
		// allowed to turn, so change the picture
		if(this.faceUp) this.setIcon(this.faceIcon);
                
                currentCard=this;
                turnUpCounter=0;         //set the counter to 0 so we can start over
             
	}
  
	public void turnDown()
	{
	
		if(!this.faceUp) return;  //already turned down
		// change the picture
		this.setIcon(this.backIcon);
		// the card is now down
		this.faceUp = false;
	}
	
	/**
	 * 
	*/
	public int getNum() 
        {
            return num; //return the number identifying the animal
        }
	
	private boolean overIcon(int x, int y)
	{
		// calculate the distance from the center of the label
		int distX = Math.abs(x - (this.getWidth() / 2));
		int distY = Math.abs(y - (this.getHeight() / 2));
		// outside icon region
		if(distX > this.iconWidthHalf || distY > this.iconHeightHalf )
			return false;
		// inside icon region
		return true;
	}
	
	
	public void mouseClicked(MouseEvent e)  //Invoked when the mouse button has been clicked (pressed and released) on a component.
	{
		// over icon, so try to turn up the card
            if ( myTurn )
            {
                 if(turnedCardsCounter<2)         //this counter is for the current player that click on the board
                 {                                  // if he hasn't clicked on two cards yet, he can chose a card tu click on
                    if(overIcon(e.getX(), e.getY())) 
                    {
                        this.turnUp();
                        output.format( "%d\n", this.getNum() ); // send card number to server
                        output.flush();
                 
                       turnedCardsCounter++;
                       if(turnedCardsCounter==2)     //two cards has turned
                       {
                           myTurn=false;
                           turnedCardsCounter=0;           //set the counter to 0 so we can start over.
                           text.setText(opponentTurnMsg());
                            
                       }
                           
                    }
                 }
                   
                } 
           
             
	}
	
	/**
	 * Invoked when a mouse button has been pressed on a component.
	 *
	 * @param e object holding information about the button press
	*/
	public void mousePressed(MouseEvent e)
	{
          
		// over icon, so remember this is a mouse press
		if(overIcon(e.getX(), e.getY())) 
                    this.mousePressedOnMe = true;
	}
	
	/**
	

	*/
	public void mouseReleased(MouseEvent e)
	{
	
	}
	
	/**
	 * Invoked when the mouse enters a component.
	 *
	 * @param e object holding information about the mouse pointer
	*/
	public void mouseEntered(MouseEvent e) {}
	

	public void mouseExited(MouseEvent e)
	{
	
	}
    }
      public static void main( String[] args )
      {
		MemoryGameClient application; // declare client application
		
		// if no command line args use local host
		if ( args.length == 0 )
			application = new MemoryGameClient( hostName , portNumber);
		else
			application = new MemoryGameClient( args[ 0 ] ,Integer.valueOf(args[ 1 ]));
		
		application.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
       }  
      
    
        
        
}
