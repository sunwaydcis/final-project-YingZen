package ui

import models.Borrower
import scalafx.Includes._
import scalafx.scene.control._
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.layout._
import scalafx.geometry.Insets
import scalafx.beans.property.StringProperty

class BorrowerScreen extends AppScreen {
  
  // Form input controls
  private val idField = new TextField { promptText = "Alphanumeric ID" }
  private val nameField = new TextField { promptText = "Letters and spaces only" }
  private val villageField = new TextField { promptText = "Village or community name" }
  private val incomeField = new TextField { promptText = "Monthly income in USD" }
  private val dependentsField = new TextField { promptText = "Number of dependents" }
  
  // Table view to display all borrowers
  private val borrowerTable = new TableView[Borrower](AppState.borrowers) {
    placeholder = new Label("No borrowers registered yet.")
    style = "-fx-background-color: white;"
    columns ++= Seq(
      new TableColumn[Borrower, String]("National ID") {
        cellValueFactory = { cellData => new StringProperty(cellData.value.nationalId) }
        prefWidth = 130
      },
      new TableColumn[Borrower, String]("Full Name") {
        cellValueFactory = { cellData => new StringProperty(cellData.value.name) }
        prefWidth = 170
      },
      new TableColumn[Borrower, String]("Village") {
        cellValueFactory = { cellData => new StringProperty(cellData.value.village) }
        prefWidth = 140
      },
      new TableColumn[Borrower, String]("Monthly Income") {
        cellValueFactory = { cellData => new StringProperty(s"$$${cellData.value.monthlyIncome}") }
        prefWidth = 120
      },
      new TableColumn[Borrower, String]("Dependents") {
        cellValueFactory = { cellData => new StringProperty(cellData.value.dependents.toString) }
        prefWidth = 100
      }
    )
  }
  
  private def registerBorrower(): Unit = {
    val idText = idField.text.value
    val nameText = nameField.text.value
    val villageText = villageField.text.value
    val incomeText = incomeField.text.value
    val dependentsText = dependentsField.text.value
    
    Borrower.validate(idText, nameText, villageText, incomeText, dependentsText) match {
      case scala.util.Success(newBorrower) =>
        val exists = AppState.borrowers.exists(existing => existing.nationalId == newBorrower.nationalId)
        if (exists) {
          showError("Registration Error", s"A borrower with National ID '${newBorrower.nationalId}' is already registered.")
        } else {
          AppState.borrowers.add(newBorrower)
          AppState.saveBorrowers() match {
            case scala.util.Success(_) =>
              showInfo("Success", s"Borrower '${newBorrower.name}' registered successfully.")
              clearFields()
            case scala.util.Failure(error) =>
              showError("Persistence Error", s"Failed to save borrower: ${error.getMessage}")
          }
        }
      case scala.util.Failure(error) =>
        showError("Validation Error", error.getMessage)
    }
  }
  
  private def clearFields(): Unit = {
    idField.text = ""
    nameField.text = ""
    villageField.text = ""
    incomeField.text = ""
    dependentsField.text = ""
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
  
  // ai-assisted: #1
  // why: Constructs the layout hierarchy for the borrower registration screen, ensuring proper input flow and keyboard execution.
  override val view: VBox = new VBox(15) {
    padding = Insets(20)
    style = "-fx-background-color: #f8fafc;"
    
    // Add header
    children.add(createHeader("Borrower Registry", "Register and manage community borrower details."))
    
    // Form Container
    val formVBox = new VBox(10) {
      padding = Insets(15)
      style = "-fx-background-color: white; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 1);"
      
      val grid = new GridPane {
        hgap = 15
        vgap = 12
        
        add(new Label("National ID:") { style = "-fx-font-weight: bold; -fx-text-fill: #334155; -fx-font-family: 'Segoe UI';" }, 0, 0)
        add(idField, 1, 0)
        
        add(new Label("Full Name:") { style = "-fx-font-weight: bold; -fx-text-fill: #334155; -fx-font-family: 'Segoe UI';" }, 0, 1)
        add(nameField, 1, 1)
        
        add(new Label("Village / Area:") { style = "-fx-font-weight: bold; -fx-text-fill: #334155; -fx-font-family: 'Segoe UI';" }, 0, 2)
        add(villageField, 1, 2)
        
        add(new Label("Monthly Income ($):") { style = "-fx-font-weight: bold; -fx-text-fill: #334155; -fx-font-family: 'Segoe UI';" }, 0, 3)
        add(incomeField, 1, 3)
        
        add(new Label("Dependents:") { style = "-fx-font-weight: bold; -fx-text-fill: #334155; -fx-font-family: 'Segoe UI';" }, 0, 4)
        add(dependentsField, 1, 4)
      }
      
      // Bind Enter key inside any field to submit the form
      idField.onAction = handle { registerBorrower() }
      nameField.onAction = handle { registerBorrower() }
      villageField.onAction = handle { registerBorrower() }
      incomeField.onAction = handle { registerBorrower() }
      dependentsField.onAction = handle { registerBorrower() }
      
      val submitBtn = new Button("Register Borrower") {
        style = "-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 16px; -fx-background-radius: 6px; -fx-cursor: hand; -fx-font-family: 'Segoe UI';"
        onAction = handle { registerBorrower() }
      }
      
      children ++= Seq(grid, submitBtn)
    }
    
    // Table Container
    val tableVBox = new VBox(8) {
      padding = Insets(15)
      style = "-fx-background-color: white; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 1);"
      VBox.setVgrow(this, Priority.Always)
      
      val titleLabel = new Label("Registered Borrowers List") {
        style = "-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-family: 'Segoe UI';"
      }
      
      children ++= Seq(titleLabel, borrowerTable)
    }
    
    children ++= Seq(formVBox, tableVBox)
  }
}
