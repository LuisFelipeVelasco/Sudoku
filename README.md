# 🎲 6×6 Sudoku Game — JavaFX

> **A fully playable 6×6 Sudoku puzzle built with JavaFX and pure Java — featuring real-time input validation, a backtracking board generator, a one-character input formatter, and a clue system, all wired together through a clean MVC architecture.**

---
https://github.com/user-attachments/assets/1dde1009-0008-4d2d-be39-3464d8360f8d

## 🎯 Purpose

Classic Sudoku engines are a rich ground for applying core OOP patterns: how do you separate game logic from presentation, enforce constraints in real time without coupling the model to the UI, and design a lifecycle that a framework's loader can drive? This project tackles those questions in a 6×6 variant:

- 🧩 **Board Generation** (`SudokuGame.java`) — Given a blank 6×6 grid, produce a fully solved, randomized puzzle using recursive backtracking, then select an initial set of revealed cells as starting clues.
- 🖼️ **Interactive UI** (`MainMenuController.java` + `main-menu-view.fxml`) — Render the board as a grid of editable `TextField` nodes, limit each cell to one character using a `TextFormatter`, validate every text change in real time, and give the player live feedback on row, column, and sub-block conflicts.

Both components share the same lifecycle contract: they implement the `SudokuInitializable` interface, which enforces a single `initialize()` entry point for setup.

---

## 🗂️ Project Structure

```text
demo/
│
├── src/main/java/com/examplez/demo/
│   ├── Launcher.java                          # 🚀 Application entry point
│   ├── GameLauncher.java                      # 🪟 JavaFX Application subclass
│   ├── SudokuInitializable.java               # 📐 Shared lifecycle interface
│   │
│   ├── controllers/
│   │   └── MainMenuController.java            # 🎮 FXML controller — UI & validation
│   │
│   └── models/
│       └── SudokuGame.java                    # 🧠 Game logic — generation & rules
│
├── src/main/resources/com/examplez/demo/
│   └── main-menu-view.fxml                    # 🖌️ Board layout and controls
│
└── pom.xml                                    # 🔧 Maven build — JavaFX 21, Java 17
```

---

## 🧠 Architecture — MVC Design

### What It Does

The project follows a strict Model–View–Controller split enforced by JavaFX's FXML loader:

1. **Generates a valid 6×6 board** by running randomized backtracking inside `SudokuGame.initialize()`.
2. **Selects starting clues** — one random cell per 2×3 sub-block — and marks them as locked in the `confirmedCells` mask.
3. **Renders the board** in `MainMenuController`, populating the `ArrayList<ArrayList<TextField>>` grid from the model and disabling locked cells.
4. **Configures board input once** by assigning a `TextFormatter` to every `TextField` to limit the input to one character, and by attaching a `textProperty` listener that validates each text change in real time.
5. **Validates input live** by checking row, column, and sub-block constraints whenever the player types, then highlights conflicts immediately.
6. **Dispenses clues on demand** — the Clue button asks the model for the first empty cell, reveals its correct value, and re-checks surrounding cells for newly created conflicts.
7. **Detects completion** by querying `SudokuGame.isTheSudokuCompleted()` after every valid entry.

### 🧩 Algorithms & Design Decisions

#### Board Generation — Randomized Backtracking

| Step | Description |
|---|---|
| 1 | Start at cell (0, 0) with the board pre-filled with zeros |
| 2 | Shuffle `{1, 2, 3, 4, 5, 6}` to randomize candidate order |
| 3 | Try each candidate: if it passes row, column, and sub-block constraints, place it |
| 4 | Recurse into the next cell, wrapping to the next row at column 6 |
| 5 | If no candidate fits, reset the cell to `"0"` and return `false` to trigger backtracking |
| 6 | When row 6 is reached, the board is fully solved — return `true` |

#### Clue Selection — Sub-Block Distribution

| Step | Description |
|---|---|
| 1 | Iterate over the six 2×3 sub-blocks, using top-left corners at rows 0, 2, 4 and columns 0, 3 |
| 2 | Within each sub-block, randomly select one cell using `ThreadLocalRandom` |
| 3 | Mark it `true` in `confirmedCells` — it will be revealed as a starting hint |

#### Real-Time Validation — Three-Plane Conflict Check

| Plane | Method | Description |
|---|---|---|
| Row | `sameNumberInSameRow` | Scans the cell's row for a duplicate of the typed value |
| Column | `sameNumberInSameColumn` | Scans the cell's column for a duplicate |
| Sub-block | `sameNumberInSameBlock` | Scans the enclosing 2×3 block for a duplicate |

If a conflict is found in any plane, `getCoordinatesRepeatedInvalidCells` returns the coordinates of the offending cell, and the controller highlights it.

### Pipeline

