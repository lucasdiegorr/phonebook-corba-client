/**
 * 
 */
package com.ldrr.graphics;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;
import javax.swing.border.TitledBorder;
import javax.swing.UIManager;
import javax.swing.JPanel;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTextField;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.util.Date;
import java.util.TreeMap;

import javax.swing.JFormattedTextField;
import javax.swing.SwingConstants;

import com.ldrr.controller.PhoneBookClient;

import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * All source code and required libraries are found at the following link:
 * https://github.com/lucasdiegorr/phonebook-corba-client
 * branch: master
 */

/**
 * @author Lucas Diego Reboucas Rocha
 * @email lucas.diegorr@gmail.com
 * @year 2014
 */
public class FrameClient {

	private JFrame frame;
	private JTable table;
	private PhoneBookClient client;
	private DefaultTableModel model;
	private JTextField textFieldNameContact;
	private JButton btnAtualizar;
	private JButton btnDeletar;
	private JLabel lblLastUpdate;
	private JLabel label;
	private JFormattedTextField formattedTextField;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FrameClient window = new FrameClient();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public FrameClient() {
		initialize();
		String ipAddress = null;
		do {
			ipAddress = JOptionPane.showInputDialog("Intruduza o endereço no Servidor:");
		} while ((ipAddress.equals("")) || (ipAddress.isEmpty()) ||(ipAddress == null));
		this.client = new PhoneBookClient(ipAddress, model);
		if (!client.init()) {
			JOptionPane.showMessageDialog(null, "Não foi possível contactar o servidor.\nPor favor verifique sua conexão e o endereço fornecido.");
			System.exit(0);
		}
		new Thread(client).start();
		refreshTable();
		new Thread(new ScheduledTask()).start();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				client.removeReference();
			}
		});
		frame.getContentPane().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				table.clearSelection();
				textFieldNameContact.setText(null);
				formattedTextField.setText(null);
				btnAtualizar.setEnabled(false);
				btnDeletar.setEnabled(false);
			}
		});
		frame.setResizable(false);
		frame.setBounds(100, 100, 450, 480);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		model = new DefaultTableModel(new Object[][] {},	new String[] {"Nome", "N\u00FAmero"}) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			@SuppressWarnings("rawtypes")
			Class[] columnTypes = new Class[] {
				String.class, Integer.class
			};
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
			@Override  
			public boolean isCellEditable(final int row, final int column) {  
				return false;  
			} 
		}; 
		frame.getContentPane().setLayout(null);

		JPanel panelTable = new JPanel();
		panelTable.setBorder(new TitledBorder(null, "Agenda", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelTable.setBounds(214, 25, 210, 390);
		panelTable.setLayout(null);

		table = new JTable();
		table.setToolTipText("Selecione um contato para poder atualizá-lo ou removê-lo.");
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				int row = table.getSelectedRow();
				textFieldNameContact.setText(model.getValueAt(row, 0).toString());
				formattedTextField.setText(model.getValueAt(row, 1).toString());
				btnAtualizar.setEnabled(true);
				btnDeletar.setEnabled(true);
			}
		});
		table.setModel(model);
		table.setBounds(10, 25, 175, 325);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBounds(10, 21, 194, 361);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		panelTable.add(scrollPane);
		frame.getContentPane().add(panelTable);

		JPanel panelContact = new JPanel();
		panelContact.setBorder(new TitledBorder(null, "Contato", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelContact.setBounds(6, 25, 199, 229);
		frame.getContentPane().add(panelContact);
		panelContact.setLayout(null);

		JLabel lblNameContact = new JLabel("Nome:");
		lblNameContact.setBounds(6, 21, 55, 16);
		panelContact.add(lblNameContact);

		textFieldNameContact = new JTextField();
		textFieldNameContact.setBounds(6, 49, 186, 28);
		panelContact.add(textFieldNameContact);
		textFieldNameContact.setColumns(10);

		JLabel lblNumberContact = new JLabel("N\u00FAmero:");
		lblNumberContact.setBounds(6, 89, 55, 16);
		panelContact.add(lblNumberContact);

		JButton btnAdicionar = new JButton("Adicionar");
		btnAdicionar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				if (!textFieldNameContact.getText().equals("") && !textFieldNameContact.getText().isEmpty()) {
					String contactName = textFieldNameContact.getText();
					int contactNumber = 0;
					try {
						contactNumber = Integer.parseInt(formattedTextField.getText().replace("-", ""));
					} catch (NumberFormatException e) {
						JOptionPane.showMessageDialog(null, "Por favor insira um telefone válido.\n Somente números.");
						return;
					}


					if (verifyContact(contactName.toUpperCase(),contactNumber)) {
						client.insertContact(contactName.toUpperCase(), contactNumber);
						String[] values = { textFieldNameContact.getText(),
								formattedTextField.getText() };
						model.addRow(values);
						refreshTable();
					}

				}else {
					JOptionPane.showMessageDialog(null, "Por favor digite um nome não nulo.");
				}
				textFieldNameContact.setText(null);
				formattedTextField.setText(null);
			}
		});
		btnAdicionar.setBounds(6, 157, 90, 28);
		panelContact.add(btnAdicionar);

		btnAtualizar = new JButton("Atualizar");
		btnAtualizar.setEnabled(false);
		btnAtualizar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int contactNumber = 00000000;
				try {
					contactNumber = Integer.parseInt(formattedTextField.getText().replace("-", ""));
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "Por favor digite um valor valido como telefone.\nSomente números.");
					return;
				}
				if (textFieldNameContact.getText() == "" || textFieldNameContact.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null, "Por favor digite um nome valido como nome do contato.");
					return;
				}
				client.updateContact(textFieldNameContact.getText(),contactNumber, model.getValueAt(table.getSelectedRow(), 0).toString(), Integer.parseInt(model.getValueAt(table.getSelectedRow(), 1).toString().replace("-", "")));
				btnAtualizar.setEnabled(false);
				btnDeletar.setEnabled(false);
				textFieldNameContact.setText(null);
				formattedTextField.setText(null);

			}
		});
		btnAtualizar.setBounds(102, 157, 90, 28);
		panelContact.add(btnAtualizar);

		btnDeletar = new JButton("Remover");
		btnDeletar.setEnabled(false);
		btnDeletar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				client.deleteContact(textFieldNameContact.getText());
				model.removeRow(table.getSelectedRow());
				btnAtualizar.setEnabled(false);
				btnDeletar.setEnabled(false);
				textFieldNameContact.setText(null);
				formattedTextField.setText(null);
			}
		});
		btnDeletar.setBounds(54, 193, 90, 28);
		panelContact.add(btnDeletar);

		MaskFormatter phoneMask = null;

		try {
			phoneMask = new MaskFormatter("####-####");
			phoneMask.setPlaceholderCharacter(' ');
		} catch (ParseException e) {
			e.printStackTrace();
		}

		formattedTextField = new JFormattedTextField(phoneMask);
		formattedTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent key) {
				if (key.getKeyCode() == KeyEvent.VK_ENTER) {
					if (!textFieldNameContact.getText().equals("") && !textFieldNameContact.getText().isEmpty()) {
						String contactName = textFieldNameContact.getText();
						int contactNumber = 0;
						try {
							contactNumber = Integer.parseInt(formattedTextField.getText().replace("-", ""));
						} catch (NumberFormatException e) {
							JOptionPane.showMessageDialog(null, "Por favor insira um telefone válido.\n Somente números.");
							return;
						}


						if (verifyContact(contactName.toUpperCase(),contactNumber)) {
							client.insertContact(contactName.toUpperCase(), contactNumber);
							String[] values = { textFieldNameContact.getText(),
									formattedTextField.getText() };
							model.addRow(values);
							refreshTable();
						}

					}else {
						JOptionPane.showMessageDialog(null, "Por favor digite um nome não nulo.");
					}
					textFieldNameContact.setText(null);
					formattedTextField.setText(null);
				}
			}
		});
		formattedTextField.setHorizontalAlignment(SwingConstants.CENTER);
		formattedTextField.setBounds(6, 117, 186, 28);
		panelContact.add(formattedTextField);

		lblLastUpdate = new JLabel("Atualizado em:");
		lblLastUpdate.setBounds(17, 264, 98, 16);
		frame.getContentPane().add(lblLastUpdate);

		label = new JLabel("");
		label.setFont(new Font("Tahoma", Font.PLAIN, 10));
		label.setBounds(17, 292, 185, 16);
		frame.getContentPane().add(label);
	}

	private boolean verifyContact(String contactName, int contactNumber) {
		if (client.getListContacts().containsKey(contactName)) {
			JOptionPane.showMessageDialog(null, "Desculpe mas esse nome já foi cadastrado.");
			return false;
		}else if (client.getListContacts().containsValue(contactNumber)) {
			int confirmation = JOptionPane.showConfirmDialog(null, "Este número está associado a outro número. Gostaria de inserí-lo mesmo assim?");
			if (confirmation == JOptionPane.OK_OPTION) {
				return true;
			}else{
				return false;
			}
		}else {
			return true;
		}
	}

	/**
	 * 
	 */
	private void refreshTable() {

		TreeMap<String, Integer> list = client.getListContacts();
		int count = model.getRowCount();
		for (int i = count-1; i >= 0; i--) {
			model.removeRow(i);
		}
		for (String key : list.keySet()) {
			String [] values = {key, new String(list.get(key).toString().substring(0, 4)+"-"+list.get(key).toString().substring(4, 8))};

			model.addRow(values);
		}

	}

	/**
	 * @author Lucas Diego Reboucas Rocha
	 *
	 */
	private class ScheduledTask implements Runnable {
		public void run() {
			while (true) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (client.isChanged()) {
					client.setChanged(false);
					refreshTable();
					label.setText(""+new Date());
				}
			}
		}
	}
}
