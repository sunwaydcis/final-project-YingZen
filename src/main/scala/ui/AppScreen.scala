package ui

import scalafx.scene.layout.VBox
import scalafx.scene.control.Label
import scalafx.geometry.Insets

// ai-assisted: #17
// why: Common base trait providing reusable header formatting and screen layout definitions.
trait AppScreen {
  def view: VBox
  
  protected def createHeader(title: String, subtitle: String): VBox = {
    val titleLabel = new Label(title) {
      style = "-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-family: 'Segoe UI', Arial;"
    }
    val subtitleLabel = new Label(subtitle) {
      style = "-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-family: 'Segoe UI', Arial;"
    }
    
    new VBox(4, titleLabel, subtitleLabel) {
      padding = Insets(0, 0, 15, 0)
    }
  }
}
