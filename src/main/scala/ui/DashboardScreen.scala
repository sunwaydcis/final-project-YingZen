package ui

import scalafx.Includes._
import scalafx.scene.control._
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.layout._
import scalafx.geometry.Insets
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import java.io.{BufferedWriter, FileWriter, File}
import scala.util.Try

case class RiskProfile(
  borrowerName: String,
  borrowerId: String,
  dti: Double,
  riskLevel: String
)

class DashboardScreen extends AppScreen {
  
  // Metric labels
  private val totalBorrowersLabel = new Label("0") { style = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;" }
  private val totalLentLabel = new Label("$0.00") { style = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #16a34a;" }
  private val totalRepaidLabel = new Label("$0.00") { style = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2563eb;" }
  private val outstandingLabel = new Label("$0.00") { style = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #ea580c;" }
  
  // Observable collection for TableView
  private val profiles = ObservableBuffer.empty[RiskProfile]
  
  // Filter ComboBox
  private val filterCombo = new ComboBox[String](Seq("All", "No Active Loan", "Low Risk", "Medium Risk", "High Risk")) {
    value = "All"
    prefWidth = 180
  }
  
  // Risk TableView
  private val riskTable = new TableView[RiskProfile](profiles) {
    placeholder = new Label("No profiles available.")
    style = "-fx-background-color: white;"
    columns ++= Seq(
      new TableColumn[RiskProfile, String]("Borrower") {
        cellValueFactory = { cellData => new StringProperty(cellData.value.borrowerName) }
        prefWidth = 150
      },
      new TableColumn[RiskProfile, String]("National ID") {
        cellValueFactory = { cellData => new StringProperty(cellData.value.borrowerId) }
        prefWidth = 130
      },
      new TableColumn[RiskProfile, String]("DTI Ratio") {
        cellValueFactory = { cellData => 
          val dtiVal = cellData.value.dti
          val dtiText = if (dtiVal == 999.0) "N/A (Zero Income)" else if (dtiVal == 0.0) "0.0%" else s"${String.format("%.1f", dtiVal * 100)}%"
          new StringProperty(dtiText)
        }
        prefWidth = 140
      },
      new TableColumn[RiskProfile, String]("Risk Level") {
        cellValueFactory = { cellData => new StringProperty(cellData.value.riskLevel) }
        prefWidth = 150
      }
    )
  }
  
  // Initialize and compute dashboard data
  def refreshDashboard(): Unit = {
    // 1. Calculate Summary Cards
    val borrowersCount = AppState.borrowers.size
    val principalSum = AppState.loans.map(loan => loan.principal).sum
    val repaymentsSum = AppState.repayments.map(repay => repay.amountPaid).sum
    val totalRepaymentDue = AppState.loans.map(loan => loan.totalRepayment).sum
    val remainingBalance = totalRepaymentDue - repaymentsSum
    
    totalBorrowersLabel.text = borrowersCount.toString
    totalLentLabel.text = s"$$${String.format("%.2f", principalSum)}"
    totalRepaidLabel.text = s"$$${String.format("%.2f", repaymentsSum)}"
    outstandingLabel.text = s"$$${String.format("%.2f", remainingBalance)}"
    
    // 2. Compute Risk Profiles
    // ai-assisted: #6
    // why: Computes borrower debt-to-income ratio and categorizes financial risk tiers.
    val rawProfiles = AppState.borrowers.toList.map { borrower =>
      val activeLoans = AppState.loans.filter(loan => loan.borrowerId == borrower.nationalId)
      val monthlyDue = activeLoans.map(loan => loan.monthlyInstallment).sum
      
      val dtiValue = if (borrower.monthlyIncome > 0) {
        monthlyDue / borrower.monthlyIncome
      } else if (monthlyDue > 0) {
        999.0 // Zero income borrower with active debt
      } else {
        0.0
      }
      
      val riskText = if (dtiValue == 0.0) {
        "No Active Loan"
      } else if (dtiValue <= 0.20) {
        "Low Risk"
      } else if (dtiValue <= 0.50) {
        "Medium Risk"
      } else {
        "High Risk"
      }
      
      RiskProfile(borrower.name, borrower.nationalId, dtiValue, riskText)
    }
    
    // Apply filter
    val selectedFilter = filterCombo.selectionModel().getSelectedItem
    val filteredProfiles = if (selectedFilter == null || selectedFilter == "All") {
      rawProfiles
    } else {
      rawProfiles.filter(profile => profile.riskLevel == selectedFilter)
    }
    
    profiles.clear()
    profiles.addAll(filteredProfiles)
  }
  
  // Set change listener on filter Combo
  filterCombo.selectionModel().selectedItem.onChange { (_, _, _) =>
    refreshDashboard()
  }
  
  // ai-assisted: #7
  // why: Formats and exports DTI risk metrics into a text report on disk.
  private def exportRiskReport(): Unit = {
    val reportFile = new File("src/main/resources/risk_report.txt")
    val result = Try {
      val parentDir = reportFile.getParentFile
      if (parentDir != null && !parentDir.exists()) {
        parentDir.mkdirs()
      }
      
      val writer = new BufferedWriter(new FileWriter(reportFile))
      try {
        writer.write("==================================================")
        writer.newLine()
        writer.write("       SDG-1 MICROFINANCE RISK ASSESSMENT REPORT  ")
        writer.newLine()
        writer.write(s"Generated: ${java.time.LocalDateTime.now().toString}")
        writer.newLine()
        writer.write("==================================================")
        writer.newLine()
        writer.newLine()
        
        writer.write(s"Total Registered Borrowers: ${totalBorrowersLabel.text.value}")
        writer.newLine()
        writer.write(s"Total Loans Disbursed:      ${totalLentLabel.text.value}")
        writer.newLine()
        writer.write(s"Total Repayments Logged:    ${totalRepaidLabel.text.value}")
        writer.newLine()
        writer.write(s"Total Outstanding Balance:  ${outstandingLabel.text.value}")
        writer.newLine()
        writer.newLine()
        
        writer.write("--------------------------------------------------")
        writer.newLine()
        writer.write(String.format("%-18s %-14s %-8s %-12s", "Borrower Name", "National ID", "DTI", "Risk Level"))
        writer.newLine()
        writer.write("--------------------------------------------------")
        writer.newLine()
        
        profiles.foreach { profile =>
          val dtiText = if (profile.dti == 999.0) "N/A" else s"${String.format("%.1f", profile.dti * 100)}%"
          writer.write(String.format("%-18s %-14s %-8s %-12s", profile.borrowerName, profile.borrowerId, dtiText, profile.riskLevel))
          writer.newLine()
        }
        writer.write("--------------------------------------------------")
        writer.newLine()
      } finally {
        writer.close()
      }
    }
    
    // ai-assisted: #4
    // why: Displays alert dialog pop-ups with headerText = None, replacing AI hallucinated null parameter with None to fix ScalaFX ambiguity.
    result match {
      case scala.util.Success(_) =>
        new Alert(AlertType.Information) {
          title = "Report Exported"
          headerText = None
          contentText = s"Risk report successfully written to '${reportFile.getAbsolutePath}'."
        }.showAndWait()
      case scala.util.Failure(error) =>
        new Alert(AlertType.Error) {
          title = "Export Error"
          headerText = None
          contentText = s"Failed to write risk report: ${error.getMessage}"
        }.showAndWait()
    }
  }
  
  override val view: VBox = new VBox(15) {
    padding = Insets(20)
    style = "-fx-background-color: #f8fafc;"
    
    children.add(createHeader("Risk Assessment Dashboard", "Overview of disbursals, repayment metrics, and borrower default risk levels."))
    
    // Metrics HBox (4 Cards)
    val metricsHBox = new HBox(12) {
      padding = Insets(0, 0, 10, 0)
      
      val createCard = (titleText: String, valLabel: Label, bgColor: String) => new VBox(8) {
        padding = Insets(12)
        style = s"-fx-background-color: $bgColor; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 6, 0, 0, 1);"
        HBox.setHgrow(this, Priority.Always)
        
        val titleLabel = new Label(titleText) { style = "-fx-font-size: 12px; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';" }
        
        children ++= Seq(titleLabel, valLabel)
      }
      
      children ++= Seq(
        createCard("TOTAL BORROWERS", totalBorrowersLabel, "#eff6ff"),
        createCard("TOTAL DISBURSED", totalLentLabel, "#f0fdf4"),
        createCard("TOTAL REPAID", totalRepaidLabel, "#f0f9ff"),
        createCard("OUTSTANDING BALANCE", outstandingLabel, "#fff7ed")
      )
    }
    
    // Risk Section VBox
    val riskVBox = new VBox(10) {
      padding = Insets(15)
      style = "-fx-background-color: white; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 1);"
      VBox.setVgrow(this, Priority.Always)
      
      val controlHBox = new HBox(10) {
        alignment = scalafx.geometry.Pos.CenterLeft
        
        val sectionTitle = new Label("Borrower Debt-to-Income Risk Profiles") {
          style = "-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-family: 'Segoe UI';"
        }
        
        val spacer = new Region { HBox.setHgrow(this, Priority.Always) }
        
        val filterLabel = new Label("Filter Risk:") { style = "-fx-font-weight: bold; -fx-text-fill: #475569; -fx-font-family: 'Segoe UI';" }
        
        val refreshBtn = new Button("Refresh") {
          style = "-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-font-weight: bold; -fx-padding: 6px 12px; -fx-background-radius: 6px; -fx-cursor: hand;"
          onAction = handle { refreshDashboard() }
        }
        
        val exportBtn = new Button("Export Risk Report") {
          style = "-fx-background-color: #0f172a; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6px 12px; -fx-background-radius: 6px; -fx-cursor: hand;"
          onAction = handle { exportRiskReport() }
        }
        
        children ++= Seq(sectionTitle, spacer, filterLabel, filterCombo, refreshBtn, exportBtn)
      }
      
      children ++= Seq(controlHBox, riskTable)
    }
    
    children ++= Seq(metricsHBox, riskVBox)
  }
}
