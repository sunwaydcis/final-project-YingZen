package ui

import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.{BorderPane, VBox, Priority, Region}
import scalafx.geometry.Insets
import scalafx.Includes._

object MicrofinanceApp extends JFXApp3 {
  
  // Constants for styles
  private val activeStyle = 
    "-fx-background-color: #1e293b; -fx-text-fill: white; -fx-font-weight: bold; " +
    "-fx-alignment: BASELINE_LEFT; -fx-padding: 10px 20px; -fx-background-radius: 4px; -fx-cursor: hand;"
    
  private val inactiveStyle = 
    "-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-weight: normal; " +
    "-fx-alignment: BASELINE_LEFT; -fx-padding: 10px 20px; -fx-background-radius: 4px; -fx-cursor: hand;"
  
  override def start(): Unit = {
    // 1. Initialize data from files
    AppState.loadData() match {
      case scala.util.Success(_) => 
        println("Successfully loaded persistent application data.")
      case scala.util.Failure(error) => 
        println(s"Starting with empty data store (loaded status: ${error.getMessage})")
    }
    
    // 2. Instantiate views
    val borrowerScreen = new BorrowerScreen()
    val loanScreen = new LoanScreen()
    val repaymentScreen = new RepaymentScreen()
    val dashboardScreen = new DashboardScreen()
    
    // 3. Create the Main Layout Container
    val mainPane = new BorderPane()
    
    // 4. Construct the Sidebar Navigation Buttons
    val dashboardNavBtn = new Button("Dashboard")
    val borrowerNavBtn = new Button("Borrowers")
    val loanNavBtn = new Button("Loans")
    val repaymentNavBtn = new Button("Repayments")
    
    val navigationButtons = Seq(dashboardNavBtn, borrowerNavBtn, loanNavBtn, repaymentNavBtn)
    
    // ai-assisted: #18
    // why: Swaps center view pane and updates navigation button active styling when sidebar menu is clicked.
    def navigateTo(screen: AppScreen, selectedBtn: Button): Unit = {
      mainPane.center = screen.view
      navigationButtons.foreach(btn => btn.style = inactiveStyle)
      selectedBtn.style = activeStyle
      
      // Dynamic refresh check for Dashboard
      screen match {
        case dash: DashboardScreen => dash.refreshDashboard()
        case _ => // No dynamic refresh needed
      }
    }
    
    // Bind click events
    dashboardNavBtn.onAction = handle { navigateTo(dashboardScreen, dashboardNavBtn) }
    borrowerNavBtn.onAction = handle { navigateTo(borrowerScreen, borrowerNavBtn) }
    loanNavBtn.onAction = handle { navigateTo(loanScreen, loanNavBtn) }
    repaymentNavBtn.onAction = handle { navigateTo(repaymentScreen, repaymentNavBtn) }
    
    // Styling the sidebar
    val sidebarVBox = new VBox(8) {
      padding = Insets(20, 10, 20, 10)
      style = "-fx-background-color: #0f172a;" // Slate-900
      prefWidth = 220
      
      val titleLabel = new Label("SDG-1 Tracker") {
        style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white; -fx-font-family: 'Segoe UI';"
        padding = Insets(0, 0, 10, 10)
      }
      
      val subtitleLabel = new Label("Microfinance Hub") {
        style = "-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-family: 'Segoe UI';"
        padding = Insets(-15, 0, 20, 10)
      }
      
      val spacer = new Region() { VBox.setVgrow(this, Priority.Always) }
      
      val footerLabel = new Label("PRG2104 Final Project") {
        style = "-fx-font-size: 10px; -fx-text-fill: #475569; -fx-alignment: center;"
        maxWidth = Double.MaxValue
      }
      
      children ++= Seq(
        titleLabel, 
        subtitleLabel, 
        dashboardNavBtn, 
        borrowerNavBtn, 
        loanNavBtn, 
        repaymentNavBtn, 
        spacer, 
        footerLabel
      )
    }
    
    mainPane.left = sidebarVBox
    
    // Set initial screen
    navigateTo(dashboardScreen, dashboardNavBtn)
    
    // 5. Initialize the Primary Stage
    stage = new PrimaryStage {
      title = "SDG-1 Microfinance Loan Tracker"
      width = 980
      height = 680
      scene = new Scene(mainPane)
    }
  }
  
  // ai-assisted: #8
  // why: Auto-saves all borrower, loan, and repayment data stores when the application stage is closed.
  override def stopApp(): Unit = {
    // Save all app data back to files on termination to ensure persistence
    AppState.saveBorrowers()
    AppState.saveLoans()
    AppState.saveRepayments()
    super.stopApp()
  }
}
