# SDG-1 Microfinance Loan Tracker

**Student Name:** Tiew Ying Zen  
**Student ID:** 24007908  
**Course Code:** PRG2104 - Object-Oriented Programming  
**Academic Session:** August 2026 Semester  
**Policy Tier:** Tier C — AI-Integrated (Individual Project)  

---

## 📌 Project Overview

The **SDG-1 Microfinance Loan Tracker** is a desktop application designed to support community micro-lenders, NGOs, and field workers operating in low-income regions. The application directly aligns with **UN Sustainable Development Goal 1 (No Poverty)** by streamlining the registration of borrowers, the disbursement of micro-loans, the scheduling and collection of repayments, and the automated assessment of borrower default risk.

---

## ⚙️ Development & Build Specifications

| Component | Specification |
|---|---|
| **Language** | Scala 3.8.4 (Scala 3.3+ compatible) |
| **GUI Framework** | ScalaFX 21.0.0-R32 (JavaFX 21) |
| **Build Tool** | sbt 1.9.8 |
| **JVM Requirement** | Java 21 (LTS) |
| **Persistence** | CSV Flat File Serialization (`scala.util.Try` wrapped) |

---

## 🚀 Setup & Execution Instructions

Follow these steps to run the application on a clean development environment:

### Prerequisites
1. Ensure **Java 21 JDK** is installed and configured in your environment path:
   ```bash
   java -version
   ```
2. Ensure **sbt** (Simple Build Tool) 1.9+ is installed:
   ```bash
   sbt -version
   ```

### Running the Application
1. Clone or extract the submission folder into your workspace.
2. Open a terminal in the root project directory (`Project_24007908`).
3. Compile the source code with strict warning flags (`-Wunused`):
   ```bash
   sbt -Wunused clean compile
   ```
4. Launch the ScalaFX Graphical User Interface:
   ```bash
   sbt run
   ```

---

## 🛠️ System Features (4 Core Features)

1. **Borrower Registration & Management (`BorrowerScreen`)**
   - Register community borrowers with income, dependents, and contact details.
   - Live validation enforcing non-empty fields and positive income values.
   - Interactive `TableView` display for registered community members.

2. **Loan Disbursal & Dynamic Rate Calculation (`LoanScreen`)**
   - Disburse `StandardMicroLoan` (interest-bearing based on APR) or `EmergencyInterestFreeLoan` (0% interest with flat admin fee).
   - Dynamic GUI inputs that adjust controls based on the selected loan type.
   - Real-time total repayment calculation using subtype polymorphism.

3. **Repayment Logging & Balance Tracking (`RepaymentScreen`)**
   - Record partial or full repayments against disbursed loans.
   - Auto-calculates and updates remaining balance, preventing overpayments.
   - Audit log of past financial transactions.

4. **Risk Assessment Dashboard & Report Exporter (`DashboardScreen`)**
   - Aggregated financial overview (total lent, total collected, net outstanding balance).
   - Debt-to-Income (DTI) risk classification (Low Risk < 30%, Moderate Risk 30-50%, High Risk > 50%).
   - Export formatted summary report to local disk storage (`risk_report.txt`).

---

## 🤖 AI Usage Summary

This project was developed under **Tier C (AI-Integrated)** guidelines.
- **AI Tools Used:** Cursor, Claude 3.7 Sonnet, ChatGPT (GPT-4o).
- **Log Location:** `ai/interaction_log.md` (20 detailed interaction entries).
- **Declaration:** `ai/declaration.md` (Signed academic honesty declaration).
- **In-Code Tagging:** `// ai-assisted: #N` blocks paired with `// why: <reason>` explanations across UI components.
- **AI Reflection:** `docs/ai_reflection.md` (300-500 word critique of AI utility, hallucinations, and manual design decisions).

---

## 📄 License & Third-Party Citations

All third-party libraries, frameworks, and resource references are listed in `docs/citations.md`.
