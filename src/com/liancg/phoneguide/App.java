package com.liancg.phoneguide;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class App
{
  private static final String APP_NAME = "Guía Telefónica";
  private static final String APP_VERSION = "v0.1b";

  public static final int DEFAULT_WIDTH = 750;
  public static final int DEFAULT_HEIGHT = 500;

  private String dbPath;
  protected String appTitle;

  protected Display display;
  protected Shell shell;
  private Group resultsGroup;
  protected Table tblResults;
  private Connection conn;
  private Group searchGroup;
  private Label lblStatus;
  protected SearchPanel panelSearch;
  protected List<String[]> searchResults;
  private Preferences preferences;

  /**
   * @param args
   */
  public static void main( String[] args)
  {
    new App().run();
  }

  private void run()
  {
    appTitle = APP_NAME + " " + APP_VERSION;

    initShell();
    centerInScreen();

    dbPath = getDBPath();

    if (dbPath != null)
    {
      File dbFile = new File( dbPath);

      if (dbFile.exists())
      {
        connectToDB( dbPath);
      }
    }

    mainLoop();
  }

  private String getDBPath()
  {
    preferences = Preferences.userNodeForPackage( App.class);
    return preferences.get( "db.path", null);
  }

  private void saveDBPath( String dbPath)
  {
    preferences.put( "db.path", dbPath);
  }

  protected void connectToDB( String filename)
  {
    try
    {
      Class.forName( "org.sqlite.JDBC");

      if (conn != null)
      {
        conn.close();
      }

      conn = DriverManager.getConnection( "jdbc:sqlite:" + filename);

      saveDBPath( filename);

      shell.setText( appTitle + " - " + new File( filename).getName());
      panelSearch.enable();
    }
    catch (SQLException ex)
    {
      // TODO Auto-generated catch block
      ex.printStackTrace();
    }
    catch (ClassNotFoundException ex)
    {
      // TODO Auto-generated catch block
      ex.printStackTrace();
    }
  }

  private void initShell()
  {
    display = new Display();
    shell = new Shell( display);
    shell.setText( appTitle + " - (sin DB)");

    Image image = new Image( display, "icon.png");
    shell.setImage( image);

    createControls();
  }

  private void createControls()
  {
    shell.setLayout( new FormLayout());

    buildMainMenu();
    buildSearchPanel();
    buildStatusBar();
    buildResultsPanel();

    shell.pack();
  }

  private void buildMainMenu()
  {
    Menu menuBar = new Menu( shell, SWT.BAR);
    shell.setMenuBar( menuBar);

    MenuItem fileItem = new MenuItem( menuBar, SWT.CASCADE);
    fileItem.setText( "Archivo");

    Menu fileMenu = buildFileMenu();
    fileItem.setMenu( fileMenu);

    MenuItem helpItem = new MenuItem( menuBar, SWT.CASCADE);
    helpItem.setText( "Ayuda");

    Menu helpMenu = buildHelpMenu();
    helpItem.setMenu( helpMenu);
  }

  @SuppressWarnings("unused")
  private Menu buildFileMenu()
  {
    Menu result = new Menu( shell, SWT.DROP_DOWN);

    MenuItem fileItem = new MenuItem( result, SWT.CASCADE);
    fileItem.setText( "Abrir BD...");
    fileItem.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( SelectionEvent event)
      {
        FileDialog dialog = new FileDialog( shell, SWT.OPEN);
        dialog.setText( "Seleccione la Base de Datos");
        dialog.setFilterNames( new String[] {"BDs Sqlite", "Todos los Archivos" });
        dialog.setFilterExtensions( new String[] {"*.db3;*.db;*.sqlite", "*.*" });

        String filename = dialog.open();

        if (filename != null)
        {
          connectToDB( filename);
        }
      }
    });

    new MenuItem( result, SWT.SEPARATOR);

    MenuItem exitItem = new MenuItem( result, SWT.CASCADE);
    exitItem.setText( "Salir");
    exitItem.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( SelectionEvent event)
      {
        System.exit( 0);
      }
    });

    return result;
  }

  private Menu buildHelpMenu()
  {
    Menu result = new Menu( shell, SWT.DROP_DOWN);

    MenuItem aboutItem = new MenuItem( result, SWT.CASCADE);
    aboutItem.setText( "Acerca de...");
    aboutItem.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( SelectionEvent event)
      {
        MessageBox dialog = new MessageBox( shell, SWT.OK);
        dialog.setMessage( appTitle + "\n(C) Copyright Lian Castellón, 2013");

        dialog.open();
      }
    });

    return result;
  }

  private void buildSearchPanel()
  {
    FormData formData = new FormData();
    formData.left = new FormAttachment( 0);
    formData.right = new FormAttachment( 100);
    formData.height = 200;

    searchGroup = new Group( shell, SWT.NONE);
    searchGroup.setLayoutData( formData);
    searchGroup.setText( "Buscar");
    searchGroup.setLayout( new FormLayout());

    formData = new FormData();
    formData.top = new FormAttachment( 0);
    formData.left = new FormAttachment( 0);
    formData.right = new FormAttachment( 100);
    formData.bottom = new FormAttachment( 100);

    panelSearch = new SearchPanel( searchGroup);
    panelSearch.setLayoutData( formData);
    panelSearch.setOnSearch( new Runnable()
    {
      @Override
      public void run()
      {
        doSearch( panelSearch);
      }
    });
    panelSearch.disable();

    shell.setDefaultButton( panelSearch.getSearchButton());
  }

  protected void doSearch( final SearchPanel panel)
  {
    Cursor cursor = shell.getCursor();
    shell.setCursor( new Cursor( display, SWT.CURSOR_WAIT));

    display.syncExec( new Runnable()
    {
      @Override
      public void run()
      {
        setStatus( "Buscando...");
        panelSearch.disable();

        display.update();
      }
    });

    searchResults = executeQuery( panel.getQueryData());

    tblResults.removeAll();

    for (String[] item : searchResults)
    {
      TableItem tableRow = new TableItem( tblResults, SWT.NONE);
      tableRow.setText( item);
    }

    setRecordCount( searchResults.size());
    panelSearch.enable();

    shell.setCursor( cursor);
  }

  private void buildResultsPanel()
  {
    FormData formData = new FormData();
    formData.left = new FormAttachment( 0);
    formData.top = new FormAttachment( searchGroup);
    formData.right = new FormAttachment( 100);
    formData.bottom = new FormAttachment( 100);

    resultsGroup = new Group( shell, SWT.NONE);
    resultsGroup.setLayoutData( formData);
    resultsGroup.setText( "Resultados");
    resultsGroup.setLayout( new FormLayout());

    addResultsTable();
  }

  private void buildStatusBar()
  {
    FormData formData = new FormData();
    formData.left = new FormAttachment( 0);
    formData.right = new FormAttachment( 100);
    formData.bottom = new FormAttachment( 100);

    lblStatus = new Label( shell, SWT.BORDER);
    lblStatus.setText( "Total: 0");
    lblStatus.setLayoutData( formData);
  }

  private void addResultsTable()
  {
    FormData formData = new FormData();
    formData.left = new FormAttachment( 0);
    formData.top = new FormAttachment( 0);
    formData.right = new FormAttachment( 100);
    formData.bottom = new FormAttachment( 100);

    TableLayout layout = new TableLayout();
    layout.addColumnData( new ColumnWeightData( 20, 100, true));
    layout.addColumnData( new ColumnWeightData( 20, 200, true));
    layout.addColumnData( new ColumnWeightData( 50, 75, true));
    layout.addColumnData( new ColumnWeightData( 20, 100, true));

    tblResults = new Table( resultsGroup, SWT.NONE);
    tblResults.setLayoutData( formData);
    tblResults.setLayout( layout);
    tblResults.setLinesVisible( true);
    tblResults.setHeaderVisible( true);

    TableColumn colPhone = new TableColumn( tblResults, SWT.LEFT);
    colPhone.setText( "Teléfono");

    TableColumn colName = new TableColumn( tblResults, SWT.LEFT);
    colName.setText( "Nombre");

    TableColumn colAddress = new TableColumn( tblResults, SWT.LEFT);
    colAddress.setText( "Dirección");

    TableColumn colPlace = new TableColumn( tblResults, SWT.LEFT);
    colPlace.setText( "Lugar");
  }

  private void centerInScreen()
  {
    shell.setSize( DEFAULT_WIDTH, DEFAULT_HEIGHT);

    Monitor primary = display.getPrimaryMonitor();
    Rectangle bounds = primary.getBounds();
    Rectangle rect = shell.getBounds();
    int x = bounds.x + (bounds.width - rect.width) / 2;
    int y = bounds.y + (bounds.height - rect.height) / 2;

    shell.setLocation( x, y);
  }

  private void mainLoop()
  {
    shell.open();

    while (!shell.isDisposed())
    {
      if (!display.readAndDispatch())
      {
        display.sleep();
      }
    }

    display.dispose();
  }

  protected void setStatus( String message)
  {
    lblStatus.setText( message);
  }

  protected void setRecordCount( int recordCount)
  {
    setStatus( "Total: " + recordCount);
  }

  private String getCondition( QueryData data)
  {
    String result = "";

    String phoneNumberField = null;
    String nameField = null;
    String lastName1Field = null;
    String lastName2Field = null;
    String addressField = null;

    switch (data.type)
    {
      case FIXED:
        phoneNumberField = "SERVICIOS";
        nameField = "EXPR1";
        lastName1Field = "APELL1";
        lastName2Field = "APELL2";
        addressField = "DIRECCION";
        break;

      case MOBILE:
        phoneNumberField = "NUMERO_TELEFONO";
        nameField = "USUARIO_TELEFONO";
        lastName1Field = "USUARIO_TELEFONO";
        lastName2Field = "USUARIO_TELEFONO";
        addressField = "DIRECCION";
        break;

      case PUBLIC:
        phoneNumberField = "Número";
        nameField = "Centro Atención";
        lastName1Field = "Cód Mercad";
        lastName2Field = "Cód Mercad";
        addressField = "Dirección";
        break;

      default:
        break;
    }

    if (!data.phoneNumber.isEmpty())
    {
      result += phoneNumberField + " like '" + data.phoneNumber + "%' ";
    }

    if (!data.name.isEmpty())
    {
      if (!result.isEmpty())
      {
        result += " AND ";
      }

      result += MessageFormat.format( "\"{0}\" like ''{1}%'' ", nameField, data.name);
    }

    if (!data.lastName1.isEmpty())
    {
      if (!result.isEmpty())
      {
        result += " AND ";
      }

      result += MessageFormat.format( "\"{0}\" like ''%{1}%'' ", lastName1Field, data.lastName1);
    }

    if (!data.lastName2.isEmpty())
    {
      if (!result.isEmpty())
      {
        result += " AND ";
      }

      result += MessageFormat.format( "\"{0}\" like ''%{1}%'' ", lastName2Field, data.lastName2);
    }

    if (!data.address.isEmpty())
    {
      if (!result.isEmpty())
      {
        result += " AND ";
      }

      result += addressField + " like '%" + data.address + "%' ";
    }

    return result;
  }

  protected List<String[]> executeQuery( QueryData queryData)
  {
    ArrayList<String[]> result = new ArrayList<String[]>();

    if (queryData.type == PhoneType.ANY)
    {
      queryData.type = PhoneType.FIXED;
      result.addAll( executeSimpleQuery( queryData));

      queryData.type = PhoneType.MOBILE;
      result.addAll( executeSimpleQuery( queryData));

      queryData.type = PhoneType.PUBLIC;
      result.addAll( executeSimpleQuery( queryData));
    }
    else
    {
      result = executeSimpleQuery( queryData);
    }

    return result;
  }

  private ArrayList<String[]> executeSimpleQuery( QueryData queryData)
  {
    ArrayList<String[]> result = new ArrayList<String[]>();

    String condition = getCondition( queryData);
    String table = null;
    String orderField = null;

    switch (queryData.type)
    {
      case FIXED:
        table = "Fijos Nacionales";
        orderField = "SERVICIOS";
        break;

      case MOBILE:
        table = "CENTER_DATOS_CLIENTE_CUBACEL";
        orderField = "NUMERO_TELEFONO";
        break;

      case PUBLIC:
        table = "Publicas";
        orderField = "Número";
        break;

      default:
        break;
    }

    String query = MessageFormat.format( "select * from \"{0}\" where {1} order by {2}", table, condition, orderField);

    try
    {
      Statement stmt = conn.createStatement();

      if (stmt.execute( query))
      {
        ResultSet resultSet = stmt.getResultSet();
        String[] rowData = null;

        while (resultSet.next())
        {
          switch (queryData.type)
          {
            case FIXED:
              rowData = getFixedPhoneData( resultSet);
              break;

            case MOBILE:
              rowData = getMobilePhoneData( resultSet);
              break;

            case PUBLIC:
              rowData = getPublicPhoneData( resultSet);
              break;

            default:
              break;
          }

          result.add( rowData);
        }
      }
    }
    catch (SQLException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return result;
  }

  private String[] getFixedPhoneData( ResultSet resultSet)
  {
    String[] result = null;

    try
    {
      /*
       * String centroFact = resultSet.getString( 1); String sector = resultSet.getString( 2);
       */

      String phone = resultSet.getString( 3);

      String name = resultSet.getString( 4);
      String lastName1 = resultSet.getString( 5);
      String lastName2 = resultSet.getString( 6);
      String address = resultSet.getString( 7);

      /*
       * String insc = resultSet.getString( 8); String insn = resultSet.getString( 9); String insk = resultSet.getString( 10); String etc1 =
       * resultSet.getString( 11); String etc2 = resultSet.getString( 12); String reparto = resultSet.getString( 13);
       */

      String location = resultSet.getString( 14);

      /* String ti = resultSet.getString( 15); */

      String fullName = getFullName( name, lastName1, lastName2);

      result = new String[] {phone, fullName, address, location };
    }
    catch (SQLException ex)
    {
      // TODO Auto-generated catch block
      ex.printStackTrace();
    }

    return result;
  }

  private String[] getMobilePhoneData( ResultSet resultSet)
  {
    String[] result = null;

    try
    {
      String phone = resultSet.getString( 1).substring( 0, 10);

      /*
       * String imsi = resultSet.getString( 2); String numberType = resultSet.getString( 3);
       */

      String name = resultSet.getString( 4);

      // String idNumber = resultSet.getString( 5);

      String address = resultSet.getString( 6);

      /*
       * String nationality = resultSet.getString( 7); String authPerson = resultSet.getString( 8); String authPersonAddress =
       * resultSet.getString( 9); String activationDate = resultSet.getString( 10); String client = resultSet.getString( 11); String
       * clientName = resultSet.getString( 12); String clasification = resultSet.getString( 13); String clientClasification =
       * resultSet.getString( 14); String contract = resultSet.getString( 15); String contractName = resultSet.getString( 16);
       */

      String province = resultSet.getString( 17);

      result = new String[] {phone, name, address, province };
    }
    catch (SQLException ex)
    {
      // TODO Auto-generated catch block
      ex.printStackTrace();
    }

    return result;
  }

  private String[] getPublicPhoneData( ResultSet resultSet)
  {
    String[] result = null;

    try
    {
      String phoneNumber = resultSet.getString( 1);
      String address = resultSet.getString( 2);
      String municipe = resultSet.getString( 3);
      /*
       * String locality = resultSet.getString( 4); String repart = resultSet.getString( 5); String localization = resultSet.getString( 6);
       */
      String attentionCenter = resultSet.getString( 7);
      /*
       * String ura = resultSet.getString( 8); String zone = resultSet.getString( 9); String lock = resultSet.getString( 10); String
       * inventary = resultSet.getString( 11); String popularCounsel = resultSet.getString( 12); String organism = resultSet.getString( 13);
       * String owner = resultSet.getString( 14); String ownerId = resultSet.getString( 15); String circunscription = resultSet.getString(
       * 16); String clientCategory = resultSet.getString( 17); String serviceType = resultSet.getString( 17); String sector =
       * resultSet.getString( 17); String marketingCode = resultSet.getString( 17); String category = resultSet.getString( 17); String
       * support = resultSet.getString( 17); String entity = resultSet.getString( 17); String socialMotivation = resultSet.getString( 17);
       */

      result = new String[] {phoneNumber, attentionCenter, address, municipe };
    }
    catch (SQLException ex)
    {
      // TODO Auto-generated catch block
      ex.printStackTrace();
    }

    return result;
  }

  private String getFullName( String name, String lastName1, String lastName2)
  {
    String result = "";

    if (name != null && !name.trim().isEmpty())
    {
      result += " " + name.trim();
    }

    if (lastName1 != null && !lastName1.trim().isEmpty())
    {
      result += " " + lastName1.trim();
    }

    if (lastName2 != null && !lastName2.trim().isEmpty())
    {
      result += " " + lastName2.trim();
    }

    if (result == "")
    {
      result = "---";
    }

    return result;
  }
}
