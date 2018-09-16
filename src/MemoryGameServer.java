/*
    /////problem: when we find a match, another card is turned.it doesnt affect the game.!
 */
package MAMAN_16_1;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.Formatter;
import java.util.*;
import javax.swing.JOptionPane;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
/**
 *
 * @author tchaklai
 **/
public class MemoryGameServer {
   
    private Player[] playerArray= new Player[2];   
    private int currentPlayerIndex;  ///randomaly chosen
    private ServerSocket serverSocket; //ccreate socket
    private ExecutorService runGame;     ///execute tasks.
    private Lock gameLock;    //locker
    private Condition waitForPlayer2; // condition set to wait for the second player to connect
    private Condition otherPlayerTurn; //  condition set to wait for the other pllayers' turn
    private Random random;
    private ArrayList<Integer> cardArray=new ArrayList<Integer>();  //all cards array
   
    private ArrayList<Integer> turnedCouple=new ArrayList<Integer>();   //turned cards array
    private int firstPlayerConnected=0;
    private int firstPlayertoStart;
   
    private int[] whoseTurn={0,1};///not really neseccary, bot just for illustration
  
    private List<String> animalArray = Arrays.asList("Bunny","Cat","Chicken","Cow","Dog","Horse","Pig","Sheep","Bunny","Cat","Chicken","Cow","Dog","Horse","Pig","Sheep"); 
    //turned cards array, set all the indexes to false. when a card is turned, its index set to true, that way we can follow the game from the server
    private boolean [] turnedCards = {false, false, false, false, false,false, false, false, false, false,false, false, false, false, false,false};
   
    private int n;   //cardNumber, determined by the user.
    
    public MemoryGameServer()
    {
        ///let the user chose the number of cards
        n = Integer.parseInt(JOptionPane.showInputDialog("Please Enter Number of Cards\n (the number has to be an even number between 2-16)"));
       
        runGame = Executors.newFixedThreadPool( 2 );  //allways two payers
        gameLock = new ReentrantLock();
        waitForPlayer2 = gameLock.newCondition();
        otherPlayerTurn = gameLock.newCondition();
        firstPlayertoStart=whoStarts();   //random
        currentPlayerIndex=firstPlayertoStart;  //current player is chosen 
 
         Collections.shuffle(animalArray);
        
        try
        {
            serverSocket = new ServerSocket( 22222, 2 ); // set up ServerSocket
         }
         catch ( IOException ioException )
         {
             ioException.printStackTrace();
            System.exit( 1 );
        }
    }
    
    public void execute()
    {
        System.out.println("waiting for players");
      
        for(int i=0;i<2;i++)
        {
            try
             {
                playerArray[i] = new Player( serverSocket.accept(), i, 0 ); 
                runGame.execute( playerArray[i] ); // execute player runnable
             }
            catch ( IOException ioException )
            {
                ioException.printStackTrace();
                 System.exit( 1 );
            }
         }
        
        
        gameLock.lock();
                
       
        try
        {
            
            playerArray[ firstPlayerConnected ].setSuspended( false ); // resume first player
            waitForPlayer2.signal(); // wake up players' thread
        }
        finally
        {
            gameLock.unlock(); // unlock game after signalling first connected player 
        }
        
    }
  
    
    public int whoStarts()  //randomaly chose who starts the game/
    {
        random=new Random();
        int player =random.nextInt(whoseTurn.length);
        return player;
        
    }
      
    
    public boolean checkforMatch(int card1,int card2 )    //check id two turned cards has the same animal on them
    {
        if(this.animalArray.get(card1).equalsIgnoreCase(this.animalArray.get(card2)))
            return true;
        else
            return false;
    }
    
    
       
       public boolean ifMoveLegalPlay(int cardNum1, int cardNum2, int player)
       {
           boolean isMatch=false;
           while ( player != currentPlayerIndex )
           {
                gameLock.lock(); // lock game to wait for other player to go

               try
               {
                    otherPlayerTurn.await(); // wait for player's turn
                 }
                catch ( InterruptedException exception )
                {
                    exception.printStackTrace();
                }
                finally
                {
                    gameLock.unlock(); // unlock game after waiting
                }
            }
           if((turnedCards[cardNum1]==false)&&(turnedCards[cardNum2]==false))   //if player finds a match
           {
               isMatch=checkforMatch(cardNum1,cardNum2 );
               if(isMatch)
               {
                    playerArray[currentPlayerIndex].PlusOnePoint();    //this player get one more point
                     
               }
               currentPlayerIndex = ( currentPlayerIndex + 1 ) % 2;   ///move is valid, so now now it's the other players' turn
               playerArray[ currentPlayerIndex ].otherPlayerTurnedCards(cardNum1,cardNum2);
               
              gameLock.lock();
            try
            {
                otherPlayerTurn.signal(); // signal other player to continue
            }
            finally
            {
                gameLock.unlock(); // unlock game after signaling
            }
            return true;
        }
        return false;
    }

         
         public boolean isGameOver()
         {
             int i;
             for(i=0;i<turnedCards.length;i++)      //check if all cards are turned
             {
                 if(turnedCards[i]==false)
                     return false;
             }
             return true;
         }
         
       
    
