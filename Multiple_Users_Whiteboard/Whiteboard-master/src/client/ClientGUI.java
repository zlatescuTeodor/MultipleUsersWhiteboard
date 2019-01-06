package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import javax.swing.GroupLayout.Group;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.GroupLayout.Alignment;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.Alignment.LEADING;


public class ClientGUI extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private final Client client;
	private final int WIDTH = 800;
	private final int HEIGHT = 600;
	private JToggleButton imageToggle;
	private JToggleButton text;
	private JToggleButton cropToggle;
	
	private JToggleButton brush;
	private JToggleButton ereser;
	
	private JToggleButton square;
	private JToggleButton triangle;
	private JToggleButton circle;
	
	
	
	
	private JButton strokeButton;
	// Start Dialog GUI objects
	private JDialog dialog;
	private DefaultListModel<String> boardListModel;
	private JLabel newBoardLabel;
	private JTextField newBoard;
	private JList<String> boardList;
	private Container dialogContainer;
	private GroupLayout layout;
	private JTextField usernameTextField;
	private JLabel usernameLabel;
	private JScrollPane boardListScroller;
	private JButton newBoardButton;
	private JButton startButton;

    private JLabel currentUserBoard;
    private JPanel sidebar;
    
    private Canvas canvas;
    
    
    /**
     * Makes the view for the client (the model)
     * @param client: the client this GUI works for
     */
	public ClientGUI(Client client) {
		this.client = client;
		startDialog();  
	}
	
	/**
     * Creates start dialog which handles username and initial board
     */
    private void startDialog() {
        dialog = new JDialog();
        dialog.setTitle("Welcome to Whiteboard");
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setDialogLayout();
        setDialogActionListeners(); 
    }
    
    /**
     * Sets layout for start dialog
     */
    public void setDialogLayout() {
        dialogContainer = new Container();
        layout = new GroupLayout(dialogContainer);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        dialogContainer.setLayout(layout);

        ParallelGroup hGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);

        SequentialGroup hUsername = layout.createSequentialGroup();
        usernameTextField = new JTextField(10);
        usernameTextField.setName("username");
        usernameLabel = new JLabel("Username:");
        hUsername.addComponent(usernameLabel).addComponent(usernameTextField);

        boardListModel = new DefaultListModel<String>();

        // Get boards from server and add to data model
        try {
            String[] boards = client.getBoards();
            for (int i=0; i<boards.length;i++) {
                boardListModel.addElement(boards[i]);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        boardList = new JList<String>(boardListModel); //data has type Object[]
        boardList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        boardList.setLayoutOrientation(JList.VERTICAL);
        boardList.setVisibleRowCount(-1);
        boardListScroller = new JScrollPane(boardList);
        boardListScroller.setPreferredSize(new Dimension(100, 150));

        SequentialGroup hNewBoard = layout.createSequentialGroup();
        newBoardLabel = new JLabel("New Board:");
        newBoard = new JTextField(10);
        newBoard.setName("newBoard");
        newBoardButton = new JButton("Add Board");
        hNewBoard.addComponent(newBoardLabel).addComponent(newBoard).addComponent(newBoardButton);

        startButton = new JButton("Start");

        hGroup.addGroup(hUsername).addComponent(boardListScroller).addGroup(hNewBoard).addComponent(startButton);

        ParallelGroup vGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        SequentialGroup vAll = layout.createSequentialGroup();

        ParallelGroup v1 = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
        v1.addComponent(usernameLabel).addComponent(usernameTextField);

        ParallelGroup v2 = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
        v2.addComponent(newBoardLabel).addComponent(newBoard).addComponent(newBoardButton);

        vAll.addGroup(v1).addComponent(boardListScroller).addGroup(v2).addComponent(startButton);

        vGroup.addGroup(vAll);

        layout.setHorizontalGroup(hGroup);
        layout.setVerticalGroup(vGroup);

        dialog.setContentPane(dialogContainer);
        dialog.pack();
        dialog.setVisible(true);
    }
    
    /**
     * Adds action listeners to start dialog
     */
    private void setDialogActionListeners() {
        
        //handles when the initial dialog is closed; kills the client
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                client.kill();
            }
        });
        
        //handles what happens when the user presses start
        //enters the user is possible and sets up the canvas
        startButton.addActionListener(new ActionListener() {
            public synchronized void actionPerformed(ActionEvent e) {
                //if the username is empty
                if (usernameTextField.getText().equals("")) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a username.", "Try again", JOptionPane.ERROR_MESSAGE);
                } 
                //if no board is selected
                else if (boardList.isSelectionEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please select a board.", "Try again", JOptionPane.ERROR_MESSAGE);
                } else
                    try {
                        //if the user has entered successfully
                        if (client.createUser(usernameTextField.getText(), boardList.getSelectedValue())) {
                            dialog.dispose();
                            setupCanvas();
                            client.makeRequest("switch "+client.getUsername()+" "+client.getCurrentBoardName()+" "+client.getCurrentBoardName());
                        } else {
                            JOptionPane.showMessageDialog(dialog, "Sorry, this username is already taken currently.", "Try again", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
            }
        });
        
        //handles making a new board and adding it to the list
        newBoardButton.addActionListener(new ActionListener() {
            public synchronized void actionPerformed(ActionEvent e) {
                if (!newBoard.getText().equals("")) {
                    NewBoardWorker worker = new NewBoardWorker(newBoard.getText());
                    worker.execute();
                } 
                //if the name is empty
                else {
                    JOptionPane.showMessageDialog(dialog, "Please enter a board name.", "Try again", JOptionPane.ERROR_MESSAGE);
                }

            }
        });

    }   
    
    /**
     * A worker to create a new board in background before adding it to the list of available boards in the beginning dialog
     *
     */
    class NewBoardWorker extends SwingWorker<Boolean, Object> {

        private String newBoardName;

        public NewBoardWorker(String newBoardName) {
            this.newBoardName = newBoardName;
        }

        /**
         * Called when execute is called on the worker
         */
        @Override
        protected Boolean doInBackground() throws Exception {  
            return client.newBoard(newBoardName);
        }   

        /**
         * After doInBackground has gotten its result, display the result in the list (or not)
         */
        @Override
        protected void done() {
            try {
                if (get()) {
                    String[] boards = client.getBoards();
                    boardListModel.removeAllElements();
                    for (int i=0; i<boards.length;i++) {
                        boardListModel.addElement(boards[i]);
                    }
                    newBoard.setText("");
                } else {
                    JOptionPane.showMessageDialog(dialog, "Sorry, this board name is already taken.", "Try again", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
	
    /**
     * When the user has entered their selected board, set up the canvas they will draw on an the menu
     */
	public void setupCanvas() {
        this.setTitle("Whiteboard");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setLayout(new BorderLayout());
        this.setResizable(false); 
        canvas = new Canvas(client);
        canvas.addDrawingController(new DrawingController(client));
        this.addMenuBar();
        this.addSideBar();
        
        this.add(canvas, BorderLayout.CENTER);
        this.add(sidebar, BorderLayout.EAST);
        
        this.pack();
        this.setVisible(true);
        
        
    }
	
	/**
	 * Creates the menubar to add to the canvas to control drawing and viewing
	 */
	private void addSideBar() {
		
		sidebar = new JPanel();
		
		this.imageToggle = new JToggleButton(new ImageIcon(((new ImageIcon("resources/image.png")).getImage())
                .getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH)));     
        this.imageToggle.setPreferredSize(new Dimension(40,40));
        this.imageToggle.setFocusPainted(false);
        
        this.cropToggle = new JToggleButton(new ImageIcon(((new ImageIcon("resources/crop.png")).getImage())
                .getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH))); 
        this.cropToggle.setPreferredSize(new Dimension(40,40));
        this.cropToggle.setFocusPainted(false);
        
        this.square = new JToggleButton(new ImageIcon(((new ImageIcon("resources/square.png")).getImage())
                .getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH)));
        this.square.setPreferredSize(new Dimension(40,40));
        this.square.setFocusPainted(false);
        
        this.triangle = new JToggleButton(new ImageIcon(((new ImageIcon("resources/triangle.png")).getImage())
                .getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH))); 
        this.triangle.setPreferredSize(new Dimension(40,40));
        this.triangle.setFocusPainted(false);
        
        this.circle = new JToggleButton(new ImageIcon(((new ImageIcon("resources/circle.png")).getImage())
                .getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH))); 
        this.circle.setPreferredSize(new Dimension(40,40));
        this.circle.setFocusPainted(false);
        
        this.brush = new JToggleButton(new ImageIcon(((new ImageIcon("resources/brushIcon.png")).getImage())
                .getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH))); 
        this.brush.setPreferredSize(new Dimension(40,40));
        this.brush.setFocusPainted(false);
        
        this.ereser = new JToggleButton(new ImageIcon(((new ImageIcon("resources/eraserIcon.png")).getImage())
                .getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH)));  
        this.ereser.setPreferredSize(new Dimension(40,40));
        this.ereser.setFocusPainted(false);
        
        this.text = new JToggleButton(new ImageIcon(((new ImageIcon("resources/text.png")).getImage())
                .getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH)));  
        this.text.setPreferredSize(new Dimension(40,40));
        this.text.setFocusPainted(false);
        
        this.strokeButton = new JButton("Stroke/Text");
        this.strokeButton.setFocusPainted(false);
        this.strokeButton.setHorizontalAlignment(SwingConstants.LEFT);
        
        
        JPanel imageBar = new JPanel();
        imageBar.add(imageToggle);
        imageBar.add(cropToggle);
        TitledBorder imageBorder = BorderFactory.createTitledBorder("Image Editing");
        imageBar.setBorder(imageBorder);
        
        JPanel modeBar = new JPanel();
        modeBar.add(brush);
        modeBar.add(ereser);
        TitledBorder modeBarBorder = BorderFactory.createTitledBorder("Mode");
        modeBar.setBorder(modeBarBorder);
        
        JPanel brushBar = new JPanel();
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(getColorsMenu());
        menuBar.add(getBrushTypeMenu());
        brushBar.add(menuBar);
        brushBar.add(strokeButton);
        TitledBorder brushBarBorder = BorderFactory.createTitledBorder("Brush");
        brushBar.setBorder(brushBarBorder);
        
        
        JPanel textBar = new JPanel();
        JMenuBar fontBar = new JMenuBar();
        fontBar.add(getFontMenu());
        textBar.add(text);
        textBar.add(fontBar);
        
        TitledBorder textBorder = BorderFactory.createTitledBorder("Text");
        textBar.setBorder(textBorder);
        
        JPanel shapesBar = new JPanel();
        shapesBar.add(square);
        shapesBar.add(triangle);
        shapesBar.add(circle);
        TitledBorder shapesBorder = BorderFactory.createTitledBorder("Shapes");
        shapesBar.setBorder(shapesBorder);
        
        sidebar.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new Insets(5,0,0,0);
        c.fill = GridBagConstraints.HORIZONTAL;
        
        c.gridx = 0;
        c.gridy = 0;
        sidebar.add(modeBar, c);
        
        c.gridx = 0;
        c.gridy = 1;
        sidebar.add(brushBar, c); 
        
        c.gridx = 0;
        c.gridy = 2;
        sidebar.add(imageBar, c); 
       
        
        c.gridx = 0;
        c.gridy = 3;
        sidebar.add(textBar, c);
        
        c.gridx = 0;
        c.gridy = 4;
        sidebar.add(shapesBar, c);
        
        strokeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JPopupMenu popup = new JPopupMenu();
                popup.add(getSlider());
                popup.pack();
                Point pos = new Point();
                Dimension size = popup.getPreferredSize();
                pos.x = (strokeButton.getWidth()/2 - size.width/2);
                pos.y = (strokeButton.getHeight());
                popup.show(strokeButton, pos.x, pos.y);
            }
        });
        
        imageToggle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setImage(e);
            }
        });
        
        /*text.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setText(e);
            }
        });*/
        
        
        square.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (square.isSelected()) {
                	brush.setSelected(false);
                	ereser.setSelected(false);
                	text.setSelected(false);
                	client.setSquareOn(true);
                	client.setEraserOn(false);
                    client.setBrushOn(false);
                    client.setText(false);
                	System.out.println("square press enable" + Boolean.toString(client.getSquareOn()) );
                }
                else {       
                	square.setEnabled(true);
                	client.setSquareOn(false);
                	System.out.println("square press disable" + Boolean.toString(client.getSquareOn()) );
                }
                
            }
        });
        
        ereser.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (ereser.isSelected()) {                    
                    square.setSelected(false);
                    brush.setSelected(false);
                    text.setSelected(false);
                    client.setEraserOn(true);
                    client.setBrushOn(true);
                    client.setSquareOn(false);
                    client.setText(false);
                }
                else {
                	client.setBrushOn(false);
                }
                
            }
        });
        brush.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (brush.isSelected()) {  
                	client.setStraight(true);
                	square.setSelected(false);
                	text.setSelected(false);
                    ereser.setSelected(false);
                    client.setEraserOn(false);
                    client.setBrushOn(true);
                    client.setSquareOn(false);
                    client.setText(false);
                   
                } 
                else {
                	client.setBrushOn(false);
                }
            }
        });
        text.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (text.isSelected()) {  
                	client.setStraight(true);
                	square.setSelected(false);
                    ereser.setSelected(false);
                    client.setEraserOn(false);
                    client.setBrushOn(false);
                    client.setText(true);
                    client.setSquareOn(false);
                   
                } 
                else {
                	client.setText(false);
                }
            }
        });
          
	}
	
	private void setImage(ActionEvent e) {
    	{
            
            JFileChooser file = new JFileChooser();
            file.setCurrentDirectory(new File(System.getProperty("user.home")));
            //filter the files
            FileNameExtensionFilter filter = new FileNameExtensionFilter("*.Images", "jpg","gif","png");
            file.addChoosableFileFilter(filter);
            int result = file.showSaveDialog(null);
             //if the user click on save in Jfilechooser
            if(result == JFileChooser.APPROVE_OPTION){
                File selectedFile = file.getSelectedFile();
                String path = selectedFile.getAbsolutePath();
                canvas.fillWithImageAndCall(path);
            }
             //if the user click on save in Jfilechooser

            else if(result == JFileChooser.CANCEL_OPTION){
                System.out.println("No File Select");
            }
          }
	}
	/*private void setText(ActionEvent e) {
		String textFont = new String();
		if(client.getFontArial() == true)
			textFont = "Arial Black";
		else
			if(client.getFontComic() == true)
				textFont = "Comic Sans MS";
		
		Color color = client.getCurrentColor();
        
        canvas.drawText(textFont, (int)client.getCurrentWidth(), color);
	}*/
	private void addMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(getUsersMenu());
        menuBar.add(getBoardsMenu());
        menuBar.add(getSaveMenu());
        menuBar.add(Box.createHorizontalGlue());
        currentUserBoard = getCurrentUserBoard();
        menuBar.add(currentUserBoard);
        menuBar.add(Box.createHorizontalGlue());
        this.setJMenuBar(menuBar);
    }
	
	private JMenu getSaveMenu() {
		
		 final JMenu mode = new JMenu("Save");
	        
	        
	        JMenuItem drawMenuItem = new JMenuItem("Save As...");
	        drawMenuItem.addActionListener(new  ActionListener() {
	            public void actionPerformed(ActionEvent event) {
	                canvas.saveMethod();
	            }});
	        
	        mode.add(drawMenuItem);
	        
	        return mode;
	}
	
	/**
     * Add the users menu to the menu mar
     * @return JMenu representing the users menu
     */
    private JMenu getUsersMenu() {
        final JMenu usersMenu = new JMenu("Users");
        //List of Users
        try {
            for (String user: client.getUsers()) {
                JLabel label = new JLabel(user);
                label.setBorder(BorderFactory.createEmptyBorder(2, 5, 3, 5));
                usersMenu.add(label);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        usersMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuCanceled(MenuEvent arg0) {
            }

            @Override
            public void menuDeselected(MenuEvent arg0) {
            }

            @Override
            public void menuSelected(MenuEvent arg0) {
                //refresh the users list whenever the menu button is pressed
                usersMenu.removeAll();
                try {
                    for (String user: client.getUsers()) {
                        JLabel label = new JLabel(user);
                        label.setBorder(BorderFactory.createEmptyBorder(2, 5, 3, 5));
                        usersMenu.add(label);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return usersMenu;
    }
	
	/**
     * Add the boards menu to the menu mar
     * @return JMenu representing the boards menu
     */
    private JMenu getBoardsMenu() {
      //add List of Boards
        final JMenu boards = new JMenu("Board(s)");
        
        //clicking the new board button brings up the new board dialog
        JMenuItem newBoardButton = new JMenuItem("New Board");
        boards.add(newBoardButton);
        newBoardButton.addActionListener(new  ActionListener() {
            public void actionPerformed(ActionEvent event) {
                client.getClientGUI().newBoardDialog();
            }
        });
        boards.addSeparator();
        
        //List of Boards
        String[] listBoards = {};
        try {
            listBoards = client.getBoards();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //when a board is clicked, the client should switch the current board to that board
        for (final String board: listBoards) {
            JMenuItem boardChoice = new JMenuItem(board);
            boardChoice.addActionListener(new  ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    canvas.switchBoard(board);
                    
                }
            });
            boards.add(boardChoice);
        }
        
        
        boards.addMenuListener(new MenuListener() {
            @Override
            public void menuCanceled(MenuEvent arg0) {
            }

            @Override
            public void menuDeselected(MenuEvent arg0) {
            }

            @Override
            public void menuSelected(MenuEvent arg0) {
              //refresh the boards list whenever the boards list is clicked
                for (int i=boards.getItemCount()-1; i>1; i--) {
                    boards.remove(i);
                }
                try {
                    for (final String board: client.getBoards()) {
                        
                        JMenuItem boardChoice = new JMenuItem(board);
                        boardChoice.addActionListener(new  ActionListener() {
                            public void actionPerformed(ActionEvent event) {
                                canvas.switchBoard(board);
                            }
                        });
                        boards.add(boardChoice);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        
        return boards;
    }
    /**
     * Add the mode menu to the menu mar
     * @return JMenu representing the mode menu
     */
    
    
    private JMenu getFontMenu() {
    	final JMenu type = new JMenu("Font");
        
        
        JMenuItem arialMenuItem = new JMenuItem("Arial");
        arialMenuItem.addActionListener(new  ActionListener() {
            public void actionPerformed(ActionEvent event) {
                client.setFontArial(true);
                client.setFontComic(false);
                
            }});
        JMenuItem comicMenuItem = new JMenuItem("Comic Sans");
        comicMenuItem.addActionListener(new  ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	client.setFontArial(false);
                client.setFontComic(true);
                
            }});
        
        type.add(arialMenuItem);
        type.addSeparator();
        type.add(comicMenuItem);
        
        type.setBorder(BorderFactory.createLineBorder(Color.BLACK,2));
        
        return type;
    }
	/**
     * Add the brushtype menu to the menu mar
     * @return JMenu representing the brushtype menu
     */
    
    
    private JMenu getBrushTypeMenu() {
    	final JMenu type = new JMenu("Type");
        
        
        JMenuItem straightMenuItem = new JMenuItem("Straight");
        straightMenuItem.addActionListener(new  ActionListener() {
            public void actionPerformed(ActionEvent event) {
                client.setStraight(true);
                System.out.println("straight on");
                client.setDotted(false);
                client.setDashed(false);
                
            }});
        JMenuItem dottedMenuItem = new JMenuItem("Dotted");
        dottedMenuItem.addActionListener(new  ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	client.setStraight(false);
                client.setDotted(true);
                client.setDashed(false);
                
            }});
        
        type.add(straightMenuItem);
        type.addSeparator();
        type.add(dottedMenuItem);
        
        type.setBorder(BorderFactory.createLineBorder(Color.BLACK,2));
        
        return type;
    }
	
    /**
     * Add the colors menu to the menu bar
     * @return JMenu representing the colors menu
     */
    private JMenu getColorsMenu() {
        class ColorChangeListener implements ChangeListener {
            JMenu colors;
            public ColorChangeListener(JMenu colors) {
                this.colors = colors;
            }
            
            @Override
            public void stateChanged(ChangeEvent e) {
                ColorSelectionModel model = (ColorSelectionModel) e.getSource();
                Color currentColor = model.getSelectedColor();
                client.setCurrentColor(currentColor);
                colors.setBorder(BorderFactory.createLineBorder(currentColor,2));
            }
        }
        //add Colors
        
        JMenu colors = new JMenu("Paint Color");
        
        JColorChooser chooser = new JColorChooser(client.getCurrentColor());
        colors.add(chooser);
        chooser.getSelectionModel().addChangeListener(new ColorChangeListener(colors));
        chooser.setPreviewPanel(new JPanel());
        colors.setBorder(BorderFactory.createLineBorder(client.getCurrentColor(),2));
        
        //remove panels
        AbstractColorChooserPanel[] panels = chooser.getChooserPanels();
        for (AbstractColorChooserPanel accp : panels) {
            if (!accp.getDisplayName().equals("Swatches")) {
                chooser.removeChooserPanel(accp);
            }
        }
        return colors;
    }
    /**
     * add slider to the menu bar
     * @return JSlider representing the slider
     */
    private JSlider getSlider() {
        class SliderChangeListener implements ChangeListener {

            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)e.getSource();
                if (!source.getValueIsAdjusting()) {
                    float weight = (float)source.getValue();
                    client.setCurrentWidth(weight);
                }
                
            }
        }
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 50, (int)Math.round(client.getCurrentWidth()));

        slider.addChangeListener(new SliderChangeListener());
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(2);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setVisible(true);
        
        return slider;
    }
	
    /**
     * The label to tell the user what their username is and which board they're using
     * @return
     */
    public JLabel getCurrentUserBoard() {
        String user = client.getUsername();
        String board = client.getCurrentBoardName();
        
        return new JLabel("Hi, " + user + ". This board is: " + board);
    }
    
    /**
     * The dialog for creating a new board when in canvas view
     */
	public void newBoardDialog() {
		final JDialog newBoardDialog = new JDialog();
		newBoardDialog.setTitle("Create New Board");
		newBoardDialog.setResizable(false);
		final Container newBoardDialogContainer = new Container();
		GroupLayout layout = new GroupLayout(newBoardDialogContainer);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		newBoardDialogContainer.setLayout(layout);

		JLabel newBoardNameLabel = new JLabel("New Board Name:");
		final JTextField newBoardName = new JTextField(10);
		JButton newBoardButton = new JButton("Create");

		ParallelGroup hGroup = layout.createParallelGroup(GroupLayout.Alignment.CENTER);

		SequentialGroup hEnter = layout.createSequentialGroup();
		hEnter.addComponent(newBoardNameLabel).addComponent(newBoardName);

		hGroup.addGroup(hEnter).addComponent(newBoardButton);

		ParallelGroup vGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		SequentialGroup vAll = layout.createSequentialGroup();

		ParallelGroup v1 = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v1.addComponent(newBoardNameLabel).addComponent(newBoardName);

		vAll.addGroup(v1).addComponent(newBoardButton);

		vGroup.addGroup(vAll);

		layout.setHorizontalGroup(hGroup);
		layout.setVerticalGroup(vGroup);

		newBoardDialog.setContentPane(newBoardDialogContainer);
		newBoardDialog.pack();
		newBoardDialog.setVisible(true);

		newBoardButton.addActionListener(new ActionListener() {
			public synchronized void actionPerformed(ActionEvent e) {
				String newBoardNameString = newBoardName.getText();
				if (newBoardNameString.equals("")) {
					JOptionPane.showMessageDialog(newBoardDialog, "Please enter a board name.", "Try again", JOptionPane.ERROR_MESSAGE);
				} else {
					try {
						boolean successful = client.newBoard(newBoardNameString);
						if (!successful) {
							JOptionPane.showMessageDialog(newBoardDialog, "Sorry, this board name is already taken.", "Try again", JOptionPane.ERROR_MESSAGE);
						} else {
							client.getBoards();
							newBoardDialog.dispose();
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});
	}
	
	/**
	 * Resets the label that tells the user what their username and current board are
	 * @param newBoard: the new label to set the label to
	 */
    public void setCurrentUserBoard(JLabel newBoard) {
    	currentUserBoard = newBoard;
    }
    
    /**
     * Gets the canvas, a controller, for the GUI
     * @return the canvas object
     */
    public Canvas getCanvas() {
        return canvas;
    }
}
