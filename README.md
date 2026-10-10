# OmniFormat Studio
### Universal Image, Document & PDF Converter Engine
**Course:** CSE00130 — Core Java & Object-Oriented Engineering  
**Context:** 3rd-Year B.Tech Computer Science Engineering, MIT-WPU  
**Deployment:** 100% Offline Local Desktop Workstation (Zero Third-Party Libraries, Standard JDK 8+)

---

## 1. Project Overview & Unique Engineering Design

Unlike generic cloud web tools (such as iLovePDF) that upload private user data to third-party servers and use generic red web upload buttons, **OmniFormat Studio** is engineered as a **high-precision developer workstation**:

- **Dark Graphite & Slate Workstation UI:** Professional engineering palette (`#0D1117`, `#161B22`, `#30363D`) with high-contrast emerald and cyan telemetry indicators.
- **Embedded Interactive Document Canvas:** An in-app visual page renderer simulating a physical US Letter sheet (drop shadow, header rule, centered image scaling, page flip controls) right inside Swing.
- **Direct Browser / Edge Integration:** Dispatches the converted PDF to Microsoft Edge or Google Chrome's native PDF viewing engine so the printable visual document opens without Notepad association issues.
- **Asset Inspector & Multi-Queue:** Add multiple images (`.png`, `.jpg`, `.bmp`), preview live thumbnails, and inspect file dimensions, byte counts, and vowel counts.
- **Academic Audit Database (JDBC CRUD):** Full embedded SQL database layer tracking conversion records, space saved, and efficiency ratios.

---

## 2. Academic Syllabus & Lab Defense Mapping

| Unit | Labs | Concepts Demonstrated | Implementation File & Method |
| :--- | :--- | :--- | :--- |
| **Unit 1** | Labs 1–4 | Constructor Overloading, StringTokenizer, StringBuffer, Wrapper Classes, Vowel Counting | `omni.core.ConversionRecord` (minimal & full constructors)<br>`omni.engine.MetadataParser.parseMetadata()`<br>`omni.engine.MetadataParser.countVowels()`<br>`omni.engine.MetadataParser.buildStatusCard()` |
| **Unit 2** | Labs 5–6 | Multilevel Inheritance Hierarchy, Multiple Interface Implementation | `FileItem` (Abstract Level 1) &rarr;<br>`ConvertedItem` (Level 2) &rarr;<br>`ConversionRecord` (Leaf Level 3 implements `Transformable`, `Auditable`) |
| **Unit 3** | Labs 7–9 | Custom Exceptions, Defensive Exception Handling (`ArithmeticException`), Background Multithreading (`Runnable`, `Thread.sleep`) | `omni.core.InvalidFormatException`<br>`omni.core.FileConversionException`<br>`omni.engine.ImageConverter.run()` (Worker Thread)<br>`ImageConverter.calculateEfficiency()` (Zero-divide defense) |
| **Unit 4** | Labs 11–15 | Java 8 Lambdas (`Predicate`, `Consumer`, `Supplier`), Modern Collections (`ArrayList`, `HashSet`, `HashMap`) | `omni.ui.ConverterFrame` (`Predicate<ConversionRecord>` for score > 70%, `Consumer` audit printer, `Supplier` timestamp)<br>`omni.engine.FormatValidator` (`HashSet`)<br>`omni.engine.MetadataParser` (`HashMap`) |
| **Unit 5** | Lab 16 | JDBC Persistence Layer with `PreparedStatement`, Full CRUD Operations | `omni.storage.ConversionDB` (`insertRecord` [CREATE], `readHighEfficiency` & `readAll` [READ], `updateRemarks` [UPDATE], `deleteAll` [DELETE]) |

---

## 3. How to Run

### Option A: 1-Click Launch (Recommended)
Double-click **`run.bat`** inside this folder (`C:\Users\vivaa\Desktop\micro\run.bat`).

### Option B: Terminal / PowerShell
```powershell
cd C:\Users\vivaa\Desktop\micro
.\run.bat
```
Or launch directly with Java:
```powershell
java -cp out omni.ui.ConverterFrame
```

### Option C: Recompiling
If you modify any source code in `src/`:
```powershell
.\compile.bat
```

---

## 4. How to Test All Features in 60 Seconds

1. Click **`+ Add Images…`** and select the pre-packaged sample files: `sample1.png`, `sample2.png`, `sample3.png`.
2. Notice the **Asset Inspector** updates with image dimensions, file size, and vowel count.
3. Select **`PDF (Portable Document)`** from the Target Format dropdown.
4. Leave **`[✓] Merge multiple queued images into single document`** checked.
5. Click **`▶ Execute Pipeline Transformation`**:
   - The worker thread will execute without freezing the UI.
   - Progress bar updates through all 4 stages.
   - Once complete, **Tab 1 automatically renders the interactive document preview** with Next/Previous page buttons!
6. Click **`🌐 Open in Edge/Chrome`** to view the PDF in your browser's PDF viewer.
7. Click **`⬇ Export / Save Document As…`** to save a copy directly to your Downloads folder.
8. Switch to **Tab 3: Audit Database** and click **`★ Query High Efficiency (>70%)`** to see the JDBC result set!
