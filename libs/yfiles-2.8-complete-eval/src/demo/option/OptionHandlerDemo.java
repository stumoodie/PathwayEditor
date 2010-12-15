/****************************************************************************
 **
 ** This file is part of yFiles-2.8. 
 ** 
 ** yWorks proprietary/confidential. Use is subject to license terms.
 **
 ** Redistribution of this file or of an unauthorized byte-code version
 ** of this file is strictly forbidden.
 **
 ** Copyright (c) 2000-2010 by yWorks GmbH, Vor dem Kreuzberg 28, 
 ** 72070 Tuebingen, Germany. All rights reserved.
 **
 ***************************************************************************/
package demo.option;

import demo.view.DemoDefaults;
import y.option.AbstractItemEditor;
import y.option.ChildChangeReporter;
import y.option.ColorOptionItem;
import y.option.CompoundEditor;
import y.option.ConstraintManager;
import y.option.DefaultEditorFactory;
import y.option.Editor;
import y.option.FileOptionItem;
import y.option.GuiFactory;
import y.option.ItemEditor;
import y.option.ObjectOptionItem;
import y.option.OptionGroup;
import y.option.OptionHandler;
import y.option.OptionItem;
import y.option.ResourceBundleGuiFactory;
import y.option.StringOptionItem;
import y.option.TableEditorFactory;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.VetoableChangeListener;
import java.beans.PropertyVetoException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.JRootPane;
import javax.swing.filechooser.FileFilter;


/**
 * Demonstrates how to create an OptionHandler whose values are
 * editable by multiple editor components. The demo also shows
 * how to localize and customize these editors, and how to register listeners
 * for <code>PropertyChange</code> events.
 * <br><br>
 * Usage Note:
 * <br>
 * Each editor is controlled by a set of buttons and check boxes:
 * <ul>
 *   <li><code>Commit</code><br>
 *       As the name implies, clicking this buttons commits the
 *       displayed values to the corresponding option items.</li>
 *   <li><code>Auto Commit</code><br>
 *       If this option is checked, changes to displayed values are
 *       automatically committed to the corresponding option items, without
 *       having to click <code>Commit</code> first.</li>
 *   <li><code>Reset</code><br>
 *       The standard option item implementations provided by the
 *       {@link y.option} package all support the notion of a backup value.
 *       The backup value is (usually) the value with which an option item was
 *       initialized. The only exception is the {@link y.option.EnumOptionItem},
 *       which allows users to explicitly set its backup value.
 *       Clicking this button (re-) sets the displayed values to the
 *       backup values of the corresponding option items.</li>
 *   <li><code>Adopt</code><br>
 *       Clicking this button sets the displayed values to the values currently
 *       stored in the corresponding option items.</li>
 *   <li><code>Auto Adopt</code><br>
 *       If this option is checked, the displayed values will be automatically
 *       updated on changes to the values of the corresponding option items,
 *       without having to click <code>Adopt</code> first.</li>
 * </ul>
 *
 */
public class OptionHandlerDemo implements Runnable
{
  /**
   * Custom Date option item that allows only values of type
   * <code>java.util.Date</code>.
   */
  private static final class DateOptionItem extends ObjectOptionItem
  {
    /**
     * Creates a new instance of DateOptionItem.
     * The initial value is the current date.
     * @param name    the name of the item
     */
    public DateOptionItem( final String name )
    {
      super(name, new java.util.Date());
    }

    /**
     * Creates a new instance of DateOptionItem.
     * @param name    the name of the item
     * @param value   the initial date of the item
     */
    public DateOptionItem( final String name, final Date value )
    {
      super(name, value);
    }

    /**
     * Returns "Date".
     */
    public String getType()
    {
      return "Date";
    }

    /**
     * Sets the value of this option item.
     *
     * @throws IllegalArgumentException if the specified <code>value</code> is
     *         not of type {@link java.util.Date}
     */
    public void setValue( final Object value )
    {
      if ( value instanceof Date || value == null )
      {
        super.setValue( value );
      }
      else
      {
        final String message = "argument type mismatch";
        throw new IllegalArgumentException( message );
      }
    }
  }

