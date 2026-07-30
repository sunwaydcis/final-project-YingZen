package models

import scala.util.Try

sealed trait Loan {
  def loanId: String
  def borrowerId: String
  def principal: Double
  def termMonths: Int
  
  def calculateInterest(): Double
  def loanTypeLabel: String
  
  def totalRepayment: Double = principal + calculateInterest()
  def monthlyInstallment: Double = totalRepayment / termMonths.toDouble
}

// ai-assisted: #5
// why: Calculates interest rate repayment totals using annual percentage rate over term duration.
case class StandardMicroLoan(
  loanId: String,
  borrowerId: String,
  principal: Double,
  termMonths: Int,
  interestRate: Double
) extends Loan {
  override def calculateInterest(): Double = {
    val durationFraction = termMonths.toDouble / 12.0
    principal * (interestRate / 100.0) * durationFraction
  }
  
  override def loanTypeLabel: String = "Standard Microfinance Loan"
}

case class EmergencyInterestFreeLoan(
  loanId: String,
  borrowerId: String,
  principal: Double,
  termMonths: Int,
  adminFee: Double
) extends Loan {
  override def calculateInterest(): Double = 0.0
  
  // Explicit override to demonstrate subtype polymorphism on a method with concrete base implementation
  override def totalRepayment: Double = principal + adminFee
  
  override def loanTypeLabel: String = "Emergency Interest-Free Loan"
}

object Loan {
  def validateStandard(
    loanId: String,
    borrowerId: String,
    principalStr: String,
    termMonthsStr: String,
    interestRateStr: String
  ): Try[StandardMicroLoan] = Try {
    val cleanLoanId = loanId.trim
    val cleanBorrowerId = borrowerId.trim
    
    require(cleanLoanId.nonEmpty, "Loan ID cannot be empty.")
    require(cleanBorrowerId.nonEmpty, "Borrower ID cannot be empty.")
    
    val principalVal = principalStr.toDouble
    require(principalVal > 0.0, "Principal must be greater than zero.")
    
    val termVal = termMonthsStr.toInt
    require(termVal > 0, "Term in months must be greater than zero.")
    
    val rateVal = interestRateStr.toDouble
    require(rateVal >= 0.0, "Interest rate cannot be negative.")
    
    StandardMicroLoan(cleanLoanId, cleanBorrowerId, principalVal, termVal, rateVal)
  }

  def validateEmergency(
    loanId: String,
    borrowerId: String,
    principalStr: String,
    termMonthsStr: String,
    adminFeeStr: String
  ): Try[EmergencyInterestFreeLoan] = Try {
    val cleanLoanId = loanId.trim
    val cleanBorrowerId = borrowerId.trim
    
    require(cleanLoanId.nonEmpty, "Loan ID cannot be empty.")
    require(cleanBorrowerId.nonEmpty, "Borrower ID cannot be empty.")
    
    val principalVal = principalStr.toDouble
    require(principalVal > 0.0, "Principal must be greater than zero.")
    
    val termVal = termMonthsStr.toInt
    require(termVal > 0, "Term in months must be greater than zero.")
    
    val feeVal = adminFeeStr.toDouble
    require(feeVal >= 0.0, "Administrative fee cannot be negative.")
    
    EmergencyInterestFreeLoan(cleanLoanId, cleanBorrowerId, principalVal, termVal, feeVal)
  }
}
