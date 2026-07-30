# Development Log — SDG-1 Microfinance Loan Tracker

This log tracks the complete chronological development progress of the ScalaFX SDG-1 Microfinance Loan Tracker application.

---

## 2026-07-21
* **Activity:** Domain Research & SDG-1 Alignment.
* **Progress:**
  * Researched UN Sustainable Development Goal 1 (No Poverty) sub-domains, focusing on financial inclusion tools for micro-lenders.
  * Identified operational bottlenecks in NGO microfinance management: tracking borrower debt-to-income (DTI) ratios, calculating APR interest vs flat admin fees, and preventing over-repayments.
  * Formulated requirements for 4 core system features: Borrower Management, Loan Disbursal, Repayment Logging, and Risk Analytics.

## 2026-07-22
* **Activity:** Object-Oriented Architecture & UML Modeling.
* **Progress:**
  * Drafted class hierarchy mapping `Borrower`, `Loan`, and `Repayment` domain models.
  * Designed subtype polymorphism structure: `sealed trait Loan` extended by `StandardMicroLoan` (APR interest) and `EmergencyInterestFreeLoan` (flat admin fee).
  * Designed parametric polymorphism persistence pattern: `CSVSerializer[T]` generic trait and `CSVDataStore[T]` controller.
  * Sketched initial UI wireframes for a 4-pane navigation system using a persistent sidebar menu.

## 2026-07-23
* **Activity:** Workspace Initialization & sbt Build Configuration.
* **Progress:**
  * Initialized sbt build definition (`build.sbt`) referencing Scala 3.3+ and ScalaFX 21.0.0-R32.
  * Set up strict compiler flags: `-release 21`, `-Wunused:all`, and `-Wconf` for warning-free builds.
  * Standardized directory structure adhering strictly to Page 5 submission rules (`src/main/scala/`, `src/main/resources/`, `docs/`, `ai/`).

## 2026-07-24
* **Activity:** Core Domain Model Implementation.
* **Progress:**
  * Implemented immutable case class `Borrower.scala` with field validation helpers (non-empty strings, positive income).
  * Implemented `Loan.scala` defining `sealed trait Loan` and concrete subclasses `StandardMicroLoan` and `EmergencyInterestFreeLoan`.
  * Implemented `Repayment.scala` for transaction logging and `LoanStatus` enum (`Active`, `Paid`, `Defaulted`).
  * Enforced strict immutability: zero `var` declarations or mutable collections in domain packages.

## 2026-07-25
* **Activity:** Generic Persistence Layer & Exception Safety.
* **Progress:**
  * Implemented generic trait `CSVSerializer[T]` with Scala 3 `given` context instances for serializing domain models.
  * Implemented `CSVDataStore[T]` class with `private` file path fields to satisfy encapsulation requirements (`S1-10`).
  * Secured all file I/O operations and string-to-number parsing using `scala.util.Try` to eliminate unhandled runtime exceptions (`S1-12`).

## 2026-07-26
* **Activity:** Base GUI Architecture & Component Design System.
* **Progress:**
  * Created `AppScreen.scala` abstract base trait to provide uniform styling tokens (colors, font hierarchy, padding ≥ 8px).
  * Implemented form validation alert helpers returning `Option[Alert]` to prevent type ambiguity compiler errors.
  * Scaffolding layout hierarchy using ScalaFX layout containers (`VBox`, `HBox`, `GridPane`, `BorderPane`).

## 2026-07-27
* **Activity:** Interactive Feature Screens & Reactive Bindings.
* **Progress:**
  * Implemented `BorrowerScreen.scala` with `TableView` listing registered clients and keyboard form submission.
  * Implemented `LoanScreen.scala` featuring dynamic property visibility bindings that toggle interest rate vs admin fee fields based on loan selection.
  * Implemented `RepaymentScreen.scala` with real-time balance calculations preventing overpayment entries.
  * Converted all GUI event handlers to the idiomatic ScalaFX syntax `onAction = handle { ... }`.

## 2026-07-28
* **Activity:** Risk Analytics Dashboard & Report Exporter.
* **Progress:**
  * Implemented `DashboardScreen.scala` aggregating total disbursals, repayment metrics, and DTI risk classifications (Low, Moderate, High).
  * Added formatted risk report exporter (`risk_report.txt`) with `scala.util.Try` exception handling.
  * Wired full navigation and stage close auto-save handlers in `MicrofinanceApp.scala`.

## 2026-07-29
* **Activity:** Edge Case Validation & Input Sanitization.
* **Progress:**
  * Added validation checks preventing negative disbursal amounts or zero income entries.
  * Verified robust alert popups for invalid string inputs across all input fields without unhandled exceptions (`S1-18`).
  * Tested empty string parsing fallback mechanisms in `CSVSerializer[T]`.

## 2026-07-30
* **Activity:** Demonstration Video Walkthrough & Keyboard Navigation.
* **Progress:**
  * Recorded feature demonstration video (`docs/demo.mp4`) showcasing end-to-end operation of all 4 system features (Borrowers, Loans, Repayments, Dashboard).
  * Audited GUI accessibility to ensure Tab key navigation moves sequentially through form fields (`S2-10`).
  * Wired default Enter key submission triggers on primary action buttons across all 4 screens.
  * Adjusted container margins and grid cell paddings to maintain consistent ≥ 8px section spacing (`S2-9`).

## 2026-07-31
* **Activity:** AI Prompt Tagging & Reflection Documentation.
* **Progress:**
  * Added `// ai-assisted: #N` blocks paired with sibling `// why: <reason>` comments across UI source code (`S4-5`, `S4-6`).
  * Completed Template 2 (`ai/interaction_log.md`) with 20 entries capturing iterative prompting and domain context.
  * Signed Template 3 (`ai/declaration.md`) and completed personal reflection (`docs/reflection.md`) and AI reflection (`docs/ai_reflection.md`).

## 2026-08-01
* **Activity:** Viva Voce Walkthrough & Code Defense Practice.
* **Progress:**
  * Practiced 3-minute rapid viva walkthrough: 60s class explanation, 60s `// ai-assisted` line-by-line walk, and 60s design weakness defense.

## 2026-08-02
* **Activity:** Final Submission Verification & Packaging.
* **Progress:**
  * Verified clean compilation with `sbt --no-server clean compile` yielding 0 warnings under Java 21 release target.
  * Conducted final structure audit matching Page 5 requirements and signed `submission_manifest.md`.
  * Verified ZIP bundle ready for Elearn submission.
