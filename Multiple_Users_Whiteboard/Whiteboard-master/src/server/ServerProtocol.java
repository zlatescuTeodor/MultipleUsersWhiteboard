package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import command.Command;


/**
 * Thread which handles each individual connection with each client and
 * communicates through the following grammar
 * 
 * Concurrency Argument:
 *   - this thread only performs actions on thread safe objects (Board, Server)
 *     (See Board.java and Server.java) 
 * 
 *
 */
public class ServerProtocol implements Runnable {
    
    private final Socket socket;
    private final Server server;
    
    public ServerProtocol(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }
    
    /**
     * Waits on the client to send data then calls the appropriate request handler
     */
    @Override
    public void run() {
        // handle the connection with the client
        try {
            handleConnection(socket);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
				if (!socket.isClosed()) socket.close();
			} catch (IOException e) {
			}
        }
    }
    
    /**
     * Handle a single client connection. Returns when client disconnects.
     * 
     * @param socket socket where the client is connected
     * @throws IOException if connection has an error or terminates unexpectedly
     */
    private void handleConnection(Socket socket) throws IOException {

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        try {
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                try {
                    
	            	String output = handleRequest(line);
	            	if(output != null) {
	            		out.println(output);
	            	}
	                
                } catch (IllegalArgumentException e) {
	                	e.printStackTrace();   
                }                
            }
        } finally {
            out.close();
            in.close();
        }
    }
    
    /**
     * Handler for client input, performing requested operations and returning an output message.
     * 
     * Receives:
	 * 
	 * New Board = "newBoard boardName"
	 * Switch Board = "switch username oldBoardName newBoardName"
	 * Exit = "exit username"
	 * Draw = "draw boardName command param1 param2 param3 ... "
	 *        Example: "draw boardName drawLineSegment x1 y1 x2 y2 color width"
	 * Get Users = "users boardName"
	 * Get boards = "boards"
	 * Check and add User = "checkAndAddUser username boardName"
	 * 
	 * 
	 * Sends: 
	 * 
	 * New Board = "newBoard boardName boolean"
	 * Switch Board = "switch username oldBoardName newBoardName command1 command2 command3..."
	 * Update Users = "users boardName user1 user2 user3..."
	 * Update Available Boards = "boards board1 board2 board3"
	 * Draw = "draw boardName command param1 param2 param3"
	 *      Example: "draw boardName drawLineSegment x1 y1 x2 y2 color width"
	 * Check and add User = "checkAndAddUser username boardName boolean"
	 * 
     * 
     * 
     * @param input message from client
     * @return message to client
     * @throws IOException 
     */
    private String handleRequest(String input) throws IOException, IllegalArgumentException {
        System.out.println(input);
    	String nameReg = "[a-zA-Z0-9\\.]+";
    	String regex = "(boards)|(newBoard "+nameReg+")|"
    			+ "(switch "+nameReg+" "+nameReg+" "+nameReg+")|"
    			+ "(exit "+nameReg+")|(users "+nameReg+")|"
    			+ "(checkAndAddUser "+nameReg+" "+nameReg+")|"
    			+ "(draw "+nameReg+"( "+nameReg+")+)";
        
        if ( ! input.matches(regex)) {
            // invalid input
        	System.out.println("Invalid input: "+input);
            return null;
        }

        String[] tokens = input.split(" ");
        
        // Get Boards
        if (tokens[0].equals("boards")) {
        	return boards(tokens);
        } 
        //New Board
        else if (tokens[0].equals("newBoard")) {
        	return newBoard(tokens);
        } 
        // Switch Board
        else if (tokens[0].equals("switch")) {
        	return switchBoard(tokens);
        }
        // Exit 
        else if (tokens[0].equals("exit")) {
        	return exit(tokens);
        } 
        // Draw Command 
        else if (tokens[0].equals("draw")) {
        	return draw(tokens);
        } 
        // Check and add User
        else if (tokens[0].equals("checkAndAddUser")) {
        	return checkAndAddUser(tokens);
        } 
        // Get Users
        else if (tokens[0].equals("users")) {
        	return users(tokens);
        }

        // Should never get here-- should return in each of the valid cases above.
        throw new UnsupportedOperationException();
    }
    

    /**
     * Boards response
     * @param tokens
     * @return
     */
    public String boards(String[] tokens) {
    	return "boards" + server.getBoards();
    }
    
    /**
     * New board response
     * @param tokens
     * @return
     */
    public String newBoard(String[] tokens) {
    	String boardName = tokens[1];
    	return "newBoard " + boardName + " " + String.valueOf(server.newBoard(boardName));
    }
    
    /**
     * Switch board response
     * @param tokens
     * @return
     */
    public String switchBoard(String[] tokens) {
        String userName = tokens[1];
        String oldBoardName = tokens[2];
        String newBoardName = tokens[3];
        String newLine = System.getProperty("line.separator");
        server.switchBoard(userName, oldBoardName, newBoardName);
        List<Command> commands = server.getCommands(newBoardName);
    	String str =  "switch " + userName + " " + oldBoardName + " " + newBoardName;
    	for (Command command: commands) {
    	    str += newLine+command.toString();
    	}
    	return str;
    }
    
    /**
     * Exit board response
     * @param tokens
     * @return
     */
    public String exit(String[] tokens) {
        String username = tokens[1];
        server.exit(username);
        return "exit " + username;
    }
    
    /**
     * draw response
     * @param tokens
     * @return
     */
    public String draw(String[] tokens) {
        String boardName = tokens[1];
        Command command = new Command(tokens);
        server.updateBoard(boardName, command);
        server.sendCommandToClients(command, socket);
        return command.toString();
    }
    
    /**
     * checkAndAddUser response
     * @param tokens
     * @return
     */
    public String checkAndAddUser(String[] tokens) {
        String boardName = tokens[2];
        String username = tokens[1];
        return "checkAndAddUser " + username + " " + boardName + " " + String.valueOf(server.checkUser(username, boardName));
    }
    
    /**
     * Get Users response
     * @param tokens
     * @return
     */
    public String users(String[] tokens) {
        String boardName = tokens[1];
        return "users "+boardName+" "+server.getUsers(boardName);
    }
    
    /**
     * testing purposes for handleRequest()
     * 
     * @param input 
     * @return output from handleRequest(input)
     * @throws IOException
     */
    public String testHandleRequest(String input) throws IOException {
        return handleRequest(input);
    }
}
