package ui

import models._
import scalafx.Includes._
import scalafx.scene.control._
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.layout._
import scalafx.geometry.Insets
import scalafx.beans.property.StringProperty
import scalafx.util.StringConverter

class LoanScreen extends AppScreen {
  
  // Input fields
  private val loanIdField = new TextField { promptText = "Unique Loan ID" }
  private val principalField = new TextField { promptText = "Principal Amount ($)" }
  private val termField = new TextField { promptText = "Term in Months" }
  
  // Borrower selection ComboBox
  private val borrowerCombo = new ComboBox[Borrower](AppState.borrowers) {
    promptText = "Select Borrower"
    prefWidth = 250
    converter = StringConverter.toStringConverter[Borrower]((borrower: Borrower) => 
      if (borrower != null) s"${borrower.name} (${borrower.nationalId})" else ""
    )
  }
  
  // Loan type ComboBox
  private val loanTypeCombo = new ComboBox[String](Seq("Standard Microloan", "Emergency Interest-Free Loan")) {
    prefWidth = 250
  }
  
  // Dynamic fields
  private val rateField = new TextField { promptText = "Interest Rate (e.g. 0.05 for 5%)" }
  private val feeField = new TextField { promptText = "Admin Fee ($)" }
  
  private val rateLabel = new Label("Annual Interest Rate:") { style = "-fx-font-weight: bold; -fx-text-fill: #334155;" }
  private val feeLabel = new Label("Administrative Fee ($):") { style = "-fx-font-weight: bold; -fx-text-fill: #334155;" }
  
  // Table view to display active loans
  private val loanTable = new TableView[Loan](AppState.loans) {
    placeholder = new Label("No active loans recorded.")
    style = "-fx-background-color: white;"
    columns ++= Seq(
      new TableColumn[Loan, String]("Loan ID") {
        cellValueFactory = { cellData => new StringProperty(cellData.value.loanId) }
        prefWidth = 100
      },
      new TableColumn[Loan, String]("Borrower") {
        cellValueFactory = { cellData => 
          val nameVal = AppState.borrowers.find(b => b.nationalId == cellData.value.borrowerId).map(b => b.name).getOrElse("Unknown")
          new StringProperty(nameVal)
        }
        prefWidth = 140
      },
      new TableColumn[Loan, String]("Principal") {
        cellValueFactory = { cellData => new StringProperty(s"$$${cellData.value.principal}") }
        prefWidth = 100
      },
      new TableColumn[Loan, String]("Term") {
        cellValueFactory = { cellData => new StringProperty(s"${cellData.value.termMonths} mo") }
        prefWidth = 80
      },
      new TableColumn[Loan, String]("Type") {
        cellValueFactory = { cellData => new StringProperty(cellData.value.loanTypeLabel) }
        prefWidth = 180
      },
      new TableColumn[Loan, String]("Total Repayment") {
        cellValueFactory = { cellData => new StringProperty(s"$$${String.format("%.2f", cellData.value.totalRepayment)}") }
        prefWidth = 120
      },
      new TableColumn[Loan, String]("Monthly Installment") {
        cellValueFactory = { cellData => new StringProperty(s"$$${String.format("%.2f", cellData.value.monthlyInstallment)}") }
        prefWidth = 120
      }
    )
  }
  
  private def disburseLoan(): Unit = {
    val selectedBorrower = borrowerCombo.selectionModel().getSelectedItem
    val selectedType = loanTypeCombo.selectionModel().getSelectedItem
    val loanIdText = loanIdField.text.value
    val principalText = principalField.text.value
    val termText = termField.text.value
    
    if (selectedBorrower == null) {
      showError("Disbursal Error", "Please select a borrower first.")
    } else if (selectedType == null) {
      showError("Disbursal Error", "Please select a loan type.")
    } else {
      val exists = AppState.loans.exists(l => l.loanId == loanIdText.trim)
      if (exists) {
        showError("Disbursal Error", s"A loan with ID '${loanIdText.trim}' already exists.")
      } else {
        val result = if (selectedType == "Standard Microloan") {
          val rateText = rateField.text.value
          Loan.validateStandard(loanIdText, selectedBorrower.nationalId, principalText, termText, rateText)
        } else {
          val feeText = feeField.text.value
          Loan.validateEmergency(loanIdText, selectedBorrower.nationalId, principalText, termText, feeText)
        }
        
        result match {
          case scala.util.Success(newLoan) =>
            AppState.loans.add(newLoan)
            AppState.saveLoans() match {
              case scala.util.Success(_) =>
                showInfo("Success", s"Loan ID '${newLoan.loanId}' disbursed to ${selectedBorrower.name}.")
                clearFields()
              case scala.util.Failure(error) =>
                showError("Persistence Error", s"Failed to save loan: ${error.getMessage}")
            }
          case scala.util.Failure(error) =>
            showError("Validation Error", error.getMessage)
        }
      }
    }
  }
  
