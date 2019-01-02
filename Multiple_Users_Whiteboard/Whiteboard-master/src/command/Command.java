package command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import client.Canvas;
/**
 * Creates command from token array passed that has already been determined to be a draw command
 * @param elements: Elements of command in format ["draw", "boardName", "command", "arg1", "arg2", "arg3", ...]
 * @return a Command object with the command and the arguments
 */
public class Command {
    private final String command;
    private final String[] arguments;
    private final String boardName;
    
    
    public Command(String[] elements) {
        String[] arguments = new String[elements.length-3];
        for (int i=3; i<elements.length;i++) {
            arguments[i-3] = elements[i];
        }
        this.command = elements[2];
        this.boardName = elements[1];
        this.arguments = arguments;
    }
    
    /**
     * Straightforwardly takes the given parameters and makes a command object out of them
     * @param boardName: name of the board the command is for
     * @param command: name of the command
     * @param arguments: the list of arguments as strings
     */
    public Command(String boardName, String command, String[] arguments) {
        this.boardName = boardName;
        this.command = command;
        this.arguments = arguments;

    }
    
    /**
     * Finds the method with a name matching the command name,
     * then invokes the method with the command's arguments
     * @param canvas: the object that the method will be invoked on
     */
    public void invokeCommand(Canvas canvas) {
        Method[] methods = Canvas.class.getMethods();
        Method method = null;
        for (int i=0; i<methods.length;i++) {
            if (methods[i].getName().equals(command)) {
                method = methods[i];
                System.out.println(methods[i].getName());
            }
        }
        if (method == null) {
            throw new RuntimeException("Command "+command+" not found.");
        } else {
            Class<?>[] parameters = method.getParameterTypes();
            if (parameters.length != arguments.length) {
                throw new RuntimeException("Incorrect number of arguments for given method.");
            } else {
                Object[] typedArgs = new Object[arguments.length];
                for (int i=0; i<typedArgs.length;i++) {
                    if (parameters[i].equals(int.class)) {
                        typedArgs[i] = Integer.valueOf(arguments[i]);
                    } else if(parameters[i].equals(float.class)) {
                        typedArgs[i] = Float.valueOf(arguments[i]);
                    } else if(parameters[i].equals(double.class)) {
                        typedArgs[i] = Double.valueOf(arguments[i]);
                    } else if(parameters[i].equals(long.class)) {
                        typedArgs[i] = Long.valueOf(arguments[i]);
                    } else if(parameters[i].equals(boolean.class)) {
                        typedArgs[i] = Boolean.valueOf(arguments[i]);
                    } else if(parameters[i].equals(short.class)) {
                        typedArgs[i] = Float.valueOf(arguments[i]);
                    } else if(parameters[i].equals(byte.class)) {
                        typedArgs[i] = Byte.valueOf(arguments[i]);
                    } else if(parameters[i].equals(char.class)) {
                        typedArgs[i] = arguments[i].charAt(0);
                    } else {
                        typedArgs[i] = parameters[i].cast(arguments[i]);
                    }
                }
                try {
                    method.invoke(canvas, typedArgs);
                } catch (IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Compares the board name given to make sure the command is for the same board
     * @param compareBoardName: the board name the command should be for
     * @return whether or not this command is for the board name given
     */
    public boolean checkBoardName(String compareBoardName) {
        return this.boardName.equals(compareBoardName);
    }
    
    @Override
    public String toString() {
        StringBuilder argumentString = new StringBuilder(" ");
        for (String arg : arguments) {
            argumentString.append(arg+" ");
        }
        argumentString.deleteCharAt(argumentString.length()-1);
        return "draw "+boardName+" "+command+argumentString;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Command)) return false;
        Command commandObj = (Command) obj;
        return commandObj.command.equals(command) && Arrays.equals(commandObj.arguments, arguments) && commandObj.boardName.equals(boardName);
    }
}