```text
User clicks Play
    │
    └──► cleanBoard()
              │
              └──► SudokuGame.initialize()
                        │
                        ├──► solve(0, 0) — Randomized backtracking → Fully solved board
                        └──► chooseCluesToShow() → confirmedCells mask (one per sub-block)
                                  │
                                  └──► MainMenuController.showBoard()
                                            │
                                            ├──► Locked cells: setText + setDisable(true)
                                            └──► Editable cells: clear/set editable state + setDisable(false)
                                                      │
                                                      └──► MainMenuController.configureTextFields()
                                                                │
                                                                ├──► First call only:
                                                                │       ├──► Attach TextFormatter to limit input to one character
                                                                │       └──► Attach textProperty listener for live validation
                                                                │
                                                                └──► On each text change:
                                                                          ├──► Empty? → clear highlights, re-validate neighbors
                                                                          ├──► Not 1–6? → show format error, reject/restore invalid input
                                                                          └──► Valid digit? → check row / column / block
                                                                                    ├──► Conflict found → highlight offenders
                                                                                    ├──► No conflict → check board completion
                                                                                    └──► Completed → show win message
```

---

## 🎮 Features

### Clue System

When the player clicks **Clue**, the controller calls `SudokuGame.giveClue()`, which scans `confirmedCells` to find the first unconfirmed empty cell, reveals its correct value, and disables it. The clue cap sits at 35 confirmed cells out of 36; attempting to exceed it displays a warning message. After each clue is revealed, nearby cells are re-validated so any conflicts it creates are surfaced immediately.

### Style Preservation

Each `TextField`'s original CSS style string is cached in `cellsStyle.get(row).get(col)` during `initialize()`. This snapshot is restored before any highlight is applied, preventing color strings from accumulating across successive validation passes.

### Lifecycle Guard (`firstGame`)

Input configuration is performed only once through `configureTextFields()`, guarded by the `firstGame` flag. On the first Play click, each board `TextField` receives:

- a `TextFormatter`, which prevents the field from containing more than one character;
- a `textProperty` listener, which calls the validation logic whenever the field value changes.

This guard prevents duplicate listeners from stacking across multiple Play clicks within the same controller session. Without it, each new game could register additional listeners on the same nodes, causing `verification(...)` to run multiple times for a single keystroke.

---

## 🛠️ Technologies & Libraries

| Library / Tool | Role |
|---|---|
| `JavaFX 21` (`javafx-controls`, `javafx-fxml`) | UI framework — scene graph, FXML loading, event handling |
| `Java 17` | Language baseline — records, pattern matching, module system |
| `Maven` + `javafx-maven-plugin 0.0.8` | Build tool and JavaFX runner (`mvn clean javafx:run`) |
| `JUnit Jupiter 5.12.1` | Unit testing, test scope |

---

## ⚙️ Setup

### Prerequisites

- Java **17 or higher**
- Maven **3.8+**

### 1. Clone the repository

```bash
git clone https://github.com/LuisFelipeVelasco/Sudoku.git
cd Sudoku
```

### 2. Run the game

```bash
mvn clean javafx:run
```

The game window opens at a fixed **550×560 px** viewport. No additional configuration is required.

### 3. Run tests

```bash
mvn test
```

---

## 📚 Learnings

**MVC with JavaFX FXML**
- The FXML loader instantiates the controller and injects `@FXML` fields before handing control to `initialize()`. This means any setup that references injected nodes must happen there — not in a constructor.
- Separating the model (`SudokuGame`) from the controller (`MainMenuController`) kept the constraint logic testable in isolation and made the UI layer responsible only for presentation and event routing.

**Style Accumulation Bug**
- Applying styles by appending strings, such as `setStyle(getStyle() + newStyle)`, causes styles to stack across calls, producing unpredictable results.
- Caching the original style string at init time and restoring it before each highlight is the correct pattern.

**TextFormatter and Listener Lifecycle**
- The `TextFormatter` controls what the user is allowed to type before the text is accepted by the `TextField`. In this project, it limits every cell to a maximum length of one character.
- The `textProperty` listener reacts after the text changes and routes the new value, old value, and source cell to the validation logic.
- Both are configured inside `configureTextFields()` and guarded by `firstGame`, so they are attached only once during the lifetime of the controller.
- This is important because listeners survive `setText()` calls, including programmatic updates triggered by `showBoard()` and `showClue()`. Registering them repeatedly would cause duplicated validation calls and noisy UI updates.

**Backtracking Termination**
- The recursive solver terminates at row index 6, one past the last row, not when the last cell is filled.
- This boundary condition must be explicit; checking `row == size` before any cell access avoids an `IndexOutOfBoundsException`.

**Interface vs Abstract Class**
- Using `SudokuInitializable` as an `interface` rather than an abstract class lets both `SudokuGame`, a pure model, and `MainMenuController`, a JavaFX controller, share the same lifecycle contract without forcing a common superclass.
- This keeps the inheritance hierarchy flat and unambiguous.

**ArrayList vs Array for the Cell Grid**
- Replacing the raw `TextField[][]` and `String[][]` arrays with `ArrayList<ArrayList<TextField>>` and `ArrayList<ArrayList<String>>` aligns the cell grid with the rest of the project's collection types.
- It avoids manual size tracking and makes the grid initialization explicit: each row is built incrementally with `add()` inside `initialize()` before being appended to the outer list.
