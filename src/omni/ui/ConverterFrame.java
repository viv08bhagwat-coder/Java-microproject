package omni.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import omni.core.ConversionRecord;
import omni.engine.AnalyzerEngine;
import omni.engine.FormatValidator;
import omni.engine.ImageConverter;
import omni.engine.MetadataParser;
import omni.engine.PdfExtractor;
import omni.storage.ConversionDB;

/* ═══════════════════════════════════════════════════════════════════════════
 *  OmniFormat — Universal Image, Document & PDF Converter Workstation
 *  Context : 3rd-Year CSE Capstone Microproject (MIT-WPU Course CSE00130)
 *
 *  Bidirectional Conversion Pipeline:
 *  - Image \u2194 Image (PNG \u2194 JPG \u2194 BMP)
 *  - Image \u2192 PDF  &  Image \u2192 DOC
 *  - PDF \u2192 Images (extracts embedded assets to PNG/JPG/BMP)
 *  - DOC \u2192 Images (extracts embedded assets to PNG/JPG/BMP)
 *  - Dedicated "Image Summary" Inspector Tab
 * ═══════════════════════════════════════════════════════════════════════════ */
public class ConverterFrame extends JFrame {

    /* ── Workstation Theme Colors ──────────────────────────────────────── */
    private static final Color BG_DARK    = new Color(18, 22, 31);     // Deep sleek dark
    private static final Color BG_CARD    = new Color(28, 33, 46);     // Card container
    private static final Color BG_INPUT   = new Color(38, 44, 60);     // Input field
    private static final Color BORDER_CLR = new Color(55, 63, 85);     // Card border
    private static final Color FG_TEXT    = new Color(240, 243, 250);  // High-contrast text
    private static final Color FG_MUTED   = new Color(150, 158, 175);  // Secondary text
    private static final Color ACCENT_TEAL= new Color(0, 180, 160);    // Primary cyan/teal
    private static final Color ACCENT_GOLD= new Color(230, 165, 30);   // Action gold
    private static final Color ACCENT_GRN = new Color(46, 175, 75);    // Success green
    private static final Color ACCENT_BLUE= new Color(70, 140, 240);   // Word blue

    /* ── UI Controls ───────────────────────────────────────────────────── */
    private JTextField        fileDisplayField;
    private JButton           browseBtn;
    private JButton           analyzeBtn;
    private JComboBox<String> formatDropdown;
    private JCheckBox         mergeCheck;
    private JButton           convertBtn;
    private JProgressBar      progressBar;
    private JLabel            statusMessageLabel;

    /* Result Action Bar */
    private JPanel            resultCard;
    private JLabel            resultLabel;
    private JButton           openOutputBtn;
    private JButton           downloadBtn;
    private JButton           openFolderBtn;

    /* Tabs: Console Log, Image Summary & JDBC DB Table */
    private JTabbedPane       tabPane;
    private JTextArea         logArea;

    /* Image Summary Tab Controls */
    private DefaultTableModel summaryModel;
    private JTable            summaryTable;
    private JLabel            summaryThumbLabel;
    private JEditorPane       summaryDetailsPane;

    /* Database Tab Controls */
    private JTable            dbTable;
    private DefaultTableModel dbModel;
    private JLabel            dbRowCountLabel;

    /* ── State Data Structures ─────────────────────────────────────────── */
    private final List<File>             selectedFiles  = new ArrayList<>();
    private File                         lastOutputFile = null;
    private String                       lastTargetFmt  = "pdf";

    /* Unit 4 / Lab 11–15 : Collections */
    private final ArrayList<ConversionRecord> history      = new ArrayList<>();
    private final HashMap<String, Integer>    formatCounts = new HashMap<>();

    /* Unit 5 / Lab 16 : JDBC Persistence */
    private final ConversionDB database;

