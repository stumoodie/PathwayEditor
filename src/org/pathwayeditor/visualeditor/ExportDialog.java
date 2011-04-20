
package at.fhj.ase.clientarch.client.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import at.fhj.ase.clientarch.common.HardwareData;
import at.fhj.ase.clientarch.common.TransferObject;
import at.fhj.ase.clientarch.common.exception.BizException;
import at.fhj.ase.clientarch.common.util.CommonUtil;

import com.toedter.calendar.JDateChooser;

/**
 * The application's export dialog
 * 
 * @author cja, sfe | www.fh-joanneum.at | Client Architectures and Design
 */
public final class ExportDialog extends JDialog implements ActionListener {
  private static final long serialVersionUID = 7280782429565524040L;

  private final String ACTION_CMD_CBX = "actionCmdCbx";
  private final String FORMATSTRING_EXPORT = "yyMMdd-HHmm";

  private JButton jBtnExport;
  private JLabel jLabelStart;
  private JLabel jLabelEnd;
  private JDateChooser jCalStart;
  private JDateChooser jCalEnd;
  private JCheckBox jCbxFromChart;

  private MainFrame mainGUI; // needed for export as chart data is read from there

  ExportDialog(MainFrame mainGUI) {
    super(mainGUI, true);

    this.mainGUI = mainGUI;
    initGUIComponents();
  }

