package models

import scala.util.Try

case class Repayment(
  repaymentId: String,
  loanId: String,
  amountPaid: Double,
  dateString: String
)

object Repayment {
  // ai-assisted: #15
  // why: Enforces data validation rules on repayment records.
  def validate(
    repaymentId: String,
    loanId: String,
    amountPaidStr: String,
    dateString: String
  ): Try[Repayment] = Try {
    val cleanRepaymentId = repaymentId.trim
    val cleanLoanId = loanId.trim
    val cleanDate = dateString.trim
    
    require(cleanRepaymentId.nonEmpty, "Repayment ID cannot be empty.")
    require(cleanLoanId.nonEmpty, "Loan ID cannot be empty.")
    require(cleanDate.nonEmpty, "Date cannot be empty.")
    
    val amountVal = amountPaidStr.toDouble
    require(amountVal > 0.0, "Repayment amount must be greater than zero.")
    
    Repayment(cleanRepaymentId, cleanLoanId, amountVal, cleanDate)
  }
}
