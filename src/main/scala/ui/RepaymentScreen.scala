package ui

import models._
import scalafx.Includes._
import scalafx.scene.control._
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.layout._
import scalafx.geometry.Insets
import scalafx.beans.property.StringProperty
import scalafx.util.StringConverter

class RepaymentScreen extends AppScreen {
  
  // Input fields
  private val repaymentIdField = new TextField { promptText = "Unique Repayment ID" }
  private val amountField = new TextField { promptText = "Amount ($)" }
  private val dateField = new TextField { 
    promptText = "YYYY-MM-DD"
    // Pre-populate with current local date
    text = java.time.LocalDate.now().toString 
  }
  
  // ComboBox for loan selection
  private val loanCombo = new ComboBox[Loan](AppState.loans) {
    promptText = "Select Loan Account"
    prefWidth = 350
    converter = StringConverter.toStringConverter[Loan]((loan: Loan) => 
      if (loan != null) {
        val borrowerOpt = AppState.borrowers.find(b => b.nationalId == loan.borrowerId)
        val borrowerName = borrowerOpt.map(b => b.name).getOrElse("Unknown")
        s"ID: ${loan.loanId} - ${borrowerName} (Principal: $$${loan.principal})"
      } else ""
    )
  }
  
  // Status labels for dynamic information display
  private val borrowerValLabel = new Label("-") { style = "-fx-font-family: 'Segoe UI';" }
  private val totalDueValLabel = new Label("-") { style = "-fx-font-family: 'Segoe UI';" }
  private val paidValLabel = new Label("-") { style = "-fx-font-family: 'Segoe UI';" }
  private val remainingValLabel = new Label("-") { style = "-fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;" }
  
  // Repayments table view
  private val repaymentTable = new TableView[Repayment](AppState.repayments) {
    placeholder = new Label("No repayments recorded.")
    style = "-fx-background-color: white;"
    columns ++= Seq(
      new TableColumn[Repayment, String]("Repayment ID") {
        cellValueFactory = { cellData => new StringProperty(cellData.value.repaymentId) }
        prefWidth = 120
      },
      new TableColumn[Repayment, String]("Loan ID") {
        cellValueFactory = { cellData => new StringProperty(cellData.value.loanId) }
        prefWidth = 100
      },
      new TableColumn[Repayment, String]("Borrower") {
        cellValueFactory = { cellData => 
          val borrowerName = AppState.loans.find(l => l.loanId == cellData.value.loanId).flatMap { l =>
            AppState.borrowers.find(b => b.nationalId == l.borrowerId)
          }.map(_.name).getOrElse("Unknown")
          new StringProperty(borrowerName)
        }
        prefWidth = 150
      },
      new TableColumn[Repayment, String]("Amount Paid") {
        cellValueFactory = { cellData => new StringProperty(s"$$${String.format("%.2f", cellData.value.amountPaid)}") }
        prefWidth = 120
      },
      new TableColumn[Repayment, String]("Date") {
        cellValueFactory = { cellData => new StringProperty(cellData.value.dateString) }
        prefWidth = 120
      }
    )
  }
  
  // Setup selected loan listener to update balances dynamically
  loanCombo.selectionModel().selectedItem.onChange { (_, _, selectedLoan) =>
    if (selectedLoan != null) {
      val borrowerOpt = AppState.borrowers.find(b => b.nationalId == selectedLoan.borrowerId)
      borrowerValLabel.text = borrowerOpt.map(b => s"${b.name} (${b.nationalId})").getOrElse("Unknown")
      
      val totalDue = selectedLoan.totalRepayment
      val totalPaid = calculateTotalPaid(selectedLoan.loanId)
      val remaining = totalDue - totalPaid
      
      totalDueValLabel.text = s"$$${String.format("%.2f", totalDue)}"
      paidValLabel.text = s"$$${String.format("%.2f", totalPaid)}"
      remainingValLabel.text = s"$$${String.format("%.2f", remaining)}"
    } else {
      borrowerValLabel.text = "-"
      totalDueValLabel.text = "-"
      paidValLabel.text = "-"
      remainingValLabel.text = "-"
    }
  }
  
  private def calculateTotalPaid(loanId: String): Double = {
    AppState.repayments
      .filter(repay => repay.loanId == loanId)
      .map(repay => repay.amountPaid)
      .sum
  }
  
  private def logRepayment(): Unit = {
    val selectedLoan = loanCombo.selectionModel().getSelectedItem
    val repaymentIdText = repaymentIdField.text.value
    val amountText = amountField.text.value
    val dateText = dateField.text.value
    
    if (selectedLoan == null) {
      showError("Payment Error", "Please select a loan account first.")
    } else {
      val exists = AppState.repayments.exists(repay => repay.repaymentId == repaymentIdText.trim)
      if (exists) {
        showError("Payment Error", s"A repayment transaction with ID '${repaymentIdText.trim}' already exists.")
      } else {
        Repayment.validate(repaymentIdText, selectedLoan.loanId, amountText, dateText) match {
          case scala.util.Success(newRepayment) =>
            val totalDue = selectedLoan.totalRepayment
            val totalPaid = calculateTotalPaid(selectedLoan.loanId)
            val remaining = totalDue - totalPaid
            
            if (newRepayment.amountPaid > remaining) {
              showError("Overpayment Error", s"Repayment amount $$${newRepayment.amountPaid} exceeds remaining balance $$${String.format("%.2f", remaining)}.")
            } else {
              AppState.repayments.add(newRepayment)
              AppState.saveRepayments() match {
                case scala.util.Success(_) =>
                  showInfo("Success", s"Logged repayment of $$${newRepayment.amountPaid} for Loan ID '${selectedLoan.loanId}'.")
                  clearFields()
                case scala.util.Failure(error) =>
                  showError("Persistence Error", s"Failed to save repayment: ${error.getMessage}")
              }
            }
          case scala.util.Failure(error) =>
            showError("Validation Error", error.getMessage)
        }
      }
    }
  }
  