    /* Unit 4 / Lab 11–15 : Functional Interfaces */
    private final Predicate<ConversionRecord> highEfficiency = rec -> rec.getEfficiencyScore() > 70.0;
    private final Consumer<ConversionRecord>  auditPrinter   = rec -> log(rec.auditLog());
    private final Supplier<String>            timeSupplier   = () -> LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("HH:mm:ss"));

    /* ═══════════════════════════════════════════════════════════════════════
     *  Constructor
     * ═══════════════════════════════════════════════════════════════════════ */
    public ConverterFrame() throws SQLException {
        this.database = new ConversionDB();
        buildUI();
        log("Image Converter Engine v2.0 Initialized \u2022 100% Offline");
        log("Supported Formats: Image (PNG, JPG, BMP) \u2194 Document (PDF)");
        log("Ready. Select one or more files using 'Browse' to begin.\n");
    }

    private void buildUI() {
        setTitle("Image Converter \u2014 Universal Document & Media Workstation (MIT-WPU)");
        setSize(1140, 760);
        setMinimumSize(new Dimension(960, 680));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(BG_DARK);
        root.setBorder(new EmptyBorder(12, 14, 12, 14));

        /* ── 1. Top Header Banner ── */
        root.add(buildHeaderBanner(), BorderLayout.NORTH);

        /* ── 2. Split Workspace (Left: Converter Controls, Right: Tabs) ── */
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setBackground(BG_DARK);
        split.setBorder(null);
        split.setDividerSize(6);
        split.setDividerLocation(480);

        split.setLeftComponent(buildConverterPanel());
        split.setRightComponent(buildInspectorTabs());

        root.add(split, BorderLayout.CENTER);
        setContentPane(root);
    }

    /* ── Header Banner ─────────────────────────────────────────────────── */
    private JPanel buildHeaderBanner() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_CARD);
        bar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_CLR, 1),
                new EmptyBorder(10, 16, 10, 16)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setBackground(BG_CARD);

        JLabel title = new JLabel("\u26A1 Image Converter");
        title.setFont(new Font("Segoe UI", Font.BOLD, 19));
        title.setForeground(ACCENT_TEAL);
        left.add(title);

        JLabel sub = new JLabel("Bidirectional Image & PDF Engine \u2022 Course CSE00130");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(FG_MUTED);
        left.add(sub);
        bar.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setBackground(BG_CARD);

        JLabel offlineBadge = new JLabel("\uD83D\uDD12 100% Offline / Local JDK 8+");
        offlineBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        offlineBadge.setForeground(ACCENT_GRN);
        right.add(offlineBadge);

        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    /* ── Left Converter Panel ──────────────────────────────────────────── */
    private JPanel buildConverterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_DARK);

        /* Card 1 : Image Selection */
        JPanel selectCard = createCardPanel(" Step 1: Select Source Files (Multiselect Enabled) ");
        selectCard.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 4, 6, 4);
        g.fill   = GridBagConstraints.HORIZONTAL;

        fileDisplayField = new JTextField("No files selected yet");
        fileDisplayField.setEditable(false);
        fileDisplayField.setBackground(BG_INPUT);
        fileDisplayField.setForeground(FG_TEXT);
        fileDisplayField.setCaretColor(FG_TEXT);
        fileDisplayField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g.gridx = 0; g.gridy = 0; g.weightx = 1.0;
        selectCard.add(fileDisplayField, g);

        JPanel step1Btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        step1Btns.setBackground(BG_CARD);

        analyzeBtn = createBtn("\uD83D\uDD0D Analyze Content", ACCENT_GOLD, Color.BLACK);
        analyzeBtn.setToolTipText("Analyze what this image or PDF is about (pages, size, core topic)");
        analyzeBtn.addActionListener(e -> {
            if (!selectedFiles.isEmpty()) {
                tabPane.setSelectedIndex(0);
                updateImageSummaryTab();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Please select a file first using 'Browse' to analyze its contents.",
                        "No File Selected", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        step1Btns.add(analyzeBtn);

        browseBtn = createBtn("Browse\u2026", ACCENT_TEAL, Color.BLACK);
        browseBtn.addActionListener(e -> browseFiles());
        step1Btns.add(browseBtn);

        g.gridx = 1; g.gridy = 0; g.weightx = 0;
        selectCard.add(step1Btns, g);

        panel.add(selectCard);
        panel.add(Box.createVerticalStrut(10));

        /* Card 2 : Target Format & Conversion */
        JPanel convertCard = createCardPanel(" Step 2: Choose Target Format & Convert ");
        convertCard.setLayout(new GridBagLayout());
        GridBagConstraints g2 = new GridBagConstraints();
        g2.insets = new Insets(6, 4, 6, 4);
        g2.fill   = GridBagConstraints.HORIZONTAL;

        JLabel fmtLbl = new JLabel("Target Format:");
        fmtLbl.setForeground(FG_TEXT);
        fmtLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.gridx = 0; g2.gridy = 0; g2.weightx = 0;
        convertCard.add(fmtLbl, g2);

        formatDropdown = new JComboBox<>(new String[]{
            "PDF \u2014 Native Printable Document (.pdf)",
            "JPG \u2014 JPEG Compressed Image (.jpg)",
            "PNG \u2014 PNG Lossless Image (.png)",
            "BMP \u2014 BMP Bitmap (.bmp)"
        });
        formatDropdown.setBackground(BG_INPUT);
        formatDropdown.setForeground(FG_TEXT);
        formatDropdown.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formatDropdown.addActionListener(e -> updateActionButtonsOnFormatChange());
        g2.gridx = 1; g2.gridy = 0; g2.weightx = 1.0;
        convertCard.add(formatDropdown, g2);

        mergeCheck = new JCheckBox("Merge multiple images into a single multi-page document", true);
        mergeCheck.setBackground(BG_CARD);
        mergeCheck.setForeground(FG_TEXT);
        mergeCheck.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2.gridx = 0; g2.gridy = 1; g2.gridwidth = 2;
        convertCard.add(mergeCheck, g2);

        convertBtn = createBtn("\u25B6 Start Conversion Pipeline", ACCENT_GRN, Color.WHITE);
        convertBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        convertBtn.setPreferredSize(new Dimension(320, 38));
        convertBtn.addActionListener(e -> startConversion());
        g2.gridx = 0; g2.gridy = 2; g2.gridwidth = 2;
        convertCard.add(convertBtn, g2);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");
        progressBar.setBackground(BG_INPUT);
        progressBar.setForeground(ACCENT_TEAL);
        progressBar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2.gridx = 0; g2.gridy = 3; g2.gridwidth = 2;
        convertCard.add(progressBar, g2);

        statusMessageLabel = new JLabel("Status: Awaiting task", SwingConstants.CENTER);
        statusMessageLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        statusMessageLabel.setForeground(FG_MUTED);
        g2.gridx = 0; g2.gridy = 4; g2.gridwidth = 2;
        convertCard.add(statusMessageLabel, g2);

        panel.add(convertCard);
        panel.add(Box.createVerticalStrut(10));

        /* Card 3 : Output File & Direct Action Handlers */
        resultCard = createCardPanel(" Step 3: Converted Output Actions ");
        resultCard.setLayout(new BorderLayout(0, 8));

        resultLabel = new JLabel("No file converted yet. Click 'Start Conversion' above.", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        resultLabel.setForeground(FG_MUTED);
        resultCard.add(resultLabel, BorderLayout.NORTH);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        actionBtns.setBackground(BG_CARD);

        // Dynamic Open button (PDF \u2192 Browser, DOC \u2192 WordPad/Word, Image \u2192 Photo viewer)
        openOutputBtn = createBtn("\uD83D\uDC41 Open Output File", ACCENT_TEAL, Color.BLACK);
        openOutputBtn.setEnabled(false);
        openOutputBtn.addActionListener(e -> openOutputFileTarget(lastOutputFile));
        actionBtns.add(openOutputBtn);

        // Download / Save As button
        downloadBtn = createBtn("\u2B07 Download / Save As\u2026", ACCENT_GOLD, Color.BLACK);
        downloadBtn.setEnabled(false);
        downloadBtn.addActionListener(e -> downloadOutputFile());
        actionBtns.add(downloadBtn);

        // Reveal in Explorer
        openFolderBtn = createBtn("\uD83D\uDCC1 Open Folder", BG_INPUT, FG_TEXT);
        openFolderBtn.setEnabled(false);
        openFolderBtn.addActionListener(e -> openContainingFolder());
        actionBtns.add(openFolderBtn);

        resultCard.add(actionBtns, BorderLayout.CENTER);
        panel.add(resultCard);

        return panel;
    }

    /* ── Right Inspector Panel (Console Log + Image Summary + JDBC DB) ─── */
    private JTabbedPane buildInspectorTabs() {
        tabPane = new JTabbedPane();
        tabPane.setBackground(BG_CARD);
        tabPane.setForeground(FG_TEXT);
        tabPane.setFont(new Font("Segoe UI", Font.BOLD, 12));

        tabPane.addTab("\uD83D\uDD0D Asset Analyzer: What This File Contains", buildImageSummaryTab());
        tabPane.addTab("\uD83D\uDCCB Live Conversion Log", buildLogTab());
        tabPane.addTab("\uD83D\uDDC4 JDBC Database (Lab 16)", buildDatabaseTab());

        return tabPane;
    }

    /* ── Tab 1 : Live Log ──────────────────────────────────────────────── */
    private JPanel buildLogTab() {
        JPanel logTab = new JPanel(new BorderLayout(0, 4));
        logTab.setBackground(BG_DARK);
        logTab.setBorder(new EmptyBorder(6, 6, 6, 6));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(12, 15, 22));
        logArea.setForeground(new Color(110, 230, 140));
        logArea.setCaretColor(Color.WHITE);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(new LineBorder(BORDER_CLR, 1));
        logTab.add(logScroll, BorderLayout.CENTER);

        JPanel logBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logBottom.setBackground(BG_CARD);
        JButton clearLogBtn = createBtn("Clear Log", BG_INPUT, FG_TEXT);
        clearLogBtn.addActionListener(e -> logArea.setText(""));
        logBottom.add(clearLogBtn);
        logTab.add(logBottom, BorderLayout.SOUTH);

        return logTab;
    }

    /* ── Tab 2 : Analyzer Engine Tab (Image & PDF Analysis) ────────────── */
    private JPanel buildImageSummaryTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(6, 6, 6, 6));

        // Top Summary Table
        String[] cols = {"#", "File Name", "Asset Type", "Size", "Pages / Res", "What It Is About"};
        summaryModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        summaryTable = new JTable(summaryModel);
        styleTable(summaryTable);
        summaryTable.getSelectionModel().addListSelectionListener(e -> onSummarySelectionChanged());

        JScrollPane tableScroll = new JScrollPane(summaryTable);
        tableScroll.setBorder(new LineBorder(BORDER_CLR, 1));
        tableScroll.setPreferredSize(new Dimension(500, 210));
        panel.add(tableScroll, BorderLayout.NORTH);

        // Center / Bottom Inspector Card
        JPanel detailCard = createCardPanel(" Image & PDF Deep Content & Structure Inspector ");
        detailCard.setLayout(new BorderLayout(12, 0));

        JPanel thumbBox = new JPanel(new BorderLayout(0, 6));
        thumbBox.setBackground(BG_CARD);

        summaryThumbLabel = new JLabel("No asset selected", SwingConstants.CENTER);
        summaryThumbLabel.setPreferredSize(new Dimension(150, 150));
        summaryThumbLabel.setBackground(BG_INPUT);
        summaryThumbLabel.setOpaque(true);
        summaryThumbLabel.setForeground(FG_MUTED);
        summaryThumbLabel.setBorder(new LineBorder(BORDER_CLR, 1));
        thumbBox.add(summaryThumbLabel, BorderLayout.NORTH);

        JLabel thumbHint = new JLabel("<html><center><small style='color:#969EAF;'>Visual Asset Preview</small></center></html>", SwingConstants.CENTER);
        thumbBox.add(thumbHint, BorderLayout.CENTER);
        detailCard.add(thumbBox, BorderLayout.WEST);

        summaryDetailsPane = new JEditorPane();
        summaryDetailsPane.setContentType("text/html");
        summaryDetailsPane.setEditable(false);
        summaryDetailsPane.setBackground(BG_CARD);
        summaryDetailsPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        summaryDetailsPane.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        summaryDetailsPane.setText("<html><body style='font-family:Segoe UI, sans-serif; color:#F0F3FA; background-color:#1F2637; padding:10px;'>"
                + "<div style='color:#00B4A0; font-size:14px; font-weight:bold;'>\uD83D\uDD0D Asset Analyzer & Content Summarizer</div>"
                + "<p style='color:#969EAF;'>Select an Image (PNG, JPG, BMP) or PDF document from the table above to run the detailed natural-language summarizer and deep structural analyzer.</p>"
                + "</body></html>");

        JScrollPane detailsScroll = new JScrollPane(summaryDetailsPane);
        detailsScroll.setBorder(new LineBorder(BORDER_CLR, 1));
        detailsScroll.getVerticalScrollBar().setUnitIncrement(14);
        detailCard.add(detailsScroll, BorderLayout.CENTER);

        panel.add(detailCard, BorderLayout.CENTER);
        return panel;
    }

    /* ── Tab 3 : JDBC Database Audit Records (Unit 5 / Lab 16) ──────────── */
    private JPanel buildDatabaseTab() {
        JPanel dbTab = new JPanel(new BorderLayout(0, 6));
        dbTab.setBackground(BG_DARK);
        dbTab.setBorder(new EmptyBorder(6, 6, 6, 6));

        JPanel dbToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        dbToolbar.setBackground(BG_CARD);

        JButton qHighBtn = createBtn("\u2605 High Efficiency (>70%)", ACCENT_GOLD, Color.BLACK);
        qHighBtn.addActionListener(e -> queryHighEfficiencyDb());
        dbToolbar.add(qHighBtn);

        JButton qAllBtn = createBtn("Show All Records", ACCENT_TEAL, Color.BLACK);
        qAllBtn.addActionListener(e -> queryAllDb());
        dbToolbar.add(qAllBtn);

        JButton clearDbBtn = createBtn("Clear DB", new Color(200, 50, 50), Color.WHITE);
        clearDbBtn.addActionListener(e -> clearDatabase());
        dbToolbar.add(clearDbBtn);

        dbRowCountLabel = new JLabel("Records: 0");
        dbRowCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        dbRowCountLabel.setForeground(FG_MUTED);
        dbToolbar.add(dbRowCountLabel);

        dbTab.add(dbToolbar, BorderLayout.NORTH);

        String[] cols = {"ID", "File Name", "Size (B)", "Source", "Target", "Score %", "Remarks", "Timestamp"};
        dbModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        dbTable = new JTable(dbModel);
        styleTable(dbTable);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        dbTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        JScrollPane dbScroll = new JScrollPane(dbTable);
        dbScroll.setBorder(new LineBorder(BORDER_CLR, 1));
        dbTab.add(dbScroll, BorderLayout.CENTER);

        return dbTab;
    }

    /* ═══════════════════════════════════════════════════════════════════════
     *  ACTION HANDLERS : File Browsing & Automatic Format Adjustment
     * ═══════════════════════════════════════════════════════════════════════ */
    private void browseFiles() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select Images or Documents to Convert");
        fc.setMultiSelectionEnabled(true);
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                String n = f.getName().toLowerCase();
                return n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg")
                    || n.endsWith(".bmp") || n.endsWith(".pdf") || n.endsWith(".doc");
            }
            @Override
            public String getDescription() {
                return "All Supported (*.png, *.jpg, *.bmp, *.pdf, *.doc)";
            }
        });

        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File[] files = fc.getSelectedFiles();
            if (files == null || files.length == 0) {
                File sf = fc.getSelectedFile();
                if (sf != null) files = new File[]{ sf };
            }
            if (files == null || files.length == 0) return;

            selectedFiles.clear();
            selectedFiles.addAll(Arrays.asList(files));

            String firstExt = getExtension(selectedFiles.get(0).getName());

            // If input is PDF: dynamically switch output options to Images!
            if (firstExt.equals("pdf")) {
                formatDropdown.removeAllItems();
                formatDropdown.addItem("PNG \u2014 Lossless Image (.png)");
                formatDropdown.addItem("JPG \u2014 Compressed Image (.jpg)");
                formatDropdown.addItem("BMP \u2014 Raw Bitmap (.bmp)");
                formatDropdown.setSelectedIndex(0);
                mergeCheck.setEnabled(false);
            } else {
                // Input is Image: options are Document (PDF) + Images
                formatDropdown.removeAllItems();
                formatDropdown.addItem("PDF \u2014 Native Printable Document (.pdf)");
                formatDropdown.addItem("JPG \u2014 JPEG Compressed Image (.jpg)");
                formatDropdown.addItem("PNG \u2014 PNG Lossless Image (.png)");
                formatDropdown.addItem("BMP \u2014 BMP Bitmap (.bmp)");
                formatDropdown.setSelectedIndex(0);
                mergeCheck.setEnabled(true);
            }

            if (selectedFiles.size() == 1) {
                File f = selectedFiles.get(0);
                fileDisplayField.setText(f.getAbsolutePath());
                log("Selected file: " + f.getName() + " (" + f.length() + " bytes)");

                // Unit 1 : StringTokenizer parsing
                String meta = f.getName() + "#" + firstExt + "#" + f.length();
                String[] parsed = MetadataParser.parseMetadata(meta);
                log("  Parsed Metadata \u2192 Name=" + parsed[0] + " | Format=" + parsed[1] + " | Size=" + parsed[2]);
                log("  Filename Vowels: " + MetadataParser.countVowels(f.getName()));
            } else {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < selectedFiles.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(selectedFiles.get(i).getName());
                }
                fileDisplayField.setText("[" + selectedFiles.size() + " files selected] " + sb.toString());
                log("Selected " + selectedFiles.size() + " files for batch transformation.");
            }

            // Populate the Asset Analyzer Tab immediately & activate it!
            updateImageSummaryTab();
            tabPane.setSelectedIndex(0);
            updateActionButtonsOnFormatChange();
            log("");
        }
    }

    private void updateActionButtonsOnFormatChange() {
        String rawFmt = (String) formatDropdown.getSelectedItem();
        if (rawFmt == null) return;
        String tgt = rawFmt.substring(0, 3).toLowerCase().trim();

        if (tgt.equals("pdf")) {
            openOutputBtn.setText("\uD83C\uDF10 View PDF in Browser (.pdf)");
            downloadBtn.setText("\u2B07 Download / Save PDF\u2026");
        } else {
            openOutputBtn.setText("\uD83D\uDDBC Open Converted Image");
            downloadBtn.setText("\u2B07 Download / Save Image\u2026");
        }
    }

    /* ── Populate Image & PDF Analyzer Engine Tab ────────────────────────── */
    private void updateImageSummaryTab() {
        summaryModel.setRowCount(0);
        int idx = 1;

        for (File f : selectedFiles) {
            try {
                AnalyzerEngine.AnalysisResult res = AnalyzerEngine.analyzeAsset(f);
                String scaleOrPages = "-";
                if (res.technicalDetails.containsKey("Pixel Dimensions")) {
                    scaleOrPages = res.technicalDetails.get("Pixel Dimensions");
                } else if (res.technicalDetails.containsKey("Total Page Count")) {
                    scaleOrPages = res.technicalDetails.get("Total Page Count") + " pages";
                }

                summaryModel.addRow(new Object[]{
                    idx++,
                    f.getName(),
                    res.fileType,
                    res.fileSizeFormatted,
                    scaleOrPages,
                    res.whatItIsAbout
                });
            } catch (Exception ex) {
                summaryModel.addRow(new Object[]{
                    idx++, f.getName(), "Asset", formatBytes(f.length()), "-", "Unprocessed asset"
                });
            }
        }

        if (summaryTable.getRowCount() > 0) {
            summaryTable.setRowSelectionInterval(0, 0);
        }
    }

    private void onSummarySelectionChanged() {
        int row = summaryTable.getSelectedRow();
        if (row >= 0 && row < selectedFiles.size()) {
            File f = selectedFiles.get(row);
            try {
                AnalyzerEngine.AnalysisResult res = AnalyzerEngine.analyzeAsset(f);

                if (res.thumbnail != null) {
                    Image sc = res.thumbnail.getScaledInstance(140, 140, Image.SCALE_SMOOTH);
                    summaryThumbLabel.setIcon(new ImageIcon(sc));
                    summaryThumbLabel.setText("");
                } else {
                    summaryThumbLabel.setIcon(null);
                    summaryThumbLabel.setText("Asset Preview");
                }

                StringBuilder html = new StringBuilder();
                html.append("<html><body style='font-family:Segoe UI, sans-serif; color:#F0F3FA; background-color:#1F2637; padding:8px;'>");

                // Header Banner
                html.append("<div style='background-color:#161B26; padding:8px 12px; border-left:4px solid #00B4A0; margin-bottom:10px;'>")
                    .append("<span style='font-size:14px; font-weight:bold; color:#00B4A0;'>\uD83D\uDD0D ").append(res.whatItIsAbout).append("</span><br>")
                    .append("<span style='font-size:11px; color:#969EAF;'>File: <b>").append(res.fileName).append("</b> &bull; ")
                    .append(res.fileType).append(" &bull; ").append(res.fileSizeFormatted).append("</span>")
                    .append("</div>");

                // SECTION 1: DETAILED CONTENT SUMMARIZER ("What It Has")
                html.append("<div style='margin-bottom:12px;'>")
                    .append("<div style='font-size:13px; font-weight:bold; color:#E6A51E; border-bottom:1px solid #373F55; padding-bottom:3px; margin-bottom:6px;'>")
                    .append("\uD83D\uDCCB Content Summarizer: What This Asset Contains & Depicts</div>")
                    .append("<div style='background-color:#262C3C; padding:8px 10px; margin-bottom:8px;'>")
                    .append("<i style='color:#FFFFFF;'>").append(res.executiveSummary != null ? res.executiveSummary : res.simpleSummary).append("</i>")
                    .append("</div>")
                    .append("<div style='font-weight:bold; color:#00B4A0; font-size:11px; margin-bottom:4px;'>KEY CONTENT ELEMENTS (WHAT IT HAS):</div>")
                    .append("<ul style='margin-top:2px; margin-bottom:6px; padding-left:18px;'>");

                for (String item : res.whatItHasList) {
                    html.append("<li style='margin-bottom:3px;'>").append(item).append("</li>");
                }
                html.append("</ul>");

                // If PDF: Discovered Headings & Top Keywords
                if (res.discoveredHeadings != null && !res.discoveredHeadings.isEmpty()) {
                    html.append("<div style='margin-top:6px; font-size:11px;'><b>Discovered Sections:</b> <span style='color:#E6A51E;'>")
                        .append(String.join(" &bull; ", res.discoveredHeadings))
                        .append("</span></div>");
                }
                if (res.topKeywords != null && !res.topKeywords.isEmpty()) {
                    html.append("<div style='margin-top:4px; font-size:11px;'><b>Core Keywords:</b> <span style='color:#2EAF4B;'>")
                        .append(String.join(", ", res.topKeywords))
                        .append("</span></div>");
                }

                // If Image: Dominant Color Swatches
                if (res.dominantColors != null && !res.dominantColors.isEmpty()) {
                    html.append("<div style='margin-top:6px; font-size:11px;'><b>Dominant Palette:</b> ");
                    for (String hex : res.dominantColors) {
                        html.append("<span style='background-color:").append(hex).append("; color:#FFFFFF; padding:1px 6px; font-family:Consolas; border:1px solid #555; margin-right:4px;'>")
                            .append(hex).append("</span>");
                    }
                    html.append("</div>");
                }
                html.append("</div>");

                // SECTION 2: DETAILED ASSET ANALYZER (Technical Metrics & Structure)
                html.append("<div>")
                    .append("<div style='font-size:13px; font-weight:bold; color:#00B4A0; border-bottom:1px solid #373F55; padding-bottom:3px; margin-bottom:6px;'>")
                    .append("\uD83D\uDD2C Technical Asset Diagnostics: Deep Structural Metrics</div>")
                    .append("<table cellpadding='3' cellspacing='0' style='width:100%; font-size:11px; border-collapse:collapse;'>");

                int rowCount = 0;
                for (Map.Entry<String, String> entry : res.technicalDetails.entrySet()) {
                    String rowBg = (rowCount++ % 2 == 0) ? "#262C3C" : "#1F2637";
                    html.append("<tr style='background-color:").append(rowBg).append(";'>")
                        .append("<td style='color:#969EAF; width:45%; padding:4px 6px;'><b>").append(entry.getKey()).append("</b></td>")
                        .append("<td style='color:#FFFFFF; padding:4px 6px;'>").append(entry.getValue()).append("</td>")
                        .append("</tr>");
                }
                html.append("</table></div>");

                html.append("</body></html>");
                summaryDetailsPane.setText(html.toString());
                summaryDetailsPane.setCaretPosition(0);

            } catch (Exception ex) {
                summaryThumbLabel.setIcon(null);
                summaryThumbLabel.setText("Error");
                summaryDetailsPane.setText("<html><body style='color:#FF6B6B; background-color:#1F2637; padding:10px;'><h3>Analysis Error</h3><p>" + ex.getMessage() + "</p></body></html>");
            }
        }
    }

    /* ═══════════════════════════════════════════════════════════════════════
     *  CONVERSION EXECUTION & OUTPUT SEPARATION
     * ═══════════════════════════════════════════════════════════════════════ */
    private void startConversion() {
        if (selectedFiles.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select one or more files first.",
                    "No Files Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String rawFmt = (String) formatDropdown.getSelectedItem();
        String targetFmt = rawFmt.substring(0, 3).toLowerCase().trim();
        this.lastTargetFmt = targetFmt;
        boolean merge = mergeCheck.isSelected();
        File outputDir = selectedFiles.get(0).getParentFile();

        convertBtn.setEnabled(false);
        browseBtn.setEnabled(false);
        openOutputBtn.setEnabled(false);
        downloadBtn.setEnabled(false);
        openFolderBtn.setEnabled(false);
        progressBar.setValue(0);
        statusMessageLabel.setText("Executing conversion pipeline...");

        log("============================================================");
        log("[" + timeSupplier.get() + "] STARTING: " + selectedFiles.size()
            + " file(s) \u2192 Target: " + targetFmt.toUpperCase());

        /* ── Unit 3 / Lab 7–9 : Background Worker Thread (Runnable) ── */
        ImageConverter converter = new ImageConverter(
            selectedFiles, targetFmt, outputDir, merge,
            new ImageConverter.ConversionCallback() {
                @Override
                public void onProgress(int percent, String message) {
                    progressBar.setValue(percent);
                    progressBar.setString(percent + "% \u2014 " + message);
                    statusMessageLabel.setText(message);
                    log("[" + timeSupplier.get() + "] " + message);
                }

                @Override
                public void onComplete(ConversionRecord record) {
                    convertBtn.setEnabled(true);
                    browseBtn.setEnabled(true);
                    lastOutputFile = record.getOutputFile();

                    history.add(record);
                    formatCounts.merge(record.getTargetFormat(), 1, Integer::sum);

                    // Enable Actions
                    openOutputBtn.setEnabled(true);
                    downloadBtn.setEnabled(true);
                    openFolderBtn.setEnabled(true);

                    // Update UI labels clearly distinguishing DOC vs PDF vs Image!
                    updateActionButtonsOnFormatChange();

                    if (lastOutputFile != null && lastOutputFile.exists()) {
                        String typeBadge = targetFmt.equals("doc") ? "Word Document (.doc)"
                                         : targetFmt.equals("pdf") ? "Printable PDF (.pdf)"
                                         : "Image (" + targetFmt.toUpperCase() + ")";
                        resultLabel.setText("<html><font color='#2EAF4B'>\u2714 SUCCESS:</font> "
                                + typeBadge + " \u2192 <b>" + lastOutputFile.getName()
                                + "</b> (" + lastOutputFile.length() + " bytes)</html>");
                        log("\u2714 OUTPUT CREATED: " + lastOutputFile.getAbsolutePath());
                    }

                    // Unit 1 : StringBuffer Status Card
                    log(MetadataParser.buildStatusCard(
                            record.getFileName(), record.getSourceFormat(),
                            record.getTargetFormat(), record.getEfficiencyScore()));

                    // Unit 2 : Multiple Interfaces
                    try {
                        log("[TRANSFORM] " + record.transform());
                    } catch (Exception ignored) { }
                    auditPrinter.accept(record);

                    // Unit 5 / Lab 16 : Persist to JDBC Database
                    try {
                        database.insertRecord(record);
                        log("[JDBC] Record stored in database.");
                        refreshDbView();
                    } catch (SQLException ex) {
                        log("[JDBC ERROR] " + ex.getMessage());
                    }

                    // Unit 4 : Predicate check
                    if (highEfficiency.test(record)) {
                        log("\u2605 [HIGH EFFICIENCY] Score exceeds 70% threshold!\n");
                    }

                    statusMessageLabel.setText("Conversion Completed Successfully!");
                    promptAfterConversion();
                }

                @Override
                public void onError(String message) {
                    convertBtn.setEnabled(true);
                    browseBtn.setEnabled(true);
                    progressBar.setValue(0);
                    progressBar.setString("Error");
                    statusMessageLabel.setText("Failed: " + message);
                    log("[ERROR] " + message + "\n");
                    JOptionPane.showMessageDialog(ConverterFrame.this,
                            message, "Conversion Error", JOptionPane.ERROR_MESSAGE);
                }
            });

        Thread worker = new Thread(converter, "OmniFormat-Worker");
        worker.start();
    }

    private void promptAfterConversion() {
        if (lastOutputFile == null || !lastOutputFile.exists()) return;

        String actionTitle = lastTargetFmt.equals("pdf") ? "\uD83C\uDF10 View PDF in Browser"
                           : "\uD83D\uDDBC Open Image";

        String[] options = {actionTitle, "\u2B07 Download / Save As\u2026", "Done"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Conversion Complete!\n\nTarget Format: " + lastTargetFmt.toUpperCase()
                + "\nOutput File: " + lastOutputFile.getName() + " (" + lastOutputFile.length() + " bytes)\n"
                + "Saved at: " + lastOutputFile.getAbsolutePath() + "\n\n"
                + "Would you like to open the file now?",
                "Conversion Successful",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            openOutputFileTarget(lastOutputFile);
        } else if (choice == 1) {
            downloadOutputFile();
        }
    }

    /* ═══════════════════════════════════════════════════════════════════════
     *  CRITICAL: TARGET-SPECIFIC OPENING (PDF vs IMAGE)
     * ═══════════════════════════════════════════════════════════════════════ */
    private void openOutputFileTarget(File file) {
        if (file == null || !file.exists()) {
            JOptionPane.showMessageDialog(this, "No file available to open.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = file.getName().toLowerCase();

        // 1. IF IT IS A PDF: Open in Edge or Chrome's native PDF viewing engine!
        if (name.endsWith(".pdf")) {
            String[] pdfBrowsers = {
                "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe",
                "C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe",
                "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
                "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe",
                "msedge.exe",
                "chrome.exe"
            };
            for (String b : pdfBrowsers) {
                try {
                    File exe = new File(b);
                    if (exe.exists() || b.endsWith(".exe")) {
                        new ProcessBuilder(b, file.getAbsolutePath()).start();
                        log("[VIEWER] Opened PDF in browser engine: " + file.getName());
                        return;
                    }
                } catch (Exception ignored) { }
            }
        }

        // 2. IF IT IS AN IMAGE: Open via system default viewer / Desktop
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(file);
                log("[VIEWER] Opened image via system default: " + file.getName());
                return;
            }
        } catch (Exception ex) {
            log("[VIEWER ERROR] " + ex.getMessage());
        }

        // 3. Fallback: System default application via Desktop.open
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(file);
                log("[VIEWER] Opened file via system default: " + file.getName());
            }
        } catch (Exception ex) {
            log("[VIEWER ERROR] " + ex.getMessage());
        }
    }

    private void openAcademicReport() {
        File rpt = new File("PROJECT_REPORT.pdf");
        if (!rpt.exists()) {
            try {
                omni.engine.ReportGenerator.generateReportPdf("PROJECT_REPORT.pdf");
                log("[REPORT] Generated academic technical report: PROJECT_REPORT.pdf");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not generate report: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        openOutputFileTarget(rpt);
    }

    private void downloadOutputFile() {
        if (lastOutputFile == null || !lastOutputFile.exists()) {
            JOptionPane.showMessageDialog(this, "No converted file to download.", "Download", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Download / Save Converted File As");

        File downloads = new File(System.getProperty("user.home"), "Downloads");
        if (downloads.exists()) fc.setCurrentDirectory(downloads);
        fc.setSelectedFile(new File(fc.getCurrentDirectory(), lastOutputFile.getName()));

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File target = fc.getSelectedFile();
            String ext = getExtension(lastOutputFile.getName());
            if (!target.getName().toLowerCase().endsWith("." + ext)) {
                target = new File(target.getParentFile(), target.getName() + "." + ext);
            }

            try {
                Files.copy(lastOutputFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                log("\u2714 [DOWNLOAD] Successfully saved to: " + target.getAbsolutePath());

                int openNow = JOptionPane.showConfirmDialog(
                        this,
                        "File downloaded successfully to:\n" + target.getAbsolutePath()
                        + "\n\nWould you like to open it now?",
                        "Download Complete",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);

                if (openNow == JOptionPane.YES_OPTION) {
                    openOutputFileTarget(target);
                }
            } catch (Exception ex) {
                log("[DOWNLOAD ERROR] " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Failed to save file:\n" + ex.getMessage(), "Download Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openContainingFolder() {
        if (lastOutputFile == null || !lastOutputFile.exists()) return;
        try {
            File folder = lastOutputFile.getParentFile();
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(folder);
                log("[FOLDER] Opened directory: " + folder.getAbsolutePath());
            }
        } catch (Exception ex) {
            log("[FOLDER ERROR] " + ex.getMessage());
        }
    }

    /* ── Unit 5 / Lab 16 : Database Actions ────────────────────────────── */
    private void queryHighEfficiencyDb() {
        try {
            populateTable(database.readHighEfficiency());
            log("[JDBC] SELECT * FROM conversion_logs WHERE efficiency_score > 70.0");
        } catch (SQLException ex) {
            log("[JDBC ERROR] " + ex.getMessage());
        }
    }

    private void queryAllDb() {
        try {
            populateTable(database.readAll());
            log("[JDBC] SELECT * FROM conversion_logs");
        } catch (SQLException ex) {
            log("[JDBC ERROR] " + ex.getMessage());
        }
    }

    private void clearDatabase() {
        int confirm = JOptionPane.showConfirmDialog(this, "Clear all audit records from database?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int count = database.deleteAll();
                dbModel.setRowCount(0);
                dbRowCountLabel.setText("Records: 0");
                log("[JDBC] DELETE FROM conversion_logs \u2192 Removed " + count + " rows.");
            } catch (SQLException ex) {
                log("[JDBC ERROR] " + ex.getMessage());
            }
        }
    }

    private void refreshDbView() {
        try {
            populateTable(database.readAll());
        } catch (SQLException ignored) { }
    }

    private void populateTable(List<ConversionRecord> list) {
        dbModel.setRowCount(0);
        int id = 1;
        for (ConversionRecord r : list) {
            dbModel.addRow(new Object[]{
                id++,
                r.getFileName(),
                r.getFileSizeBytes(),
                r.getSourceFormat(),
                r.getTargetFormat(),
                String.format(Locale.US, "%.1f", r.getEfficiencyScore()),
                r.getRemarks(),
                r.getTimestamp()
            });
        }
        dbRowCountLabel.setText("Records: " + list.size());
    }

    /* ═══════════════════════════════════════════════════════════════════════
     *  HELPERS
     * ═══════════════════════════════════════════════════════════════════════ */
    private void log(String msg) {
        if (logArea != null) {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }
    }

    private JPanel createCardPanel(String title) {
        JPanel p = new JPanel();
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        new LineBorder(BORDER_CLR, 1), title, TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 12), ACCENT_TEAL),
                new EmptyBorder(8, 10, 10, 10)));
        return p;
    }

    private JButton createBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { if (btn.isEnabled()) btn.setBackground(bg.brighter()); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        return btn;
    }

    private void styleTable(JTable table) {
        table.setBackground(BG_INPUT);
        table.setForeground(FG_TEXT);
        table.setGridColor(BORDER_CLR);
        table.setSelectionBackground(ACCENT_TEAL);
        table.setSelectionForeground(Color.BLACK);
        table.setFont(new Font("Consolas", Font.PLAIN, 11));
        table.setRowHeight(22);
        table.getTableHeader().setBackground(BG_CARD);
        table.getTableHeader().setForeground(ACCENT_TEAL);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
    }

    private static String getExtension(String name) {
        int dot = name.lastIndexOf('.');
        return (dot > 0) ? name.substring(dot + 1).toLowerCase() : "bin";
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format(Locale.US, "%.1f %cB", bytes / Math.pow(1024, exp), pre);
    }

    /* ═══════════════════════════════════════════════════════════════════════
     *  Main Entry Point
     * ═══════════════════════════════════════════════════════════════════════ */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        SwingUtilities.invokeLater(() -> {
            try {
                ConverterFrame frame = new ConverterFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to launch OmniFormat:\n" + e.getMessage(), "Launch Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