  /**
   * Custom <code>ItemEditor</code> implementation for
   * <code>DateOptionItem</code>.
   *
   * The editor component displays three <code>JTextField</code>s to enter
   * day, month, and year of a date.
   *
   * No validation checks are performed on the user input.
   */
  private static final class DateItemEditor extends AbstractItemEditor
  {
    // value
    private Date date;

    // editor components
    private final JPanel panel;
    private final JTextField day;
    private final JTextField month;
    private final JTextField year;

    // utilities for Date <-> String conversions
    private final SimpleDateFormat parser;
    private final SimpleDateFormat formatDay;
    private final SimpleDateFormat formatMonth;
    private final SimpleDateFormat formatYear;


    /**
     * Creates a new instance of DateItemEditor.
     */
    public DateItemEditor( final DateOptionItem item )
    {
      super(item);

      panel = new JPanel(new GridBagLayout());
      day   = new JTextField(2);
      month = new JTextField(2);
      year  = new JTextField(4);

      parser = new SimpleDateFormat("dd MM yyyy");
      formatDay   = new SimpleDateFormat("dd");
      formatMonth = new SimpleDateFormat("MM");
      formatYear  = new SimpleDateFormat("yyyy");


      // Adopt the text value as editor value when <ENTER> is pressed.
      final KeyAdapter keyAdapter = new KeyAdapter()
      {
        public void keyPressed(final KeyEvent e)
        {
          if (KeyEvent.VK_ENTER == e.getKeyCode())
          {
            parseDateAndSetValue(day.getText(), month.getText(), year.getText());
          }
        }
      };

      // Adopt the text value as editor value when a modified textfield looses
      // focus.
      final FocusAdapter focusAdapter = new FocusAdapter()
      {
        public void focusLost(final FocusEvent e)
        {
          if (!isValueUndefined() && !e.isTemporary())
          {
            parseDateAndSetValue(day.getText(), month.getText(), year.getText());
          }
        }
      };

      day.addKeyListener(keyAdapter);
      day.addFocusListener(focusAdapter);
      month.addKeyListener(keyAdapter);
      month.addFocusListener(focusAdapter);
      year.addKeyListener(keyAdapter);
      year.addFocusListener(focusAdapter);


      final GridBagConstraints gbc = new GridBagConstraints();
      gbc.anchor = GridBagConstraints.WEST;
      gbc.fill = GridBagConstraints.NONE;
      panel.add(day,   gbc);
      panel.add(month, gbc);
      panel.add(year,  gbc);
      gbc.weightx = 1.0;
      panel.add(new JPanel(), gbc);


      // display initial item value
      adoptItemValue();
    }

    public void commitValue() {
      parseDateAndSetValue(day.getText(), month.getText(), year.getText());
      super.commitValue();
    }

    public Object getValue()
    {
      return date;
    }

    /**
     * Sets the value of this editor.
     */
    public void setValue( final Object value )
    {
      setValueImpl(value);
    }

    public boolean isEnabled()
    {
      return panel.isEnabled();
    }

    public void setEnabled( final boolean enabled )
    {
      final boolean oldEnabled = isEnabled();
      if (oldEnabled != enabled)
      {
        panel.setEnabled(enabled);
        day.setEnabled(enabled);
        month.setEnabled(enabled);
        year.setEnabled(enabled);

        // notify interested parties
        publishEnabledChange(oldEnabled, enabled);
      }
    }

    /**
     * Returns always "false" - no value undefined support.
     */
    public boolean isValueUndefined()
    {
      // no value undefined support
      return false;
    }

    /**
     * Does nothing - no value undefined support.
     */
    public void setValueUndefined( final boolean b )
    {
      // no value undefined support
    }

    /**
     * Returns the editor component.
     */
    public JComponent getComponent()
    {
      return panel;
    }

    /**
     * Sets the value of this editor.
     * Supports only values of type <code>java.util.Date</code>.
     */
    private void setValueImpl(final Object value)
    {
      if ( null != date ? !date.equals(value) : null != value )
      {
        final Date oldValue = date;
        try
        {
          // notify interested parties
          fireVetoableChange(PROPERTY_VALUE, oldValue, value);
        }
        catch ( PropertyVetoException pve )
        {
          // rejected
          return;
        }

        date = (Date)value;

        day.setText(formatDay.format(date));
        month.setText(formatMonth.format(date));
        year.setText(formatYear.format(date));

        // notify interested parties
        publishValueChange(oldValue, value);
      }
    }

