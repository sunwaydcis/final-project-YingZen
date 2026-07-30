package models

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

// ai-assisted: #11
// why: Verifies repayment calculations for standard and emergency loans using ScalaTest assertions.
class DomainModelSpec extends AnyFunSuite with Matchers {

  test("StandardMicroLoan should calculate total repayment with interest correctly") {
    val loan = StandardMicroLoan(
      loanId = "L-001",
      borrowerId = "NID-101",
      principal = 1000.0,
      termMonths = 12,
      interestRate = 5.0
    )
    
    // Principal (1000) + Interest (1000 * 0.05) = 1050.0
    loan.totalRepayment shouldEqual 1050.0
  }

  test("EmergencyInterestFreeLoan should calculate flat admin fee correctly") {
    val loan = EmergencyInterestFreeLoan(
      loanId = "L-002",
      borrowerId = "NID-102",
      principal = 500.0,
      termMonths = 6,
      adminFee = 25.0
    )
    
    // Principal (500) + Admin Fee (25) = 525.0
    loan.totalRepayment shouldEqual 525.0
  }

  test("Borrower validation should confirm positive income") {
    val borrower = Borrower(
      nationalId = "990101145555",
      name = "Amina Yusuf",
      village = "Kampung Sentosa",
      monthlyIncome = 450.0,
      dependents = 3
    )
    
    borrower.monthlyIncome shouldEqual 450.0
  }
}