  private def clearFields(): Unit = {
    repaymentIdField.text = ""
    amountField.text = ""
    dateField.text = java.time.LocalDate.now().toString
    loanCombo.selectionModel().clearSelection()
  }
  
  private def showError(titleText: String, messageText: String): Unit = {
    new Alert(AlertType.Error) {
      title = titleText
      headerText = None
      contentText = messageText
    }.showAndWait()
  }
  
  private def showInfo(titleText: String, messageText: String): Unit = {
    new Alert(AlertType.Information) {
      title = titleText
      headerText = None
      contentText = messageText
    }.showAndWait()
  }
  
  // ai-assisted: #3
  // why: Constructs the repayment logging view, connecting dynamic calculations of remaining balance to input validation checks.
  override val view: VBox = new VBox(15) {
    padding = Insets(20)
    style = "-fx-background-color: #f8fafc;"
    
    children.add(createHeader("Repayment Tracker", "Log payments and track borrower outstanding balances."))
    
    // Top Row: Form and Info card side-by-side
    val formAndInfoGrid = new GridPane {
      hgap = 15
      vgap = 12
      padding = Insets(15)
      style = "-fx-background-color: white; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 1);"
      
      add(new Label("Select Active Loan:") { style = "-fx-font-weight: bold; -fx-text-fill: #334155; -fx-font-family: 'Segoe UI';" }, 0, 0)
      add(loanCombo, 1, 0)
      
      add(new Label("Repayment ID:") { style = "-fx-font-weight: bold; -fx-text-fill: #334155; -fx-font-family: 'Segoe UI';" }, 0, 1)
      add(repaymentIdField, 1, 1)
      
      add(new Label("Repayment Amount ($):") { style = "-fx-font-weight: bold; -fx-text-fill: #334155; -fx-font-family: 'Segoe UI';" }, 0, 2)
      add(amountField, 1, 2)
      
      add(new Label("Payment Date:") { style = "-fx-font-weight: bold; -fx-text-fill: #334155; -fx-font-family: 'Segoe UI';" }, 0, 3)
      add(dateField, 1, 3)
      
      // Dynamic loan status card inside the grid
      val statusVBox = new VBox(8) {
        padding = Insets(12)
        style = "-fx-background-color: #f1f5f9; -fx-background-radius: 6px; -fx-border-color: #e2e8f0; -fx-border-radius: 6px;"
        
        val sectionTitle = new Label("Loan Account Status") { style = "-fx-font-weight: bold; -fx-text-fill: #0f172a; -fx-font-family: 'Segoe UI';" }
        
        val detailsGrid = new GridPane {
          hgap = 10
          vgap = 6
          
          add(new Label("Borrower:") { style = "-fx-text-fill: #64748b; -fx-font-family: 'Segoe UI';" }, 0, 0)
          add(borrowerValLabel, 1, 0)
          
          add(new Label("Total Loan Due:") { style = "-fx-text-fill: #64748b; -fx-font-family: 'Segoe UI';" }, 0, 1)
          add(totalDueValLabel, 1, 1)
          
          add(new Label("Total Paid:") { style = "-fx-text-fill: #64748b; -fx-font-family: 'Segoe UI';" }, 0, 2)
          add(paidValLabel, 1, 2)
          
          add(new Label("Outstanding:") { style = "-fx-text-fill: #64748b; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold;" }, 0, 3)
          add(remainingValLabel, 1, 3)
        }
        
        children ++= Seq(sectionTitle, detailsGrid)
      }
      
      add(statusVBox, 2, 0, 1, 4)
      
      // Bind Enter key to submit
      repaymentIdField.onAction = handle { logRepayment() }
      amountField.onAction = handle { logRepayment() }
      dateField.onAction = handle { logRepayment() }
      
      val submitBtn = new Button("Submit Repayment") {
        style = "-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 16px; -fx-background-radius: 6px; -fx-cursor: hand; -fx-font-family: 'Segoe UI';"
        onAction = handle { logRepayment() }
      }
      
      add(submitBtn, 1, 4)
    }
    
    // Bottom Row: Table representation of repayments
    val tableVBox = new VBox(8) {
      padding = Insets(15)
      style = "-fx-background-color: white; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 1);"
      VBox.setVgrow(this, Priority.Always)
      
      val titleLabel = new Label("Repayment Transaction History") {
        style = "-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-family: 'Segoe UI';"
      }
      
      children ++= Seq(titleLabel, repaymentTable)
    }
    
    children ++= Seq(formAndInfoGrid, tableVBox)
  }
}