    /**
     * Tries to parse the specified data into a <code>Date</code> instance.
     * No validation checks are performed on the data.
     *
     * @throws RuntimeException if the specified data cannot be parsed into
     * a <code>Date</code> instance.
     */
    private void parseDateAndSetValue( final String day,
                                       final String month,
                                       final String year )
    {
      try
      {
        // parseDateAndSetValue is only called on user input,
        // so we neither want nor need to update the editor components
        setValueImpl(parser.parse(day + " " + month + " " + year));
      }
      catch (ParseException pe)
      {
        throw new RuntimeException(pe);
      }
    }
  }

  /**
   * Custom editor factory that supports <code>DateOptionItem</code>.
   */
  private static final class CustomEditorFactory extends DefaultEditorFactory
  {
    /**
     * Overwritten to support <code>DateOptionItem</code> instances.
     */
    public ItemEditor createEditor( final OptionItem item, final Map attributes )
    {
      if (item instanceof DateOptionItem)
      {
        final ItemEditor editor =  new DateItemEditor((DateOptionItem)item);

        // IMPORTANT:
        // Register the new editor on the item. If this is not done,
        // features like automatic adoption of item values or constraint
        // handling will not work
        item.addEditor(editor);

        return editor;
      }
      else
      {
        return super.createEditor(item, attributes);
      }
    }
  }


  private final GuiFactory i18n;

  /**
   * Private constructor to prevent external instantiation.
   */
  public OptionHandlerDemo()
  {
    // setup a guifactory
    ResourceBundleGuiFactory gf = null;
    try
    {
      gf = new ResourceBundleGuiFactory();
      gf.addBundle( OptionHandlerDemo.class.getName() );
    }
    catch ( final MissingResourceException mre )
    {
      System.err.println( "Could not find resources! " + mre );
    }
    i18n = gf;
  }