    private class Player implements Runnable
    {
      private Socket connection; // connection to client
      private Scanner input; // input from client
      private Formatter output; // output to client
      private int playerIndex; // tracks which player this is
      private String turn; // mark for this player
      private boolean suspended = true; // whether thread is suspended
      private int points;   //Points, every turned couple is another point
      
      public Player( Socket socket, int _playerIndex, int _points)
      {
          playerIndex= _playerIndex; // store this player's number
          points=_points;   ///players' points
          connection = socket; // store socket for client
          
         try // obtain streams from Socket
         {
            input = new Scanner( connection.getInputStream() );    
            output = new Formatter( connection.getOutputStream() );
         }
         catch ( IOException ioException )
         {
            ioException.printStackTrace();
            System.exit( 1 );
         }
       }
       
    public void checkWin()
         {
             if (playerArray[currentPlayerIndex].getPoints()>playerArray[( currentPlayerIndex + 1 ) % 2].getPoints())  //current player wins
             {
                  output.format("You Win"+"\n" );
                  output.flush();
             }
             else if (playerArray[currentPlayerIndex].getPoints()<playerArray[( currentPlayerIndex + 1 ) % 2].getPoints()) //current player loses
             {
                 output.format("Opponent Win"+"\n" );
                 output.flush();
             }
             else                               //even
             {
                 output.format("Game Ended in a Tie ");  
                 output.flush();
             }
           
         }
     public void otherPlayerTurnedCards(int cardNum1,int cardNum2)     
       {
            if ( !isGameOver() )
           {
                        turnedCards[cardNum1]=true;       //set the cards to be true in the turned cards array
                        turnedCards[cardNum2]=true;  
			output.format("Opponent Turned Cards"+"\n" );      
                        output.flush();
			output.format( "%d\n", cardNum1 ); // send card numbers of move
                        output.flush();
                        output.format("%d\n", cardNum2);
			output.flush();                            
		  
           }
            else
            {
                checkWin();
            }
       }
        public int getPoints()
        {
            return points;
        }
        
        public void PlusOnePoint()
        {
            points=points+1;
        }
               
       public void run()
       {
         
            try
            {
                 output.format( "%s\n",n); // send n, chosen by the user
                 output.flush(); // flush output          
           
            for(int i=0;i<n;i++)
            {
                output.format( "%s\n",animalArray.get(i));
                output.flush(); 
            }
             
            output.format("%s\n",playerIndex ); // send player's its index
            output.flush(); // flush output     
              
              
            output.format("%s\n", currentPlayerIndex ); // send playerthe current  index
            output.flush();
              
               

            // if player 0, wait for another player to arrive
            if ( playerIndex == firstPlayerConnected )
            {
                output.format( "first player connected, waiting for other player"+"\n");          
                output.flush(); // flush output               
                gameLock.lock(); // lock game to wait for second player
              
                try
                {
                    while( suspended )
                    {
                      waitForPlayer2.await(); // wait for player 1
                    }
                  
                }
               catch ( InterruptedException exception )
              {
                 exception.printStackTrace();
               }
               finally
               {
                  
                   gameLock.unlock();
               }
                 
            }    
            else
            {
               output.format( "Other Player is Connected"+"\n");          
                output.flush(); // flush output               
               
           }  
            
                 
            
            if(playerIndex==firstPlayertoStart) // unlock game for the current player
            {
                     
                output.format( "You Start the Game!"+"\n" );
                output.flush(); // flush output 
                     
            }   
            else
            {
                    
                 output.format( "The opponent Starts the Game"+"\n" );
                 output.flush(); // flush output 
            }
            

            // while game not over
	     while (!isGameOver()) 
             {
               
		// initialize turned cards numbers
                    int cardNumber1=0;
                    int cardNumber2=0;
                    
                
                    while (true)
                    {
                        if (input.hasNext())  //check for first card
                        {
                            cardNumber1 = input.nextInt(); //get the number from client
                            turnedCouple.add(cardNumber1);
                            System.out.println(cardNumber1);  //just a check
                            break;
                        }
                    }
                    while (true)
                    {
                        if (input.hasNext())  //check for second card
                        {
                            cardNumber2 = input.nextInt();
                            turnedCouple.add(cardNumber2);
                            System.out.println(cardNumber2);  
                      
                                 
                            break;
                        }
                    }
                   
                   if( ifMoveLegalPlay(cardNumber1, cardNumber2, playerIndex))   ///if card isn't turned
                   {
                       output.format("Valid Move"+"\n"); // notify client
		       output.flush(); // flush output
                   }
                   else
                   {
                          output.format("Invalid Move"+"\n");  //if card is turned
			  output.flush(); // flush 
                   }
                   
                    
                    
                   
              
             }  
             checkWin();
   
         }
         finally
         {
            try
            {
                
               connection.close(); // close connection to client
            }
            catch ( IOException ioException )
            {
               ioException.printStackTrace();
               System.exit( 1 );
            }
         }
         
      }
       

	// set whether or not thread is suspended
      public void setSuspended( boolean status )
      {
         suspended = status; // set value of suspended
      }
   }

	public static void main(String[] args) {
		MemoryGameServer application = new MemoryGameServer();
		application.execute();
	}

         
         
      }



  

    
    
    
    

