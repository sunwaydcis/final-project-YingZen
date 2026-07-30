package ui

import models._
import persistence._
import scalafx.collections.ObservableBuffer
import scala.util.Try

// ai-assisted: #10
// why: Centralizes reactive ObservableBuffer state management and auto-persistence hooks.
object AppState {
  
  // Data stores using our generic CSVDataStore and implicit serializers
  val borrowerStore: CSVDataStore[Borrower] = new CSVDataStore[Borrower]("src/main/resources/borrowers.csv")
  val loanStore: CSVDataStore[Loan] = new CSVDataStore[Loan]("src/main/resources/loans.csv")
  val repaymentStore: CSVDataStore[Repayment] = new CSVDataStore[Repayment]("src/main/resources/repayments.csv")
  
  // ScalaFX reactive collections to bind with the GUI components
  val borrowers: ObservableBuffer[Borrower] = ObservableBuffer.empty[Borrower]
  val loans: ObservableBuffer[Loan] = ObservableBuffer.empty[Loan]
  val repayments: ObservableBuffer[Repayment] = ObservableBuffer.empty[Repayment]
  
  def loadData(): Try[Unit] = Try {
    // Load borrowers
    borrowerStore.loadAll().map { list =>
      borrowers.clear()
      borrowers.addAll(list)
    }.get
    
    // Load loans
    loanStore.loadAll().map { list =>
      loans.clear()
      loans.addAll(list)
    }.get
    
    // Load repayments
    repaymentStore.loadAll().map { list =>
      repayments.clear()
      repayments.addAll(list)
    }.get
  }
  
  def saveBorrowers(): Try[Unit] = {
    borrowerStore.saveAll(borrowers.toList)
  }
  
  def saveLoans(): Try[Unit] = {
    loanStore.saveAll(loans.toList)
  }
  
  def saveRepayments(): Try[Unit] = {
    repaymentStore.saveAll(repayments.toList)
  }
}
