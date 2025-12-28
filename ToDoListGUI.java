import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

class Task {
    enum Priority { LOW, MEDIUM, HIGH }

    String description;
    boolean isComplete;
    Priority priority;
    String dueDate;

    Task(String description, Priority priority, String dueDate) {
        this.description = description;
        this.isComplete = false;
        this.priority = priority;
        this.dueDate = dueDate;
    }

    void toggleComplete() { isComplete = !isComplete; }

    void setDescription(String description) { this.description = description; }

    public String getDescription() { return description; }

    public Priority getPriority() { return priority; }

    public String getDueDate() { return dueDate; }

    public void setPriority(Priority priority) { this.priority = priority; }

    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    @Override
    public String toString() {
        return (isComplete ? "✅ " : "⬜ ") + description +
                " [" + priority + "]" +
                (dueDate != null ? " (Due: " + dueDate + ")" : "");
    }
}

class TaskListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(
            JList<?> list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof Task) {
            Task task = (Task) value;
            // Prefix with a checkbox mark to indicate completion status
            String checkbox = task.isComplete ? "✅ " : "⬜ ";
            String display = "<html>";
            if (task.isComplete) {
                setForeground(Color.GRAY);
                setFont(new Font(getFont().getName(), Font.ITALIC, getFont().getSize()));
                display += "<s>"; // strike-through the main text
            }
            // Basic HTML-escape for description
            String desc = task.getDescription() == null ? "" : task.getDescription();
            desc = desc.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");

            display += checkbox + desc +
                       " <span style='color:blue;'>[" + task.getPriority() + "]</span>";
            if (task.getDueDate() != null && !task.getDueDate().isEmpty()) {
                display += " <span style='color:green;'>(Due: " + task.getDueDate() + ")</span>";
            }
            if (task.isComplete) display += "</s>";
            display += "</html>";
            setText(display);
        }
        return this;
    }
}

public class ToDoListGUI extends JFrame {
    private java.util.List<Task> allTasks = new ArrayList<>();
    private DefaultListModel<Task> listModel = new DefaultListModel<>();
    private JList<Task> taskJList = new JList<>(listModel);

    private JTextField taskInput = new JTextField();
    private JComboBox<Task.Priority> priorityInput = new JComboBox<>(Task.Priority.values());
    private JTextField dueDateInput = new JTextField("yyyy-mm-dd");
    private JButton addButton = new JButton("Add Task");
    private JButton toggleButton = new JButton("Toggle Complete");
    private JButton removeButton = new JButton("Remove Task");
    private JButton editButton = new JButton("Edit Task");
    private JButton saveButton = new JButton("Save Tasks");
    private JButton loadButton = new JButton("Load Tasks");
    private JButton clearCompletedButton = new JButton("Clear Completed");
    private JButton sortButton = new JButton("Sort Tasks");

    private JComboBox<String> filterCompletionCombo = new JComboBox<>(new String[]{"All", "Completed", "Incomplete"});
    private JComboBox<Task.Priority> filterPriorityCombo = new JComboBox<>(Task.Priority.values());
    private JTextField searchInput = new JTextField(12);
    private JButton filterButton = new JButton("Apply Filters");