  /**
   * Creates an OptionHandler.
   */
  private OptionHandler createHandler()
  {
    /*
     * Ok, let's create an OptionHandler and add some items.
     * Nothing new so far.
     */
    final OptionHandler op = new OptionHandler("Grid");

    op.useSection("Misc");
    op.addItem(new DateOptionItem("Date"));
    op.addInt("Rows",5);
    op.addInt("Columns",5,1,100);
    op.addCommentItem(getI18nString("Grid.Misc.Comment1"));
    OptionItem col = op.addColor("Color", Color.blue, true);
    col.setAttribute( ColorOptionItem.ATTRIBUTE_SHOW_ALPHA, Boolean.TRUE);
    op.addFile("Open","blafasel");
    op.addFile("Save","");
    op.addEnum("Model",new String[]{"Random","Deterministic","Buba"},2);
    op.addBool("Invert",true);

    JFileChooser chooser = new JFileChooser( System.getProperty( "user.dir" ) );
    chooser.setDialogType( JFileChooser.SAVE_DIALOG );
    chooser.addChoosableFileFilter( new FileFilter()
    {
      public boolean accept( final File f )
      {
        return f.getName().toLowerCase().endsWith( ".txt" );
      }

      public String getDescription()
      {
        return "*.txt";
      }
    } );

    /*
     * Register the custom file chooser for one of our file items.
     */
    OptionItem item;
    item = op.getItem( "Misc", "Save" );
    item.setAttribute( FileOptionItem.ATTRIBUTE_FILE_CHOOSER, chooser );

    op.useSection( "Enums" );

    op.addEnum( "ComboBox", new String[]{"val1", "val2", "val3"}, 0 );
    op.addEnum( "RadioHorizontal", new String[]{"Button1", "Button2", "Button3"}, 0 );
    op.addEnum( "RadioVertical", new String[]{"Button1", "Button2", "Button3"}, 0 );
    op.addEnum( "NoI18n", new String[]{"English", "Deutsch", "Francais"}, 0 );

    op.addString( "Input", "lalala" );
    op.addInt( "Rows", 10 );
    op.addCommentItem( getI18nString("Grid.Enums.Comment1") );
    op.addInt( "Columns", 10 );

    op.addEnum( "Layout", new String[]{"Rows", "Columns"}, 0 );


    /*
     * Let's pep it up:
     * Different styles of enumeration items
     */
    item = op.getItem( "Enums", "RadioHorizontal" );
    item.setAttribute( DefaultEditorFactory.ATTRIBUTE_ENUM_STYLE,
                       DefaultEditorFactory.STYLE_RADIO_BUTTONS );
    item.setAttribute( DefaultEditorFactory.ATTRIBUTE_ENUM_ALIGNMENT,
                       DefaultEditorFactory.ALIGNMENT_HORIZONTAL );
    item = op.getItem( "Enums", "RadioVertical" );
    item.setAttribute( DefaultEditorFactory.ATTRIBUTE_ENUM_STYLE,
                       DefaultEditorFactory.STYLE_RADIO_BUTTONS );
    item.setAttribute( DefaultEditorFactory.ATTRIBUTE_ENUM_ALIGNMENT,
                       DefaultEditorFactory.ALIGNMENT_VERTICAL );

    op.useSection( "Groups" );
    op.addBool("Options",true);
    op.addDouble("Quality", 0.5, 0.0, 1.0, 2);
    op.addEnum( "Layout", new String[]{"rows", "columns"}, 0 );
    op.addInt("Rows",5,1,20);
    op.addColor("RowsColor", Color.blue, true);
    op.addInt("Columns",5,1,20);
    op.addColor("ColumnsColor", Color.blue, true);

    op.useSection( "Strings" );
    op.addString( "TextField", "bla blubber" );
    op.addString( "MultiLine", "bla bla bla\nblubber blubbber" );
    op.addString( "OneLineEmpty", "" );
    op.addString( "MultiLineEmpty", "" );

    item = op.getItem( "Strings", "MultiLine" );
    item.setAttribute( StringOptionItem.ATTRIBUTE_ROWS, new Integer( 5 ) );
    item.setAttribute( StringOptionItem.ATTRIBUTE_POPUP_ROWS, new Integer( 20 ) );
    item.setAttribute( StringOptionItem.ATTRIBUTE_COLUMNS, new Integer( 15 ) );
    item.setAttribute( StringOptionItem.ATTRIBUTE_POPUP_COLUMNS, new Integer( 40 ) );
    item = op.getItem( "Strings", "OneLineEmpty" );
    item.setAttribute( DefaultEditorFactory.ATTRIBUTE_STRING_STYLE,
                       DefaultEditorFactory.STYLE_TEXT_AREA );
    item = op.getItem( "Strings", "MultiLineEmpty" );
    item.setAttribute( StringOptionItem.ATTRIBUTE_ROWS, new Integer( 5 ) );
    item.setAttribute( StringOptionItem.ATTRIBUTE_COLUMNS, new Integer( 15 ) );

    op.useSection("Info");
    op.addCommentItem(getI18nString("Grid.Info.Comment1"));

    final OptionHandler op2 = new OptionHandler("Innerhandler");
    op2.useSection("Inner");
    op2.addInt("Rows",5,1,300);
    op2.addInt("Columns",5,1,20);
    op2.addString("String","value asdf asdf asdf asdf",4).setAttribute(DefaultEditorFactory.FILL_SPACE_WEIGHT, new Double(2));
    op2.addString("String2","value asdf asdf asdf asdf");

    op.addOptionHandler(op2, "Innerhandler");

    /*
     * Now let's see something new:
     * We create a constraint that ensures that the quality slider is disabled
     * when the options checkbox is unchecked.
     */
    ConstraintManager cm = new ConstraintManager( op );
    cm.setEnabledOnValueEquals( "Options", Boolean.TRUE,
                                "Quality" );

    /*
     * Another new feature: grouping items
     */
    OptionGroup og;
    og = new OptionGroup();
    og.setAttribute( OptionGroup.ATTRIBUTE_TITLE, "OPTIONS_AND_QUALITY" );
    og.addItem( op.getItem( "Groups", "Options" ) );
    og.addItem( op.getItem( "Groups", "Quality" ) );

    og = new OptionGroup();
    cm.setEnabledOnValueEquals( "Layout", "rows", og );
    og.setAttribute( OptionGroup.ATTRIBUTE_TITLE, "ROWS" );
    og.addItem( op.getItem( "Groups", "Rows" ) );
    og.addItem( op.getItem( "Groups", "RowsColor" ) );

    og = new OptionGroup();
    cm.setEnabledOnValueEquals( "Layout", "columns", og );
    og.setAttribute( OptionGroup.ATTRIBUTE_TITLE, "COLUMNS" );
    og.addItem( op.getItem( "Groups", "Columns" ) );
    og.addItem( op.getItem( "Groups", "ColumnsColor" ) );

    /*
     * This is the way to create cards ...
     * First we need a controller id.
     */
    final Object ctrId = new Object();

    /*
     * Now, let's set up a group specifying what goes to the first card.
     */
    og = new OptionGroup();
    og.setAttribute( OptionGroup.ATTRIBUTE_TITLE, "ROWS" );
    og.setAttribute( DefaultEditorFactory.ATTRIBUTE_CONTROLLER_ID, ctrId );
    og.setAttribute( DefaultEditorFactory.ATTRIBUTE_CARD_ID, "Rows" );
    og.addItem( op.getItem( "Enums", "Input" ) );
    og.addItem( op.getItem( "Enums", "Rows" ) );

    /*
     * The second card ...
     */
    og = new OptionGroup();
    og.setAttribute( OptionGroup.ATTRIBUTE_TITLE, "COLUMNS" );
    og.setAttribute( DefaultEditorFactory.ATTRIBUTE_CONTROLLER_ID, ctrId );
    og.setAttribute( DefaultEditorFactory.ATTRIBUTE_CARD_ID, "Columns" );
    og.addItem( op.getItem( "Enums", "_COMMENT" ) );
    og.addItem( op.getItem( "Enums", "Columns" ) );

    /*
     * And finally, we need to specify which item controlls the cards.
     */
    op.getItem( "Enums", "Layout" )
      .setAttribute( DefaultEditorFactory.ATTRIBUTE_CONTROLLER_ID, ctrId );

    /*
     * Hmmm, descriptions might be handy, too ...
     */
    op.section( "Misc" )
      .setAttribute( "OptionSection.longDescription",
                     getI18nString( "Grid.Misc.longDescription" ) );
    op.getItem( "Misc", "Rows" )
      .setAttribute( "OptionItem.longDescription",
                     getI18nString( "Grid.Misc.Rows.longDescription" ) );
    op.getItem( "Misc", "Columns" )
      .setAttribute( "OptionItem.longDescription",
                     getI18nString( "Grid.Misc.Columns.longDescription" ) );
    op.getItem( "Groups", "Rows" )
      .setAttribute( "OptionItem.longDescription",
                     getI18nString( "Grid.Groups.Rows.longDescription" ) );
    op.getItem( "Groups", "Columns" )
      .setAttribute( "OptionItem.longDescription",
                     getI18nString( "Grid.Groups.Columns.longDescription" ) );
    op.section( "Info" )
      .setAttribute( "OptionSection.longDescription",
                     getI18nString( "Grid.Info.longDescription" ) );
    op2.section( "Inner" )
       .setAttribute( "OptionSection.longDescription",
                      getI18nString( "Grid.Inner.longDescription" ) );

    return op;
  }

