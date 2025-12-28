PROJECT TITLE: ADVANCED TO-DO LIST MANAGER (JAVA SWING)
A desktop task management application built with Java Swing. This project demonstrates the implementation of a graphical user interface, custom list rendering, real-time filtering, and persistent data storage.

KEY FEATURES
Dynamic Task Management: Create, edit, and delete tasks within a single window.

Custom UI Rendering:

Completed tasks are displayed with a gray color and a strike-through effect.

Status indicators (Checkboxes) provide instant visual feedback on task completion.

Priority and Deadlines: Assign High, Medium, or Low priorities and track due dates using the YYYY-MM-DD format.

Advanced Filtering:

Filter by completion status: All, Completed, or Incomplete.

Filter by specific priority levels.

Keyword Search: Instant matching across all task descriptions.

Intelligent Sorting: A multi-stage sorting algorithm that organizes tasks by Status, then Priority, and finally by Due Date.

Data Persistence: Save and Load functionality using localized File IO to maintain data across different sessions.

Context Menu: Right-click support on list items for rapid task operations like Toggling and Editing.

TECHNICAL STACK
Language: Java 8 or higher.

Framework: Swing and AWT (Abstract Window Toolkit).

Architecture: Model-View-Controller (MVC) logic patterns.

Storage: Tab-Separated Value (TSV) persistence.

TECHNICAL HIGHLIGHTS
Custom Cell Rendering The project implements a TaskListCellRenderer. It uses HTML-in-Swing to allow for complex styling like strike-throughs and colored priority tags within a standard JList element.

Comparator-Based Sorting The sorting logic uses a chained Comparator to handle multiple criteria:

Boolean Sort: Prioritizes incomplete tasks.

Enum Sort: Sorts by the ordinal value of the Priority enum.

Date Sort: Chronological ordering, handling empty dates as future events.

Thread Safety The application utilizes SwingUtilities.invokeLater to ensure that the GUI is constructed on the Event Dispatch Thread (EDT), preventing race conditions and ensuring thread-safe UI updates.


FILE STRUCTURE
ToDoListGUI.java: Contains the main application logic and UI components.

Task.java: The data model for individual task objects.

todo_data.txt: The auto-generated file used for persistent storage.