  private void initGUIComponents() {
    this.setName("Export");
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    jLabelStart = new JLabel();
    jLabelEnd = new JLabel();
    jBtnExport = new JButton();
    jCalStart = new JDateChooser();
    jCalEnd = new JDateChooser();
    jCbxFromChart = new JCheckBox("Only use the data out of chart");

    jLabelStart.setText("Start Date:");
    jLabelStart.setName("jLabelStart");

    jLabelEnd.setText("End Date:");
    jLabelEnd.setName("jLabelEnd");

    jBtnExport.setText("Export");
    jBtnExport.setName("jBtnExport");
    jBtnExport.addActionListener(this);
    jBtnExport.setActionCommand(MainFrame.ACTION_CMD_EXPORT);

    jCbxFromChart.addActionListener(this);
    jCbxFromChart.setActionCommand(ACTION_CMD_CBX);

    javax.swing.GroupLayout thisLayout = new javax.swing.GroupLayout(this.getContentPane());
    this.getContentPane().setLayout(thisLayout);
    thisLayout
              .setHorizontalGroup(thisLayout
                                            .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(
                                                      thisLayout
                                                                .createSequentialGroup()
                                                                .addContainerGap()
                                                                .addGroup(
                                                                          thisLayout
                                                                                    .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                                    .addGroup(
                                                                                              thisLayout
                                                                                                        .createSequentialGroup()
                                                                                                        .addComponent(jLabelStart)
                                                                                                        .addPreferredGap(
                                                                                                                         javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                                        .addComponent(jCalStart,
                                                                                                                      javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                      143,
                                                                                                                      javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                        .addPreferredGap(
                                                                                                                         javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                                        .addComponent(jLabelEnd)
                                                                                                        .addPreferredGap(
                                                                                                                         javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                                        .addComponent(jCalEnd,
                                                                                                                      javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                      143,
                                                                                                                      javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                    .addGroup(
                                                                                              javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                              thisLayout
                                                                                                        .createSequentialGroup()
                                                                                                        .addComponent(jLabelStart)
                                                                                                        .addPreferredGap(
                                                                                                                         javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                                                                         12, Short.MAX_VALUE)
                                                                                                        .addComponent(jLabelEnd)
                                                                                                        .addPreferredGap(
                                                                                                                         javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                                                                         12, Short.MAX_VALUE)
                                                                                                        .addGroup(
                                                                                                                  thisLayout
                                                                                                                            .createParallelGroup(
                                                                                                                                                 javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                            .addComponent(jCbxFromChart)
                                                                                                                            .addComponent(jBtnExport))))
                                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
    thisLayout.setVerticalGroup(thisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                          .addGroup(
                                                    thisLayout.createSequentialGroup().addContainerGap()
                                                              .addGroup(
                                                                        thisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                  .addComponent(jLabelStart)
                                                                                  .addComponent(jCalStart, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabelEnd)
                                                                                  .addComponent(jCalEnd, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                              .addGroup(
                                                                        thisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                  .addComponent(jLabelStart))
                                                              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                                                              .addComponent(jCbxFromChart).addComponent(jBtnExport).addContainerGap()));

  }

  @Override
  public final void actionPerformed(ActionEvent e) {
    final String actionCommand = e.getActionCommand();
    if (actionCommand.equals(MainFrame.ACTION_CMD_EXPORT)) {
      onExport();
    } else if (actionCommand.equals(ACTION_CMD_CBX)) {
      onCheckbox();
    }
  }

  /** Reverts the current state of the jDataChoosers concerning their visibility if the checkbox is clicked */
  private void onCheckbox() {
    jCalEnd.setEnabled(!jCalEnd.isEnabled());
    jCalStart.setEnabled(!jCalStart.isEnabled());
  }

  private void onExport() {
    try {
      final String fileName = (jCbxFromChart.isSelected() ? createCsvFileFromChart() : createCsvFileFromDatabase());
      JOptionPane.showMessageDialog(this, (fileName == null ? "Data export unsuccessful" : "Data successfully exported to: " + fileName));
      this.setVisible(false);
      this.dispose();
    } catch (BizException be) {
      JOptionPane.showMessageDialog(this, be.getMessage(), "Wrong input", JOptionPane.ERROR_MESSAGE);
    } catch (RemoteException re) {
      JOptionPane.showMessageDialog(this, re.getMessage(), "Connection problem", JOptionPane.ERROR_MESSAGE);
      re.printStackTrace();
    }
  }

  private String createCsvFileFromDatabase() throws BizException, RemoteException {
    final Date startDate = jCalStart.getDate();
    final Date endDate = jCalEnd.getDate();
    if (CommonUtil.isNull(startDate) || CommonUtil.isNull(endDate)) {
      throw new BizException("Incorrect dates (did you set dates at all?)");
    }

    final Timestamp start = new Timestamp(jCalStart.getDate().getTime());
    final Timestamp end = new Timestamp(jCalEnd.getDate().getTime());
    if (start.after(end)) {
      throw new BizException("Incorrect dates (start-date after end-date?)");
    }

    final TransferObject dataFromServer = ClientController.INSTANCE.getHwComponent().getDataFromDB(start, end);
    return performExport(dataFromServer.getDataList(), start, end);
  }

  private String createCsvFileFromChart() {
    return performExport(mainGUI.getCurrentChartData(), new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
  }

  /**
   * Performs the actual file export into a CSV-file while processin the given {@link List} and setting start- and end-Timestamp.
   * 
   * @return a String which contains the exact filename
   */
  private String performExport(List<HardwareData> dataList, Timestamp start, Timestamp end) {
    final SimpleDateFormat sdf = new SimpleDateFormat(FORMATSTRING_EXPORT);
    final String startDateString = sdf.format(new Date(start.getTime()));
    final String endDateString = sdf.format(new Date(end.getTime()));
    final String fileName = "\\export" + startDateString + "_" + endDateString + ".csv";

    try {
      final FileWriter writer = new FileWriter(fileName);

      // write the headline
      final String SEP = ";";
      final String NEWLINE = "\n";
      final StringBuilder sb = new StringBuilder(50); // the strings have about that length
      writer.append("CPU [%]").append(SEP).append("RAM [%]").append(SEP).append("Time").append(NEWLINE);

      // write line for line and flush it inbetween to prevent any loss of data
      for (HardwareData hwData : dataList) {
        sb.append(hwData.getCpu()).append(SEP).append(hwData.getRam()).append(SEP).append(hwData.getTimestamp());
        writer.append(sb.toString()).append(NEWLINE);
        writer.flush();
        sb.setLength(0); // reset the StringBuilder preventing always creating a new instance (performance)
      }

      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }

    return fileName;
  }
}
