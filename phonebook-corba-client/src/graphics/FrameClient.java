/**
 * 
 */
package graphics;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.table.DefaultTableModel;

import controller.PhoneBookClient;

import javax.swing.border.TitledBorder;
import javax.swing.UIManager;
import javax.swing.JPanel;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTextField;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.TreeMap;

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
	private JTextField textFieldNumberContact;
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
		this.client = new PhoneBookClient(JOptionPane.showInputDialog("Intruduza o endereço no Servidor:"));
		if (!client.init()) {
			JOptionPane.showMessageDialog(null, "Não foi possível contactar o servidor.\nPor favor verifique sua conexão e o endereço fornecido.");
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
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				int row = table.getSelectedRow();
				textFieldNameContact.setText(model.getValueAt(row, 0).toString());
				textFieldNumberContact.setText(model.getValueAt(row, 1).toString());
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
		panelContact.setBounds(6, 25, 199, 236);
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

		textFieldNumberContact = new JTextField();
		textFieldNumberContact.setBounds(6, 110, 186, 31);
		panelContact.add(textFieldNumberContact);
		textFieldNumberContact.setColumns(10);

		JButton btnAdicionar = new JButton("Adicionar");
		btnAdicionar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				if (!textFieldNameContact.getText().equals("") && !textFieldNameContact.getText().isEmpty()) {
					String contactName = textFieldNameContact.getText();
					int contactNumber = 0;
					try {
						contactNumber = Integer.parseInt(textFieldNumberContact.getText());
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}

					if (verifyContact(contactName,contactNumber)) {
						client.insertContact(contactName, contactNumber);
						String[] values = { textFieldNameContact.getText(),
								textFieldNumberContact.getText() };
						model.addRow(values);
						refreshTable();
					}

				}else {
					JOptionPane.showMessageDialog(null, "Por favor digite um nome não nulo.");
				}
				textFieldNameContact.setText(null);
				textFieldNumberContact.setText(null);
			}

			private boolean verifyContact(String contactName, int contactNumber) {
				if (client.getListContacts().containsKey(contactName) || client.getListContacts().containsValue(contactNumber)) {
					JOptionPane.showMessageDialog(null, "Desculpe mas esse nome já foi cadastrado.");
					return false;
				}else {
					return true;
				}
			}
		});
		btnAdicionar.setBounds(6, 153, 90, 28);
		panelContact.add(btnAdicionar);

		JButton btnAtualizar = new JButton("Atualizar");
		btnAtualizar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int contactNumber = 0;
				try {
					contactNumber = Integer.parseInt(textFieldNumberContact.getText());
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				client.updateContact(textFieldNameContact.getText(),contactNumber, model.getValueAt(table.getSelectedRow(), 0).toString(), Integer.parseInt(model.getValueAt(table.getSelectedRow(), 1).toString()));
				textFieldNameContact.setText(null);
				textFieldNumberContact.setText(null);
			}
		});
		btnAtualizar.setBounds(102, 153, 90, 28);
		panelContact.add(btnAtualizar);

		JButton btnDeletar = new JButton("Deletar");
		btnDeletar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				client.deleteContact(textFieldNameContact.getText());
				model.removeRow(table.getSelectedRow());
				textFieldNameContact.setText(null);
				textFieldNumberContact.setText(null);
			}
		});
		btnDeletar.setBounds(54, 189, 90, 28);
		panelContact.add(btnDeletar);
	}

	/**
	 * 
	 */
	private void refreshTable() {

		TreeMap<String, Integer> list = client.getListContacts();
		System.out.println("Tem " + model.getRowCount());
		int count = model.getRowCount();
		for (int i = count-1; i >= 0; i--) {
			model.removeRow(i);
		}
		for (String key : list.keySet()) {
			String [] values = {key, list.get(key).toString()};
			model.addRow(values);
		}

	}

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
				}
			}
		}
	}
}
