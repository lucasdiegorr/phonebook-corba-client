/**
 * 
 */
package graphics;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;

import controller.PhoneBookClient;

import javax.swing.border.TitledBorder;
import javax.swing.UIManager;

import java.awt.Color;

import javax.swing.JPanel;

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
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
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
		this.client = new PhoneBookClient("127.0.0.1", "900");
		client.init();
		client.insertContact("Lucas", 88770835);
		client.insertContact("Diego", 97492417);
		client.insertContact("Rocha", 32624153);
		client.insertContact("Jamilly", 88770835);
		client.insertContact("Bill", 97492417);
		client.insertContact("Valdeco", 32624153);
		client.insertContact("Nelda", 88770835);
		client.insertContact("Nilda", 97492417);
		client.insertContact("Regina", 32624153);
		client.insertContact("Higor", 88770835);
		client.insertContact("Kelly", 97492417);
		client.insertContact("Arthur", 32624153);
		for (String key : client.getListContacts().keySet()) {
			String[] values = {key, this.client.getListContacts().get(key).toString()};
			model.addRow(values);
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 450);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		model = new DefaultTableModel(new Object[][] {},	new String[] {"Nome", "N\u00FAmero"}) {
			Class[] columnTypes = new Class[] {
					String.class, Integer.class
				};
				public Class getColumnClass(int columnIndex) {
					return columnTypes[columnIndex];
				}
			}; 
		frame.getContentPane().setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Agenda", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setBounds(214, 11, 210, 390);
		panel.setLayout(null);

		table = new JTable();
		table.setModel(model);
		table.setBounds(5, 20, 180, 330);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 22, 194, 360);
		scrollPane.setLayout(null);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		
		JLabel lblNome = new JLabel("Nome");
		lblNome.setBounds(10, 0, 46, 14);
		scrollPane.add(lblNome);
		
		JLabel lblNmero = new JLabel("N\u00FAmero");
		lblNmero.setBounds(106, 0, 46, 14);
		scrollPane.add(lblNmero);
		
		scrollPane.add(table);
		panel.add(scrollPane);
		frame.getContentPane().add(panel);
	}
}