  /**
   * Creates the GUI.
   */
  private JComponent createGUI( final OptionHandler handler )
  {
    /*
     * First we want to create a view we already know and love,
     * so we instantiate a CustomEditorFactory and create an editor.
     */
    DefaultEditorFactory defaultFactory = new CustomEditorFactory();
    defaultFactory.setGuiFactory( i18n );
    Editor editor1 = defaultFactory.createEditor( handler );

    /*
     * Now we want to see something new: a table view.
     * Same procedure as before: instantiate the appropriate factory
     * and create an editor.
     *
     * Note:
     * We used the same OptionHandler instance as before!
     */
    TableEditorFactory tableFactory = new TableEditorFactory();

    // we want to support our custom DateOptionItem in the table view, too.
    tableFactory.setItemFactory(defaultFactory);

    tableFactory.setGuiFactory( i18n );
    Editor editor2 = tableFactory.createEditor( handler );
    final JPanel editorPane = new JPanel( new BorderLayout() );
    editorPane.add( createEditorPane( editor1, getI18nString( "Editor.title.Default" ),
                                      false, true ),
                    BorderLayout.WEST );
    editorPane.add( createEditorPane( editor2, getI18nString( "Editor.title.Table"  ),
                                      true, true ),
                    BorderLayout.CENTER );


    /*
     * We set up property change listeners to print onto the console when
     * a property change occurs.
     */
    final JTextArea console = new JTextArea();
    console.setEditable( false );
    console.setBackground( Color.white );

    final PropertyChangeListener itemListener = new PropertyChangeListener()
    {
      final StringBuffer buffer = new StringBuffer();
      final Object[] args = new Object[5];
      final MessageFormat formatter = new MessageFormat( getI18nString( "Demo.ItemListener.Format" ) );
      public void propertyChange( final PropertyChangeEvent evt )
      {
        args[0] = evt.getSource().getClass().getName();
        args[1] = ((OptionItem)evt.getSource()).getName();
        args[2] = evt.getPropertyName();
        args[3] = evt.getOldValue();
        args[4] = evt.getNewValue();
        buffer.setLength( 0 );
        formatter.format( args, buffer, null );
        buffer.append( "\n" );
        console.append( buffer.toString() );
      }
    };

    final PropertyChangeListener editorListener = new PropertyChangeListener()
    {
      final StringBuffer buffer = new StringBuffer();
      final Object[] args = new Object[5];
      final MessageFormat formatter = new MessageFormat( getI18nString( "Demo.EditorListener.Format" ) );
      public void propertyChange( final PropertyChangeEvent evt )
      {
        args[0] = evt.getSource().getClass().getName();
        args[1] = ((ItemEditor)evt.getSource()).getItem().getName();
        args[2] = evt.getPropertyName();
        args[3] = evt.getOldValue();
        args[4] = evt.getNewValue();
        buffer.setLength( 0 );
        formatter.format( args, buffer, null );
        buffer.append( "\n" );
        console.append( buffer.toString() );
      }
    };

    // register the listener on all items
    handler.addChildPropertyChangeListener( itemListener );

    // register the listener on all item editors
    ((ChildChangeReporter)editor1).addChildPropertyChangeListener( editorListener );
    ((ChildChangeReporter)editor2).addChildPropertyChangeListener( editorListener );


    VetoableChangeListener editorVeto = new VetoableChangeListener()
    {
      final StringBuffer buffer = new StringBuffer();
      final Object[] args = new Object[5];
      final MessageFormat formatter = new MessageFormat( getI18nString( "Demo.EditorVeto.Format" ) );
      final Integer ten = new Integer(10);
      public void vetoableChange( final PropertyChangeEvent evt )
              throws PropertyVetoException
      {
        final ItemEditor editor = (ItemEditor)evt.getSource();
        final String itemName = editor.getItem().getName();
        if ("Color".equals(itemName))
        {
          if (Color.yellow.equals(evt.getNewValue()))
          {
            args[0] = evt.getSource().getClass().getName();
            args[1] = editor.getItem().getName();
            args[2] = evt.getPropertyName();
            args[3] = evt.getOldValue();
            args[4] = evt.getNewValue();
            buffer.setLength( 0 );
            formatter.format( args, buffer, null );
            buffer.append( "\n" );
            console.append( buffer.toString() );
            throw new PropertyVetoException("RevertColor", evt);
          }
        }
      }
    };
    ((ChildChangeReporter)editor1).addChildVetoableChangeListener( editorVeto );
    ((ChildChangeReporter)editor2).addChildVetoableChangeListener( editorVeto );

    VetoableChangeListener itemVeto = new VetoableChangeListener()
    {
      final StringBuffer buffer = new StringBuffer();
      final Object[] args = new Object[5];
      final MessageFormat formatter = new MessageFormat( getI18nString( "Demo.ItemVeto.Format" ) );
      final Integer two = new Integer(2);
      public void vetoableChange( final PropertyChangeEvent evt )
              throws PropertyVetoException
      {
        final OptionItem item = (OptionItem)evt.getSource();
        if ("Color".equals(item.getName()))
        {
          if (Color.red.equals(evt.getNewValue()))
          {
            args[0] = evt.getSource().getClass().getName();
            args[1] = item.getName();
            args[2] = evt.getPropertyName();
            args[3] = evt.getOldValue();
            args[4] = evt.getNewValue();
            buffer.setLength( 0 );
            formatter.format( args, buffer, null );
            buffer.append( "\n" );
            console.append( buffer.toString() );
            throw new PropertyVetoException("RevertColor", evt);
          }
        }
      }
    };
    handler.addChildVetoableChangeListener( itemVeto );

    /*
     * Some rather uninteresting stuff:
     * Putting everything into frame and displaying that.
     */
    final JButton clearConsole = new JButton( getI18nString( "CLEAR_ACTION" ) );
    clearConsole.addActionListener( new ActionListener()
    {
      public void actionPerformed( final ActionEvent e )
      {
        console.setText( "" );
      }
    });

    final JScrollPane jsp = new JScrollPane( console );
    final Dimension d = jsp.getPreferredSize();
    d.height = 100;
    jsp.setPreferredSize( d );

    GridBagConstraints gbc = new GridBagConstraints();
    final JPanel consolePane = new JPanel( new GridBagLayout() );

    gbc.fill = GridBagConstraints.BOTH;
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    consolePane.add( jsp, gbc );

    gbc.anchor = GridBagConstraints.EAST;
    gbc.fill = GridBagConstraints.VERTICAL;
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;
    consolePane.add( clearConsole, gbc );


    final JPanel contentPane = new JPanel( new GridLayout( 1, 1 ) );
    contentPane.setBorder( BorderFactory.createEmptyBorder( 5,5,5,5 ) );
    contentPane.add( new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                    true,
                                    editorPane,
                                    consolePane ) );
    return contentPane;
  }

  /**
   * Creates a component displaying an editor view with some controls.
   */
  private JPanel createEditorPane( final Editor editor, final String title,
                                   final boolean autoCommit,
                                   final boolean autoAdopt )
  {
    final JPanel ep1 = new JPanel( new BorderLayout() );
    ep1.setBorder( BorderFactory.createTitledBorder( title ) );
    ep1.add( editor.getComponent(), BorderLayout.CENTER );
    ep1.add( createControlPane( editor, autoCommit, autoAdopt ),
             BorderLayout.SOUTH );
    return ep1;
  }

  /**
   * Creates controls for the specified editor.
   */
  private JComponent createControlPane( final Editor editor,
                                        final boolean autoCommitFlag,
                                        final boolean autoAdoptFlag )
  {
    final JCheckBox autoCommit = new JCheckBox( getI18nString( "AUTO_COMMIT_ACTION" ) );
    final JCheckBox autoAdopt = new JCheckBox( getI18nString( "AUTO_ADOPT_ACTION" ) );
    final JButton commit = new JButton( getI18nString( "COMMIT_ACTION" ) );
    final JButton adopt = new JButton( getI18nString( "ADOPT_ACTION" ) );
    final JButton reset = new JButton( getI18nString( "RESET_ACTION" ) );

    autoCommit.addActionListener( new ActionListener()
    {
      public void actionPerformed( ActionEvent e )
      {
        final boolean state = autoCommit.isSelected(); 
        commit.setEnabled( !state );
        setAutoCommit( state, editor );
      }
    });

    autoAdopt.addActionListener( new ActionListener()
    {
      public void actionPerformed( ActionEvent e )
      {
        final boolean state = autoAdopt.isSelected();
        adopt.setEnabled( !state );
        setAutoAdopt( state, editor );
      }
    });

    commit.setToolTipText( getI18nString( "COMMIT_ACTION.TOOLTIP" ) );
    commit.addActionListener( new ActionListener()
    {
      public void actionPerformed( ActionEvent e )
      {
        editor.commitValue();
      }
    });

    adopt.setToolTipText( getI18nString( "ADOPT_ACTION.TOOLTIP" ) );
    adopt.addActionListener( new ActionListener()
    {
      public void actionPerformed( ActionEvent e )
      {
        editor.adoptItemValue();
      }
    });

    reset.setToolTipText( getI18nString( "RESET_ACTION.TOOLTIP" ) );
    reset.addActionListener( new ActionListener()
    {
      public void actionPerformed( ActionEvent e )
      {
        editor.resetValue();
      }
    });

    if ( !autoCommitFlag )
    {
      autoCommit.setSelected( true );
    }
    autoCommit.doClick();
    if ( !autoAdoptFlag )
    {
      autoAdopt.setSelected( true );
    }
    autoAdopt.doClick();

    final JPanel controlPane = new JPanel( new GridBagLayout() );
    final GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets.left = 4;
    gbc.insets.right = 4;
    gbc.gridx = 0;
    gbc.gridy = 0;
    controlPane.add( commit, gbc );
    gbc.gridx++;
    controlPane.add( reset, gbc );
    gbc.gridx++;
    controlPane.add( adopt, gbc );
    gbc.insets.left = 0;
    gbc.gridx = 0;
    gbc.gridy++;
    controlPane.add( autoCommit, gbc );
    gbc.gridx+=2;
    controlPane.add( autoAdopt, gbc );
    return controlPane;
  }

  /**
   * Sets the <code>autoCommit</code> property to the specified value,
   * if the specified editor support setting said property.
   */
  private static void setAutoCommit( final boolean autoCommit,
                                     final Editor editor )
  {
    if ( editor instanceof CompoundEditor )
    {
      for ( Iterator it = ((CompoundEditor)editor).editors(); it.hasNext(); )
      {
        setAutoCommit( autoCommit, (Editor)it.next() );
      }
    }
    if ( editor instanceof ItemEditor )
    {
      ((ItemEditor)editor).setAutoCommit( autoCommit );
    }
  }

  /**
   * Sets the <code>autoAdopt</code> property for all items of the specified
   * option handler.
   */
  private static void setAutoAdopt( final boolean autoAdopt,
                                    final Editor editor )
  {
    if ( editor instanceof CompoundEditor )
    {
      for ( Iterator it = ((CompoundEditor)editor).editors(); it.hasNext(); )
      {
        setAutoAdopt( autoAdopt, (Editor)it.next() );
      }
    }
    if ( editor instanceof ItemEditor )
    {
      ((ItemEditor)editor).setAutoAdopt( autoAdopt );
    }
  }

  /**
   * Centers the specified window.
   */
  private static void centerOnScreen( final Window w )
  {
    final Dimension wd = w.getSize();
    final Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();

    int x = sd.width - wd.width;
    x = (x > 0) ? x/2 : 0;
    int y = sd.height - wd.height;
    y = (y > 0) ? y/3 : 0;

    w.setLocation( x, y );
  }

  /**
   * Creates an OptionHandler, creates a GUI for the handler, and displays it.
   */
  public void run()
  {
    final JFrame frame = new JFrame( getI18nString( "Demo.title" ) );
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    addContentTo( frame.getRootPane() );
    frame.pack();
    centerOnScreen( frame );
    frame.setVisible( true );
  }

  public void addContentTo( final JRootPane rootPane )
  {
    rootPane.setContentPane( createGUI( createHandler() ) );
  }

  /**
   * Convenience method, so we do not have to check for <code>null</code>
   * when doing i18n.
   */
  private String getI18nString( final String key )
  {
    return i18n != null ? i18n.getString( key ) : key;
  }

  /**
   * The main method.
   */
  public static void main( final String[] args )
  {
    // set the locale as given from the arguments
    if (args.length > 1)
    {
      Locale.setDefault(new Locale(args[0],args[1]));
    }
    else if (args.length > 0)
    {
      Locale.setDefault(new Locale(args[0],""));
    }

    EventQueue.invokeLater(new Runnable() {
      public void run() {
        DemoDefaults.initLnF();
        (new OptionHandlerDemo()).run();
      }
    });
  }
}