  private def clearFields(): Unit = {
    loanIdField.text = ""
    principalField.text = ""
    termField.text = ""
    rateField.text = ""
    feeField.text = ""
    borrowerCombo.selectionModel().clearSelection()
    loanTypeCombo.selectionModel().clearSelection()
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
  
  // ai-assisted: #2
  // why: Sets up the dynamic form bindings in the Loan management screen so that interest rate vs admin fee inputs toggle dynamically.
  override val view: VBox = new VBox(15) {
    padding = Insets(20)
    style = "-fx-background-color: #f8fafc;"
    
    children.add(createHeader("Loan Disbursal", "Disburse standard or emergency loans to registered borrowers."))
    
    // Dynamic binding configuration
    rateField.visible <== loanTypeCombo.selectionModel().selectedItem === "Standard Microloan"
    rateField.managed <== loanTypeCombo.selectionModel().selectedItem === "Standard Microloan"
    rateLabel.visible <== loanTypeCombo.selectionModel().selectedItem === "Standard Microloan"
    rateLabel.managed <== loanTypeCombo.selectionModel().selectedItem === "Standard Microloan"
    
    feeField.visible <== loanTypeCombo.selectionModel().selectedItem === "Emergency Interest-Free Loan"
    feeField.managed <== loanTypeCombo.selectionModel().selectedItem === "Emergency Interest-Free Loan"
    feeLabel.visible <== loanTypeCombo.selectionModel().selectedItem === "Emergency Interest-Free Loan"
    feeLabel.managed <== loanTypeCombo.selectionModel().selectedItem === "Emergency Interest-Free Loan"
    
    val formVBox = new VBox(10) {
      padding = Insets(15)
      style = "-fx-background-color: white; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 1);"
      
      val grid = new GridPane {
        hgap = 15
        vgap = 12
        
        add(new Label("Select Borrower:") { style = "-fx-font-weight: bold; -fx-text-fill: #334155; -fx-font-family: 'Segoe UI';" }, 0, 0)
        add(borrowerCombo, 1, 0)
        
        add(new Label("Loan ID:") { style = "-fx-font-weight: bold; -fx-text-fill: #334155; -fx-font-family: 'Segoe UI';" }, 0, 1)
        add(loanIdField, 1, 1)
        
        add(new Label("Principal Amount ($):") { style = "-fx-font-weight: bold; -fx-text-fill: #334155; -fx-font-family: 'Segoe UI';" }, 0, 2)
        add(principalField, 1, 2)
        
        add(new Label("Term (Months):") { style = "-fx-font-weight: bold; -fx-text-fill: #334155; -fx-font-family: 'Segoe UI';" }, 0, 3)
        add(termField, 1, 3)
        
        add(new Label("Loan Type:") { style = "-fx-font-weight: bold; -fx-text-fill: #334155; -fx-font-family: 'Segoe UI';" }, 0, 4)
        add(loanTypeCombo, 1, 4)
        
        add(rateLabel, 0, 5)
        add(rateField, 1, 5)
        
        add(feeLabel, 0, 6)
        add(feeField, 1, 6)
      }
      
      // Bind Enter key to submit
      loanIdField.onAction = handle { disburseLoan() }
      principalField.onAction = handle { disburseLoan() }
      termField.onAction = handle { disburseLoan() }
      rateField.onAction = handle { disburseLoan() }
      feeField.onAction = handle { disburseLoan() }
      
      val submitBtn = new Button("Disburse Loan") {
        style = "-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 16px; -fx-background-radius: 6px; -fx-cursor: hand; -fx-font-family: 'Segoe UI';"
        onAction = handle { disburseLoan() }
      }
      
      children ++= Seq(grid, submitBtn)
    }
    
    val tableVBox = new VBox(8) {
      padding = Insets(15)
      style = "-fx-background-color: white; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 1);"
      VBox.setVgrow(this, Priority.Always)
      
      val titleLabel = new Label("Disbursed Loans List") {
        style = "-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-family: 'Segoe UI';"
      }
      
      children ++= Seq(titleLabel, loanTable)
    }
    
    children ++= Seq(formVBox, tableVBox)
  }
}