    public ToDoListGUI() {
        setTitle("To-Do List Manager");
        setSize(800, 540);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Top Input Panel (description, priority, due date, Add + filters)
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        topPanel.setBorder(new EmptyBorder(8, 8, 4, 8));
        topPanel.add(new JLabel("Task:"));
        topPanel.add(taskInput);
        taskInput.setPreferredSize(new Dimension(200, 24));
        topPanel.add(new JLabel("Due Date (yyyy-mm-dd):"));
        topPanel.add(dueDateInput);
        dueDateInput.setPreferredSize(new Dimension(90, 24));
        topPanel.add(new JLabel("Priority:"));
        topPanel.add(priorityInput);
        topPanel.add(addButton);

        // Filter Controls row directly under topPanel
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        filterBar.add(new JLabel("Filter By:"));
        filterBar.add(filterCompletionCombo);
        filterBar.add(new JLabel("Priority:"));
        filterBar.add(filterPriorityCombo);
        filterBar.add(new JLabel("Search:"));
        filterBar.add(searchInput);
        filterBar.add(filterButton);

        // Center: Task List
        taskJList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        taskJList.setCellRenderer(new TaskListCellRenderer());
        // Add right-click context menu for task actions
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem popupToggle = new JMenuItem("Toggle Complete");
        JMenuItem popupEdit = new JMenuItem("Edit Task");
        JMenuItem popupRemove = new JMenuItem("Remove Task");
        JMenuItem popupClearCompleted = new JMenuItem("Clear Completed");
        JMenuItem popupSort = new JMenuItem("Sort Tasks");
        JMenuItem popupSave = new JMenuItem("Save Tasks");
        JMenuItem popupLoad = new JMenuItem("Load Tasks");
        JMenuItem popupApplyFilters = new JMenuItem("Apply Filters");
        popupMenu.add(popupToggle);
        popupMenu.add(popupEdit);
        popupMenu.add(popupRemove);
        popupMenu.addSeparator();
        popupMenu.add(popupClearCompleted);
        popupMenu.add(popupSort);
        popupMenu.addSeparator();
        popupMenu.add(popupSave);
        popupMenu.add(popupLoad);
        popupMenu.addSeparator();
        popupMenu.add(popupApplyFilters);

        // Wire popup actions to existing methods
        popupToggle.addActionListener(e -> toggleSelectedTaskCompletion());
        popupEdit.addActionListener(e -> editSelectedTask());
        popupRemove.addActionListener(e -> removeSelectedTask());
        popupClearCompleted.addActionListener(e -> clearCompletedTasks());
        popupSort.addActionListener(e -> sortTasks());
        popupSave.addActionListener(e -> saveTasksToFile());
        popupLoad.addActionListener(e -> loadTasksFromFile());
        popupApplyFilters.addActionListener(e -> applyFilters());

        taskJList.addMouseListener(new MouseAdapter() {
            private void showPopup(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
                    int idx = taskJList.locationToIndex(e.getPoint());
                    if (idx != -1) {
                        Rectangle cellBounds = taskJList.getCellBounds(idx, idx);
                        if (cellBounds != null && cellBounds.contains(e.getPoint())) {
                            taskJList.setSelectedIndex(idx);
                        } else {
                            taskJList.clearSelection();
                        }
                    } else {
                        taskJList.clearSelection();
                    }
                    boolean hasSel = !taskJList.isSelectionEmpty();
                    popupToggle.setEnabled(hasSel);
                    popupEdit.setEnabled(hasSel);
                    popupRemove.setEnabled(hasSel);
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) { showPopup(e); }

            @Override
            public void mouseReleased(MouseEvent e) { showPopup(e); }
        });
        JScrollPane scrollPane = new JScrollPane(taskJList);

        // Bottom Controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        controlPanel.add(toggleButton);
        controlPanel.add(editButton);
        controlPanel.add(removeButton);
        controlPanel.add(clearCompletedButton);
        controlPanel.add(sortButton);
        controlPanel.add(saveButton);
        controlPanel.add(loadButton);

        // Layout setup
        setLayout(new BorderLayout(10, 10));
        add(topPanel, BorderLayout.NORTH);
        add(filterBar, BorderLayout.AFTER_LAST_LINE);
        add(scrollPane, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        // Button Actions
        addButton.addActionListener(e -> addTask());
        toggleButton.addActionListener(e -> toggleSelectedTaskCompletion());
        editButton.addActionListener(e -> editSelectedTask());
        removeButton.addActionListener(e -> removeSelectedTask());
        clearCompletedButton.addActionListener(e -> clearCompletedTasks());
        sortButton.addActionListener(e -> sortTasks());
        saveButton.addActionListener(e -> saveTasksToFile());
        loadButton.addActionListener(e -> loadTasksFromFile());
        filterButton.addActionListener(e -> applyFilters());

        taskJList.addListSelectionListener(e -> updateButtonStates());
        taskInput.addActionListener(e -> addTask());
        updateButtonStates();

        setVisible(true);
    }

    private void addTask() {
        String desc = taskInput.getText().trim();
        if (desc.isEmpty()) return;

        String dueDateStr = dueDateInput.getText().trim();
        if (dueDateStr.equalsIgnoreCase("")) dueDateStr = null;
        else if (!dueDateStr.isEmpty()) {
            try {
                LocalDate.parse(dueDateStr);
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Invalid due date. Use yyyy-mm-dd.");
                return;
            }
        } else dueDateStr = null;

        Task.Priority prio = (Task.Priority) priorityInput.getSelectedItem();
        Task newTask = new Task(desc, prio, dueDateStr);
        allTasks.add(newTask);
        refreshTaskList();
        taskInput.setText("");
        dueDateInput.setText("");
    }

    private void toggleSelectedTaskCompletion() {
        int idx = taskJList.getSelectedIndex();
        if (idx != -1) {
            Task t = listModel.get(idx);
            t.toggleComplete();
            refreshTaskList();
        }
    }

    private void editSelectedTask() {
        int idx = taskJList.getSelectedIndex();
        if (idx != -1) {
            Task t = listModel.get(idx);

            String newDesc = (String) JOptionPane.showInputDialog(this, "Edit description:", "Edit Task",
                    JOptionPane.PLAIN_MESSAGE, null, null, t.getDescription());
            if (newDesc == null || newDesc.trim().isEmpty()) return;

            String newDue = (String) JOptionPane.showInputDialog(this, "Edit due date (yyyy-mm-dd, leave blank to clear):",
                    "Edit Due Date", JOptionPane.PLAIN_MESSAGE, null, null, t.getDueDate() == null ? "" : t.getDueDate());
            if (newDue != null && !newDue.trim().isEmpty()) {
                try { LocalDate.parse(newDue.trim()); }
                catch (DateTimeParseException e) { JOptionPane.showMessageDialog(this, "Invalid due date."); return; }
            } else newDue = null;

            Task.Priority newPrio = (Task.Priority) JOptionPane.showInputDialog(this, "Edit Priority:",
                    "Edit Priority", JOptionPane.PLAIN_MESSAGE, null, Task.Priority.values(), t.getPriority());
            if (newPrio == null) return;
            t.setDescription(newDesc.trim());
            t.setDueDate(newDue);
            t.setPriority(newPrio);
            refreshTaskList();
        }
    }

    private void removeSelectedTask() {
        int idx = taskJList.getSelectedIndex();
        if (idx != -1) {
            Task t = listModel.get(idx);
            allTasks.remove(t);
            refreshTaskList();
        }
    }

    private void clearCompletedTasks() {
        allTasks.removeIf(t -> t.isComplete);
        refreshTaskList();
    }

    private void sortTasks() {
        allTasks.sort(Comparator
                .comparing((Task t) -> t.isComplete)
                .thenComparing((Task t) -> t.priority)
                .thenComparing((Task t) -> t.dueDate == null ? LocalDate.MAX : LocalDate.parse(t.dueDate)));
        refreshTaskList();
    }

    private void applyFilters() {
        listModel.clear();
        String completion = (String) filterCompletionCombo.getSelectedItem();
        Task.Priority priorityFilter = (Task.Priority) filterPriorityCombo.getSelectedItem();
        String keyword = searchInput.getText().toLowerCase().trim();
        for (Task t : allTasks) {
            boolean matchesCompletion = "All".equals(completion) ||
                ("Completed".equals(completion) && t.isComplete) ||
                ("Incomplete".equals(completion) && !t.isComplete);
            boolean matchesPriority = priorityFilter == null || t.priority == priorityFilter;
            boolean matchesKeyword = keyword.isEmpty() || t.description.toLowerCase().contains(keyword);
            if (matchesCompletion && matchesPriority && matchesKeyword) {
                listModel.addElement(t);
            }
        }
    }

    private void refreshTaskList() {
        listModel.clear();
        for (Task t : allTasks) listModel.addElement(t);
        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean hasSel = !taskJList.isSelectionEmpty();
        toggleButton.setEnabled(hasSel);
        editButton.setEnabled(hasSel);
        removeButton.setEnabled(hasSel);
    }

    private void saveTasksToFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Tasks");
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                for (Task t : allTasks) {
                    String due = t.getDueDate() == null ? "" : t.getDueDate();
                    pw.println(escape(t.getDescription()) + "\t" + t.getPriority() + "\t" + due + "\t" + t.isComplete);
                }
                JOptionPane.showMessageDialog(this, "Tasks saved!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Save error: " + ex.getMessage());
            }
        }
    }

    private void loadTasksFromFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Load Tasks");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                allTasks.clear();
                listModel.clear();
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\t", -1);
                    if (parts.length >= 4) {
                        String desc = unescape(parts[0]);
                        Task.Priority prio = Task.Priority.valueOf(parts[1]);
                        String due = parts[2].isEmpty() ? null : parts[2];
                        boolean complete = Boolean.parseBoolean(parts[3]);
                        Task t = new Task(desc, prio, due);
                        t.isComplete = complete;
                        allTasks.add(t);
                        listModel.addElement(t);
                    }
                }
                updateButtonStates();
                JOptionPane.showMessageDialog(this, "Tasks loaded!");
            } catch (IOException | IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Load error: " + ex.getMessage());
            }
        }
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n");
    }
    private String unescape(String s) {
        return s.replace("\\t", "\t").replace("\\n", "\n").replace("\\\\", "\\");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ToDoListGUI::new);
    }
}