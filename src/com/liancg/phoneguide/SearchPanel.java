package com.liancg.phoneguide;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class SearchPanel extends Composite
{
  private Label lblPhoneNumber;
  private Text txtPhoneNumber;
  private Label lblName;
  private Text txtName;
  private Label lblLastName1;
  private Text txtLastName1;
  private Label lblLastName2;
  private Text txtLastName2;
  private Label lblAddress;
  private Text txtAddress;
  private Button btnSearch;
  protected Clickable clickable;
  private Combo cmbPhoneType;
  private Label lblPhoneType;
  private final Control[] allControls;

  public SearchPanel( Composite parent)
  {
    super( parent, SWT.NONE);
    setLayout( new FormLayout());

    addPhoneNumberControl();
    addPhoneTypeControl();
    addNameControl();
    addLastName1Control();
    addLastName2Control();
    addAddressControl();
    addSearchButton();

    allControls = new Control[] {txtPhoneNumber, txtName, txtLastName1, txtLastName2, txtAddress, cmbPhoneType, btnSearch};

    pack();
  }

  private void addPhoneNumberControl()
  {
    FormData formData = new FormData();
    formData.left = new FormAttachment( 0, 10);
    formData.top = new FormAttachment( 0, 10);
    formData.width = 100;

    lblPhoneNumber = new Label( this, SWT.NONE);
    lblPhoneNumber.setText( "Telefono: ");
    lblPhoneNumber.setLayoutData( formData);
    lblPhoneNumber.setAlignment( SWT.RIGHT);

    formData = new FormData();
    formData.left = new FormAttachment( lblPhoneNumber);
    formData.top = new FormAttachment( 0, 5);
    formData.width = 150;

    txtPhoneNumber = new Text( this, SWT.BORDER);
    txtPhoneNumber.setLayoutData( formData);
  }

  private void addPhoneTypeControl()
  {
    FormData formData = new FormData();
    formData.left = new FormAttachment( txtPhoneNumber, 0, 5);
    formData.top = new FormAttachment( 0, 10);
    formData.width = 110;

    lblPhoneType = new Label( this, SWT.NONE);
    lblPhoneType.setText( "Tipo: ");
    lblPhoneType.setLayoutData( formData);
    lblPhoneType.setAlignment( SWT.RIGHT);

    formData = new FormData();
    formData.left = new FormAttachment( lblPhoneType);
    formData.top = new FormAttachment( 0, 5);
    formData.width = 100;

    cmbPhoneType = new Combo( this, SWT.DROP_DOWN);
    cmbPhoneType.setLayoutData( formData);
    cmbPhoneType.setItems( new String[] {"Fijo", "Móvil", "Pública", "Todos"});
    cmbPhoneType.select( 3);
  }

  private void addNameControl()
  {
    FormData formData = new FormData();
    formData.right = new FormAttachment( lblPhoneNumber, 0, SWT.RIGHT);
    formData.top = new FormAttachment( txtPhoneNumber, 3);

    lblName = new Label( this, SWT.NONE);
    lblName.setText( "Nombre: ");
    lblName.setLayoutData( formData);

    formData = new FormData();
    formData.left = new FormAttachment( txtPhoneNumber, 0, SWT.LEFT);
    formData.top = new FormAttachment( txtPhoneNumber);
    formData.width = 200;

    txtName = new Text( this, SWT.BORDER);
    txtName.setLayoutData( formData);
  }

  private void addLastName1Control()
  {
    FormData formData = new FormData();
    formData.right = new FormAttachment( lblName, 0, SWT.RIGHT);
    formData.top = new FormAttachment( txtName, 3);

    lblLastName1 = new Label( this, SWT.NONE);
    lblLastName1.setText( "Primer Apellido: ");
    lblLastName1.setLayoutData( formData);

    formData = new FormData();
    formData.left = new FormAttachment( txtName, 0, SWT.LEFT);
    formData.top = new FormAttachment( txtName);
    formData.width = 200;

    txtLastName1 = new Text( this, SWT.BORDER);
    txtLastName1.setLayoutData( formData);
  }

  private void addLastName2Control()
  {
    FormData formData = new FormData();
    formData.right = new FormAttachment( lblLastName1, 0, SWT.RIGHT);
    formData.top = new FormAttachment( txtLastName1, 3);

    lblLastName2 = new Label( this, SWT.NONE);
    lblLastName2.setText( "Segundo Apellido: ");
    lblLastName2.setLayoutData( formData);

    formData = new FormData();
    formData.left = new FormAttachment( txtLastName1, 0, SWT.LEFT);
    formData.top = new FormAttachment( txtLastName1);
    formData.width = 200;

    txtLastName2 = new Text( this, SWT.BORDER);
    txtLastName2.setLayoutData( formData);
  }

  private void addAddressControl()
  {
    FormData formData = new FormData();
    formData.right = new FormAttachment( lblLastName2, 0, SWT.RIGHT);
    formData.top = new FormAttachment( txtLastName2, 3);

    lblAddress = new Label( this, SWT.NONE);
    lblAddress.setText( "Dirección: ");
    lblAddress.setLayoutData( formData);

    formData = new FormData();
    formData.left = new FormAttachment( txtLastName2, 0, SWT.LEFT);
    formData.top = new FormAttachment( txtLastName2);
    formData.width = 400;

    txtAddress = new Text( this, SWT.BORDER);
    txtAddress.setLayoutData( formData);
  }

  private void addSearchButton()
  {
    FormData formData = new FormData();
    formData.right = new FormAttachment( 100);
    formData.top = new FormAttachment( 0, 10);
    formData.width = 200;
    formData.height = 100;

    btnSearch = new Button( this, SWT.BORDER);
    btnSearch.setText( "Buscar");
    btnSearch.setLayoutData( formData);
    btnSearch.addSelectionListener( new SelectionListener(){
      @Override
      public void widgetSelected( SelectionEvent arg0)
      {
        if (clickable != null)
        {
          clickable.click();
        }
      }

      @Override
      public void widgetDefaultSelected( SelectionEvent arg0)
      {
      }
    });
  }

  private void enableControls( Control[] controls, boolean enable)
  {
    for (Control control: controls)
    {
      control.setEnabled( enable);
    }
  }

  public QueryData getQueryData()
  {
    QueryData result = new QueryData();

    result.phoneNumber = txtPhoneNumber.getText().trim();
    result.type = PhoneType.values()[cmbPhoneType.getSelectionIndex()];
    result.name = txtName.getText().trim();
    result.lastName1 = txtLastName1.getText().trim();
    result.lastName2 = txtLastName2.getText().trim();
    result.address = txtAddress.getText().trim();

    return result;
  }

  public void setOnSearch( Clickable clickable)
  {
    this.clickable = clickable;
  }

  public Button getSearchButton()
  {
    return btnSearch;
  }

  public void disableControls()
  {
    enableControls( allControls, false);
  }

  public void enableControls()
  {
    enableControls( allControls, true);
  }
}
