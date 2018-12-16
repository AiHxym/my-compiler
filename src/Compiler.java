import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class Compiler extends JFrame{
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenu projectMenu;
    private JMenu helpMenu;
    private JTextArea jTextArea;
    private JScrollPane jScrollPane;
    private JMenuItem openItem, closeItem, saveItem,aboutItem;
    private JMenuItem compileItem, runItem;

    private FileDialog open, save;
    private File file;

    private JPanel tablePanel; //放置所有表格
    //token表格
    private JScrollPane tokenJScrollPane;
    private JTable tokenTable;
    String[] tokenColumnNames = {"符号类型","所在行", "符号值"};
    private TableModel tokenTableModel = new DefaultTableModel(tokenColumnNames, 0);

    //symbol表格
    private JScrollPane symbolJScrollPane;
    private JTable symbolTable;
    String[] symbolColumnNames = {"变量名", "变量类型", "变量值", "变量层次", "变量地址"};
    private TableModel symbolTableModel = new DefaultTableModel(symbolColumnNames, 0);

    //pcode表格
    private JScrollPane pcodeJScrollPane;
    private JTable pcodeTable;
    String[] pcodeColumnNames = {"F", "L", "A"};
    private TableModel pcodeTableModel = new DefaultTableModel(pcodeColumnNames, 0);

    private JTextArea errorMessage;
    private JScrollPane errorPane;

    private GSAnalysis gsa;
    private List<Token> allToken;
    private List<PerSymbol> allSymbol;
    private List<PerPcode> allPcode;
    private List<String> errors;
    private String consoleMessage;
    private int readNum = 0;
    private List<String> output;
    private boolean success = false;


}
