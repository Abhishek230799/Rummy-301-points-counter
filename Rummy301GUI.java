import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;

public class Rummy301GUI {
    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton addRoundButton;
    private JLabel dealerLabel;

    private final int MAX_SCORE = 301;
    private final Set<Integer> eliminatedColumns = new HashSet<>();
    private final Map<Integer, Integer> playerTotals = new HashMap<>();
    private final java.util.List<String> playerNameList = new ArrayList<>();

    private int roundCounter = 0;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Rummy301GUI().createAndShowGUI());
    }

    private void createAndShowGUI() {
        frame = new JFrame("🃏 Rummy 301 Points Counter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 700);
        frame.setLocationRelativeTo(null);

        int playerCount = getPlayerCount();
        String[] playerNames = getPlayerNames(playerCount);

        tableModel = new DefaultTableModel() {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableModel.addColumn("Round");
        for (String name : playerNames) {
            tableModel.addColumn(name);
        }

        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 1; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(50, 115, 220));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setOpaque(true);

        JScrollPane scrollPane = new JScrollPane(table);

        dealerLabel = new JLabel("Dealer: ");
        dealerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dealerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        dealerLabel.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        dealerLabel.setForeground(new Color(30, 80, 170));

        addRoundButton = new JButton("➕ Add Round");
        addRoundButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        addRoundButton.setFocusPainted(false);
        addRoundButton.setBackground(new Color(50, 115, 220));
        addRoundButton.setForeground(Color.WHITE);
        addRoundButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        addRoundButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addRoundButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                addRoundButton.setBackground(new Color(30, 80, 170));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                addRoundButton.setBackground(new Color(50, 115, 220));
            }
        });
        addRoundButton.addActionListener(e -> addRound());

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(240, 245, 250));

        panel.add(dealerLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel southPanel = new JPanel();
        southPanel.setBackground(new Color(240, 245, 250));
        southPanel.add(addRoundButton);
        panel.add(southPanel, BorderLayout.SOUTH);

        frame.setContentPane(panel);
        frame.setVisible(true);

        tableModel.addTableModelListener(e -> highlightEliminatedColumns());
    }

    private void highlightEliminatedColumns() {
        for (int col = 1; col < tableModel.getColumnCount(); col++) {
            final int column = col;
            if (eliminatedColumns.contains(col)) {
                table.getColumnModel().getColumn(col).setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                                   boolean hasFocus, int row, int column) {
                        Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                        c.setBackground(new Color(220, 220, 220)); // Gray for eliminated
                        return c;
                    }
                });
            } else {
                table.getColumnModel().getColumn(col).setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                                   boolean hasFocus, int row, int column) {
                        Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                        c.setBackground(Color.WHITE);
                        return c;
                    }
                });
            }
        }
        table.repaint();
    }

    private int getPlayerCount() {
        while (true) {
            String input = JOptionPane.showInputDialog("Enter number of players:");
            try {
                int count = Integer.parseInt(input);
                if (count > 1) return count;
            } catch (NumberFormatException ignored) {}
            JOptionPane.showMessageDialog(null, "Invalid number. Please try again.");
        }
    }

    private String[] getPlayerNames(int count) {
        String[] names = new String[count];
        for (int i = 0; i < count; i++) {
            String name = JOptionPane.showInputDialog("Enter name for player " + (i + 1) + ":");
            names[i] = name;
            playerNameList.add(name);
            playerTotals.put(i + 1, 0);
        }
        return names;
    }

    private void addRound() {
        int rowCount = tableModel.getRowCount();

        if (rowCount > 0 && "Total".equals(tableModel.getValueAt(rowCount - 1, 0))) {
            tableModel.removeRow(rowCount - 1);
            rowCount--;
        }

        Object[] row = new Object[tableModel.getColumnCount()];
        roundCounter++;
        row[0] = "Round " + roundCounter;

        int dealerIndex = (roundCounter - 1) % playerNameList.size();
        String dealerName = playerNameList.get(dealerIndex);
        dealerLabel.setText("🃏 Dealer for Round " + roundCounter + ": " + dealerName);

        for (int col = 1; col < tableModel.getColumnCount(); col++) {
            if (eliminatedColumns.contains(col)) {
                row[col] = "-";
                continue;
            }

            String playerName = tableModel.getColumnName(col).replace(" ❌", "");
            int points = 0;
            while (true) {
                String input = JOptionPane.showInputDialog("Enter points for " + playerName + ":");
                if (input == null) return;
                try {
                    points = Integer.parseInt(input);
                    if (points < 0) throw new NumberFormatException();
                    break;
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(frame, "Enter a valid non-negative number.");
                }
            }

            row[col] = points;
            playerTotals.put(col, playerTotals.get(col) + points);
        }

        tableModel.addRow(row);
        addTotalRow();
        eliminateAndCheck();
    }

    private void addTotalRow() {
        Object[] totalRow = new Object[tableModel.getColumnCount()];
        totalRow[0] = "Total";

        for (int col = 1; col < tableModel.getColumnCount(); col++) {
            if (eliminatedColumns.contains(col)) {
                totalRow[col] = "-";
            } else {
                totalRow[col] = playerTotals.getOrDefault(col, 0);
            }
        }

        tableModel.addRow(totalRow);
    }

    private void eliminateAndCheck() {
        java.util.List<String> eliminatedThisRound = new ArrayList<>();

        for (int col = 1; col < tableModel.getColumnCount(); col++) {
            if (eliminatedColumns.contains(col)) continue;

            int total = playerTotals.getOrDefault(col, 0);
            if (total >= MAX_SCORE) {
                eliminatedColumns.add(col);
                String name = tableModel.getColumnName(col);
                eliminatedThisRound.add(name);
                table.getColumnModel().getColumn(col).setHeaderValue(name + " ❌");
            }
        }

        table.getTableHeader().repaint();

        for (String name : eliminatedThisRound) {
            JOptionPane.showMessageDialog(frame,
                    "🏁 " + name.replace(" ❌", "") + " is eliminated (301+ points).",
                    "Eliminated", JOptionPane.WARNING_MESSAGE);
        }

        int activePlayers = tableModel.getColumnCount() - 1 - eliminatedColumns.size();
        if (activePlayers == 1) {
            String winner = "";
            for (int col = 1; col < tableModel.getColumnCount(); col++) {
                if (!eliminatedColumns.contains(col)) {
                    winner = tableModel.getColumnName(col).replace(" ❌", "");
                    break;
                }
            }

            JOptionPane.showMessageDialog(frame,
                    "🏆 " + winner + " is the LAST player standing and WINS the game!",
                    "Game Over", JOptionPane.INFORMATION_MESSAGE);
            addRoundButton.setEnabled(false);
        }
    }
}
